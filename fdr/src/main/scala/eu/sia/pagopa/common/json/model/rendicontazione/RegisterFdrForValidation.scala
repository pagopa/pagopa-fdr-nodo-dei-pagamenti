package eu.sia.pagopa.common.json.model.rendicontazione

import spray.json.{DefaultJsonProtocol, DeserializationException, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}

import scala.language.implicitConversions
import scala.util.Try

object RegisterFdrForValidationRequest extends DefaultJsonProtocol {

  implicit val format: RootJsonFormat[RegisterFdrForValidationRequest] = new RootJsonFormat[RegisterFdrForValidationRequest] {
    def write(req: RegisterFdrForValidationRequest): JsObject = {
      JsObject(Map[String, JsValue](
        "flowId" -> JsString(req.flowId),
        "pspId" -> JsString(req.pspId),
        "organizationId" -> JsString(req.organizationId),
        "flowTimestamp" -> JsString(req.flowTimestamp),
      ))
    }

    def read(json: JsValue): RegisterFdrForValidationRequest = {
      val map = json.asJsObject.fields
      Try(
        RegisterFdrForValidationRequest(
          map("flowId").asInstanceOf[JsString].value,
          map("pspId").asInstanceOf[JsString].value,
          map("organizationId").asInstanceOf[JsString].value,
          map("flowTimestamp").asInstanceOf[JsString].value
        )
      ).recover({ case _ =>
        throw DeserializationException("RegisterFdrForValidationRequest expected")
      }).get
    }

  }
}

case class RegisterFdrForValidationRequest(flowId: String, pspId: String, organizationId: String, flowTimestamp: String)