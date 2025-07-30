package eu.sia.pagopa.common.message

import eu.sia.pagopa.common.repo.re.model.Re
import org.mongodb.scala.bson.{BsonArray, BsonInt32, BsonString, Document}

import java.time.{LocalDateTime, ZoneId}
import java.util.Date

object CategoriaEvento extends Enumeration {
  val INTERNO, INTERFACCIA = Value
}
object SottoTipoEvento extends Enumeration {
  val REQ, RESP, INTERN = Value
}
object SottoTipoEventoEvh extends Enumeration {
  val REQ, RES = Value
}
object CategoriaEventoEvh extends Enumeration {
  val INTERFACE, INTERNAL = Value
}
object SoapReceiverType extends Enumeration {
  val NEXI, FDR = Value
}

object Componente extends Enumeration {
  val FDR, NDP_FDR, FDR_NOTIFIER, AZURE_STORAGE_ACCOUNT = Value
}

case class ReExtra(uri: Option[String] = None, headers: Seq[(String, String)] = Nil, httpMethod: Option[String] = None, callRemoteAddress: Option[String] = None, statusCode: Option[Int] = None, elapsed: Option[Long] = None, soapProtocol: Boolean = false)

case class ReRequest(
                      override val sessionId: String,
                      override val testCaseId: Option[String] = None,
                      re: Re,
                      reExtra: Option[ReExtra] = None,
                      retry: Int = 0
                    ) extends BaseMessage

case class ReEventHub(
                       serviceIdentifier: String,
                       uniqueId: String,
                       created: LocalDateTime,
                       sessionId: Option[String] = None,
                       eventType: String,
                       fdrStatus: Option[String] = None,
                       fdr: Option[String] = None,
                       pspId: Option[String] = None,
                       organizationId: Option[String] = None,
                       fdrAction: Option[String] = None,
                       httpType: String,
                       httpMethod: Option[String],
                       httpUrl: Option[String] = None,
                       blobBodyRef: Option[BlobBodyRef] = None,
                       header: Map[String, Seq[String]]
                     ) {
  def toDocument: Document = {
    Document(
      "PartitionKey" -> created.toLocalDate.toString,
      "serviceIdentifier" -> serviceIdentifier,
      "uniqueId" -> uniqueId,
      "created" -> created.toString,
      "sessionId" -> sessionId.map(BsonString(_)),
      "eventType" -> eventType,
      "fdrStatus" -> fdrStatus.map(BsonString(_)),
      "fdr" -> fdr.map(BsonString(_)),
      "pspId" -> pspId.map(BsonString(_)),
      "organizationId" -> organizationId.map(BsonString(_)),
      "fdrAction" -> fdrAction.map(BsonString(_)),
      "httpType" -> httpType,
      "httpMethod" -> httpMethod.map(BsonString(_)),
      "httpUrl" -> httpUrl.map(BsonString(_)),
      "blobBodyRef" -> blobBodyRef.map(blob => Document(
        "storageAccount" -> blob.storageAccount.map(BsonString(_)),
        "containerName" -> blob.containerName.map(BsonString(_)),
        "fileName" -> blob.fileName.map(BsonString(_)),
        "fileLength" -> BsonInt32(blob.fileLength)
      )),
      "header" -> Document(header.map { case (key, values) =>
        key -> BsonArray.fromIterable(values.map(v => BsonString(v)))
      })
    )
  }
}

case class BlobBodyRef(
                        storageAccount: Option[String],
                        containerName: Option[String],
                        fileName: Option[String],
                        fileLength: Int
                      )
