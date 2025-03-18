package eu.sia.pagopa.common.json.model.rendicontazione

import org.joda.time.format.ISODateTimeFormat
import spray.json.{DefaultJsonProtocol, DeserializationException, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}

import java.time.LocalDateTime
import java.util.Date
import scala.language.implicitConversions
import scala.util.Try

object PaymentTest extends DefaultJsonProtocol {

  implicit val format: RootJsonFormat[PaymentTest] = new RootJsonFormat[PaymentTest] {
    def write(req: PaymentTest): JsObject = {
      JsObject(Map[String, JsValue](
        "fdr" -> JsString(req.fdr),
        "pspId" -> JsString(req.pspId),
        "organizationId" -> JsString(req.organizationId),
        "retry" -> JsNumber(req.retry),
        "revision" -> JsNumber(req.revision)
      ))
    }

    def read(json: JsValue): Payment = {
      val map = json.asJsObject.fields
      Try(
        PaymentTest(
          map("fdr").asInstanceOf[JsString].value,
          map("pspId").asInstanceOf[JsString].value,
          map("organizationId").asInstanceOf[JsString].value,
          map("retry").asInstanceOf[JsNumber].value.toInt,
          map("revision").asInstanceOf[JsNumber].value.toInt
        )
      ).recover({ case _ =>
                throw DeserializationException("NotifyFlowRequest expected")
              }).get
    }

  }
}

case class PaymentTest(
                    flowId: Integer,
                    iuv: String,
                    iur: String,
                    index: Integer,
                    amount: Integer,
                    payDate: String,
                    payStatus: PayStatusEnum.Value,
                    transferId: Integer,
                    created: String,
                    updated: String
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