package eu.sia.pagopa.common.json.schema.internal.serialization

import eu.sia.pagopa.common.json.schema._
import eu.sia.pagopa.common.json.schema.internal.Keywords
import eu.sia.pagopa.common.json.schema.internal.constraints.Constraints._
import eu.sia.pagopa.common.json.schema.internal.draft4.constraints.ObjectConstraints4
import eu.sia.pagopa.common.json.schema.internal.refs.Ref
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

object Severe {
  override def toString = "severe"
}

trait SchemaReads {
  self: SchemaVersion =>

  // entry point
  implicit val schemaTypeReads: Reads[SchemaType] = rootReads.map(asSchemaType)

  lazy val schemaReads: Reads[SchemaType] = (json: JsValue) => {
    schemaReadsSeq.foldLeft[JsResult[SchemaType]](JsError("Invalid JSON schema")) { case (acc, reads) =>
      acc match {
        case err @ JsError(errs) if errs.exists(_._2.exists(_.args.contains(Severe))) => err
        case succ @ JsSuccess(_, _)                                                   => succ
        case _                                                                        => reads.reads(json)
      }
    }
  }

  def schemaReadsSeq: Seq[Reads[SchemaType]]
  def anyConstraintReads: Reads[AnyConstraints]
  def stringReads: Reads[SchemaString]
  def numberReads: Reads[SchemaNumber]
  def integerReads: Reads[SchemaInteger]
  def tupleReads: Reads[SchemaTuple]
  def arrayReads: Reads[SchemaArray]
  def objectReads: Reads[SchemaObject]

  def anyKeywords: Set[String]
  def arrayKeywords: Set[String]
  def objectKeywords: Set[String]

  def schemaLocation: String

  val withSchemaValueReader: Reads[SchemaType] = schemaReads or jsValueReads.map(asSchemaType)

  lazy val jsValueReads: Reads[SchemaValue] = {
    case bool @ JsBoolean(_) => JsSuccess(SchemaValue(bool))
    case s @ JsString(_)     => JsSuccess(SchemaValue(s))
    case a @ JsArray(_)      => JsSuccess(SchemaValue(a))
    case other               => JsError(s"Expected either Json boolean, string or array, got $other.")
  }

  def rootReads: Reads[SchemaType] = (json: JsValue) => {
    val maybeSchema = (json \ "$schema").validateOpt[String]
    for {
      $schema <- maybeSchema
      schema <- schemaReads.reads(json)
    } yield $schema match {
      case Some(v) if v == schemaLocation =>
        SchemaRoot(Some(self), schema)
      case _ => schema
    }
  }

  lazy val nullReader: Reads[SchemaNull] =
    anyConstraintReads.flatMap(any => Reads.pure(SchemaNull(any)))

  lazy val booleanReader: Reads[SchemaBoolean] =
    anyConstraintReads.flatMap(any => Reads.pure(SchemaBoolean(any)))

  lazy val compoundReader: Reads[CompoundSchemaType] = {
    case obj @ JsObject(fields) =>
      obj \ "type" match {
        case JsDefined(JsArray(values)) =>
          val jsResults: Seq[JsResult[SchemaType]] = values.toSeq.map(value => dispatchType(value.as[String]).reads(JsObject(List("type" -> value) ++ fields.filterNot(_._1 == "type"))))
          val successes = jsResults.collect { case JsSuccess(success, _) => success }
          JsSuccess(CompoundSchemaType(successes))
        case _ => JsError("Expected Json array while reading compound type.")
      }
    case _ => JsError("Expected Json object while reading compound type.")
  }

  def createDelegateReader[A <: SchemaType with HasProps[A]](delegateReads: Reads[A], keywords: Set[String]): Reads[A] = {
    case json @ JsObject(props) =>
      delegateReads
        .reads(json)
        .map(schema => {
          addRemainingProps(schema, props.toList, keywords)
        })
    case err => JsError(s"Expected Json object during read, got $err")
  }

  private def addRemainingProps[A <: HasProps[A]](init: A, props: Iterable[(String, JsValue)], keywords: Set[String]): A = {
    val remainingProps = props.filterNot { case (propName, _) => keywords.contains(propName) }
    val remaining: Iterable[(String, SchemaType)] = remainingProps.map { case (name, value) =>
      name -> schemaReads.reads(value).asOpt.fold[SchemaType](SchemaValue(value))(x => x)
    }
    init.withProps(remaining.toSeq)
  }

  lazy val typeReader: Reads[SchemaType] = (__ \ "type").read[String].flatMap(dispatchType)

  def dispatchType: String => Reads[SchemaType] = {
    case "boolean" => booleanReader.map(asSchemaType)
    case "string"  => stringReads.map(asSchemaType)
    case "integer" => integerReads.map(asSchemaType)
    case "number"  => numberReads.map(asSchemaType)
    case "array"   => delegatingArrayReader.map(asSchemaType) orElse delegatingTupleReader.map(asSchemaType)
    case "object"  => delegatingObjectReader.map(asSchemaType)
    case "null"    => nullReader.map(asSchemaType)
    case other     => Reads.apply(_ => JsError(s"Invalid JSON schema. Unknown $other type.")).map(asSchemaType)
  }

  lazy val delegatingTupleReader: Reads[SchemaTuple] = createDelegateReader(tupleReads, arrayKeywords)
  lazy val delegatingArrayReader: Reads[SchemaArray] = createDelegateReader(arrayReads, arrayKeywords)
  lazy val delegatingObjectReader: Reads[SchemaObject] = createDelegateReader(objectReads, objectKeywords)
  lazy val delegatingRefReads: Reads[SchemaRef] = createDelegateReader(refReads, anyKeywords ++ Set("$ref"))

  lazy val refReads: Reads[SchemaRef] = {
    (
      (__ \ Keywords.Ref).readNullable[String] and
      anyConstraintReads
    ).tupled.flatMap { case (ref, anyConstraints) =>
      ref.fold[Reads[SchemaRef]](Reads.apply(_ => JsError("No ref found")))(r => Reads.pure(SchemaRef(Ref(r), anyConstraints)))
    }
  }

  def readJsNull(path: JsPath): Reads[Option[JsValue]] =
    Reads[Option[JsValue]] { json =>
      path
        .applyTillLast(json)
        .fold(
          identity,
          _.fold(
            // const not found
            _ => JsSuccess(None),
            js => JsSuccess(Some(js))
          )
        )
    }

  def optional(keyword: Option[String]): Reads[Option[SchemaType]] =
    keyword.fold[Reads[Option[SchemaType]]](Reads.pure(None))(_if => (__ \ _if).lazyReadNullable(schemaReads))

  val schemaTypeMapReader: Reads[Map[String, SchemaType]] =
    (json: JsValue) => json.validate[Map[String, SchemaType]]

  val mapReadsInstanceWithJsValueReader: Reads[Map[String, SchemaType]] =
    (json: JsValue) => json.validate[Map[String, SchemaType]](Reads.mapReads(withSchemaValueReader))

  /** Read a JsArray of JsObjects as a Seq of SchemaType.
    */
  def schemaTypeSeqReader(check: ElementCheck = (_ => true, None)): Reads[Seq[SchemaType]] = {
    case JsArray(els) if !els.exists(check._1) =>
      JsError(check._2.getOrElse("Error while reading JsArray."))
    case JsArray(els) =>
      // ignore all non-objects
      val results: Seq[JsResult[SchemaType]] = els.toSeq.filter(check._1).map(Json.fromJson[SchemaType])
      if (results.exists(_.isError)) mergeErrors(results)
      else JsSuccess(results.collect { case JsSuccess(s, _) => s })
    case other => JsError(s"Expected array of Json objects, got $other.")
  }

  private def mergeErrors(results: Seq[JsResult[SchemaType]]): JsResult[Seq[SchemaType]] =
    results
      .collect { case err @ JsError(_) =>
        err
      }
      .reduceLeft[JsError] { case (e1, e2) =>
        JsError.merge(e1, e2)
      }

  def emptyObject: SchemaType = SchemaObject(Seq.empty, ObjectConstraints4())

  def tuples2Attributes(props: Iterable[(String, SchemaType)]): List[SchemaProp] =
    props.map { case (name, schema) => SchemaProp(name, schema) }.toList

  type ElementCheck = ((JsValue) => Boolean, Option[ErrorMessage])
  type ErrorMessage = String

  def anyJsValue: ElementCheck = ((_: JsValue) => true, None)

  def asSchemaType[A <: SchemaType](s: A): SchemaType = s

  def readStrictOption[A: Reads](keyword: String): Reads[Option[A]] = (json: JsValue) => {
    (__ \ keyword).readNullable[A].reads(json) match {
      case JsError(errs) =>
        JsError(errs.map { case (p, errors) =>
          p -> errors.map(err => JsonValidationError(err.messages, Json.obj("errors" -> "Invalid schema"), Severe))
        })
      case succ => succ
    }
  }

  def lazyReadStrictOption[A](r: Reads[A], keyword: String): Reads[Option[A]] = (json: JsValue) => {
    (__ \ keyword).lazyReadNullable[A](r).reads(json) match {
      case JsError(errs) =>
        JsError(errs.map { case (p, errors) => p -> errors.map(err => JsonValidationError(err.messages, Severe)) })
      case succ => succ
    }
  }
}
