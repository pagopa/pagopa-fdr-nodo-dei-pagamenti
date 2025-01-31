package eu.sia.pagopa

import eu.sia.pagopa.common.json.model.{FlussiRendicontazioneEvent, IUVRendicontatiEvent}
import eu.sia.pagopa.common.message.{BlobBodyRef, ReEventHub}
import eu.sia.pagopa.common.util.{AppObjectMapper, RandomStringUtils, Util}
import eu.sia.pagopa.testutil.TestItems
import net.openhft.hashing.LongHashFunction
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

class StaticUnitTests() extends AnyFlatSpec with should.Matchers {

  "fdr re eventhub json" should "ok" in {
    val now = Util.now().truncatedTo(ChronoUnit.SECONDS)

    val testDTF: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
    val created = LocalDateTime.parse(testDTF.format(now))
    val sessionIdOriginal = UUID.randomUUID().toString
    val sessionId = UUID.randomUUID().toString
    val dataOraEvento = now.toString
    val status = "OK"
    val sottoTipoEvento = "REQ"
    val erogatore = "NDP_FDR"
    val fruitore = "nodo-doc-dev"
    val stazione = "nodo-doc-dev"
    val noticeNumber = s"${TestItems.prefixNew}${RandomStringUtils.randomNumeric(15)}"
    val paymentToken = UUID.randomUUID().toString
    val idDominio = "00000000099"
    val iuv = RandomStringUtils.randomNumeric(15)
    val ccp = "n/a"
    val fileName = s"${sessionId}_nodoInviaFlussoRendicontazione_RES"
    val psp = "nodo-doc-dev"
    val flowId = RandomStringUtils.randomNumeric(11)
    val flowName = s"$dataOraEvento$psp-$flowId"
    val flowAction = "nodoInviaFlussoRendicontazione"
    val payload: Option[String] = None
    val uniqueId: String = s"${dataOraEvento.substring(0, 10)}_${
      LongHashFunction
        .xx()
        .hashChars(s"$dataOraEvento$sessionId$sessionIdOriginal$status$sottoTipoEvento$erogatore$fruitore$stazione$noticeNumber$paymentToken$idDominio$iuv$ccp$info")
    }"

    val reEventHub = ReEventHub(
      "FDR001",
      uniqueId,
      created,
      Some(sessionId),
      "INTERFACE",
      None,
      Some(flowName),
      Some(psp),
      Some(idDominio),
      Some(flowAction),
      sottoTipoEvento,
      Some("POST"),
      None,
      Some(BlobBodyRef(Some("pagopadweufdrresa"), Some("payload"), Some(fileName), (payload.map(_.length).getOrElse(0)))),
      Map()
    )

    val x = AppObjectMapper.objectMapper.writeValueAsString(reEventHub)
    val y =
      s"""
         |{
         |  "serviceIdentifier": "FDR001",
         |  "uniqueId": "$uniqueId",
         |  "created": "${created.toString}",
         |  "sessionId": "$sessionId",
         |  "eventType": "INTERFACE",
         |  "fdrStatus": null,
         |  "fdr": "$flowName",
         |  "pspId": "nodo-doc-dev",
         |  "organizationId": "00000000099",
         |  "fdrAction": "nodoInviaFlussoRendicontazione",
         |  "httpType": "REQ",
         |  "httpMethod": "POST",
         |  "httpUrl": null,
         |  "blobBodyRef": {
         |    "storageAccount": "pagopadweufdrresa",
         |    "containerName": "payload",
         |    "fileName": "$fileName",
         |    "fileLength": ${payload.map(_.length).getOrElse(0)}
         |  },
         |  "header": {}
         |}""".stripMargin.replace(" ", "").replace("\n", "").replace("\r", "")
    assert(y == x)
  }

  "iuv rendicontati eventhub json" should "ok" in {
    val now = Util.now().truncatedTo(ChronoUnit.SECONDS)

    val date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(now)
    val random = RandomStringUtils.randomNumeric(9)
    val random15 = s"${RandomStringUtils.randomNumeric(15)}"
    val idFlusso = s"${date}${TestItems.PSP}-$random"
    val uniqueId = UUID.randomUUID().toString
    val iuv = s"${TestItems.prefixstazionePV2}${random15}"

    val iuvRendicontatiEvent = IUVRendicontatiEvent(
      iuv,
      random15,
      10.00,
      0,
      now,
      "2",
      idFlusso,
      now,
      TestItems.PA,
      TestItems.PSP,
      TestItems.intPSP,
      uniqueId,
      now
    )

    val x = AppObjectMapper.objectMapper.writeValueAsString(iuvRendicontatiEvent)
    val y =
      s"""
         |{
         |  "IUV": "$iuv",
         |  "IUR": "$random15",
         |  "IMPORTO": 10.0,
         |  "COD_ESITO": 0,
         |  "DATA_ESITO_SINGOLO_PAGAMENTO": "$now",
         |  "IDSP": "2",
         |  "ID_FLUSSO": "$idFlusso",
         |  "DATA_ORA_FLUSSO": "$now",
         |  "ID_DOMINIO": "${TestItems.PA}",
         |  "PSP": "${TestItems.PSP}",
         |  "INT_PSP": "${TestItems.intPSP}",
         |  "UNIQUE_ID": "$uniqueId",
         |  "INSERTED_TIMESTAMP": "$now"
         |}""".stripMargin.replace(" ", "").replace("\n", "").replace("\r", "")
    assert(y == x)
  }

  "flussi rendicontazione eventhub json" should "ok" in {
    val now = Util.now().truncatedTo(ChronoUnit.SECONDS);

    val date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(Util.now())
    val random = RandomStringUtils.randomNumeric(9)
    val idFlusso = s"${date}${TestItems.PSP}-$random"
    val uniqueId = UUID.randomUUID().toString
    val causale = "causale"

    val seqDates = Seq(s"$now", s"$now")
    val jsonSeqDates = AppObjectMapper.objectMapper.writeValueAsString(seqDates)

    val flussiRendicontazioneEvent = FlussiRendicontazioneEvent(
      idFlusso,
      now,
      now,
      now,
      causale,
      2,
      20.0,
      TestItems.PA,
      TestItems.PSP,
      TestItems.intPSP,
      uniqueId,
      seqDates
    )

    val x = AppObjectMapper.objectMapper.writeValueAsString(flussiRendicontazioneEvent)

    val y =
      s"""
         |{
         |  "ID_FLUSSO": "$idFlusso",
         |  "DATA_ORA_FLUSSO": "$now",
         |  "INSERTED_TIMESTAMP": "$now",
         |  "DATA_REGOLAMENTO": "$now",
         |  "CAUSALE": "$causale",
         |  "NUM_PAGAMENTI": 2,
         |  "SOMMA_VERSATA": 20.0,
         |  "ID_DOMINIO": "${TestItems.PA}",
         |  "PSP": "${TestItems.PSP}",
         |  "INT_PSP": "${TestItems.intPSP}",
         |  "UNIQUE_ID": "$uniqueId",
         |  "ALL_DATES": $jsonSeqDates
         |}""".stripMargin.replace(" ", "").replace("\n", "").replace("\r", "")
    assert(y == x)
  }

}
