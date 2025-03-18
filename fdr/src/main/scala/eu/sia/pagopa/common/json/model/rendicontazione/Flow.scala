package eu.sia.pagopa.common.json.model.rendicontazione

import spray.json.{DefaultJsonProtocol, DeserializationException, JsArray, JsNumber, JsObject, JsString, JsValue}

import scala.language.implicitConversions
import scala.util.Try

object Flow extends DefaultJsonProtocol {

  def read(json: JsValue): Flow = {
      val map = json.asJsObject.fields
      val senderType = map("sender").asInstanceOf[JsObject].fields("type").asInstanceOf[JsString].value
      val senderId = map("sender").asInstanceOf[JsObject].fields("id").asInstanceOf[JsString].value
      val senderPspId = map("sender").asInstanceOf[JsObject].fields("pspId").asInstanceOf[JsString].value
      val senderPspName = map("sender").asInstanceOf[JsObject].fields("pspName").asInstanceOf[JsString].value
      val senderBrokerId = map("sender").asInstanceOf[JsObject].fields("pspBrokerId").asInstanceOf[JsString].value
      val senderChannelId = map("sender").asInstanceOf[JsObject].fields("channelId").asInstanceOf[JsString].value
      val senderPassword = map("sender").asInstanceOf[JsObject].fields("password").asInstanceOf[JsString].value

      val receiverId = map("receiver").asInstanceOf[JsObject].fields("id").asInstanceOf[JsString].value
      val receiverEcId = map("receiver").asInstanceOf[JsObject].fields("organizationId").asInstanceOf[JsString].value
      val receiverEcName = map("receiver").asInstanceOf[JsObject].fields("organizationName").asInstanceOf[JsString].value

      val paymentListInput = map("paymentList").asInstanceOf[JsArray].elements
      val payments = paymentListInput.map(v =>
        PaymentTest(
          v.asInstanceOf[JsObject].fields("flowId").asInstanceOf[JsNumber].value.toInt,
          v.asInstanceOf[JsObject].fields("iuv").asInstanceOf[JsString].value,
          v.asInstanceOf[JsObject].fields("iur").asInstanceOf[JsString].value,
          v.asInstanceOf[JsObject].fields("index").asInstanceOf[JsNumber].value.intValue,
          v.asInstanceOf[JsObject].fields("amount").asInstanceOf[JsNumber].value.toInt,
          v.asInstanceOf[JsObject].fields("payDate").asInstanceOf[JsString].value,
          v.asInstanceOf[JsObject].fields("payStatus").asInstanceOf[PayStatusEnum.Value],
          v.asInstanceOf[JsObject].fields("transferId").asInstanceOf[JsNumber].value.toInt,
          v.asInstanceOf[JsObject].fields("created").asInstanceOf[JsString].value,
          v.asInstanceOf[JsObject].fields("updated").asInstanceOf[JsString].value,
        )
      )
      Try(
        Flow(
          map("name").asInstanceOf[JsString].value,
          map("date").asInstanceOf[JsString].value,
          map("revision").asInstanceOf[JsNumber].value.toInt,
          map("status").asInstanceOf[JsString].value,
          map("isLatest").asInstanceOf[Boolean],
          map("pspDomainId").asInstanceOf[JsString].value,
          map("orgDomainId").asInstanceOf[JsString].value,
          map("totPayments").asInstanceOf[JsNumber].value.toInt,
          map("totAmount").asInstanceOf[JsNumber].value.toInt,
          map("computedTotPayments").asInstanceOf[JsNumber].value.toInt,
          map("computedTotAmount").asInstanceOf[JsNumber].value.toInt,
          map("regulationDate").asInstanceOf[JsString].value,
          map("regulation").asInstanceOf[JsString].value,
          Sender(SenderTypeEnum.withName(senderType), senderId, senderPspId, senderPspName, senderBrokerId, senderChannelId, senderPassword),
          Receiver(receiverId, receiverEcId, receiverEcName),
          map("bicCodePouringBank").asInstanceOf[JsString].value,
          map("created").asInstanceOf[JsString].value,
          map("updated").asInstanceOf[JsString].value,
          map("published").asInstanceOf[JsString].value,
          payments
        )
      ).recover({ case _ =>
                throw DeserializationException("ConvertFlowRequest expected")
              }).get
    }
}

case class Flow(
                              name: String,
                              date: String,
                              revision: Integer,
                              status: String,
                              isLatest: Boolean,
                              pspDomainId: String,
                              orgDomainId: String,
                              totPayments: Integer,
                              totAmount: Integer,
                              computedTotPayments: Integer,
                              computedTotAmount: Integer,
                              regulationDate: String,
                              regulation: String,
                              sender: Sender,
                              receiver: Receiver,
                              bicCodePouringBank: String,
                              created: String,
                              updated: String,
                              published: String,
                              paymentList: Seq[PaymentTest]
                            )

//object ConvertFdrResponse extends DefaultJsonProtocol {
//
//  implicit val format: RootJsonFormat[ConvertFdrResponse] = new RootJsonFormat[ConvertFdrResponse] {
//    def write(res: ConvertFdrResponse): JsObject = {
//      var fields: Map[String, JsValue] =
//        Map("outcome" -> JsString(res.outcome))
//      if (res.description.isDefined) {
//        fields = fields + ("description" -> JsString(res.description.get))
//      }
//      JsObject(fields)
//    }
//
//    def read(json: JsValue): NotifyFdrResponse = ???
//  }
//}
//
//case class NotifyFdrResponse(outcome: String, description: Option[String])