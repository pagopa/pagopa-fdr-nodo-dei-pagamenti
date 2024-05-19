package eu.sia.pagopa

import akka.http.javadsl.model.StatusCodes
import eu.sia.pagopa.common.json.model.rendicontazione.GetXmlRendicontazioneResponse
import eu.sia.pagopa.common.util.xml.XmlUtil
import eu.sia.pagopa.common.util.{Constant, RandomStringUtils, Util}
import eu.sia.pagopa.commonxml.XmlEnum
import spray.json._
import eu.sia.pagopa.common.json.model._
import eu.sia.pagopa.common.message.ReExtra
import eu.sia.pagopa.testutil.TestItems

import java.time.format.DateTimeFormatter
import scala.util.parsing.json.JSON
import scala.xml.XML

//@org.scalatest.Ignore
class RestRendicontazioniTests() extends BaseUnitTest {

  "notifyFlussoRendicontazione" must {
    "ok" in {
      val date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(Util.now())
      val random = RandomStringUtils.randomNumeric(9)
      val idFlusso = s"${date}${TestItems.PSP}-$random"

      val payload = s"""{
         |  "fdr": "$idFlusso",
         |  "pspId": "${TestItems.PSP}",
         |  "organizationId": "${TestItems.PA}",
         |  "retry": 1,
         |  "revision": 1
      }""".stripMargin

      await(
        notifyFlussoRendicontazione(
          Some(payload),
          testCase = Some("OK"),
          responseAssert = (resp, status) => {
            assert(status == StatusCodes.OK.intValue)
            assert(resp.contains("{\"message\":\"OK\"}"))
          }
        )
      )
    }
    "ko fdr fase3 error" in {
      val date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(Util.now())
      val random = RandomStringUtils.randomNumeric(9)
      val idFlusso = s"${date}${TestItems.PSP}-$random"

      val payload =
        s"""{
           |  "fdr": "$idFlusso",
           |  "pspId": "${TestItems.PSP}",
           |  "organizationId": "${TestItems.PA}",
           |  "retry": 1,
           |  "revision": 1
      }""".stripMargin

      await(
        notifyFlussoRendicontazione(
          Some(payload),
          testCase = Some("KO"),
          responseAssert = (resp, status) => {
            assert(status == StatusCodes.INTERNAL_SERVER_ERROR.intValue)
            assert(resp.contains("{\"message\":\"KO\"}"))
          }
        )
      )
    }
    "ko fdr fase3 error payments" in {
      val date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(Util.now())
      val random = RandomStringUtils.randomNumeric(9)
      val idFlusso = s"${date}${TestItems.PSP}-$random"

      val payload =
        s"""{
           |  "fdr": "$idFlusso",
           |  "pspId": "${TestItems.PSP}",
           |  "organizationId": "${TestItems.PA}",
           |  "retry": 1,
           |  "revision": 1
      }""".stripMargin

      await({
        actorUtility.configureMocker("OK" -> { (messageType, _) => {
          messageType match {
            case "internalGetFdrPayment" => "KO"
            case _ => "OK"
          }
        }
        })

        notifyFlussoRendicontazione(
          Some(payload),
          testCase = Some("KO"),
          responseAssert = (resp, status) => {
            assert(status == StatusCodes.INTERNAL_SERVER_ERROR.intValue)
            assert(resp.contains("{\"message\":\"KO\"}"))
          }
        )
      })
    }
  }


  "nodoInviaFlussoRendicontazioneFTP" must {
    "ok" in {
      val date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(Util.now())
      val random = RandomStringUtils.randomNumeric(9)
      val idFlusso = s"${date}${TestItems.PSP}-$random"

      val nodoInviaFlussoRendicontazione = inviaFlussoRendicontazionePayload(
        TestItems.PSP,
        TestItems.intPSP,
        TestItems.canale,
        TestItems.canalePwd,
        TestItems.PA,
        idFlussoReq = Some(idFlusso),
        dateReq = Some(date),
        None,
        None,
        None,
        false,
        ""
      )

      val nodoInviaFlussoRendicontazioneReplaced = nodoInviaFlussoRendicontazione
        .replaceAll("\"", "\\\\\"")
        .replaceAll("\n", "")

      val payload =
        s"""
           |{
           | "content": "${nodoInviaFlussoRendicontazioneReplaced}"
           |}
           |""".stripMargin

      await(
        nodoInviaFlussoRendicontazioneFTP(
          Some(payload),
          testCase = Some("OK"),
          responseAssert = (resp, status) => {
            assert(status == StatusCodes.OK.intValue)
            assert(resp.contains("{\"message\":\"OK\"}"))
          }
        )
      )
    }
    "ko same flow sent" in {
      val date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(Util.now())
      val random = RandomStringUtils.randomNumeric(9)
      val idFlusso = s"${date}${TestItems.PSP}-$random"

      val nodoInviaFlussoRendicontazione = inviaFlussoRendicontazionePayload(
        TestItems.PSP,
        TestItems.intPSP,
        TestItems.canale,
        TestItems.canalePwd,
        TestItems.PA,
        idFlussoReq = Some(idFlusso),
        dateReq = Some(date),
        None,
        None,
        None,
        false,
        ""
      )

      val nodoInviaFlussoRendicontazioneReplaced = nodoInviaFlussoRendicontazione
        .replaceAll("\"", "\\\\\"")
        .replaceAll("\n", "")

      val payload =
        s"""
           |{
           | "content": "${nodoInviaFlussoRendicontazioneReplaced}"
           |}
           |""".stripMargin

      await(
        for {
          _ <- {
            nodoInviaFlussoRendicontazioneFTP(
              Some(payload),
              testCase = Some("OK"),
              responseAssert = (resp, status) => {
                assert(status == StatusCodes.OK.intValue)
                assert(resp.contains("{\"message\":\"OK\"}"))
              }
            )}
          _ <- {
            nodoInviaFlussoRendicontazioneFTP(
            Some(payload),
            testCase = Some("KO"),
            responseAssert = (resp, status) => {
              assert(status == StatusCodes.INTERNAL_SERVER_ERROR.intValue)
              assert(resp.contains("{\"message\":\"KO\"}"))
            }
          )}
        } yield ()
      )
    }
    "ko empty payload" in {
      await(
        nodoInviaFlussoRendicontazioneFTP(
          None,
          testCase = Some("OK"),
          responseAssert = (resp, status) => {
            assert(status == StatusCodes.BAD_REQUEST.intValue)
            assert(resp.contains("{\"message\":\"KO\"}"))
          }
        )
      )
    }
    "ko wrong request xml" in {
      val date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(Util.now())
      val random = RandomStringUtils.randomNumeric(9)
      val idFlusso = s"${date}${TestItems.PSP}-$random"

      val nodoInviaFlussoRendicontazione = inviaFlussoRendicontazionePayload(
        TestItems.PSP,
        TestItems.intPSP,
        TestItems.canale,
        TestItems.canalePwd,
        TestItems.PA,
        idFlussoReq = Some(idFlusso),
        dateReq = Some(date),
        None,
        None,
        None,
        false,
        ""
      )

      val nodoInviaFlussoRendicontazioneReplaced = nodoInviaFlussoRendicontazione
        .replaceAll("\"", "\\\\\"")
        .replaceAll("\n", "")
        .replaceAll("nodoInviaFlussoRendicontazione", "nodoInviaRPT")
        .replaceAll("identificativoPSP", "idPSP")

      val payload =
        s"""
           |{
           | "content": "${nodoInviaFlussoRendicontazioneReplaced}"
           |}
           |""".stripMargin

      await(
        nodoInviaFlussoRendicontazioneFTP(
          Some(payload),
          testCase = Some("KO"),
          responseAssert = (resp, status) => {
            assert(status == StatusCodes.BAD_REQUEST.intValue)
            assert(resp.contains("{\"message\":\"KO\"}"))
          }
        )
      )
    }
    "ko invalid request" in {
      val date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(Util.now())
      val random = RandomStringUtils.randomNumeric(9)
      val idFlusso = s"${date}${TestItems.PSP}-$random"

      val nodoInviaFlussoRendicontazione = inviaFlussoRendicontazionePayload(
        TestItems.PSP,
        TestItems.intPSP,
        TestItems.canale,
        TestItems.canalePwd,
        TestItems.PA,
        idFlussoReq = Some(idFlusso),
        dateReq = Some(date),
        None,
        None,
        None,
        false,
        ""
      )

      val payload =
        s"""
           |{
           | "content": ${nodoInviaFlussoRendicontazione}
           |}
           |""".stripMargin

      await(
        nodoInviaFlussoRendicontazioneFTP(
          Some(payload),
          testCase = Some("OK"),
          responseAssert = (resp, status) => {
            assert(status == StatusCodes.BAD_REQUEST.intValue)
            assert(resp.contains("{\"message\":\"KO\"}"))
          }
        )
      )
    }
  }

}
