package eu.sia.pagopa.rendicontazioni.actor.async

import com.azure.core.util.BinaryData
import eu.sia.pagopa.ActorProps
import eu.sia.pagopa.Main.system
import eu.sia.pagopa.common.actor.{BaseActor, HttpFdrServiceManagement}
import eu.sia.pagopa.common.enums.EsitoRE
import eu.sia.pagopa.common.json.model.{FdREventToEventHub, FdREventToHistory}
import eu.sia.pagopa.common.message.{CategoriaEvento, Componente, SottoTipoEvento}
import eu.sia.pagopa.common.repo.Repositories
import eu.sia.pagopa.common.repo.re.model.{Fdr1EventHub, Fdr1Metadata, Re}
import eu.sia.pagopa.common.util.Util
import eu.sia.pagopa.common.util.Util.{mapToJson, toMap}

import java.time.LocalDateTime
import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

final case class FdREventActor(repositories: Repositories, actorProps: ActorProps) extends BaseActor {

  private def saveForHistory(event: FdREventToHistory): Unit = {
    // save on pagopaweufdrsa.fdr1flows as gzip
    // save on fdr-re.fdr1metadata
    val compressedBytes: Array[Byte] = Util.gzipContent(event.soapRequest.getBytes("UTF-8"))
    val binaryData: BinaryData = BinaryData.fromBytes(compressedBytes)
    val filename = s"${event.nifr.identificativoFlusso}_${UUID.randomUUID().toString}.xml.zip"
    log.info(s"FdREventToHistory saving ${filename}")
    actorProps.containerBlobFunction(filename, binaryData, log)
    log.info(s"FdREventToHistory ${filename} saved")

    log.debug(s"FdREventToHistory saving metadata")
    val fdr1Metadata = Fdr1Metadata(
      event.nifr.identificativoPSP,
      event.nifr.identificativoIntermediarioPSP,
      event.nifr.identificativoCanale,
      event.nifr.identificativoDominio,
      event.nifr.identificativoFlusso,
      event.nifr.dataOraFlusso,
      filename,
      s"${event.nifr.identificativoPSP}${event.nifr.identificativoDominio}"
    )

    repositories.mongoRepository.saveFdrMetadata(fdr1Metadata)
    log.info(s"FdREventToHistory metadata saved")
  }

  private def sendToEventHub(event: FdREventToEventHub): Unit = {
    // send data to pagopa-fdr-to-event-hub
    log.debug(s"FdREventToEventHub TODO")
    val fdr1EventHub = Fdr1EventHub(
      event.sessionId,
      event.insertedTimestamp,
      event.flussoRiversamento,
      event.nifr.identificativoFlusso,
      event.nifr.dataOraFlusso,
      event.nifr.identificativoDominio,
      event.nifr.identificativoPSP,
      event.nifr.identificativoIntermediarioPSP
    )

    val re = Re(
      psp = Some(event.nifr.identificativoPSP),
      idDominio = Some(event.nifr.identificativoDominio),
      componente = Componente.NDP_FDR.toString,
      categoriaEvento = CategoriaEvento.INTERNO.toString,
      sessionId = Some(event.sessionId),
      payload = Some(mapToJson(toMap(fdr1EventHub)).getBytes),
      esito = Some(EsitoRE.INVIATA.toString),
      tipoEvento = Some(actorClassId),
      sottoTipoEvento = SottoTipoEvento.INTERN.toString,
      insertedTimestamp = LocalDateTime.now(),
      erogatore = Some(Componente.NDP_FDR.toString),
      businessProcess = Some(actorClassId),
      erogatoreDescr = Some(Componente.NDP_FDR.toString),
      flowName = Some(event.nifr.identificativoFlusso),
      flowAction = Some("FdRToEventHub")
    )

    // execute POST notification to pagopa-fdr-to-event-hub
    HttpFdrServiceManagement.internalFdrToEventHub(event.sessionId, mapToJson(toMap(fdr1EventHub)), actorProps, re)
      .recoverWith {
        case _ if event.retry < 3 =>
          log.info(s"Rescheduling message ${event.nifr.identificativoPSP} ${event.nifr.identificativoFlusso}")
          // Reschedule the message after 5 seconds
          context.system.scheduler.scheduleOnce(5.seconds, self, event.copy(retry = event.retry + 1))
          Future.failed(new Exception("Retrying"))
        case _ =>
          log.error(s"[ALERT] Max retries reached ${event.nifr.identificativoPSP} ${event.nifr.identificativoFlusso}")
          // Handle maximum retries exceeded or other failures here
          Future.failed(new Exception("Max retries reached"))
      }
  }

  override def receive: Receive = {
    case event: FdREventToHistory =>
      saveForHistory(event)
    case event: FdREventToEventHub =>
      log.debug(s"FdREventToHistory arrived ${event}")
      sendToEventHub(event)
    case _ =>
      log.error(s"""########################
                   |EVH ACT unmanaged message type
                   |########################""".stripMargin)
  }

}
