package eu.sia.pagopa

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.typesafe.config.Config
import eu.sia.pagopa.common.exception.{DigitPaErrorCodes, DigitPaException}
import eu.sia.pagopa.common.repo.fdr.FdrRepository
import eu.sia.pagopa.common.util.azurehubevent.Appfunction.ReEventFunc
import eu.sia.pagopa.common.util.web.NodoRoute
import eu.sia.pagopa.common.util.xml.XmlUtil.StringBase64Binary
import eu.sia.pagopa.common.util.xml.XsdValid
import eu.sia.pagopa.common.util.{DDataChecks, NodoLogger, StringUtils}
import eu.sia.pagopa.commonxml.XmlEnum
import eu.sia.pagopa.testutil.SpecsUtils
import it.pagopa.config.PaymentType
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import scala.concurrent.ExecutionContext
import scala.util.Try

class ChecksUnitTests  extends AnyFlatSpec with should.Matchers {

  val log = mock[NodoLogger]

  "route errors" should "ok" in {
    val settings = mock[ActorSystem.Settings]
    val config = mock[Config]
    val as = mock[ActorSystem]
    val fdrRepository = mock[FdrRepository]
    when(as.settings).thenReturn(settings)
    when(settings.config).thenReturn(config)
    when(config.getInt("config.http.server-request-timeout")).thenReturn(10)
    val ec = mock[ExecutionContext]
    val log = mock[NodoLogger]
    val nr = new NodoRoute(as,fdrRepository,Map(),"",200,mock[ReEventFunc],mock[ActorProps])(ec,log,mock[Materializer])
    val timeout = nr.akkaHttpTimeout("sid")
    val enc = nr.akkaErrorEncoding("sid","UTF-8")
    assert(timeout.status.isFailure())
    assert(enc.status.isFailure())
  }

  "getConfigurationKeys" should "ok" in {
    val t = Try(DDataChecks.getConfigurationKeys(TestDData.ddataMap,"aaaa","bbb"))
    assert(t.isFailure)
  }

  "checkPspCanaleTipoVersamento" should "ok" in {
    val t = DDataChecks.checkPspCanaleTipoVersamento(log,TestDData.ddataMap,TestDData.ddataMap.psps.head._2,TestDData.ddataMap.channels.head._2,
      None)
    assert(t.isFailure)
    assert(t.failed.get.asInstanceOf[DigitPaException].code == DigitPaErrorCodes.PPT_AUTORIZZAZIONE)
    assert(t.failed.get.asInstanceOf[DigitPaException].message == "Configurazione psp-canale non corretta")
  }
  "checkPspCanaleTipoVersamento 2" should "ok" in {
    val t = DDataChecks.checkPspCanaleTipoVersamento(log,TestDData.ddataMap,TestDData.ddataMap.psps(TestItems.PSP),TestDData.ddataMap.channels(TestItems.canale),
      Some(new PaymentType("DDD",None)))
    assert(t.isFailure)
    assert(t.failed.get.asInstanceOf[DigitPaException].code == DigitPaErrorCodes.PPT_AUTORIZZAZIONE)
    assert(t.failed.get.asInstanceOf[DigitPaException].message == "Configurazione psp-canale-tipoVersamento non corretta")
  }

  "StringUtils" should "getStringDecodedByString" in {
    assert("test" == StringUtils.getStringDecodedByString(StringBase64Binary.encodeBase64ToString("test".getBytes),true).get)
  }

  "XSD" should "success" in {
    val p = SpecsUtils.loadTestXML(s"/requests/nodoInviaFlussoRendicontazione.xml")
    assert(XsdValid.checkOnly(p,XmlEnum.NODO_INVIA_FLUSSO_RENDICONTAZIONE_NODOPERPSP, true).isFailure)
  }

  "PA" should "success" in {
    val t1 = DDataChecks.checkPA(log,TestDData.ddataMap,TestItems.PA)
    assert(t1.isSuccess)
    val t2 = DDataChecks.checkPA(log,TestDData.ddataMap,TestItems.PA_DISABLED)
    assert(t2.isFailure)
    assert(t2.failed.get.asInstanceOf[DigitPaException].code == DigitPaErrorCodes.PPT_DOMINIO_DISABILITATO)
    val t3 = DDataChecks.checkPA(log,TestDData.ddataMap,"#")
    assert(t3.isFailure)
    assert(t3.failed.get.asInstanceOf[DigitPaException].code == DigitPaErrorCodes.PPT_DOMINIO_SCONOSCIUTO)
  }
  "PSP" should "success" in {
    val t1 = DDataChecks.checkPsp(log,TestDData.ddataMap,TestItems.PSP)
    assert(t1.isSuccess)
    val t2 = DDataChecks.checkPsp(log,TestDData.ddataMap,TestItems.PSP_DISABLED)
    assert(t2.isFailure)
    assert(t2.failed.get.asInstanceOf[DigitPaException].code == DigitPaErrorCodes.PPT_PSP_DISABILITATO)
    val t3 = DDataChecks.checkPsp(log,TestDData.ddataMap,"#")
    assert(t3.isFailure)
    assert(t3.failed.get.asInstanceOf[DigitPaException].code == DigitPaErrorCodes.PPT_PSP_SCONOSCIUTO)
  }
  "INT_PSP" should "success" in {
    val t1 = DDataChecks.checkIntermediarioPSP(log,TestDData.ddataMap,TestItems.intPSP)
    assert(t1.isSuccess)
    val t2 = DDataChecks.checkIntermediarioPSP(log,TestDData.ddataMap,TestItems.intPSP_DISABLED)
    assert(t2.isFailure)
    assert(t2.failed.get.asInstanceOf[DigitPaException].code == DigitPaErrorCodes.PPT_INTERMEDIARIO_PSP_DISABILITATO)
    val t3 = DDataChecks.checkIntermediarioPSP(log,TestDData.ddataMap,"#")
    assert(t3.isFailure)
    assert(t3.failed.get.asInstanceOf[DigitPaException].code == DigitPaErrorCodes.PPT_INTERMEDIARIO_PSP_SCONOSCIUTO)
  }
  "INT_PA" should "success" in {
    val t1 = DDataChecks.checkIntermediarioPA(log,TestDData.ddataMap,TestItems.testIntPA)
    assert(t1.isSuccess)
    val t2 = DDataChecks.checkIntermediarioPA(log,TestDData.ddataMap,TestItems.testIntPA_DISABLED)
    assert(t2.isFailure)
    assert(t2.failed.get.asInstanceOf[DigitPaException].code == DigitPaErrorCodes.PPT_INTERMEDIARIO_PA_DISABILITATO)
    val t3 = DDataChecks.checkIntermediarioPA(log,TestDData.ddataMap,"#")
    assert(t3.isFailure)
    assert(t3.failed.get.asInstanceOf[DigitPaException].code == DigitPaErrorCodes.PPT_INTERMEDIARIO_PA_SCONOSCIUTO)
  }
  "CANALE" should "success" in {
    val t1 = DDataChecks.checkCanale(log,TestDData.ddataMap,TestItems.canale,Some(TestItems.canalePwd), false)
    assert(t1.isSuccess)
    val t2 = DDataChecks.checkCanale(log,TestDData.ddataMap,TestItems.canale_DISABLED,Some(""), false)
    assert(t2.isFailure)
    assert(t2.failed.get.asInstanceOf[DigitPaException].code == DigitPaErrorCodes.PPT_CANALE_DISABILITATO)
    val t3 = DDataChecks.checkCanale(log,TestDData.ddataMap,"#",None, false)
    assert(t3.isFailure)
    assert(t3.failed.get.asInstanceOf[DigitPaException].code == DigitPaErrorCodes.PPT_CANALE_SCONOSCIUTO)
    val t4 = DDataChecks.checkCanale(log,TestDData.ddataMap,TestItems.canale,Some("wrongpass"), true)
    assert(t4.isFailure)
    assert(t4.failed.get.asInstanceOf[DigitPaException].code == DigitPaErrorCodes.PPT_AUTENTICAZIONE)
  }
  "STAZIONE" should "success" in {
    val t1 = DDataChecks.checkStazione(log,TestDData.ddataMap,TestItems.stazione,Some(TestItems.stazionePwd), true)
    assert(t1.isSuccess)
    val t2 = DDataChecks.checkStazione(log,TestDData.ddataMap,TestItems.stazione_DISABLED,Some(""), false)
    assert(t2.isFailure)
    assert(t2.failed.get.asInstanceOf[DigitPaException].code == DigitPaErrorCodes.PPT_STAZIONE_INT_PA_DISABILITATA)
    val t3 = DDataChecks.checkStazione(log,TestDData.ddataMap,"#",None, false)
    assert(t3.isFailure)
    assert(t3.failed.get.asInstanceOf[DigitPaException].code == DigitPaErrorCodes.PPT_STAZIONE_INT_PA_SCONOSCIUTA)
    val t4 = DDataChecks.checkStazione(log,TestDData.ddataMap,TestItems.stazione,Some("wrongpass"), true)
    assert(t4.isFailure)
    assert(t4.failed.get.asInstanceOf[DigitPaException].code == DigitPaErrorCodes.PPT_AUTENTICAZIONE)
  }
  "ALTRI CHECK" should "success" in {
  }

}
