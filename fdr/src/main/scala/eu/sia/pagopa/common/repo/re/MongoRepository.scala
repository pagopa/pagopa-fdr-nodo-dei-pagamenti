package eu.sia.pagopa.common.repo.re

import com.typesafe.config.Config
import eu.sia.pagopa.common.repo.re.model.Fdr1Metadata
import eu.sia.pagopa.common.util.NodoLogger
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class MongoRepository(config:Config, log: NodoLogger)(implicit ec: ExecutionContext) {

  lazy val mongoConnectionString = config.getString("azure-mongo-metadata.connection-string")
  lazy val mongoMetadataDatabase = config.getString("azure-mongo-metadata.db-name")
  lazy val mongoMetadataDocument = config.getString("azure-mongo-metadata.document-name")

  val mongoClient: MongoClient = MongoClient(mongoConnectionString)
  val database: MongoDatabase = mongoClient.getDatabase(mongoMetadataDatabase)

  def saveFdrMetadata(data: Fdr1Metadata): Unit = {
    val document: MongoCollection[Document] = mongoClient.getDatabase(mongoMetadataDatabase).getCollection(mongoMetadataDocument)
    val insertFuture = document.insertOne(data.toDocument).toFuture()

    insertFuture.onComplete {
      case Success(result) =>
        log.info(s"FdR Metadata ${data.getPsp()} ${data.getFlowId()} saved")
      case Failure(exception) =>
        log.error(exception, s"Problem to save on Mongo ${data.getPsp()} ${data.getFlowId()}")
    }

  }


}
