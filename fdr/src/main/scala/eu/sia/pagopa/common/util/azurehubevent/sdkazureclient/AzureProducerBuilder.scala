package eu.sia.pagopa.common.util.azurehubevent.sdkazureclient

import akka.Done
import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.dispatch.MessageDispatcher
import com.azure.core.amqp.AmqpTransportType
import com.azure.core.util.BinaryData
import com.azure.messaging.eventhubs.{EventData, EventDataBatch, EventHubClientBuilder, EventHubProducerAsyncClient}
import com.azure.storage.blob.{BlobAsyncClient, BlobClientBuilder}
import eu.sia.pagopa.Main.ConfigData
import eu.sia.pagopa.common.json.model.{Event, FlussiRendicontazioneEvent, IUVRendicontatiEvent}
import eu.sia.pagopa.common.message._
import eu.sia.pagopa.common.util._
import eu.sia.pagopa.common.util.azurehubevent.AppObjectMapper
import eu.sia.pagopa.common.util.azurehubevent.Appfunction.{ReEventFunc, defaultOperation, sessionId}
import org.slf4j.MDC
import reactor.core.publisher.{Flux, Mono}

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.time
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import scala.collection.Seq
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.util.Try

trait AzureEventProducer {

  protected def configName = "tdb"
  protected var producer: Option[EventHubProducerAsyncClient] = None
  protected implicit var executionContext: MessageDispatcher = _

  val QI_EVENT_MSG_PUBLISHED = "QI Event PUBLISHED \npayload:"
  val QI_EVENT_MSG_FAILED = "QI Event FAILED \npayload:"
  val QI_EVENT_MSG_FAILED_PRODUCER_NOT_INITIALIZED = "QI Event FAILED...Producer not initialized,call .init() method first \npayload:"

  val FAILED_ACT_VER_EVENT_MSG_FAILED = "FailedActivateVerify Biz Event FAILED \npayload:"


  def init(system: ActorSystem): Unit = {
    val eventConfigAzureSdkClient = system.settings.config.getConfig(s"azure-hub-event.azure-sdk-client.$configName")
    val eventHubName = eventConfigAzureSdkClient.getString("event-hub-name")
    val connectionString = eventConfigAzureSdkClient.getString("connection-string")

    executionContext = system.dispatchers.lookup("eventhub-dispatcher")
    producer = Some(new EventHubClientBuilder().transportType(AmqpTransportType.AMQP_WEB_SOCKETS).connectionString(connectionString, eventHubName).buildAsyncProducerClient())

    val coordinatedShutdown = system.settings.config.getBoolean("coordinatedShutdown")
    if (coordinatedShutdown) {
      CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseBeforeActorSystemTerminate, s"$configName-producer-stop") { () =>
        producer.foreach(_.close())
        Future.successful(Done)
      }
    } else {
      system.registerOnTermination(() => {
        producer.foreach(_.close())
      })
    }
  }

  // TODO [FC] review this method!
  private def addOrNewBatch(producer: EventHubProducerAsyncClient, batch: EventDataBatch, eventDataSeq: Seq[EventData], log: NodoLogger): Mono[_ <: Seq[EventDataBatch]] = {
    if (eventDataSeq.isEmpty) {
      Mono.just(Seq(batch))
    } else {
      Try(if (batch.tryAdd(eventDataSeq.head)) {
        log.debug("add to batch")
        addOrNewBatch(producer, batch, eventDataSeq.tail, log)
      } else {
        log.debug("creating new batch")
        for {
          res <- producer
            .createBatch()
            .flatMap(newBatch => {
              addOrNewBatch(producer, newBatch, eventDataSeq, log)
            })
        } yield Seq(batch) ++ res

      }).recover({ case e =>
        log.debug("discarding message")
        addOrNewBatch(producer, batch, eventDataSeq.tail, log)
      }).get
    }
  }

  protected def publish(items: Seq[Event], log: NodoLogger): Unit = {
    val logMessage: (Event, String) => String = (event: Event, msg: String) => event match {
      case _: IUVRendicontatiEvent =>
        s"$msg${AppObjectMapper.objectMapper.writeValueAsString(event)}"
      case _: FlussiRendicontazioneEvent =>
        s"$msg${AppObjectMapper.objectMapper.writeValueAsString(event)}"
    }

    try {
      val mdcMap = MDC.getCopyOfContextMap

      if (producer.nonEmpty) {
        log.debug(s"create eventDatas")
        val eventDataSeq: Seq[EventData] = items.map(r => {
          val eventData = new EventData(AppObjectMapper.objectMapper.writeValueAsString(r))
          eventData.getProperties.put(Constant.MDCKey.SERVICE_IDENTIFIER, Constant.SERVICE_IDENTIFIER)
          eventData
        })
        producer.get
          .createBatch()
          .flatMap(batch => {
            addOrNewBatch(producer.get, batch, eventDataSeq, log)
          })
          .flatMap(batches => {
            Flux
              .fromIterable(batches.asJava)
              .flatMap(d => {
                producer.get.send(d)
              })
              .collectList()
          })
          // TODO [FC] it is necessary?
//          .subscribe(
//            (f: java.util.List[Void]) => {
//              MDC.setContextMap(mdcMap)
//
//              items.foreach(x => log.info(logMessage(x, QI_EVENT_MSG_PUBLISHED)))
//            },
//            (ex: Throwable) => {
//              MDC.setContextMap(mdcMap)
//              items.foreach(x => log.error(ex, logMessage(x, FAILED_ACT_VER_EVENT_MSG_FAILED)))
//            }
//          )
      } else {
        items.foreach(x => log.warn(logMessage(x, QI_EVENT_MSG_FAILED_PRODUCER_NOT_INITIALIZED)))
      }
    } catch {
      case ex: Throwable =>
        items.foreach(x => log.error(ex, logMessage(x, QI_EVENT_MSG_FAILED)))
    }

  }
}

object AzureIuvRendicontatiProducer extends AzureEventProducer {
  override val configName = "fdr-qi-reported-iuv"

  def send(log: NodoLogger, event: IUVRendicontatiEvent) = {
    Future(publish(Seq(event), log))
  }
}

object AzureFlussiRendicontazioneProducer extends AzureEventProducer{
  override val configName = "fdr-qi-flows"

  def send(log: NodoLogger, event: FlussiRendicontazioneEvent) = {
    Future(publish(Seq(event), log))
  }
}

object AzureProducerBuilder {

  def build()(implicit ec: ExecutionContext, system: ActorSystem, log: NodoLogger): ReEventFunc = {
    log.info(s"Starting Azure Hub Event Service ...")
    val eventConfigAzureSdkClient = system.settings.config.getConfig("azure-hub-event.azure-sdk-client.re-event")
    val blobContainerClient = system.settings.config.getConfig("azure-hub-event.azure-sdk-client.blob-re")
    val eventHubName = eventConfigAzureSdkClient.getString("event-hub-name")
    val connectionString = eventConfigAzureSdkClient.getString("connection-string")
    val clientTimeoutMs = eventConfigAzureSdkClient.getLong("client-timeoput-ms")

    val reXmlLog = Try(system.settings.config.getBoolean("reXmlLog")).getOrElse(true)
    val reJsonLog = Try(system.settings.config.getBoolean("reJsonLog")).getOrElse(false)

    val reProducer: EventHubProducerAsyncClient =
      new EventHubClientBuilder().transportType(AmqpTransportType.AMQP_WEB_SOCKETS).connectionString(connectionString, eventHubName).buildAsyncProducerClient()

    val coordinatedShutdown = system.settings.config.getBoolean("coordinatedShutdown")
    if (coordinatedShutdown) {
      CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseBeforeActorSystemTerminate, s"reproducer-stop") { () =>
        log.info("Stopping reProducer")
        reProducer.close()
        Future.successful(Done)
      }
    } else {
      system.registerOnTermination(() => {
        log.info("Stopping reProducer")
        reProducer.close()
      })
    }

    (request: ReRequest, log: NodoLogger, data: ConfigData) => {
      val reProudcerEnabledAzureSdkClient = Try(DDataChecks.getConfigurationKeys(data, "azureSdkClientReEventEnabled").toBoolean).getOrElse(false)

      if (reProudcerEnabledAzureSdkClient) {
        val executionContext: MessageDispatcher = system.dispatchers.lookup("eventhub-dispatcher")
        Future(businessLogicForPublish(reProducer, data, request, clientTimeoutMs, system)(log))(executionContext) recoverWith { case e: Throwable =>
          log.error(e, "Producer sdk azure re event error")
          Future.failed(e)
        }
      } else {
        log.debug("CONFIGURATION_KEYS [azure-sdk-client.re-event.enabled]=false. Not forward to Azure")
        Future.successful(())
      }
      Future(defaultOperation(request, log, reXmlLog, reJsonLog, data))
    }
  }

  private def businessLogicForPublish(reProducer: EventHubProducerAsyncClient, ddataMap: ConfigData, request: ReRequest, clientTimeoutMs: Long, system: ActorSystem)(implicit log: NodoLogger): Unit = {
    // TODO [FC] uncomment
    request.re.tipoEvento match {
      case Some(reTipoEvento) =>
        val key = ConfigUtil.getGdeConfigKey(reTipoEvento, request.re.sottoTipoEvento)
        ddataMap.gdeConfigurations.get(key) match {
          case Some(gdeConfig) =>
            if (gdeConfig.eventHubEnabled) {
              if (gdeConfig.eventHubPayloadEnabled) {
                publish(reProducer, Array(request).toSeq, clientTimeoutMs, log, system)
              } else {
                log.debug("Clean re payload")
                val newRe = request.re.copy(payload = None)
                publish(reProducer,Array(request.copy(re = newRe)).toSeq, clientTimeoutMs, log, system)
              }
            } else {
              log.debug(s"GDE_CONFIG key '$key', eventHub not enabled. Not forward to Azure")
            }
          case None =>
            log.debug(s"Cache GDE_CONFIG found but not found key '$key'. Forward to Azure")
            publish(reProducer,Array(request).toSeq, clientTimeoutMs, log, system)
        }
      case None =>
        log.debug(s"RE tipoEvento empty. Forward to Azure")
        publish(reProducer,  Array(request).toSeq, clientTimeoutMs, log, system)
    }

  }

  private def publish(producer: EventHubProducerAsyncClient, reRequestSeq: Seq[ReRequest], clientTimeoutMs: Long, log: NodoLogger, system: ActorSystem): Unit = {
    log.debug(s"create eventDatas")

    // TODO [FC]
    val eventDataSeq: Flux[(EventData)] = Flux.fromIterable(
      reRequestSeq
        .map(r => {
          val blobBodyRef =
            SottoTipoEvento.withName(r.re.sottoTipoEvento) match {
              case SottoTipoEvento.REQ | SottoTipoEvento.RESP =>
                saveBlobToAzure(r, system)
              case SottoTipoEvento.INTERN => None
            }

          val eventType = CategoriaEvento.withName(r.re.categoriaEvento) match {
            case CategoriaEvento.INTERNO => CategoriaEventoEvh.INTERNAL
            case CategoriaEvento.INTERFACCIA => CategoriaEventoEvh.INTERFACE
          }

          val httpMethod: String = r.reExtra.flatMap(_.httpMethod).getOrElse("")
          val key = r.sessionId
          val reEventHub = ReEventHub(
            Constant.FDR_VERSION,
            r.re.uniqueId,
            r.re.insertedTimestamp,
            r.re.sessionId,
            eventType.toString,
            r.re.status,
            r.re.flowName,
            r.re.psp,
            r.re.idDominio,
            r.re.flowAction,
            r.re.sottoTipoEvento,
            Some(httpMethod),
            r.reExtra.flatMap(_.uri),
            blobBodyRef,
            r.reExtra.map(ex => ex.headers.groupBy(_._1).map(v => (v._1, v._2.map(_._2)))).getOrElse(Map())
          )

          val eventData = new EventData(AppObjectMapper.objectMapper.writeValueAsString(reEventHub))
          eventData.getProperties.put(sessionId, key)
          MDC.getCopyOfContextMap.entrySet().asScala.map(a => eventData.getProperties.put(a.getKey, a.getValue))
          eventData
        })
        .asJava
    )

    val currentBatch: AtomicReference[EventDataBatch] = new AtomicReference(producer.createBatch().block())

    eventDataSeq
      .flatMap(eventData => {
        val msg = if (log.isDebugEnabled) {
          s"add to batch and send record \nheaders=[\n\t${eventData.getProperties.entrySet().iterator().asScala.map(a => s"${a.getKey}=${a.getValue}").mkString("\n\t")}\n] \nvalue=${eventData.getBodyAsString}"
        } else {
          ""
        }

        val batch: EventDataBatch = currentBatch.get()
        if (batch.tryAdd(eventData)) {
          log.debug(msg)
          Mono.empty[Void]
        } else {
          Mono.when({
            log.debug(s"eventData not inserted to batch because is full, send all event and create new batch")
            producer.send(batch)
            val eventDataBatch: Mono[EventDataBatch] = producer.createBatch()
            eventDataBatch.map(newBatch => {
              currentBatch.set(newBatch)
              if (!newBatch.tryAdd(eventData)) {
                val ex = new IllegalArgumentException(s"EventData is too large for an empty batch. Max size: ${newBatch.getMaxSizeInBytes}")
                log.error(
                  ex,
                  s"eventData tot inserted to batch because is too large, record \nheaders=[\n\t${eventData.getProperties.entrySet().iterator().asScala.map(a => s"${a.getKey}=${a.getValue}").mkString("\n\t")}\n] \nvalue=${eventData.getBodyAsString}"
                )
                throw ex
              } else {
                log.debug(msg)
                newBatch
              }
            })
          })
        }
      })
      .`then`()
      .doFinally(_ => {
        val batch = currentBatch.getAndSet(null)
        if (batch != null || batch.getCount > 0) {
          log.debug(s"send last event of batch")
          producer.send(batch).block(time.Duration.of(clientTimeoutMs, ChronoUnit.MILLIS))
        }
      })
      .subscribe(defaultConsumer(log), errorConsumer(log), completedRunnable(log))
  }

  // TODO copy this
  private def saveBlobToAzure(r: ReRequest, system: ActorSystem): Option[BlobBodyRef] = {
    val fileName = s"${r.sessionId}_${r.re.tipoEvento.get}_${r.re.sottoTipoEvento}"
    val blobContainerClient = system.settings.config.getConfig("azure-hub-event.azure-sdk-client.blob-re")
    val blocContainerName = blobContainerClient.getString("container-name")
    val blobReConnectionString = blobContainerClient.getString("connection-string")
    var blobAsyncClient: Option[BlobAsyncClient] = None
    r.re.payload.foreach(v => {
      blobAsyncClient = Some(new BlobClientBuilder()
        .connectionString(blobReConnectionString)
        .blobName(fileName).containerName(blocContainerName)
        .buildAsyncClient())
      blobAsyncClient.get.upload(BinaryData.fromStream(new ByteArrayInputStream(new String(v).getBytes(StandardCharsets.UTF_8)))).subscribe()
    })
    blobAsyncClient.map(bc => {
      BlobBodyRef(Some(bc.getAccountName), Some(bc.getContainerName), Some(fileName), (r.re.payload.map(_.length).getOrElse(0)))
    })
  }

  private def defaultConsumer(log: NodoLogger): Consumer[_ >: Void] = { (f: Void) =>
    {
      if (log.isDebugEnabled) {
        log.debug(s"eventData send")
      }
    }
  }

  private def errorConsumer(log: NodoLogger): Consumer[_ >: Throwable] = { (ex: Throwable) =>
    {
      log.error(ex, s"Failed to produce: ${ex.getMessage}")
    }
  }

  private def completedRunnable(log: NodoLogger): Runnable = { () =>
    {
      log.debug("Completed sending events.")
    }
  }
}
