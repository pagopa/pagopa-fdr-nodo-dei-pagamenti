package eu.sia.pagopa.common.util

import java.nio.charset.{Charset, StandardCharsets}
import java.time.format.DateTimeFormatter

object Constant {

  final val OK = "OK"
  final val KO = "KO"

  val HTTP_RESP_SESSION_ID_HEADER = "sessionId"

  val UTF_8: Charset = StandardCharsets.UTF_8
  val DTF_DATETIME: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  val SUSPEND_JOBS_KEY = "scheduler.suspendAllJobs"

  val INSTANCE_KEY = "INSTANCE"
  val APP_NAME_KEY = "APP_NAME"
  val APP_VERSION_KEY = "APP_VERSION"
  val INSTANCE = sys.env.get(INSTANCE_KEY).getOrElse("")
  val APP_NAME = sys.env.get(APP_NAME_KEY).getOrElse("")
  val APP_VERSION = sys.env.get(APP_VERSION_KEY).getOrElse("")

  object KeyName {
    val EMPTY_KEY = "NO_KEY"
    val SOAP_INPUT = "soap-input"
    val REST_INPUT = "rest-input"
    val RE_FEEDER = "re-feeder"
    val FTP_RETRY = "ftp-retry"
    val FTP_SENDER = "ftp-sender"
    val RENDICONTAZIONI = "rendicontazioni"
    val DEAD_LETTER_MONITOR = "dead-letter-monitor"
  }

  object MDCKey {
    val SESSION_ID = "sessionId"
    val ACTOR_CLASS_ID = "actorClassId"
  }

  object ContentType extends Enumeration {
    val XML, JSON, TEXT, MULTIPART_FORM_DATA = Value
  }

  object Sftp {
    val RENDICONTAZIONI = "pushFileRendicontazioni"
  }

}
