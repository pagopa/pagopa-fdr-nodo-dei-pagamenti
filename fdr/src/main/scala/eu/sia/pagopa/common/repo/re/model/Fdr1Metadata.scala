package eu.sia.pagopa.common.repo.re.model

import org.mongodb.scala.bson.Document

import java.time.format.DateTimeFormatter
import java.time.{ZoneOffset, ZonedDateTime}


case class Fdr1Metadata(
                         psp:String,
                         brokerPsp:String,
                         channel:String,
                         creditorInstitution:String,
                         flowId: String,
                         flowDate: javax.xml.datatype.XMLGregorianCalendar,
                         payloadFilename:String,
                         pspCreditorInstitution:String  // used for sharding
                       ) {

  def getPsp(): String = psp
  def getBrokerPsp(): String = brokerPsp
  def getChannel(): String = channel
  def getCreditorInstitution(): String = creditorInstitution
  def getFlowId(): String = flowId
  def getFlowDate(): javax.xml.datatype.XMLGregorianCalendar = flowDate
  def getPayloadFilename(): String = payloadFilename
  def getPspCreditorInstitution(): String = payloadFilename

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
      "payloadFilename" -> payloadFilename,
      "pspCreditorInstitution" -> pspCreditorInstitution
    )
  }

}
