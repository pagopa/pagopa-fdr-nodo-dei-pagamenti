package eu.sia.pagopa.common.util.azurestorageblob

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import com.azure.core.util.BinaryData
import com.azure.storage.blob.BlobServiceClientBuilder
import com.azure.storage.blob.models.BlobStorageException
import eu.sia.pagopa.common.util.NodoLogger
import eu.sia.pagopa.common.util.azurehubevent.Appfunction.ContainerBlobFunc

import scala.jdk.CollectionConverters._
import scala.concurrent.{ExecutionContext, Future}

object AzureStorageBlobClient {

  def build()(implicit ec: ExecutionContext, system: ActorSystem, log: NodoLogger): ContainerBlobFunc = {
    val azureStorageBlobEnabled = system.settings.config.getBoolean("azure-storage-blob.enabled")

    if( azureStorageBlobEnabled ) {
      log.info("Starting Azure Storage Blob Client FdR1-Flows Service...")
      val containerName = system.settings.config.getString("azure-storage-blob.fdr1-flows-container-name")
      val connectionString = system.settings.config.getString("azure-storage-blob.connection-string")

      val blobServiceClient = new BlobServiceClientBuilder()
        .connectionString(connectionString)
        .buildClient()

      val containerClient = blobServiceClient.getBlobContainerClient(containerName)

      (fileName: String, metadata: Map[String, String],  fileContent: BinaryData, log: NodoLogger) => {
//        val executionContext: MessageDispatcher = system.dispatchers.lookup("blobstorage-dispatcher")
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

//        Future(containerClient.getBlobClient(fileName).upload(fileContent))(executionContext) recoverWith {
//          case e: Throwable =>
//            log.error(e, "Error calling azure-storage-blob")
//            Future.failed(e)
//        }
      }
    } else {
      log.info("Azure Storage Blob Client Service not enabled: config-app [azure-storage-blob.enabled]=false")
      // TODO [FC]
      (fileName: String, metadata: Map[String, String], fileContent: BinaryData, log: NodoLogger) => {
        Future.successful(())
      }
    }
  }
}
