package eu.sia.pagopa.common.json.model.rendicontazione

import spray.json.{DefaultJsonProtocol, DeserializationException, JsObject, JsString, JsValue}

import scala.util.Try

object Receiver extends DefaultJsonProtocol {

  def write(receiver: Receiver): JsObject = {
    JsObject(Map(
      "id" -> JsString(receiver.id),
      "organizationId" -> JsString(receiver.organizationId),
      "organizationName" -> JsString(receiver.organizationName)
    ))
  }

  def read(json: JsValue): Receiver = {
    val map = json.asJsObject.fields
    Try(
      Receiver(
        map("id").asInstanceOf[JsString].value,
        map("organizationId").asInstanceOf[JsString].value,
        map("organizationName").asInstanceOf[JsString].value
      )
    ).recover({ case e =>
      throw DeserializationException("Error on mapping Receiver fields: " + e.getMessage)
    }).get
  }

}

case class Receiver(
                     id: String,
                     organizationId: String,
                     organizationName: String
                   )

