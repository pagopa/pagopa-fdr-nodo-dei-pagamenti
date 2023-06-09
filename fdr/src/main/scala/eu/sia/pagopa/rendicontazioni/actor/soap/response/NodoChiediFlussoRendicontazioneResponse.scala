package eu.sia.pagopa.rendicontazioni.actor.soap.response

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import eu.sia.pagopa.Main.ConfigData
import eu.sia.pagopa.common.actor.NodoLogging
import eu.sia.pagopa.common.exception
import eu.sia.pagopa.common.exception.{DigitPaErrorCodes, DigitPaException}
import eu.sia.pagopa.common.message.{SoapRequest, SoapResponse}
import eu.sia.pagopa.common.repo.re.model.Re
import eu.sia.pagopa.common.util._
import eu.sia.pagopa.common.util.xml.XsdValid
import eu.sia.pagopa.commonxml.XmlEnum
import org.slf4j.MDC
import scalaxbmodel.nodoperpa.{FaultBean, NodoChiediFlussoRendicontazioneRisposta}

trait NodoChiediFlussoRendicontazioneResponse { this: NodoLogging =>

  def makeResponseWithFault(e: DigitPaException): NodoChiediFlussoRendicontazioneRisposta = {
    NodoChiediFlussoRendicontazioneRisposta(Option(FaultBean(e.faultCode, e.faultString, FaultId.NODO_DEI_PAGAMENTI_SPC, Option(e.message), None)), None)
  }

  def errorHandler(sessionId: String, testCaseId: Option[String], outputXsdValid: Boolean, e: DigitPaException, re: Option[Re]): SoapResponse = {
    val respObj = makeResponseWithFault(e)
    (for {
      respPayload <- XmlEnum.nodoChiediFlussoRendicontazioneRisposta2Str_nodoperpa(respObj)
      _ <- XsdValid.checkOnly(respPayload, XmlEnum.NODO_CHIEDI_FLUSSO_RENDICONTAZIONE_RISPOSTA_NODOPERPA, outputXsdValid)
    } yield SoapResponse(sessionId, Some(respPayload), StatusCodes.OK.intValue, re, testCaseId, None))
      .recover({ case e: Throwable =>
        log.error(e, "Errore creazione risposta negativa")
        val cfb = exception.DigitPaException(DigitPaErrorCodes.PPT_SYSTEM_ERROR, e)
        val failRes = makeResponseWithFault(cfb)
        val payloadInviaRPTRispostaFail = XmlEnum.nodoChiediFlussoRendicontazioneRisposta2Str_nodoperpa(failRes).get
        SoapResponse(sessionId, Some(payloadInviaRPTRispostaFail), StatusCodes.OK.intValue, re, testCaseId, None)
      })
      .get
  }

  def actorError(req: SoapRequest, replyTo: ActorRef, ddataMap: ConfigData, e: DigitPaException, re: Option[Re]): Unit = {
    MDC.put(Constant.MDCKey.SESSION_ID, req.sessionId)
    val outputXsdValid = DDataChecks.getConfigurationKeys(ddataMap, "validate_output").toBoolean
    val res = errorHandler(req.sessionId, req.testCaseId, outputXsdValid, e, re)
    replyTo ! res
  }
}
