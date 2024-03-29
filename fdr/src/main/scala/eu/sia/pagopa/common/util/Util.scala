package eu.sia.pagopa.common.util

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.{ContentType => _}
import akka.routing.RoundRobinGroup
import com.typesafe.config.Config

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.zip.{GZIPInputStream, GZIPOutputStream}
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
    map
      .map { i =>
        def quote(x: Any): String = "\"" + x + "\""
        val key: String = quote(i._1)
        val value: String = i._2 match {
          case elem: Seq[_] =>
            elem
              .map {
                case ee: Map[_, _] => mapToJson(ee.asInstanceOf[Map[String, Any]])
                case _             => quote(_)
              }
              .mkString("[", ",", "]")
          case elem: Option[_] =>
            elem.map(quote).getOrElse("null")
          case elem: Map[_, _] =>
            mapToJson(elem.asInstanceOf[Map[String, Any]])
          case elem =>
            quote(elem)
        }
        s"$key : $value"
      }
      .mkString("{", ", ", "}")
  }

  def zipContent(bytes: Array[Byte]) = {
    val bais = new ByteArrayOutputStream(bytes.length)
    val gzipOut = new GZIPOutputStream(bais)
    gzipOut.write(bytes)
    gzipOut.close()
    val compressed = bais.toByteArray
    bais.close()
    compressed
  }

  def unzipContent(compressed: Array[Byte]) = {
    Try {
      val bais = new ByteArrayInputStream(compressed)
      new GZIPInputStream(bais).readAllBytes()
    }
  }
}
