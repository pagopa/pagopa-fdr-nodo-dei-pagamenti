package eu.sia.pagopa.common.json.model.rendicontazione

import spray.json.{DefaultJsonProtocol, DeserializationException, JsNumber, JsString, JsValue}

import scala.util.Try

object FlowPayment extends DefaultJsonProtocol {

  def read(json: JsValue): FlowPayment = {
    val map = json.asJsObject.fields
    Try(
      FlowPayment(
        map("iuv").asInstanceOf[JsString].value,
        map("iur").asInstanceOf[JsString].value,
        map("index").asInstanceOf[JsNumber].value.intValue,
        map("pay").asInstanceOf[JsNumber].value.toDouble,
        map("payDate").asInstanceOf[JsString].value,
        PayStatusEnum.withName(map("payStatus").asInstanceOf[JsString].value),
        map("idTransfer").asInstanceOf[JsNumber].value.toInt,
      )
    ).recover({ case e =>
      throw DeserializationException("Error on mapping Payment fields: " + e.getMessage)
    }).get
  }
}

case class FlowPayment(
                        iuv: String,
                        iur: String,
                        index: Integer,
                        pay: Double,
                        payDate: String,
                        payStatus: PayStatusEnum.Value,
                        transferId: Integer
                      )