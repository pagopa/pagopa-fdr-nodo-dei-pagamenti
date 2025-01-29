package eu.sia.pagopa.config.actor

import akka.actor.ActorSystem
import com.azure.core.util.BinaryData
import com.azure.storage.blob.{BlobAsyncClient, BlobClientBuilder}
import eu.sia.pagopa.Main.materializer.system
import eu.sia.pagopa.common.actor.BaseActor
import eu.sia.pagopa.common.json.model.FdREventToHistory
import eu.sia.pagopa.common.message.{BlobBodyRef, CategoriaEvento, CategoriaEventoEvh, ReEventHub, ReRequest, SottoTipoEvento}
import eu.sia.pagopa.common.repo.Repositories
import eu.sia.pagopa.common.repo.re.model.Fdr1Metadata
import eu.sia.pagopa.common.util.{Constant, Util}
import eu.sia.pagopa.{ActorProps, BootstrapUtil}

import java.util.UUID
import scala.util.{Failure, Success}

final case class ReActor(repositories: Repositories, actorProps: ActorProps) extends BaseActor {

  private def saveRe(request: ReRequest): Unit = {
    // save on pagopaweufdrsa.re-payload as gzip
    // save on fdr-re.events

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

    repositories.mongoRepository.saveReEvent(reEventHub)
  }

  private def saveBlob(r: ReRequest, system: ActorSystem): Option[BlobBodyRef] = {
    val connectionString = system.settings.config.getString("azure-storage-blob.connection-string")
    val containerName = system.settings.config.getString("azure-storage-blob.re-payload-container-name")

    val fileName = s"${r.sessionId}_${r.re.tipoEvento.get}_${r.re.sottoTipoEvento}.xml.zip"

    var blobAsyncClient: Option[BlobAsyncClient] = None
    var compressedBytes: Array[Byte] = Array.empty[Byte]
    if (r.re.payload.isDefined) {
      compressedBytes = Util.gzipContent(r.re.payload.get)

      blobAsyncClient = Some(new BlobClientBuilder()
        .connectionString(connectionString)
        .blobName(fileName).containerName(containerName)
        .buildAsyncClient())
      blobAsyncClient.get.upload(BinaryData.fromBytes(compressedBytes)).subscribe()
    }
    blobAsyncClient.map(bc => {
      BlobBodyRef(Some(bc.getAccountName), Some(bc.getContainerName), Some(fileName), compressedBytes.length)
    })
  }

  override def receive: Receive = {
    case event: ReRequest =>
      log.debug("Message to RE Actor arrived")
      saveRe(event)
    case _ =>
      log.error(s"""########################
                   |RE ACT unmanaged message type
                   |########################""".stripMargin)
  }

}
