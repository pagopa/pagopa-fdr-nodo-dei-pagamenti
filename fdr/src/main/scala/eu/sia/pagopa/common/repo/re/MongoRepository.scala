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

    // Gestione del risultato
    insertFuture.onComplete {
      case Success(result) =>
        println("Inserimento completato!")
      case Failure(exception) =>
        println(s"Errore durante l'inserimento: ${exception.getMessage}")
    }

  }

//  lazy val cosmosEndpoint = config.getString("azure-cosmos-data.endpoint")
//  lazy val cosmosKey = config.getString("azure-cosmos-data.key")
//  lazy val cosmosDbName = config.getString("azure-cosmos-data.db-name")
//  lazy val cosmosConsistencyLevel = ConsistencyLevel.valueOf(config.getString("azure-cosmos-data.consistency-level"))
//  lazy val cosmosTableName = config.getString("azure-cosmos-data.table-name")
//  lazy val cosmosTableNameReceipts = config.getString("azure-cosmos-receipts-rt.table-name")
//  lazy val client = new CosmosClientBuilder().endpoint(cosmosEndpoint).key(cosmosKey).consistencyLevel(cosmosConsistencyLevel).buildClient

//  def query(query: SqlQuerySpec) = {
//    log.info("executing query:" + query.getQueryText)
//    val container = client.getDatabase(cosmosDbName).getContainer(cosmosTableName)
//    container.queryItems(query, new CosmosQueryRequestOptions, classOf[PositiveBizEvent])
//  }

//  def getRtByKey(key: String): Future[Option[RtEntity]] = {
//    Future {
//      val container = client.getDatabase(cosmosDbName).getContainer(cosmosTableNameReceipts)
//      val response = container.readItem(key, new PartitionKey(key), classOf[String])
//
//      if (response.getStatusCode == 200) {
//        val json = response.getItem
//        Some(Json.parse(json).as[RtEntity])
//      } else {
//        None
//      }
//    }.recover {
//      case ex: CosmosException =>
//        log.error(s"Failed to read item with key $key: ${ex.getMessage}")
//        None
//    }
//  }
//
//  def save(item:CosmosPrimitive) = {
//    Future({
//      val s = client.getDatabase(cosmosDbName).getContainer(cosmosTableName)
//        .createItem(item)
//      s.getStatusCode
//    })
//  }

}
