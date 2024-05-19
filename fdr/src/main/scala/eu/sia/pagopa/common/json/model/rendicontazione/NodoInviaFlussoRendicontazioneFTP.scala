package eu.sia.pagopa.common.json.model.rendicontazione

import spray.json.{DefaultJsonProtocol, DeserializationException, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}

import scala.language.implicitConversions
import scala.util.Try

object NodoInviaFlussoRendicontazioneFTPReq extends DefaultJsonProtocol {

  implicit val format: RootJsonFormat[NodoInviaFlussoRendicontazioneFTPReq] = new RootJsonFormat[NodoInviaFlussoRendicontazioneFTPReq] {
    def write(req: NodoInviaFlussoRendicontazioneFTPReq): JsObject = {
      JsObject(Map[String, JsValue](
        "content" -> JsString(req.content)
      ))
    }

    def read(json: JsValue): NodoInviaFlussoRendicontazioneFTPReq = {
      val map = json.asJsObject.fields
      Try(
        NodoInviaFlussoRendicontazioneFTPReq(
          map("content").asInstanceOf[JsString].value
        )
      ).recover({ case _ =>
                throw DeserializationException("NodoInviaFlussoRendicontazioneFTPReq expected")
              }).get
    }

  }
}

case class NodoInviaFlussoRendicontazioneFTPReq(content: String)