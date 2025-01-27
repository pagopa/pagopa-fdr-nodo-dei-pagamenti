package eu.sia.pagopa.common.util

import eu.sia.pagopa.Main.{ConfigData, repositories}
import eu.sia.pagopa.common.actor.NodoLogging
import eu.sia.pagopa.common.enums.EsitoRE
import eu.sia.pagopa.common.message._
import eu.sia.pagopa.common.repo.re.model.Re
import Appfunction.RePayloadContainerBlobFunc

trait ReUtil { this: NodoLogging =>

  def traceInternalRequest(message: SoapRequest, re: Re, reExtra: ReExtra, rePayloadContainerBlobFunc: RePayloadContainerBlobFunc, ddataMap: ConfigData): Unit = {
    import StringUtils.Utf8String
    Util.logPayload(log, Some(message.payload))
    val reRequestReq = ReRequest(
      sessionId = message.sessionId,
      testCaseId = message.testCaseId,
      re = re.copy(
        insertedTimestamp = message.timestamp,
        payload = Some(message.payload.getUtf8Bytes),
        categoriaEvento = CategoriaEvento.INTERNO.toString,
        sottoTipoEvento = SottoTipoEvento.INTERN.toString,
        esito = Some(EsitoRE.RICEVUTA.toString),
        businessProcess = Some(message.primitive)
      ),
      reExtra = Some(reExtra)
    )
    rePayloadContainerBlobFunc(reRequestReq, repositories, log)
  }

  def traceInternalRequest(message: RestRequest, re: Re, reExtra: ReExtra, rePayloadContainerBlobFunc: RePayloadContainerBlobFunc, ddataMap: ConfigData): Unit = {
    val reRequestReq = ReRequest(
      sessionId = message.sessionId,
      testCaseId = message.testCaseId,
      re = re.copy(
        insertedTimestamp = message.timestamp,
        categoriaEvento = CategoriaEvento.INTERNO.toString,
        sottoTipoEvento = SottoTipoEvento.INTERN.toString,
        esito = Some(EsitoRE.RICEVUTA.toString),
        businessProcess = Some(message.primitive)
      ),
      reExtra = Some(reExtra)
    )
    rePayloadContainerBlobFunc(reRequestReq, repositories, log)
  }

  def traceInterfaceRequest(message: SoapRequest, re: Re, reExtra: ReExtra, rePayloadContainerBlobFunc: RePayloadContainerBlobFunc, ddataMap: ConfigData): Unit = {
    import StringUtils.Utf8String
    Util.logPayload(log, Some(message.payload))
    val reRequestReq = ReRequest(
      sessionId = message.sessionId,
      testCaseId = message.testCaseId,
      re = re.copy(
        insertedTimestamp = message.timestamp,
        payload = Some(message.payload.getUtf8Bytes),
        categoriaEvento = CategoriaEvento.INTERFACCIA.toString,
        sottoTipoEvento = SottoTipoEvento.REQ.toString,
        esito = Some(EsitoRE.RICEVUTA.toString),
        businessProcess = Some(message.primitive)
      ),
      reExtra = Some(reExtra)
    )
    rePayloadContainerBlobFunc(reRequestReq, repositories, log)
  }

  def traceInterfaceRequest(message: RestRequest, re: Re, reExtra: ReExtra, rePayloadContainerBlobFunc: RePayloadContainerBlobFunc, ddataMap: ConfigData): Unit = {
    import StringUtils.Utf8String
    Util.logPayload(log, message.payload)
    val reRequestReq = ReRequest(
      sessionId = message.sessionId,
      testCaseId = message.testCaseId,
      re = re.copy(
        insertedTimestamp = message.timestamp,
        payload = message.payload.map(_.getUtf8Bytes),
        categoriaEvento = CategoriaEvento.INTERFACCIA.toString,
        sottoTipoEvento = SottoTipoEvento.REQ.toString,
        esito = Some(EsitoRE.RICEVUTA.toString),
        businessProcess = Some(message.primitive)
      ),
      reExtra = Some(reExtra)
    )
    rePayloadContainerBlobFunc(reRequestReq, repositories, log)
  }

}
