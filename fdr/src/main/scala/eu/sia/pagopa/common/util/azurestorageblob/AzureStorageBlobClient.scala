package eu.sia.pagopa.common.util.azurestorageblob

import akka.actor.ActorSystem
import com.azure.core.util.BinaryData
import com.azure.storage.blob.BlobServiceClientBuilder
import com.azure.storage.blob.models.BlobStorageException
import eu.sia.pagopa.common.util.NodoLogger
import eu.sia.pagopa.common.util.azurehubevent.Appfunction.ContainerBlobFunc

import scala.jdk.CollectionConverters._
import scala.concurrent.{ExecutionContext, Future}

object AzureStorageBlobClient {

  def fdr1FlowsBuild()(implicit ec: ExecutionContext, system: ActorSystem, log: NodoLogger): ContainerBlobFunc = {
    val containerName = system.settings.config.getString("azure-storage-blob.fdr1-flows-container-name")
    val connectionString = system.settings.config.getString("azure-storage-blob.connection-string")
    build(containerName, connectionString)
  }

  def rePayloadBuild()(implicit ec: ExecutionContext, system: ActorSystem, log: NodoLogger): ContainerBlobFunc = {
    val containerName = system.settings.config.getString("azure-storage-blob.re-payload-container-name")
    val connectionString = system.settings.config.getString("azure-storage-blob.connection-string")
    build(containerName, connectionString)
  }

  private def build(containerName: String, connectionString: String)(implicit ec: ExecutionContext, system: ActorSystem, log: NodoLogger): ContainerBlobFunc = {
    val azureStorageBlobEnabled = system.settings.config.getBoolean("azure-storage-blob.enabled")

    if( azureStorageBlobEnabled ) {
      log.info(s"Starting Azure Storage Blob Client ${containerName} Service...")

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
            log.error(e, s"Error interacting with Azure Blob Storage ${containerName}: ${e.getMessage}")
            Future.failed(e)
          case e: Throwable =>
            log.error(e, s"Unexpected error on ${containerName}")
            Future.failed(e)
        }
      }
    } else {
      log.info("Azure Storage Blob Client Service not enabled: config-app [azure-storage-blob.enabled]=false")
      // TODO [FC] review
      (fileName: String, metadata: Map[String, String], fileContent: BinaryData, log: NodoLogger) => {
        Future.successful(())
      }
    }
  }

}
