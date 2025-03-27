package eu.sia.pagopa

import akka.http.javadsl.model.StatusCodes
import eu.sia.pagopa.common.util.Util.gzipContent
import eu.sia.pagopa.common.util.{RandomStringUtils, Util}
import eu.sia.pagopa.testutil.TestItems

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Base64

//@org.scalatest.Ignore
class RestRendicontazioniTests() extends BaseUnitTest {

  "convertFlussoRendicontazione" must {
    "ok" in {
      val date = Instant.now()
      val random = RandomStringUtils.randomNumeric(9)
      val idFlusso = s"${date}${TestItems.PSP}-$random"
      val regulation = "1234567890"

      val jsonContent = convertFlussoRendicontazionePayload(idFlusso, String.valueOf(date.getEpochSecond), date.toString, regulation)
      val encodedCompressedFlow = new String(Base64.getEncoder.encode(gzipContent(jsonContent.getBytes)))
      val payload = s"""{
         |  "payload": "$encodedCompressedFlow",
         |  "encoding": "base64"
      }""".stripMargin

      await(
        convertFlussoRendicontazioneActorPerRequest(
          Some(payload),
          testCase = Some("OK"),
          responseAssert = (resp, status) => {
            assert(status == StatusCodes.OK.intValue)
            assert(resp.contains("{\"message\":\"OK\"}"))
          }
        )
      )
    }
    "ko fdr fase3 error in date format" in {
      val date = Instant.now()
      val random = RandomStringUtils.randomNumeric(9)
      val idFlusso = s"${date}${TestItems.PSP}-$random"
      val regulation = "1234567890"

      val jsonContent = convertFlussoRendicontazionePayload(idFlusso, String.valueOf(date.toEpochMilli), date.toString, regulation)
      val encodedCompressedFlow = new String(Base64.getEncoder.encode(gzipContent(jsonContent.getBytes)))
      val payload = s"""{
                       |  "payload": "$encodedCompressedFlow",
                       |  "encoding": "base64"
      }""".stripMargin

      await(
        convertFlussoRendicontazioneActorPerRequest(
          Some(payload),
          testCase = Some("KO"),
          responseAssert = (resp, status) => {
            assert(status == StatusCodes.INTERNAL_SERVER_ERROR.intValue)
            assert(resp.contains("{\"error\":\"Errore generico.\"}"))
          }
        )
      )
    }
    "ko fdr fase3 no encoding" in {
      val date = Instant.now()
      val random = RandomStringUtils.randomNumeric(9)
      val idFlusso = s"${date}${TestItems.PSP}-$random"
      val regulation = "1234567890"

      val jsonContent = convertFlussoRendicontazionePayload(idFlusso, String.valueOf(date.getEpochSecond), date.toString, regulation)
      val encodedCompressedFlow = new String(Base64.getEncoder.encode(gzipContent(jsonContent.getBytes)))
      val payload = s"""{
                       |  "payload": "$encodedCompressedFlow"
      }""".stripMargin

      await(
        convertFlussoRendicontazioneActorPerRequest(
          Some(payload),
          testCase = Some("OK"),
          responseAssert = (resp, status) => {
            assert(status == StatusCodes.OK.intValue)
            assert(resp.contains("{\"message\":\"OK\"}"))
          }
        )
      )
    }
  }

  "ko fdr fase3 bad request" in {
    val date = Instant.now()
    val random = RandomStringUtils.randomNumeric(9)
    val idFlusso = s"${date}${TestItems.PSP}-$random"
    val jsonContent =
      s"""{
         |  "fdr": "$idFlusso",
      }""".stripMargin
    val encodedCompressedFlow = new String(Base64.getEncoder.encode(gzipContent(jsonContent.getBytes)))
    val payload = s"""{
                     |  "payload": "$encodedCompressedFlow"
      }""".stripMargin

    await(
      convertFlussoRendicontazioneActorPerRequest(
        Some(payload),
        testCase = Some("KO"),
        responseAssert = (resp, status) => {
          assert(status == StatusCodes.BAD_REQUEST.intValue())
          assert(resp.contains("{\"error\":\"The provided FdR 3 flow JSON is invalid:"))
        }
      )
    )
  }

  "registerFdrForValidation" must {
    "ok" in {
      val date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(Util.now())
      val dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(Util.now())
      val random = RandomStringUtils.randomNumeric(9)
      val idFlusso = s"${date}${TestItems.PSP}-$random"

      val payload =
        s"""{
           |  "flowId": "$idFlusso",
           |  "pspId": "${TestItems.PSP}",
           |  "organizationId": "${TestItems.PA}",
           |  "flowTimestamp": "$dateTime"
      }""".stripMargin

      await(
        registerFdrForValidation(
          Some(payload),
          testCase = Some("OK"),
          responseAssert = (resp, status) => {
            assert(status == StatusCodes.OK.intValue)
            assert(resp.contains("{\"message\":\"OK\"}"))
          }
        )
      )
    }
    "ko missing field" in {
      val date = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(Util.now())
      val dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(Util.now())
      val random = RandomStringUtils.randomNumeric(9)
      val idFlusso = s"${date}${TestItems.PSP}-$random"

      val payload =
        s"""{
           |  "flowId": "$idFlusso",
           |  "pspId": "${TestItems.PSP}",
           |  "flowTimestamp": "$dateTime"
      }""".stripMargin

      await(
        registerFdrForValidation(
          Some(payload),
          testCase = Some("KO"),
          responseAssert = (resp, status) => {
            assert(status == StatusCodes.BAD_REQUEST.intValue)
            assert(resp.contains("{\"message\":\"Invalid organizationId\"}"))
          }
        )
      )
    }
    "ko invalid psp" in {
      val date = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(Util.now())
      val dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(Util.now())
      val random = RandomStringUtils.randomNumeric(9)
      val idFlusso = s"${date}fakepsp-$random"

      val payload =
        s"""{
           |  "flowId": "$idFlusso",
           |  "pspId": "fakepsp",
           |  "organizationId": "${TestItems.PA}",
           |  "flowTimestamp": "$dateTime"
      }""".stripMargin

      await(
        registerFdrForValidation(
          Some(payload),
          testCase = Some("KO"),
          responseAssert = (resp, status) => {
            assert(status == StatusCodes.BAD_REQUEST.intValue)
            assert(resp.contains("{\"message\":\"PSP sconosciuto.\"}"))
          }
        )
      )
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
        flussoNonValido = false,
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
              assert(status == StatusCodes.BAD_REQUEST.intValue)
              assert(resp.contains("{\"error\":\"flusso di rendicontazione gia' presente"))
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
            assert(resp.contains("{\"error\":\"Invalid request\"}"))
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
        flussoNonValido = false,
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
            assert(resp.contains("{\"error\":\"Invalid content\"}"))
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
        flussoNonValido = false,
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
            assert(resp.contains("{\"error\":\"Invalid content\"}"))
          }
        )
      )
    }
  }

}
