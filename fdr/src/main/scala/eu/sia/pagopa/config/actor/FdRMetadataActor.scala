package eu.sia.pagopa.config.actor

import akka.actor.ActorSystem
import com.azure.core.util.BinaryData
import com.azure.storage.blob.BlobClientBuilder
import eu.sia.pagopa.Main.materializer.system
import eu.sia.pagopa.common.actor.BaseActor
import eu.sia.pagopa.common.json.model.FdREventToHistory
import eu.sia.pagopa.common.message.BlobBodyRef
import eu.sia.pagopa.common.repo.Repositories
import eu.sia.pagopa.common.repo.re.model.Fdr1Metadata
import eu.sia.pagopa.common.util.Util
import eu.sia.pagopa.ActorProps

import scala.jdk.CollectionConverters._
import java.util.UUID
import scala.util.{Failure, Success}

final case class FdRMetadataActor(repositories: Repositories, actorProps: ActorProps) extends BaseActor {

  private def saveForHistory(event: FdREventToHistory): Unit = {
    // save on pagopaweufdrsa.fdr1flows as gzip
    // save on fdr-re.fdr1metadata
    val blobBodyRef: Option[BlobBodyRef] = saveBlob(event, system)

    if (blobBodyRef.isDefined) {
      log.debug(s"FdREventToHistory saving metadata ${blobBodyRef.get}")
      val fdr1Metadata = Fdr1Metadata(
        event.nifr.identificativoPSP,
        event.nifr.identificativoIntermediarioPSP,
        event.nifr.identificativoCanale,
        event.nifr.identificativoDominio,
        event.nifr.identificativoFlusso,
        event.nifr.dataOraFlusso,
        blobBodyRef,
        s"${event.nifr.identificativoPSP}${event.nifr.identificativoDominio}"
      )

      val insertFuture = repositories.mongoRepository.saveFdrMetadata(fdr1Metadata)
      insertFuture.onComplete {
        case Success(result) =>
          log.debug(s"FdR Metadata ${result} ${fdr1Metadata.getPsp()} ${fdr1Metadata.getFlowId()} saved")
        case Failure(exception) => {
          log.error(exception, s"Problem to save on Mongo ${fdr1Metadata.getPsp()} ${fdr1Metadata.getFlowId()}")
          self.tell(event.copy(retry = (event.retry + 1)), self)
        }
      }
      log.debug(s"FdREventToHistory metadata ${event.sessionId} saved")

    } else {
      log.debug("Reschedule save fdr1-flow blob")
      self.tell(event.copy(retry = event.retry + 1), self)
    }
  }

  private def saveBlob(event: FdREventToHistory, system: ActorSystem): Option[BlobBodyRef] = {
    val connectionString = system.settings.config.getString("azure-storage-blob.connection-string")
    val containerName = system.settings.config.getString("azure-storage-blob.fdr1-flows-container-name")

    val filename = s"${event.nifr.identificativoFlusso}_${UUID.randomUUID().toString}.xml.zip"

    val blobAsyncClient = Some(new BlobClientBuilder()
      .connectionString(connectionString)
      .blobName(filename).containerName(containerName)
      .buildAsyncClient())
    if (blobAsyncClient.isDefined) {

      val metadata: Map[String, String] = Map(
        "elaborate" -> event.elaborate.toString,
        "sessionId" -> event.sessionId,
        "insertedTimestamp" -> event.insertedTimestamp.toString,
      )

      val compressedBytes: Array[Byte] = Util.gzipContent(event.soapRequest.getBytes("UTF-8"))

      val binaryData: BinaryData = BinaryData.fromBytes(compressedBytes)
      blobAsyncClient.get.upload(binaryData, true)
        .flatMap(_ => blobAsyncClient.get.setMetadata(metadata.asJava))
        .subscribe()

      blobAsyncClient.map(bc => {
        BlobBodyRef(Some(bc.getAccountName), Some(bc.getContainerName), Some(filename), compressedBytes.length)
      })
    }
    else {
      log.debug("Reschedule save fdr1-flow blob - problem to initialize blob async client")
      self.tell(event.copy(retry = event.retry + 1), self)
      Option.empty
    }
  }

  override def receive: Receive = {
    case event: FdREventToHistory =>
      log.info(s"FdREventToHistory ${event.retry}")
      saveForHistory(event)
    case _ =>
      log.error(s"""########################
                   |FDR METADATA ACT unmanaged message type
                   |########################""".stripMargin)
  }

}
