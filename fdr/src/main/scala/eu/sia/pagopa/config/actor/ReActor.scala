package eu.sia.pagopa.config.actor

import akka.actor.ActorSystem
import com.azure.core.util.BinaryData
import com.azure.storage.blob.{BlobAsyncClient, BlobClientBuilder}
import eu.sia.pagopa.Main.materializer.system
import eu.sia.pagopa.common.actor.BaseActor
import eu.sia.pagopa.common.message.{BlobBodyRef, CategoriaEvento, CategoriaEventoEvh, ReEventHub, ReRequest, SottoTipoEvento}
import eu.sia.pagopa.common.repo.Repositories
import eu.sia.pagopa.common.util.{Constant, Util}
import eu.sia.pagopa.ActorProps

import scala.jdk.CollectionConverters._
import java.util.UUID
import scala.util.{Failure, Success}

final case class ReActor(repositories: Repositories, actorProps: ActorProps) extends BaseActor {

  private def saveRe(request: ReRequest): Unit = {
    // save on pagopaweufdrsa.re-payload as gzip
    // save on fdr-re.events

    val maxRetry = 3
    if (request.retry < maxRetry) {

      val blobBodyRef =
        SottoTipoEvento.withName(request.re.sottoTipoEvento) match {
          case SottoTipoEvento.REQ | SottoTipoEvento.RESP => saveBlob(request, system)
          case SottoTipoEvento.INTERN => None
        }

      val eventType = CategoriaEvento.withName(request.re.categoriaEvento) match {
        case CategoriaEvento.INTERNO => CategoriaEventoEvh.INTERNAL
        case CategoriaEvento.INTERFACCIA => CategoriaEventoEvh.INTERFACE
      }

      val httpMethod: String = request.reExtra.flatMap(_.httpMethod).getOrElse("")

      val reEventHub = ReEventHub(
        Constant.FDR_VERSION,
        request.re.uniqueId,
        request.re.insertedTimestamp,
        request.re.sessionId,
        eventType.toString,
        request.re.status,
        request.re.flowName,
        request.re.psp,
        request.re.idDominio,
        request.re.flowAction,
        request.re.sottoTipoEvento,
        Some(httpMethod),
        request.reExtra.flatMap(_.uri),
        blobBodyRef,
        request.reExtra.map(ex => ex.headers.groupBy(_._1).map(v => (v._1, v._2.map(_._2)))).getOrElse(Map())
      )

      val insertFuture = repositories.mongoRepository.saveReEvent(reEventHub)
      insertFuture.onComplete {
        case Success(result) =>
          log.debug(s"RE Event with sessionId ${reEventHub.sessionId} saved  ${result}")
        case Failure(exception) =>
          log.error(exception, s"Problem to save on Mongo RE sessionId ${reEventHub.sessionId}")
          self.tell(request.copy(retry = request.retry + 1), self)
      }
    }
    else {
      log.error(s"[ALERT] REEvent with sessionId ${request.re.uniqueId} retried more than ${maxRetry}.")
    }
  }

  private def saveBlob(r: ReRequest, system: ActorSystem): Option[BlobBodyRef] = {
    val connectionString = system.settings.config.getString("azure-storage-blob.connection-string")
    val containerName = system.settings.config.getString("azure-storage-blob.re-payload-container-name")

    val filename = s"${r.sessionId}_${r.re.tipoEvento.get}_${r.re.sottoTipoEvento}_${UUID.randomUUID().toString}.xml.zip"

    val blobAsyncClient = Some(new BlobClientBuilder()
      .connectionString(connectionString)
      .blobName(filename).containerName(containerName)
      .buildAsyncClient())

    if (blobAsyncClient.isDefined) {
      var compressedBytes: Array[Byte] = Array.empty[Byte]
      if (r.re.payload.isDefined) {
        val metadata: Map[String, String] = Map(
          "sessionId" -> r.sessionId,
          "insertedTimestamp" -> r.re.insertedTimestamp.toString,
        )
        compressedBytes = Util.gzipContent(r.re.payload.get)
        val binaryData: BinaryData = BinaryData.fromBytes(compressedBytes)
        blobAsyncClient.get.upload(binaryData, true)
          .flatMap(_ => blobAsyncClient.get.setMetadata(metadata.asJava))
          .subscribe()
      }

      val blobBodyRef = blobAsyncClient.map(bc => {
        BlobBodyRef(Some(bc.getAccountName), Some(bc.getContainerName), Some(filename), compressedBytes.length)
      })

      // clear memory
      compressedBytes = Array.empty[Byte]

      blobBodyRef
    }
    else {
      log.debug("Reschedule save fdr1-flow blob - problem to initialize blob async client")
      Option.empty
    }

  }

  override def receive: Receive = {
    case reRequest: ReRequest =>
      saveRe(reRequest)
      context.become(idle) // clear reference after processing
    case _ =>
      log.error(s"""########################
                   |RE ACT unmanaged message type
                   |########################""".stripMargin)
  }

  def idle: Receive = {
    case reRequest: ReRequest =>
      saveRe(reRequest)
  }

}
