package eu.sia.pagopa.common.repo.re.model

import eu.sia.pagopa.common.message.BlobBodyRef
import org.mongodb.scala.bson.{BsonInt32, BsonString, Document}

import java.time.format.DateTimeFormatter
import java.time.{ZoneOffset, ZonedDateTime}


case class Fdr1Metadata(
                         psp:String,
                         brokerPsp:String,
                         channel:String,
                         creditorInstitution:String,
                         flowId: String,
                         flowDate: javax.xml.datatype.XMLGregorianCalendar,
                         blobBodyRef: Option[BlobBodyRef] = None,
                         pspCreditorInstitution:String  // used for sharding
                       ) {

  def getPsp(): String = psp
  def getFlowId(): String = flowId

  def toDocument: Document = {
    val formatter = DateTimeFormatter.ISO_INSTANT

    // convert flowDate to ISO-8601 string
    val flowDateString = ZonedDateTime.ofInstant(flowDate.toGregorianCalendar.toInstant, ZoneOffset.UTC).format(formatter)

    Document(
      "psp" -> psp,
      "brokerPsp" -> brokerPsp,
      "channel" -> channel,
      "creditorInstitution" -> creditorInstitution,
      "flowId" -> flowId,
      "flowDate" -> flowDateString,
      "blobBodyRef" -> blobBodyRef.map(blob => Document(
        "storageAccount" -> blob.storageAccount.map(BsonString(_)),
        "containerName" -> blob.containerName.map(BsonString(_)),
        "fileName" -> blob.fileName.map(BsonString(_)),
        "fileLength" -> BsonInt32(blob.fileLength)
      )),
      "pspCreditorInstitution" -> pspCreditorInstitution
    )
  }

}
