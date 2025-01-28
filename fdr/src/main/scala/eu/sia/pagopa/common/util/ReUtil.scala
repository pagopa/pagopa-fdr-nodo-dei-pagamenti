package eu.sia.pagopa.common.util

import akka.actor.ActorRef
import eu.sia.pagopa.Main.ConfigData
import eu.sia.pagopa.common.actor.NodoLogging
import eu.sia.pagopa.common.enums.EsitoRE
import eu.sia.pagopa.common.message._
import eu.sia.pagopa.common.repo.re.model.Re
import eu.sia.pagopa.common.util.azurehubevent.Appfunction.ReEventFunc


trait ReUtil { this: NodoLogging =>

  def traceInternalRequest(reActor: ActorRef, message: SoapRequest, re: Re, reExtra: ReExtra, ddataMap: ConfigData): Unit = {
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
    log.info("traceInternalRequestTest")
    reActor.tell(reRequestReq, null)
  }

  def traceInternalRequest(reActor: ActorRef, message: RestRequest, re: Re, reExtra: ReExtra, ddataMap: ConfigData): Unit = {
    //    import StringUtils.Utf8String
    //Util.logPayload(log, Some(message.payload.get))
    val reRequestReq = ReRequest(
      sessionId = message.sessionId,
      testCaseId = message.testCaseId,
      re = re.copy(
        insertedTimestamp = message.timestamp,
        //payload = Some(message.payload.get.getUtf8Bytes),
        categoriaEvento = CategoriaEvento.INTERNO.toString,
        sottoTipoEvento = SottoTipoEvento.INTERN.toString,
        esito = Some(EsitoRE.RICEVUTA.toString),
        businessProcess = Some(message.primitive)
      ),
      reExtra = Some(reExtra)
    )
    reActor.tell(reRequestReq, null)
  }

  def traceInterfaceRequest(reActor: ActorRef, message: SoapRequest, re: Re, reExtra: ReExtra, ddataMap: ConfigData): Unit = {
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
    reActor.tell(reRequestReq, null)
  }

  def traceInterfaceRequest(reActor: ActorRef, message: RestRequest, re: Re, reExtra: ReExtra, ddataMap: ConfigData): Unit = {
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
    reActor.tell(reRequestReq, null)
  }

}
