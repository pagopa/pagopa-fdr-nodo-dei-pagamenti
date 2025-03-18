package eu.sia.pagopa.common.json.model.rendicontazione

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonEntityStreamingSupport.json
import spray.json.{DefaultJsonProtocol, DeserializationException, JsObject, JsString, JsValue, RootJsonFormat}

import scala.reflect.runtime.universe.Try

object Sender extends DefaultJsonProtocol {

  implicit object SenderJsonFormat extends RootJsonFormat[Sender] {

    def write(sender: Sender): JsObject = {
      var fields: Map[String, JsValue] = {
        Map(
          "type" -> JsString(sender._type.toString),
          "id" -> JsString(sender.id),
          "pspId" -> JsString(sender.pspId),
          "pspName" -> JsString(sender.pspName),
          "pspBrokerId" -> JsString(sender.pspBrokerId),
          "channelId" -> JsString(sender.channelId),
          "password" -> JsString(sender.password)
        )
      }

      JsObject(fields)
    }

    def read(json: JsValue): Sender = {
      val map = json.asJsObject.fields
      Try(
        Sender(
          map("sender_type").asInstanceOf[SenderTypeEnum.Value],
          map("sender_id").asInstanceOf[JsString].value,
          map("psp_id").asInstanceOf[JsString].value
          map("sender_psp_name").asInstanceOf[JsString].value,
          map("sender_psp_broker_id").asInstanceOf[JsString].value,
          map("sender_channel_id").asInstanceOf[JsString].value,
          map("sender_password").asInstanceOf[JsString].value
        )
      ).recover({ case _ =>
        throw DeserializationException("ConvertFlowRequest expected")
      }).get
    }
  }

}

case class Sender(
                   _type: SenderTypeEnum.Value,
                   id: String,
                   pspId: String,
                   pspName: String,
                   pspBrokerId: String,
                   channelId: String,
                   password: String
                 )

object SenderTypeEnum extends Enumeration {
  val LEGAL_PERSON, ABI_CODE, BIC_CODE = Value
}
