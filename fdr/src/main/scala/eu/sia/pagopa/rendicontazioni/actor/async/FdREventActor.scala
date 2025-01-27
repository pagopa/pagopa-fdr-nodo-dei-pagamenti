package eu.sia.pagopa.rendicontazioni.actor.async

import com.azure.core.util.BinaryData
import eu.sia.pagopa.ActorProps
import eu.sia.pagopa.common.actor.BaseActor
import eu.sia.pagopa.common.json.model.FdREventToHistory
import eu.sia.pagopa.common.repo.Repositories
import eu.sia.pagopa.common.repo.re.model.Fdr1Metadata
import eu.sia.pagopa.common.util.Util

import java.util.UUID

final case class FdREventActor(repositories: Repositories, actorProps: ActorProps) extends BaseActor {

  private def saveForHistory(event: FdREventToHistory): Unit = {
    // save on pagopaweufdrsa.fdr1flows as gzip
    // save on fdr-re.fdr1metadata
    val compressedBytes: Array[Byte] = Util.gzipContent(event.soapRequest.getBytes("UTF-8"))
    val binaryData: BinaryData = BinaryData.fromBytes(compressedBytes)
    val filename = s"${event.nifr.identificativoFlusso}_${UUID.randomUUID().toString}.xml.zip"
    val metadata: Map[String, String] = Map(
      "elaborate" -> event.elaborate.toString,
      "sessionId" -> event.sessionId,
      "insertedTimestamp" -> event.insertedTimestamp.toString,
    )

    log.info(s"FdREventToHistory saving ${filename}")
    actorProps.fdr1FlowsContainerBlobFunction(filename, metadata, binaryData, log)
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

  override def receive: Receive = {
    case event: FdREventToHistory =>
      saveForHistory(event)
    case _ =>
      log.error(s"""########################
                   |EVH ACT unmanaged message type
                   |########################""".stripMargin)
  }

}
