package eu.sia.pagopa

import eu.sia.pagopa.common.message.ReExtra
import eu.sia.pagopa.common.repo.re.model.Re
import eu.sia.pagopa.common.util.{Appfunction, NodoLogger}
import org.mockito.MockitoSugar.mock
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import java.time.LocalDateTime

class FormatUnitTests  extends AnyFlatSpec with should.Matchers {

  val log = mock[NodoLogger]

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
        LocalDateTime.now(),"","INTERFACCIA",
        "REQ",
        esito = Some("RICEVUTA"),
        payload = Some(
          <xml><fault><faultCode>fCode</faultCode></fault><faultString>fString</faultString><description>fDescr</description></xml>.toString().getBytes
        )
      ),
      Some(ReExtra(
        None,Seq(),None,None,None,None,true
      ))
    ).get.contains("""payload: [<xml><fault><faultCode>fCode</faultCode></fault><faultString>fString</faultString><description>fDescr</description></xml>]""".stripMargin))

    assert(Appfunction.fmtMessage(
      Re(
        LocalDateTime.now(),"","INTERFACCIA",
        "RESP",
        esito = Some("INVIATA"),
        payload = Some(
          <xml><fault><faultCode>fCode</faultCode></fault><faultString>fString</faultString><description>fDescr</description></xml>.toString().getBytes
        )
      ),
      Some(ReExtra(
        None,Seq(),None,None,None,None,true
      ))
    ).get.contains("""payload: [<xml><fault><faultCode>fCode</faultCode></fault><faultString>fString</faultString><description>fDescr</description></xml>]""".stripMargin))

    assert(Appfunction.fmtMessage(
      Re(
        LocalDateTime.now(),"","INTERFACCIA",
        "RESP",
        esito = Some("INVIATA"),
        payload = Some(
          <xml>test</xml>.toString().getBytes
        )
      ),
      Some(ReExtra(
        None,Seq(),None,None,None,None,true
      ))
    ).get.contains("""payload: [<xml>test</xml>]""".stripMargin))

    assert(Appfunction.fmtMessage(
      Re(
        LocalDateTime.now(),"","INTERFACCIA",
        "REQ",
        esito = Some("RICEVUTA"),
        payload = Some(
          <xml>test</xml>.toString().getBytes
        )
      ),
      Some(ReExtra(
        None,Seq(),None,None,None,None,true
      ))
    ).get.contains("""payload: [<xml>test</xml>]""".stripMargin))

    assert(Appfunction.fmtMessage(
      Re(
        LocalDateTime.now(),"","INTERFACCIA",
        "REQ",
        esito = None,
        payload = Some(
          <xml>test</xml>.toString().getBytes
        )
      ),
      Some(ReExtra(
        None,Seq(),None,None,None,None,true
      ))
    ).get.contains("""payload: [<xml>test</xml>]""".stripMargin))

    assert(Appfunction.fmtMessage(
      Re(
        LocalDateTime.now(),"","CAMBIO_STATO",
        "REQ",
        esito = Some("RICEVUTA"),
        payload = Some(
          <xml>test</xml>.toString().getBytes
        )
      ),
      Some(ReExtra(
        None,Seq(),None,None,None,None,true
      ))
    ).get == """Re Request => TIPO_EVENTO[REQ/n.a] ESITO[RICEVUTA] STATO[STATO non presente]""")

    assert(Appfunction.fmtMessage(
      Re(
        LocalDateTime.now(),"","INTERFACCIA",
        "REQ",
        esito = Some("AAAA"),
        payload = Some(
          <xml><fault><faultCode>fCode</faultCode></fault><faultString>fString</faultString><description>fDescr</description></xml>.toString().getBytes
        )
      ),
      Some(ReExtra(
        None,Seq(),None,None,None,None,true
      ))
    ).get.contains("""payload: [<xml><fault><faultCode>fCode</faultCode></fault><faultString>fString</faultString><description>fDescr</description></xml>]""".stripMargin))

  }

  "fmtMessageJson" should "ok" in {
    assert(Appfunction.fmtMessageJson(
      Re(
        LocalDateTime.now(),"","INTERFACCIA",
        "REQ",
        esito = Some("RICEVUTA"),
        payload = Some(
          <xml>
            <fault><faultCode>fCode</faultCode></fault>
            <faultString>fString</faultString>
            <description>fDescr</description>
          </xml>.toString().getBytes
        )
      ),
      Some(ReExtra(
        None,Seq(),None,None,None,None,true
      )),TestItems.ddataMap
    ).get == """{"internalMessage":"SERVER --> REQUEST: messaggio da [subject:nd]","categoriaEvento":"INTERFACCIA","caller":"SERVER","httpType":"REQUEST","isSoapProtocol":true,"esito":"KO","faultCode":"fCode","subject":"nd","subjectDescr":"nd"}""")

    assert(Appfunction.fmtMessageJson(
      Re(
        LocalDateTime.now(),"","INTERFACCIA",
        "RESP",
        esito = Some("INVIATA"),
        payload = Some(
          <xml>
            <fault><faultCode>fCode</faultCode></fault>
            <faultString>fString</faultString>
            <description>fDescr</description>
          </xml>.toString().getBytes
        )
      ),
      Some(ReExtra(
        None,Seq(),None,None,None,None,true
      )),TestItems.ddataMap
    ).get == """{"internalMessage":"SERVER --> RESPONSE: risposta a [subject:nd] [esito:KO] [faultCode:fCode]","categoriaEvento":"INTERFACCIA","caller":"SERVER","httpType":"RESPONSE","isSoapProtocol":true,"esito":"KO","faultCode":"fCode","subject":"nd","subjectDescr":"nd"}""")

    assert(Appfunction.fmtMessageJson(
      Re(
        LocalDateTime.now(),"","INTERFACCIA",
        "RESP",
        esito = Some("INVIATA"),
        payload = Some(
          <xml>test
          </xml>.toString().getBytes
        )
      ),
      Some(ReExtra(
        None,Seq(),None,None,None,None,true
      )),TestItems.ddataMap
    ).get == """{"internalMessage":"SERVER --> RESPONSE: risposta a [subject:nd] [esito:OK]","categoriaEvento":"INTERFACCIA","caller":"SERVER","httpType":"RESPONSE","isSoapProtocol":true,"esito":"OK","subject":"nd","subjectDescr":"nd"}""")

    assert(Appfunction.fmtMessageJson(
      Re(
        LocalDateTime.now(),"","INTERFACCIA",
        "REQ",
        esito = Some("RICEVUTA"),
        payload = Some(
          <xml>test
          </xml>.toString().getBytes
        )
      ),
      Some(ReExtra(
        None,Seq(),None,None,None,None,true
      )),TestItems.ddataMap
    ).get == """{"internalMessage":"SERVER --> REQUEST: messaggio da [subject:nd]","categoriaEvento":"INTERFACCIA","caller":"SERVER","httpType":"REQUEST","isSoapProtocol":true,"esito":"OK","subject":"nd","subjectDescr":"nd"}""")

    assert(Appfunction.fmtMessageJson(
      Re(
        LocalDateTime.now(),"","INTERFACCIA",
        "REQ",
        esito = None,
        payload = Some(
          <xml>test
          </xml>.toString().getBytes
        )
      ),
      Some(ReExtra(
        None,Seq(),None,None,None,None,true
      )),TestItems.ddataMap
    ).get == """{"internalMessage":"Tipo di REQ/RESP non identificata per esito mancante","categoriaEvento":"INTERFACCIA","caller":"nd","httpType":"nd","isSoapProtocol":true,"esito":"OK","subject":"nd","subjectDescr":"nd"}""")

    assert(Appfunction.fmtMessageJson(
      Re(
        LocalDateTime.now(),"","CAMBIO_STATO",
        "REQ",
        esito = Some("RICEVUTA"),
        payload = Some(
          <xml>test
          </xml>.toString().getBytes
        )
      ),
      Some(ReExtra(
        None,Seq(),None,None,None,None,true
      )),TestItems.ddataMap
    ).get == """{"internalMessage":"Cambio stato in [nd]","categoriaEvento":"CAMBIO_STATO","isSoapProtocol":true,"esito":"OK"}""")

    assert(Appfunction.fmtMessageJson(
      Re(
        LocalDateTime.now(),"","INTERFACCIA",
        "REQ",
        esito = Some("AAAA"),
        payload = Some(
          <xml>
            <fault><faultCode>fCode</faultCode></fault>
            <faultString>fString</faultString>
            <description>fDescr</description>
          </xml>.toString().getBytes
        )
      ),
      Some(ReExtra(
        None,Seq(),None,None,None,None,true
      )),TestItems.ddataMap
    ).get == """{"internalMessage":"Tipo di REQ/RESP non identificata per sotto tipo evento non valido","categoriaEvento":"INTERFACCIA","caller":"nd","httpType":"nd","isSoapProtocol":true,"esito":"KO","faultCode":"fCode","subject":"nd","subjectDescr":"nd"}""")

  }


}
