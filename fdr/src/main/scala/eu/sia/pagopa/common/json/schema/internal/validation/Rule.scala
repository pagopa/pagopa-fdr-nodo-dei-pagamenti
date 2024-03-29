package eu.sia.pagopa.common.json.schema.internal.validation

import play.api.libs.json.{ JsPath, JsonValidationError }
import scalaz.{ Failure, Success }

trait RuleLike[I, O] {

  /** Apply the Rule to `data`
    *
    * @param data
    *   The data to validate
    * @return
    *   The Result of validating the data
    */
  def validate(data: I): VA[O]
}

trait Rule[I, O] extends RuleLike[I, O] {

//  /**
//    * Compose two Rules
//    * {{{
//    *   val r1: Rule[JsValue, String] = // implementation
//    *   val r2: Rule[String, Date] = // implementation
//    *   val r = r1.compose(r2)
//    *
//    * }}}
//    *
//    * @param path a prefix for the errors path if the result is a `Failure`
//    * @param sub the second Rule to apply
//    * @return The combination of the two Rules
//    */
//  def compose[P](path: Path)(sub: => RuleLike[O, P]): Rule[I, P] =
//    this.flatMap { o => Rule(_ => sub.validate(o)) }.repath(path ++ _)

//  def flatMap[B](f: O => Rule[I, B]): Rule[I, B] =
//    Rule { d =>
//      this.validate(d)
//        .map(f)
//        .fold(
//          es => Failure(es),
//          r => r.validate(d))
//    }

//  /**
//    * Create a new Rule that try `this` Rule, and apply `t` if it fails
//    * {{{
//    *   val rb: Rule[JsValue, A] = From[JsValue]{ __ =>
//    *     ((__ \ "name").read[String] ~ (__ \ "foo").read[Int])(B.apply _)
//    *   }
//    *
//    *   val rc: Rule[JsValue, A] = From[JsValue]{ __ =>
//    *     ((__ \ "name").read[String] ~ (__ \ "bar").read[Int])(C.apply _)
//    *   }
//    *   val rule = rb orElse rc orElse Rule(_ => typeFailure)
//    * }}}
//    *
//    * @param t an alternative Rule
//    * @return a Rule
//    */
//  def orElse[OO >: O](t: => RuleLike[I, OO]): Rule[I, OO] =
//    Rule(d => this.validate(d) orElse t.validate(d))

  // would be nice to have Kleisli in play
//  def compose[P](sub: => RuleLike[O, P]): Rule[I, P] = compose(Path)(sub)
//  def compose[P](m: Mapping[ValidationError, O, P]): Rule[I, P] = compose(Rule.fromMapping(m))

  /** Create a new Rule the validate `this` Rule and `r2` simultaneously If `this` and `r2` both fail, all the error are returned
    * {{{
    *   val valid = Json.obj(
    *       "firstname" -> "Julien",
    *       "lastname" -> "Tournay")
    *   val composed = notEmpty |+| minLength(3)
    *   (Path \ "firstname").read(composed).validate(valid) // Success("Julien")
    * }}}
    */
  def |+|[OO <: O](r2: RuleLike[I, OO]): Rule[I, O] = Rule[I, O] { v =>
    Rule.keepAnd(this.validate(v), r2.validate(v)).leftMap {
      _.groupBy(_._1).map { case (path, errs) =>
        path -> errs.flatMap(_._2)
      }.toSeq
    }
  }

  /** This methods allows you to modify the Path of errors (if the result is a Failure) when aplying the Rule
    */
  def repath(f: JsPath => JsPath): Rule[I, O] =
    Rule { d =>
      this.validate(d).leftMap {
        _.map { case (p, errs) =>
          f(p) -> errs
        }
      }
    }

}

object Rule {
  import scala.language.experimental.macros

  def keepAnd[E, EE >: E, A, B](a: Validated[E, A], o: Validated[E, B]): Validated[EE, B] = (a, o) match {
    case (Success(_), Success(v))   => Success(v)
    case (Success(_), Failure(e))   => Failure(e)
    case (Failure(e), Success(_))   => Failure(e)
    case (Failure(e1), Failure(e2)) => Failure(e1 ++ e2)
  }

//  def viaEither[E, A, EE, AA](v: Validated[E, A], f: Either[Seq[E], A] => Either[Seq[EE], AA]): Validated[EE, AA] =
//    f(v.toEither).fold(Failure.apply, Success.apply)

//  def viaEither[EE, AA](f: Either[Seq[E], A] => Either[Seq[EE], AA]): Validation[EE, AA] =
//    f(asEither).fold(Failure.apply, Success.apply)

  /** Turn a `A => Rule[B, C]` into a `Rule[(A, B), C]`
    * {{{
    *   val passRule = From[JsValue] { __ =>
    *       ((__ \ "password").read(notEmpty) ~ (__ \ "verify").read(notEmpty))
    *         .tupled.compose(Rule.uncurry(Rules.equalTo[String]).repath(_ => (Path \ "verify")))
    *     }
    * }}}
    */
//  def uncurry[A, B, C](f: A => Rule[B, C]): Rule[(A, B), C] =
//    Rule { case (a, b) => f(a).validate(b) }

//  import play.api.libs.functional._

//  implicit def zero[O] = toRule(RuleLike.zero[O])

  def apply[I, O](m: Mapping[(JsPath, Seq[JsonValidationError]), I, O]) = new Rule[I, O] {
    def validate(data: I): VA[O] = m(data)
  }

//  def toRule[I, O](r: RuleLike[I, O]) = new Rule[I, O] {
//    def validate(data: I): VA[O] = r.validate(data)
//  }

  def fromMapping[I, O](f: Mapping[JsonValidationError, I, O]): Rule[I, O] =
    Rule[I, O](f(_: I).leftMap(errs => Seq(JsPath -> errs)))

//  implicit def applicativeRule[I] = new Applicative[({ type λ[O] = Rule[I, O] })#λ] {
//    override def pure[A](a: A): Rule[I, A] =
//      Rule(_ => Success(a))
//
//    override def map[A, B](m: Rule[I, A], f: A => B): Rule[I, B] =
//      Rule(d => m.validate(d).map(f))

//    override def apply[A, B](mf: Rule[I, A => B], ma: Rule[I, A]): Rule[I, B] =
//      Rule { d =>
//        val a: VA[A] = ma.validate(d)
//        val f: VA[A => B] = mf.validate(d)
//        val keep: Validated[(Path, Seq[ValidationError]), A] = keepAnd(f, a)
//        viaEither[(Path, Seq[ValidationError]), A, (Path, Seq[ValidationError]), B](keep, e => e.right.flatMap(x => {
//          val ei: Either[Seq[(Path, Seq[ValidationError])], (A) => B] = f.toEither
//          ei.right.map(y => y(x))
//        }))
//      }
//  }

//  implicit def functorRule[I] = new Functor[({ type λ[O] = Rule[I, O] })#λ] {
//    def fmap[A, B](m: Rule[I, A], f: A => B): Rule[I, B] = applicativeRule[I].map(m, f)
//  }

//  implicit def functorExtractorRule[I, O]: VariantExtractor[({ type λ[O] = Rule[I, O] })#λ] =
//    VariantExtractor.functor[({ type λ[O] = Rule[I, O] })#λ](functorRule)

  // XXX: Helps the compiler a bit
//  import play.api.libs.functional.syntax._
//  implicit def cba[I] = functionalCanBuildApplicative[({ type λ[O] = Rule[I, O] })#λ]
//  implicit def fbo[I, O] = toFunctionalBuilderOps[({ type λ[O] = Rule[I, O] })#λ, O] _
//  implicit def ao[I, O] = toApplicativeOps[({ type λ[O] = Rule[I, O] })#λ, O] _
//  implicit def f[I, O] = toFunctorOps[({ type λ[O] = Rule[I, O] })#λ, O] _
}
