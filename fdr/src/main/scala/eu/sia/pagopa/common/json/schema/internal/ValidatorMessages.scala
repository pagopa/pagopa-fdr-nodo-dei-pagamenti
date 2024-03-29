package eu.sia.pagopa.common.json.schema.internal

import com.osinka.i18n.{ Lang, Messages }

import java.text.MessageFormat
import scala.util.Try

object ValidatorMessages {

  val DefaultMessages: Map[String, String] = Map(
    "obj.missing.prop.dep" -> "Missing property dependency {0}.",
    "obj.max.props" -> "Too many properties. {0} properties found, but only a maximum of {1} is allowed.",
    "obj.min.props" -> "Found {0} properties, but a minimum of {1} is required.",
    "obj.additional.props" -> "Additional properties are not allowed, but found properties {0}.",
    "obj.required.prop" -> "Property {0} missing.",
    "arr.max" -> "Too many items. {0} items found, but only a maximum of {1} is allowed.",
    "arr.min" -> "Found {0} items, but a minimum of {1} is required.",
    "arr.dups" -> "Found duplicates.",
    "arr.out.of.bounds" -> "Array index {0} out of bounds.",
    "arr.invalid.index" -> "Invalid array index {0}.",
    "str.pattern" -> "''{0}'' does not match pattern ''{1}''.",
    "str.invalid.pattern" -> "Invalid pattern ''{0}''.",
    "str.min.length" -> "''{0}'' does not match minimum length of {1}.",
    "str.max.length" -> "''{0}'' exceeds maximum length of {1}.",
    "str.format" -> "''{0}'' does not match format {1}.",
    "num.multiple.of" -> "{0} is not a multiple of {1}.",
    "num.max" -> "{0} exceeds maximum value of {1}.",
    "num.max.exclusive" -> "{0} exceeds exclusive maximum value of {1}.",
    "num.min" -> "{0} is smaller than required minimum value of {1}.",
    "num.min.exclusive" -> "{0} is smaller than required exclusive minimum value of {1}.",
    "any.not" -> "Instance matches schema although it must not.",
    "any.all" -> "Instance does not match all schemas.",
    "any.any" -> "Instance does not match any of the schemas.",
    "any.one.of.none" -> "Instance does not match any schema.",
    "any.one.of.many" -> "Instance matches more than one schema.",
    "any.enum" -> "Instance is invalid enum value.",
    "any.const" -> "Instance does not match const value.",
    "comp.no.schema" -> "No schema applicable.",
    "err.expected.type" -> "Wrong type. Expected {0}, was {1}.",
    "err.unresolved.ref" -> "Could not resolve ref {0}.",
    "err.prop.not.found" -> "Could not find property {0}.",
    "err.ref.expected" -> "Expected to find ref at {0}.",
    "err.res.scope.id.empty" -> "Resolution scope ID must not be empty.",
    "err.parse.json" -> "Could not parse JSON.",
    "err.max.depth" -> "Maximum recursion depth reached.",
    "err.dependencies.not.found" -> "Dependency not found.",
    "err.definitions.not.found" -> "Definition not found.",
    "err.patternProperties.not.found" -> "Pattern Properties not found.",
    "err.false.schema" -> "Boolean false schema encountered.",
    "err.contains" -> "Array does not contain valid item.",
    "err.if.then.else" -> "Conditional validation failed."
  )

  def apply(msg: String, args: Any*)(implicit lang: Lang): String = {
    Try(Messages(msg, args: _*)).getOrElse(new MessageFormat(DefaultMessages(msg)).format(args.map(_.asInstanceOf[Object]).toArray))
  }

}
