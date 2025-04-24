package eu.sia.pagopa.testutil

import com.typesafe.config.{Config, ConfigFactory}

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

object TestDefaults {
  def configWithReportingFtp(enabled: Boolean): Config =
    ConfigFactory.parseString(
      s"""
         | reportingFtpEnabled = $enabled
         |""".stripMargin
    )
}

object GzipUtil {
  def gzip(input: String): Array[Byte] = {
    val byteStream = new ByteArrayOutputStream()
    val gzipStream = new GZIPOutputStream(byteStream)
    gzipStream.write(input.getBytes("UTF-8"))
    gzipStream.close()
    byteStream.toByteArray
  }
}