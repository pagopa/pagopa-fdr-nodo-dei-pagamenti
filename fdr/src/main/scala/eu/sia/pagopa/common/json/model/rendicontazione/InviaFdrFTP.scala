package eu.sia.pagopa.common.json.model.rendicontazione

import spray.json.{DefaultJsonProtocol, DeserializationException, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}

import scala.language.implicitConversions
import scala.util.Try

object InviaFdrFTPRequest extends DefaultJsonProtocol {

  implicit val format: RootJsonFormat[InviaFdrFTPRequest] = new RootJsonFormat[InviaFdrFTPRequest] {
    def write(req: InviaFdrFTPRequest): JsObject = {
      JsObject(Map[String, JsValue](
        "fdr" -> JsString(req.fdr),
        "pspId" -> JsString(req.pspId),
        "organizationId" -> JsString(req.organizationId)
      ))
    }

    def read(json: JsValue): InviaFdrFTPRequest = {
      val map = json.asJsObject.fields
      Try(
        InviaFdrFTPRequest(
          map("fdr").asInstanceOf[JsString].value,
          map("pspId").asInstanceOf[JsString].value,
          map("organizationId").asInstanceOf[JsString].value
        )
      ).recover({ case _ =>
                throw DeserializationException("InviaFdrFTPRequest expected")
              }).get
    }

  }
}

case class InviaFdrFTPRequest(fdr: String, pspId: String, organizationId: String)
