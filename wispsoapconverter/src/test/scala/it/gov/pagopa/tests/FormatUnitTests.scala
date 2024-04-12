package it.gov.pagopa.tests

import it.gov.pagopa.common.message.{Re, ReExtra}
import it.gov.pagopa.common.util.AppLogger
import it.gov.pagopa.common.util.azure.Appfunction
import it.gov.pagopa.common.util.azure.cosmos.{CategoriaEvento, Componente, Esito, SottoTipoEvento}
import it.gov.pagopa.tests.testutil.TestItems
import org.mockito.MockitoSugar.mock
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import java.time.Instant

class FormatUnitTests  extends AnyFlatSpec with should.Matchers {

  val log = mock[AppLogger]

  "formatHeaders" should "ok" in {
    val x = Appfunction.formatHeaders(
      Some(Seq(
        "h1"->"h_1"
      ))
    )
    assert(x == "h1=h_1")
  }

  "fmtMessage" should "ok" in {
    assert(Appfunction.fmtMessage(
      Re(
        Instant.now(),Componente.WISP_SOAP_CONVERTER,CategoriaEvento.INTERFACCIA,
        SottoTipoEvento.REQ,
        esito = Esito.RICEVUTA,
        payload = Some(
          <xml><fault><faultCode>fCode</faultCode></fault><faultString>fString</faultString><description>fDescr</description></xml>.toString().getBytes
        )
      ),
      Some(ReExtra())
    ).get == """Re Request => TIPO_EVENTO[REQ/n.a] FRUITORE[n.a] EROGATORE[n.a] ESITO[RICEVUTA] DETTAGLIO[Il nodo ha ricevuto un messaggio]
                          |httpUri: [UNKNOWN]
                          |httpHeaders: [UNKNOWN]
                          |httpStatusCode: [UNKNOWN]
                          |elapsed: [UNKNOWN]
                          |payload: [<xml><fault><faultCode>fCode</faultCode></fault><faultString>fString</faultString><description>fDescr</description></xml>]""".stripMargin)

    assert(Appfunction.fmtMessage(
      Re(
        Instant.now(),Componente.WISP_SOAP_CONVERTER,CategoriaEvento.INTERFACCIA,
        SottoTipoEvento.RESP,
        esito = Esito.INVIATA,
        payload = Some(
          <xml><fault><faultCode>fCode</faultCode></fault><faultString>fString</faultString><description>fDescr</description></xml>.toString().getBytes
        )
      ),
      Some(ReExtra())
    ).get == """Re Request => TIPO_EVENTO[RESP/n.a] FRUITORE[n.a] EROGATORE[n.a] ESITO[INVIATA] DETTAGLIO[Il nodo ha risposto al messaggio ricevuto]
                          |httpUri: [UNKNOWN]
                          |httpHeaders: [UNKNOWN]
                          |httpStatusCode: [UNKNOWN]
                          |elapsed: [UNKNOWN]
                          |payload: [<xml><fault><faultCode>fCode</faultCode></fault><faultString>fString</faultString><description>fDescr</description></xml>]""".stripMargin)

    assert(Appfunction.fmtMessage(
      Re(
        Instant.now(),Componente.WISP_SOAP_CONVERTER,CategoriaEvento.INTERFACCIA,
        SottoTipoEvento.RESP,
        esito = Esito.INVIATA,
        payload = Some(
          <xml>test</xml>.toString().getBytes
        )
      ),
      Some(ReExtra())
    ).get == """Re Request => TIPO_EVENTO[RESP/n.a] FRUITORE[n.a] EROGATORE[n.a] ESITO[INVIATA] DETTAGLIO[Il nodo ha risposto al messaggio ricevuto]
                          |httpUri: [UNKNOWN]
                          |httpHeaders: [UNKNOWN]
                          |httpStatusCode: [UNKNOWN]
                          |elapsed: [UNKNOWN]
                          |payload: [<xml>test</xml>]""".stripMargin)

    assert(Appfunction.fmtMessage(
      Re(
        Instant.now(),Componente.WISP_SOAP_CONVERTER,CategoriaEvento.INTERFACCIA,
        SottoTipoEvento.REQ,
        esito = Esito.RICEVUTA,
        payload = Some(
          <xml>test</xml>.toString().getBytes
        )
      ),
      Some(ReExtra())
    ).get == """Re Request => TIPO_EVENTO[REQ/n.a] FRUITORE[n.a] EROGATORE[n.a] ESITO[RICEVUTA] DETTAGLIO[Il nodo ha ricevuto un messaggio]
                          |httpUri: [UNKNOWN]
                          |httpHeaders: [UNKNOWN]
                          |httpStatusCode: [UNKNOWN]
                          |elapsed: [UNKNOWN]
                          |payload: [<xml>test</xml>]""".stripMargin)

    assert(Appfunction.fmtMessage(
      Re(
        Instant.now(),Componente.WISP_SOAP_CONVERTER,CategoriaEvento.INTERNO,
        SottoTipoEvento.INTERN,
        esito = Esito.CAMBIO_STATO,
        payload = Some(
          <xml>test</xml>.toString().getBytes
        )
      ),
      Some(ReExtra())
    ).get == """Re Request => TIPO_EVENTO[INTERN/n.a] ESITO[CAMBIO_STATO] STATO[STATO non presente]""")

  }

  "fmtMessageJson" should "ok" in {
    assert(Appfunction.fmtMessageJson(
      Re(
        Instant.now(),Componente.WISP_SOAP_CONVERTER,CategoriaEvento.INTERFACCIA,
        SottoTipoEvento.REQ,
        esito = Esito.RICEVUTA,
        payload = Some(
          <xml>
            <fault><faultCode>fCode</faultCode></fault>
            <faultString>fString</faultString>
            <description>fDescr</description>
          </xml>.toString().getBytes
        )
      ),
      Some(ReExtra()),TestItems.ddataMap
    ).get == """{"internalMessage":"SERVER --> REQUEST: messaggio da [subject:nd]","categoriaEvento":"INTERFACCIA","caller":"SERVER","httpType":"REQUEST","esito":"KO","faultCode":"fCode","subject":"nd","subjectDescr":"nd"}""")

    assert(Appfunction.fmtMessageJson(
      Re(
        Instant.now(),Componente.WISP_SOAP_CONVERTER,CategoriaEvento.INTERFACCIA,
        SottoTipoEvento.RESP,
        esito = Esito.INVIATA,
        payload = Some(
          <xml>
            <fault><faultCode>fCode</faultCode></fault>
            <faultString>fString</faultString>
            <description>fDescr</description>
          </xml>.toString().getBytes
        )
      ),
      Some(ReExtra()),TestItems.ddataMap
    ).get == """{"internalMessage":"SERVER --> RESPONSE: risposta a [subject:nd] [esito:KO] [faultCode:fCode]","categoriaEvento":"INTERFACCIA","caller":"SERVER","httpType":"RESPONSE","esito":"KO","faultCode":"fCode","subject":"nd","subjectDescr":"nd"}""")

    assert(Appfunction.fmtMessageJson(
      Re(
        Instant.now(),Componente.WISP_SOAP_CONVERTER,CategoriaEvento.INTERFACCIA,
        SottoTipoEvento.RESP,
        esito = Esito.INVIATA,
        payload = Some(
          <xml>test
          </xml>.toString().getBytes
        )
      ),
      Some(ReExtra()),TestItems.ddataMap
    ).get == """{"internalMessage":"SERVER --> RESPONSE: risposta a [subject:nd] [esito:OK]","categoriaEvento":"INTERFACCIA","caller":"SERVER","httpType":"RESPONSE","esito":"OK","subject":"nd","subjectDescr":"nd"}""")

    assert(Appfunction.fmtMessageJson(
      Re(
        Instant.now(),Componente.WISP_SOAP_CONVERTER,CategoriaEvento.INTERFACCIA,
        SottoTipoEvento.REQ,
        esito = Esito.RICEVUTA,
        payload = Some(
          <xml>test
          </xml>.toString().getBytes
        )
      ),
      Some(ReExtra()),TestItems.ddataMap
    ).get == """{"internalMessage":"SERVER --> REQUEST: messaggio da [subject:nd]","categoriaEvento":"INTERFACCIA","caller":"SERVER","httpType":"REQUEST","esito":"OK","subject":"nd","subjectDescr":"nd"}""")



    assert(Appfunction.fmtMessageJson(
      Re(
        Instant.now(),Componente.WISP_SOAP_CONVERTER,CategoriaEvento.INTERNO,
        SottoTipoEvento.INTERN,
        esito = Esito.CAMBIO_STATO,
        payload = Some(
          <xml>test
          </xml>.toString().getBytes
        )
      ),
      Some(ReExtra()),TestItems.ddataMap
    ).get == """{"internalMessage":"Cambio stato in [nd]","categoriaEvento":"INTERNO","esito":"OK"}""")

  }


}
