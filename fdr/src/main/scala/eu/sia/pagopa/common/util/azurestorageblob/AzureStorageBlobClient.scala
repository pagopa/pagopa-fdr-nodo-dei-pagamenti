package eu.sia.pagopa.common.util.azurestorageblob

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import com.azure.core.util.BinaryData
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient
import com.azure.storage.blob.{BlobAsyncClient, BlobClientBuilder, BlobServiceClientBuilder}
import com.azure.storage.blob.models.BlobStorageException
import eu.sia.pagopa.Main.ConfigData
import eu.sia.pagopa.common.message.{BlobBodyRef, CategoriaEvento, CategoriaEventoEvh, ReEventHub, ReRequest, SottoTipoEvento}
import eu.sia.pagopa.common.repo.Repositories
import eu.sia.pagopa.common.util.{Constant, NodoLogger}
import eu.sia.pagopa.common.util.azurehubevent.Appfunction.{Fdr1FlowsContainerBlobFunc, RePayloadContainerBlobFunc}
import eu.sia.pagopa.common.util.azurehubevent.sdkazureclient.AzureProducerBuilder.{businessLogicForPublish, saveBlobToAzure}

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import scala.jdk.CollectionConverters._
import scala.concurrent.{ExecutionContext, Future}

object AzureStorageBlobClient {

  def fdr1FlowsBuild()(implicit ec: ExecutionContext, system: ActorSystem, log: NodoLogger): Fdr1FlowsContainerBlobFunc = {
    val azureStorageBlobEnabled = system.settings.config.getBoolean("azure-storage-blob.enabled")

    if( azureStorageBlobEnabled ) {
      val connectionString = system.settings.config.getString("azure-storage-blob.connection-string")
      val containerName = system.settings.config.getString("azure-storage-blob.fdr1-flows-container-name")
      log.info(s"Starting Azure Storage Blob Client Service on ${containerName}...")

      val blobServiceClient = new BlobServiceClientBuilder()
        .connectionString(connectionString)
        .buildClient()

      val containerClient = blobServiceClient.getBlobContainerClient(containerName)

      (fileName: String, metadata: Map[String, String],  fileContent: BinaryData, log: NodoLogger) => {
        Future {
          val blobClient = containerClient.getBlobClient(fileName)
          blobClient.upload(fileContent)
          blobClient.setMetadata(metadata.asJava)
        }.recoverWith {
          case e: BlobStorageException =>
            log.error(e, s"Error interacting with Azure Blob Storage: ${e.getMessage}")
            Future.failed(e)
          case e: Throwable =>
            log.error(e, "Unexpected error")
            Future.failed(e)
        }
      }
    } else {
      log.info("Azure Storage Blob Client Service not enabled: config-app [azure-storage-blob.enabled]=false")
      (fileName: String, metadata: Map[String, String], fileContent: BinaryData, log: NodoLogger) => {
        Future.successful(())
      }
    }
  }

  def rePayloadBuild()(implicit ec: ExecutionContext, system: ActorSystem, log: NodoLogger, repositories: Repositories): RePayloadContainerBlobFunc = {
    val azureStorageBlobEnabled = system.settings.config.getBoolean("azure-storage-blob.enabled")

    if( azureStorageBlobEnabled ) {
      val connectionString = system.settings.config.getString("azure-storage-blob.connection-string")
      val containerName = system.settings.config.getString("azure-storage-blob.re-payload-container-name")
      log.info(s"Starting Azure Storage Blob Client Service on ${containerName}...")

      val blobServiceClient = new BlobServiceClientBuilder()
        .connectionString(connectionString)
        .buildClient()

      val containerClient = blobServiceClient.getBlobContainerClient(containerName)

      // TODO [FC] modify

      (request: ReRequest, repositories: Repositories, log: NodoLogger) => {
        val executionContext: MessageDispatcher = system.dispatchers.lookup("eventhub-dispatcher")
        Future(saveReEvent(request)(log, system, repositories))(executionContext) recoverWith { case e: Throwable =>
          log.error(e, "Producer sdk azure re event error")
          Future.failed(e)
        }
      }

    } else {
      log.info("Azure Storage Blob Client Service not enabled: config-app [azure-storage-blob.enabled]=false")
//      (fileName: String, metadata: Map[String, String], fileContent: BinaryData, log: NodoLogger) => {
      (request: ReRequest, repositories: Repositories, log: NodoLogger) => {
        Future.successful(())
      }
    }
  }

  private def saveReEvent(request: ReRequest)(implicit log: NodoLogger, system: ActorSystem, repositories: Repositories): Unit = {
    val blobBodyRef =
      SottoTipoEvento.withName(request.re.sottoTipoEvento) match {
        case SottoTipoEvento.REQ | SottoTipoEvento.RESP =>
          saveBlob(request, system)
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

    val fileName = s"${r.sessionId}_${r.re.tipoEvento.get}_${r.re.sottoTipoEvento}"

    var blobAsyncClient: Option[BlobAsyncClient] = None
    r.re.payload.foreach(v => {
      blobAsyncClient = Some(new BlobClientBuilder()
        .connectionString(connectionString)
        .blobName(fileName).containerName(containerName)
        .buildAsyncClient())
      blobAsyncClient.get.upload(BinaryData.fromStream(new ByteArrayInputStream(new String(v).getBytes(StandardCharsets.UTF_8)))).subscribe()
    })
    blobAsyncClient.map(bc => {
      BlobBodyRef(Some(bc.getAccountName), Some(bc.getContainerName), Some(fileName), (r.re.payload.map(_.length).getOrElse(0)))
    })
  }

}
