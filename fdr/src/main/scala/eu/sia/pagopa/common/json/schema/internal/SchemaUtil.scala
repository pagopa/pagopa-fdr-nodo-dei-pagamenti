package eu.sia.pagopa.common.json.schema.internal

import eu.sia.pagopa.common.json.schema.internal.validation.Validated
import play.api.libs.json._
import scalaz.Failure

object SchemaUtil {

  def dropSlashIfAny(path: String): String = if (path.startsWith("/#")) path.substring(1) else path

  def failure(keyword: String, msg: String, schemaPath: Option[JsPath], instancePath: JsPath, instance: JsValue, additionalInfo: JsObject = Json.obj()): Validated[JsonValidationError, JsValue] =
    Failure(Seq(JsonValidationError(msg, createErrorObject(keyword, schemaPath, instancePath, instance, additionalInfo))))

  def createErrorObject(keyword: String, schemaPath: Option[JsPath], instancePath: JsPath, instance: JsValue, additionalInfo: JsObject): JsObject = {
    Json
      .obj("keyword" -> keyword)
      .deepMerge(schemaPath.fold(Json.obj("schemaPath" -> ""))(p => Json.obj("schemaPath" -> dropSlashIfAny(p.toString()))))
      .deepMerge(Json.obj("instancePath" -> instancePath.toString(), "value" -> instance, "errors" -> additionalInfo))
  }

  def typeOfAsString(json: JsValue): String = {
    json match {
      case JsString(_)  => "string"
      case JsNumber(_)  => "number"
      case JsBoolean(_) => "boolean"
      case JsObject(_)  => "object"
      case JsArray(_)   => "array"
      case JsNull       => "null"
      case JsFalse      => "boolean"
      case JsTrue       => "boolean"
    }
  }

  def toJson(errors: collection.Seq[(JsPath, collection.Seq[JsonValidationError])]): JsArray = {
    val emptyErrors = Json.arr()
    errors.foldLeft(emptyErrors) { case (accumulatedErrors, (_, validationErrors)) =>
      val maybeError = validationErrors.foldLeft(None: Option[JsObject])((aggregatedError, err) =>
        err.args.headOption match {
          case Some(o @ JsObject(_)) =>
            Some(aggregatedError.fold(deepMerge(o, Json.obj("msgs" -> err.messages)))(errObj => deepMerge(errObj, Json.obj("msgs" -> err.messages))))
          case _ => aggregatedError
        }
      )
      maybeError.fold(accumulatedErrors)(o => accumulatedErrors :+ o)
    }
  }

  private def deepMerge(obj: JsObject, other: JsObject): JsObject = {
    def merge(existingObject: JsObject, otherObject: JsObject): JsObject = {
      val result = existingObject.fields.toMap ++ otherObject.fields.toMap.map { case (otherKey, otherValue) =>
        val maybeExistingValue = existingObject.fields.toMap.get(otherKey)

        val newValue = (maybeExistingValue, otherValue) match {
          case (Some(e: JsObject), o: JsObject) => merge(e, o)
          case (Some(e: JsArray), o: JsArray)   => e ++ o
          case _                                => otherValue
        }
        otherKey -> newValue
      }
      JsObject(result)
    }
    merge(obj, other)
  }

}
