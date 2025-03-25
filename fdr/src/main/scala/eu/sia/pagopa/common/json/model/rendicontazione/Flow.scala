package eu.sia.pagopa.common.json.model.rendicontazione

import spray.json.{DefaultJsonProtocol, DeserializationException, JsArray, JsNumber, JsObject, JsString, JsValue}

import java.time.Instant
import scala.util.Try

object Flow extends DefaultJsonProtocol {

  def read(json: JsValue): Flow = {
    val map = json.asJsObject.fields

    val sender = Sender.read(map("sender").asInstanceOf[JsObject])
    val receiver = Receiver.read(map("receiver").asInstanceOf[JsObject])

    val paymentListInput = map("payments").asInstanceOf[JsArray].elements
    val payments = paymentListInput.map(v =>
      FlowPayment.read(v)
    )
    Try(
      Flow(
        map("fdr").asInstanceOf[JsString].value,
        Instant.ofEpochSecond(map("fdrDate").asInstanceOf[JsNumber].value.toLong).toString,
        map("revision").asInstanceOf[JsNumber].value.toInt,
        map("status").asInstanceOf[JsString].value,
        map("computedTotPayments").asInstanceOf[JsNumber].value.toInt,
        map("computedSumPayments").asInstanceOf[JsNumber].value.toInt,
        map("regulationDate").asInstanceOf[JsString].value,
        map("regulation").asInstanceOf[JsString].value,
        sender,
        receiver,
        map("bicCodePouringBank").asInstanceOf[JsString].value,
        Instant.ofEpochSecond(map("created").asInstanceOf[JsNumber].value.toLong).toString,
        Instant.ofEpochSecond(map("updated").asInstanceOf[JsNumber].value.toLong).toString,
        payments
      )
    ).recover({ case e =>
      throw DeserializationException("Error on mapping Flow fields: " + e.getMessage)
    }).get
  }
}

case class Flow(
                 name: String,
                 date: String,
                 revision: Integer,
                 status: String,
                 computedTotPayments: Integer,
                 computedSumPayments: Integer,
                 regulationDate: String,
                 regulation: String,
                 sender: Sender,
                 receiver: Receiver,
                 bicCodePouringBank: String,
                 created: String,
                 updated: String,
                 paymentList: Seq[FlowPayment]
               )