package eu.sia.pagopa.common.util

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.{ContentType => _}
import akka.routing.RoundRobinGroup
import com.typesafe.config.Config

import scala.reflect.runtime.universe._
import scala.reflect.runtime.{currentMirror => cm}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}
import java.time.temporal.ChronoUnit
import java.util.zip.{GZIPInputStream, GZIPOutputStream}
import javax.xml.datatype.XMLGregorianCalendar
import scala.collection.immutable.HashMap
import scala.util.Try
import scala.util.matching.Regex

object Util {

  def now(): LocalDateTime = {
    LocalDateTime.now().truncatedTo(ChronoUnit.MICROS)
  }

  val OBFUSCATE_REGEX_MAP = Seq(new Regex("(<(?:\\w*?:{0,1})password(?:.*)>)(.*)(<\\/(?:\\w*?:{0,1})password(?:.*)>)") -> "$1XXXXXXXXXX$3")

  def obfuscate(str: String): String = {
    //nel nostro caso che abbiamo xml con un solo tag password va bene se dovessero essercene di più va ripensata
    OBFUSCATE_REGEX_MAP.foldLeft(str)((s, rexT) => {
      rexT._1.replaceFirstIn(s, rexT._2)
    })
  }

  def logPayload(log: NodoLogger, payload: Option[String]): Unit = {
    if (log.isDebugEnabled) {
      log.debug(payload.map(Util.obfuscate).getOrElse("[NO PAYLOAD]"))
    }
  }

  def createLocalRouter(actorName: String, routerName: String, roles: Set[String] = Set())(implicit system: ActorSystem): ActorRef = {
    system.actorOf(RoundRobinGroup(List(s"/user/$actorName")).props(), name = routerName)
  }

  def faultXmlResponse(faultcode: String, faultstring: String, detail: Option[String]) =
    s"""<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><soap:Fault><faultcode>$faultcode</faultcode><faultstring>$faultstring</faultstring>${detail
      .map(x => s"<detail>$x</detail>")
      .getOrElse("")}</soap:Fault></soap:Body></soap:Envelope>"""

  def getActorRouterName(primitiva: String, sender: Option[String]): String = {
    s"$primitiva${sender.map(sen => s"_$sen").getOrElse("")}"
  }

  implicit class configMapperOps(config: Config) {
    import scala.jdk.CollectionConverters._
    def toMap: Map[String, AnyRef] =
      config.entrySet().asScala.map(pair => (pair.getKey, config.getAnyRef(pair.getKey))).toMap

  }

  def mapToJson(map: Map[String, Any]): String = {
    def quote(value: Any): String = value match {
      case str: String =>
        if (str.startsWith("\"") && str.endsWith("\"")) str
        else "\"" + str.replace("\"", "\\\"") + "\""
      case date: LocalDate => "\"" + date.toString + "\""
      case dateTime: LocalDateTime => "\"" + dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\""
      case xmlCal: XMLGregorianCalendar => "\"" + xmlCal.toXMLFormat + "\""
      case other => other.toString
    }

    def toJson(value: Any): String = value match {
      case null => "null"
      case seq: Seq[_] => seq.map(toJson).mkString("[", ",", "]")
      case subMap: Map[_, _] => mapToJson(subMap.asInstanceOf[Map[String, Any]])
      case other => quote(other)
    }

    map.map { case (key, value) =>
      val quotedKey = quote(key)
      val quotedValue = toJson(value)
      s"$quotedKey : $quotedValue"
    }.mkString("{", ", ", "}")
  }

  def gzipContent(bytes: Array[Byte]) = {
    val bais = new ByteArrayOutputStream(bytes.length)
    val gzipOut = new GZIPOutputStream(bais)
    gzipOut.write(bytes)
    gzipOut.close()
    val compressed = bais.toByteArray
    bais.close()
    compressed
  }

  def ungzipContent(compressed: Array[Byte]) = {
    Try {
      val bais = new ByteArrayInputStream(compressed)
      new GZIPInputStream(bais).readAllBytes()
    }
  }

  def toMap(obj: Any): Map[String, Any] = {
    val mirror = cm.reflect(obj)
    val members = mirror.symbol.typeSignature.members.collect {
      case m: MethodSymbol if m.isCaseAccessor => m
    }

    members.map { member =>
      val fieldName = member.name.toString
      val fieldValue = mirror.reflectMethod(member).apply()

      fieldName -> convertValue(fieldValue)
    }.toMap
  }

  private def convertValue(value: Any): Any = value match {
    case Some(v)             => convertValue(v) // Unwrap Option
    case None                => null            // Handle None
    case l: List[_]          => l.map(convertValue) // Handle Lists
    case m: Map[_, _]        => m.map { case (k, v) => k.toString -> convertValue(v) }
    case p if isCaseClass(p) => toMap(p)        // Recursively handle case classes
    case v                   => v              // Base case: return the value as is
  }

  private def isCaseClass(obj: Any): Boolean = {
    val symbol = cm.reflect(obj).symbol
    symbol.isClass && symbol.asClass.isCaseClass
  }
}
