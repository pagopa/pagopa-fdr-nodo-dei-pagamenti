package eu.sia.pagopa.common.json.model.rendicontazione

import spray.json.{DefaultJsonProtocol, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}

object Payments extends DefaultJsonProtocol {

  implicit object PaymentsJsonFormat extends RootJsonFormat[Payment] {
    def write(payments: Payment): JsObject = {
      var fields: Map[String, JsValue] = {
        Map(
          "iuv" -> JsString(payments.iuv),
          "iur" -> JsString(payments.iur),
          "pay" -> JsNumber(payments.pay),
          "payStatus" -> JsString(payments.payStatus.toString),
          "payDate" -> JsString(payments.payDate)
        )
      }

      if (payments.index.isDefined) {
        fields = fields + ("index" -> JsNumber(payments.index.get))
      }
      JsObject(fields)
    }

    def read(value: JsValue): Payment = ???
  }

}

case class Payment(
                    iuv: String,
                    iur: String,
                    index: Option[Integer],
                    pay: BigDecimal,
                    payStatus: PayStatusEnum.Value,
                    payDate: String
                  )

object PayStatusEnum extends Enumeration {
  val EXECUTED, REVOKED, NO_RPT = Value
}

case class PaymentsRequest(payments: Seq[Payment])

object PaymentsResponse {}

case class PaymentsResponse()


