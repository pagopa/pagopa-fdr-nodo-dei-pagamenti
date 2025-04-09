package eu.sia.pagopa.common.json.model.rendicontazione

import spray.json.{DefaultJsonProtocol, DeserializationException, JsObject, JsString, JsValue}

import scala.util.Try

object Sender extends DefaultJsonProtocol {

  def write(sender: Sender): JsObject = {
    var fields: Map[String, JsValue] = {
      Map(
        "type" -> JsString(sender._type.toString),
        "id" -> JsString(sender.id),
        "pspId" -> JsString(sender.pspId),
        "pspName" -> JsString(sender.pspName),
        "pspBrokerId" -> JsString(sender.pspBrokerId),
        "channelId" -> JsString(sender.channelId),
        "password" -> JsString(sender.password.getOrElse("PLACEHOLDER"))
      )
    }

    JsObject(fields)
  }

  def read(json: JsValue): Sender = {
    val map = json.asJsObject.fields
    Try(
      Sender(
        SenderTypeEnum.withName(map("type").asInstanceOf[JsString].value),
        map("id").asInstanceOf[JsString].value,
        map("pspId").asInstanceOf[JsString].value,
        map("pspName").asInstanceOf[JsString].value,
        map("pspBrokerId").asInstanceOf[JsString].value,
        map("channelId").asInstanceOf[JsString].value,
        Try{map("password").asInstanceOf[JsString].value}.toOption,
      )
    ).recover({ case e =>
      throw DeserializationException("Error on mapping Sender fields: " + e.getMessage)
    }).get
  }

}

case class Sender(
                   _type: SenderTypeEnum.Value,
                   id: String,
                   pspId: String,
                   pspName: String,
                   pspBrokerId: String,
                   channelId: String,
                   password: Option[String]
                 )

object SenderTypeEnum extends Enumeration {
  val LEGAL_PERSON, ABI_CODE, BIC_CODE = Value
}
