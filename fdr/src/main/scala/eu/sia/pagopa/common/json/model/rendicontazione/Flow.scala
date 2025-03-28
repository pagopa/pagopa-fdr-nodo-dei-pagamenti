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
        Try{map("revision").asInstanceOf[JsNumber].value.toInt}.toOption,
        Try{map("status").asInstanceOf[JsString].value}.toOption,
        map("computedTotPayments").asInstanceOf[JsNumber].value.toInt,
        map("computedSumPayments").asInstanceOf[JsNumber].value.toInt,
        map("regulationDate").asInstanceOf[JsString].value,
        map("regulation").asInstanceOf[JsString].value,
        sender,
        receiver,
        Try{map("bicCodePouringBank").asInstanceOf[JsString].value}.toOption,
        Try{Instant.ofEpochSecond(map("created").asInstanceOf[JsNumber].value.toLong).toString}.toOption,
        Try{Instant.ofEpochSecond(map("updated").asInstanceOf[JsNumber].value.toLong).toString}.toOption,
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
                 revision: Option[Int],
                 status: Option[String],
                 computedTotPayments: Integer,
                 computedSumPayments: Integer,
                 regulationDate: String,
                 regulation: String,
                 sender: Sender,
                 receiver: Receiver,
                 bicCodePouringBank: Option[String],
                 created: Option[String],
                 updated: Option[String],
                 paymentList: Seq[FlowPayment]
               )