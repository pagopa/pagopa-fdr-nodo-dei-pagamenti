package eu.sia.pagopa.common.json.schema

import eu.sia.pagopa.common.json.schema.internal.refs.{ Ref, Refs }
import eu.sia.pagopa.common.json.schema.internal.validation.{ VA, Validated }
import play.api.libs.json._
import scalaz.{ Failure, ReaderWriterState, Semigroup }

import scala.language.reflectiveCalls
import scala.util.{ Failure => ScalaFailure, Success => ScalaSuccess, Try }

package object internal {

  /** Traverse the given schema and returns a Map of Refs representing the resolution scope of the mapped schema elements.
    * @param schema
    *   the schema element to be traversed
    * @param resolutionScope
    *   the current resolution scope
    * @param knownSchemas
    *   Map containing all found scopes so far
    * @return
    *   Map containing all found scopes
    */
  def collectSchemas(schema: SchemaType, resolutionScope: Option[Ref], knownSchemas: Map[String, SchemaType] = Map.empty): Map[String, SchemaType] = {

    val currentScope = schema.constraints.id.map(i => Refs.mergeRefs(Ref(i), resolutionScope))
    val updatedMap = currentScope.fold(knownSchemas)(id => knownSchemas + (id.value -> schema))
    val m = schema match {
      case SchemaObject(props, _, _) =>
        props.foldLeft(updatedMap) { (schemas, prop) =>
          {
            collectSchemas(prop.schemaType, currentScope orElse resolutionScope, schemas)
          }
        }
      case SchemaArray(item, _, _) => collectSchemas(item, currentScope, updatedMap)
      case SchemaTuple(items, _, _) =>
        items.foldLeft(updatedMap) { (schemas, item) =>
          collectSchemas(item, currentScope, schemas)
        }
      case SchemaRoot(_, s) => collectSchemas(s, resolutionScope, updatedMap)
      case _                => updatedMap
    }

    schema.constraints.subSchemas.foldLeft(m) { (schemas, s) =>
      collectSchemas(s, currentScope orElse resolutionScope, schemas)
    }
  }

  def failure(keyword: String, msg: String, schemaPath: Option[JsPath], instancePath: JsPath, instance: JsValue, additionalInfo: JsObject = Json.obj()): Validated[JsonValidationError, JsValue] = {

    Failure(Seq(JsonValidationError(msg, SchemaUtil.createErrorObject(keyword, schemaPath, instancePath, instance, additionalInfo))))
  }

  // provide semigroup for unit for use with ReaderWriterState
  implicit def UnitSemigroup: Semigroup[Unit] = new Semigroup[Unit] {
    override def append(f1: Unit, f2: => Unit): Unit = ()
  }

  implicit class TryExtensions[A](t: Try[A]) {
    def toJsonEither: Either[JsonValidationError, A] = t match {
      case ScalaSuccess(result)    => Right(result)
      case ScalaFailure(throwable) => Left(JsonValidationError(throwable.getMessage))
    }
  }

  implicit class EitherExtensions[A, B](either: Either[A, B]) {
    def orElse(e: => Either[A, B]): Either[A, B] = either match {
      case r @ Right(_) => r
      case Left(_)      => e
    }
  }

  def using[T <: { def close(): Unit }, A](resource: T)(block: T => A): A = {
    try {
      block(resource)
    } finally {
      if (resource != null) resource.close()
    }
  }

  /** Type params in this order are:
    *   - reader
    *   - writer
    *   - state
    *   - value
    */

  type ValidationStep[A] = ReaderWriterState[SchemaResolutionContext, Unit, VA[JsValue], A]
  type Props = Seq[(String, JsValue)]
  type PropertyValidationResult = (String, VA[JsValue])
  type ValidProperties = Seq[(String, JsValue)]
  type InvalidProperties = Seq[VA[JsValue]]

}
