package eu.sia.pagopa.common.json.model.rendicontazione

import spray.json.{DefaultJsonProtocol, DeserializationException, JsArray, JsNumber, JsObject, JsString, JsValue}

import java.time.Instant
import scala.util.Try

object Convert extends DefaultJsonProtocol {

  def read(json: JsValue): Convert = {
    val map = json.asJsObject.fields

    Try(
      Convert(
        map("payload").asInstanceOf[JsString].value,
        map("encoding").asInstanceOf[JsString].value,
      )
    ).recover({ case e =>
      throw DeserializationException("Error on mapping Flow fields: " + e.getMessage)
    }).get
  }
}

case class Convert(
                 payload: String,
                 encoding: String
               )