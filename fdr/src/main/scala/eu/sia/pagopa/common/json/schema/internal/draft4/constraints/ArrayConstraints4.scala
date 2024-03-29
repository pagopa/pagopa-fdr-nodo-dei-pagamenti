package eu.sia.pagopa.common.json.schema.internal.draft4.constraints

import com.osinka.i18n.Lang
import eu.sia.pagopa.common.json.schema.internal.Keywords
import eu.sia.pagopa.common.json.schema.internal.constraints.Constraints.{ AnyConstraints, ArrayConstraints, Constraint }
import eu.sia.pagopa.common.json.schema.internal.validation.VA
import eu.sia.pagopa.common.json.schema.internal.validators.TupleValidators
import eu.sia.pagopa.common.json.schema.{ SchemaArray, SchemaResolutionContext, SchemaTuple, SchemaType, SchemaValue }
import play.api.libs.json.{ JsBoolean, JsNumber, JsValue }
import scalaz.Success

case class ArrayConstraints4(
    maxItems: Option[Int] = None,
    minItems: Option[Int] = None,
    additionalItems: Option[SchemaType] = None,
    unique: Option[Boolean] = None,
    any: AnyConstraints = AnyConstraints4()
) extends ArrayConstraints
    with Constraint {

  import eu.sia.pagopa.common.json.schema.internal.validators.ArrayConstraintValidators._

  override def subSchemas: Set[SchemaType] =
    additionalItems.map(Set(_)).getOrElse(Set.empty) ++ any.subSchemas

  override def validate(schema: SchemaType, json: JsValue, resolutionContext: SchemaResolutionContext)(implicit lang: Lang): VA[JsValue] = {

    val reader = for {
      minItemsRule <- validateMinItems(minItems)
      maxItemsRule <- validateMaxItems(maxItems)
      uniqueRule <- validateUniqueness(unique)
    } yield {
      minItemsRule |+| maxItemsRule |+| uniqueRule
    }

    schema match {
      case t: SchemaTuple =>
        TupleValidators.validateTuple(additionalItems, t).flatMap(x => reader.map(f => f |+| x)).run(resolutionContext).repath(_.compose(resolutionContext.instancePath)).validate(json)
      case _: SchemaArray =>
        reader.run(resolutionContext).repath(_.compose(resolutionContext.instancePath)).validate(json)
      case _ => Success(json)
    }
  }

  def resolvePath(path: String): Option[SchemaType] = path match {
    case Keywords.Array.MinItems        => minItems.map(min => SchemaValue(JsNumber(min)))
    case Keywords.Array.MaxItems        => maxItems.map(max => SchemaValue(JsNumber(max)))
    case Keywords.Array.AdditionalItems => additionalItems
    case Keywords.Array.UniqueItems     => unique.map(u => SchemaValue(JsBoolean(u)))
    case other                          => any.resolvePath(other)
  }
}
