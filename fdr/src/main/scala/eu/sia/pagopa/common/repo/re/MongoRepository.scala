package eu.sia.pagopa.common.repo.re

import com.typesafe.config.Config
import eu.sia.pagopa.common.message.ReEventHub
import eu.sia.pagopa.common.repo.re.model.Fdr1Metadata
import eu.sia.pagopa.common.util.NodoLogger
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class MongoRepository(config:Config, log: NodoLogger)(implicit ec: ExecutionContext) {

  lazy val mongoConnectionString = config.getString("azure-mongo.connection-string")
  lazy val mongoMetadataDatabase = config.getString("azure-mongo.db-name")
  lazy val mongoMetadataDocument = config.getString("azure-mongo.metadata-document-name")
  lazy val mongoEventsDocument = config.getString("azure-mongo.events-document-name")

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

  def saveReEvent(data: ReEventHub): Unit = {
    val document: MongoCollection[Document] = mongoClient.getDatabase(mongoMetadataDatabase).getCollection(mongoEventsDocument)
    val insertFuture = document.insertOne(data.toDocument).toFuture()

    insertFuture.onComplete {
      case Success(result) =>
        log.info(s"RE Event ${data.sessionId} ${data.fdrAction} saved")
      case Failure(exception) =>
        log.error(exception, s"Problem to save on Mongo ${data.sessionId} ${data.fdrAction}")
    }

  }


}
