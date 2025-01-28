package eu.sia.pagopa.rendicontazioni.actor.soap

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import eu.sia.pagopa.{ActorProps, BootstrapUtil}
import eu.sia.pagopa.common.actor.PerRequestActor
import eu.sia.pagopa.common.enums.EsitoRE
import eu.sia.pagopa.common.exception
import eu.sia.pagopa.common.exception.{DigitPaErrorCodes, DigitPaException}
import eu.sia.pagopa.common.json.model.FdREventToHistory
import eu.sia.pagopa.common.message._
import eu.sia.pagopa.common.repo.Repositories
import eu.sia.pagopa.common.repo.fdr.enums.RendicontazioneStatus
import eu.sia.pagopa.common.repo.fdr.model.Rendicontazione
import eu.sia.pagopa.common.repo.re.model.Re
import eu.sia.pagopa.common.util._
import eu.sia.pagopa.common.util.xml.XsdValid
import eu.sia.pagopa.commonxml.XmlEnum
import eu.sia.pagopa.config.actor.{FdRMetadataActor, ReActor}
import eu.sia.pagopa.rendicontazioni.actor.BaseFlussiRendicontazioneActor
import eu.sia.pagopa.rendicontazioni.actor.soap.response.NodoInviaFlussoRendicontazioneResponse
import eu.sia.pagopa.rendicontazioni.util.CheckRendicontazioni
import org.slf4j.MDC
import scalaxbmodel.flussoriversamento.CtFlussoRiversamento
import scalaxbmodel.nodoperpsp.{NodoInviaFlussoRendicontazione, NodoInviaFlussoRendicontazioneRisposta}

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}
import scala.concurrent.Future
import scala.util.{Failure, Try}

case class NodoInviaFlussoRendicontazioneActor(repositories: Repositories, actorProps: ActorProps)
  extends BaseFlussiRendicontazioneActor
    with PerRequestActor
    with ReUtil
    with NodoInviaFlussoRendicontazioneResponse {

  var req: SoapRequest = _
  var replyTo: ActorRef = _

  var reFlow: Option[Re] = None

  val checkUTF8: Boolean = context.system.settings.config.getBoolean("bundle.checkUTF8")
  val inputXsdValid: Boolean = Try(DDataChecks.getConfigurationKeys(ddataMap, "validate_input").toBoolean).getOrElse(false)
  val outputXsdValid: Boolean = Try(DDataChecks.getConfigurationKeys(ddataMap, "validate_output").toBoolean).getOrElse(false)

  val RESPONSE_NAME = "nodoInviaFlussoRendicontazioneRisposta"

  val reActor = actorProps.routers(BootstrapUtil.actorRouter(BootstrapUtil.actorClassId(classOf[ReActor])))

  override def receive: Receive = { case soapRequest: SoapRequest =>

    req = soapRequest
    replyTo = sender()

    reFlow = Some(
      Re(
        componente = Componente.NDP_FDR.toString,
        categoriaEvento = CategoriaEvento.INTERNO.toString,
        sessionId = Some(req.sessionId),
        payload = None,
        esito = Some(EsitoRE.CAMBIO_STATO.toString),
        tipoEvento = Some(actorClassId),
        sottoTipoEvento = SottoTipoEvento.INTERN.toString,
        insertedTimestamp = soapRequest.timestamp,
        erogatore = Some(Componente.NDP_FDR.toString),
        businessProcess = Some(actorClassId),
        erogatoreDescr = Some(Componente.NDP_FDR.toString),
        flowAction = Some(req.primitive)
      )
    )

    val pipeline = for {
      _ <- Future.successful(())

      // syntax check
      nifr <- Future.fromTry(parseInput(soapRequest.payload, inputXsdValid))

      _ = MDC.put(Constant.MDCKey.FDR, nifr.identificativoFlusso)
      now = Util.now()
      re_ = Re(
        idDominio = Some(nifr.identificativoDominio),
        psp = Some(nifr.identificativoPSP),
        componente = Componente.NDP_FDR.toString,
        categoriaEvento = CategoriaEvento.INTERNO.toString,
        tipoEvento = Some(actorClassId),
        sottoTipoEvento = SottoTipoEvento.INTERN.toString,
        fruitore = Some(nifr.identificativoCanale),
        erogatore = Some(Componente.NDP_FDR.toString),
        canale = Some(nifr.identificativoCanale),
        esito = Some(EsitoRE.RICEVUTA.toString),
        sessionId = Some(req.sessionId),
        insertedTimestamp = now,
        businessProcess = Some(actorClassId),
        erogatoreDescr = Some(Componente.NDP_FDR.toString),
        flowName = Some(nifr.identificativoFlusso),
        flowAction = Some(req.primitive)
      )
      _ = reFlow = Some(re_)

      // semantic check
      // semantic check
      (pa, psp, canale) <- Future.fromTry(checks(ddataMap, nifr, checkPassword = true, actorClassId))
      _ <- Future.fromTry(checkFormatoIdFlussoRendicontazione(nifr.identificativoFlusso, nifr.identificativoPSP, actorClassId))

      _ = reFlow = reFlow.map(r => r.copy(fruitoreDescr = canale.flatMap(c => c.description), pspDescr = psp.flatMap(p => p.description)))

      _ = log.debug("Check duplicates on db")
      _ <- checksDB(nifr)

      dataOraFlussoNew = LocalDateTime.parse(nifr.dataOraFlusso.toString, DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault()))
      oldRendi <- repositories.fdrRepository.findRendicontazioniByIdFlusso(
        nifr.identificativoPSP,
        nifr.identificativoFlusso,
        LocalDateTime.of(dataOraFlussoNew.getYear, 1, 1, 0, 0, 0),
        LocalDateTime.of(dataOraFlussoNew.getYear, 12, 31, 23, 59, 59)
      )

      _ = log.debug("Check dataOraFlusso new with dataOraFlusso old")
      _ <- oldRendi match {
        case Some(old) =>
          val idDominioNew = nifr.identificativoDominio
          if (dataOraFlussoNew.isAfter(old.dataOraFlusso)) {
            log.debug("Check idDominio new with idDominio old")
            if (idDominioNew == old.dominio) {
              Future.successful(())
            } else {
              Future.failed(
                exception.DigitPaException(
                  s"Il file con identificativoFlusso [${nifr.identificativoFlusso}] ha idDominio old[${old.dominio}], idDominio new [$idDominioNew]",
                  DigitPaErrorCodes.PPT_SEMANTICA
                )
              )
            }
          } else {
            Future.failed(
              exception.DigitPaException(
                s"Esiste già un file con identificativoFlusso [${nifr.identificativoFlusso}] più aggiornato con dataOraFlusso [${old.dataOraFlusso.toString}]",
                DigitPaErrorCodes.PPT_SEMANTICA
              )
            )
          }
        case None =>
          Future.successful(())
      }

      (flussoRiversamento, _) <- validateRendicontazione(nifr, checkUTF8, inputXsdValid, repositories.fdrRepository)
      (esito, rendicontazioneSaved, _, _) <- saveRendicontazione(
        nifr.identificativoFlusso,
        nifr.identificativoPSP,
        nifr.identificativoIntermediarioPSP,
        nifr.identificativoCanale,
        nifr.identificativoDominio,
        nifr.dataOraFlusso,
        nifr.xmlRendicontazione,
        checkUTF8,
        flussoRiversamento,
        repositories.fdrRepository
      )

      _ = log.info(FdrLogConstant.logGeneraPayload(RESPONSE_NAME))
      nodoInviaFlussoRisposta = NodoInviaFlussoRendicontazioneRisposta(None, esito)
      _ = log.info(FdrLogConstant.logSintattico(RESPONSE_NAME))
      resultMessage <- Future.fromTry(wrapInBundleMessage(nodoInviaFlussoRisposta))
      _ = reFlow = reFlow.map(r => r.copy(status = Some("PUBLISHED")))
//      _ = traceInternalRequest(soapRequest, reFlow.get, soapRequest.reExtra, reEventFunc, ddataMap)
      _ = traceInternalRequestTest(reActor, soapRequest, reFlow.get, soapRequest.reExtra, ddataMap)
      sr = SoapResponse(req.sessionId, Some(resultMessage), StatusCodes.OK.intValue, reFlow, req.testCaseId, None)
    } yield (sr, nifr, flussoRiversamento, rendicontazioneSaved)

    pipeline
      .recover({
        case e: DigitPaException =>
          log.warn(e, FdrLogConstant.logGeneraPayload(s"negative $RESPONSE_NAME, [${e.getMessage}]"))
          errorHandler(req.sessionId, req.testCaseId, outputXsdValid, e, reFlow)
        case e: Throwable =>
          log.warn(e, FdrLogConstant.logGeneraPayload(s"negative $RESPONSE_NAME, [${e.getMessage}]"))
          errorHandler(req.sessionId, req.testCaseId, outputXsdValid, exception.DigitPaException(DigitPaErrorCodes.PPT_SYSTEM_ERROR, e), reFlow)
      }).map { case (sr: SoapResponse, nifr: NodoInviaFlussoRendicontazione, flussoRiversamento: CtFlussoRiversamento, rendicontazioneSaved: Rendicontazione) =>
        log.info(FdrLogConstant.logEnd(actorClassId))
//        traceInterfaceRequest(soapRequest, reFlow.get, soapRequest.reExtra, reEventFunc, ddataMap)
        traceInterfaceRequestTest(reActor, soapRequest, reFlow.get, soapRequest.reExtra, ddataMap)
        replyTo ! sr

        if (rendicontazioneSaved.stato.equals(RendicontazioneStatus.VALID)) {

          // send data to history
          actorProps.routers(BootstrapUtil.actorRouter(BootstrapUtil.actorClassId(classOf[FdRMetadataActor])))
            .tell(
              FdREventToHistory(
                sessionId = req.sessionId,
                nifr = nifr,
                soapRequest = soapRequest.payload,
                insertedTimestamp = rendicontazioneSaved.insertedTimestamp,
                elaborate = true,
                retry = 0
              ),
              replyTo)
        }

        complete()
      }
  }

  override def actorError(e: DigitPaException): Unit = {
    actorError(req, replyTo, ddataMap, e, reFlow)
  }

  private def checksDB(nifr: NodoInviaFlussoRendicontazione) = {
    val datazoned =
      LocalDateTime.parse(nifr.dataOraFlusso.toString, DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault()))
      CheckRendicontazioni.checkFlussoRendicontazioneNotPresentOnSamePsp(repositories.fdrRepository, nifr.identificativoFlusso, nifr.identificativoPSP, datazoned).flatMap {
      case Some(r) =>
        Future.failed(
          exception.DigitPaException(
            s"flusso di rendicontazione gia' presente(identificativo PSP ${r.psp}, identificativo flusso ${r.idFlusso}, data - ora ${r.dataOraFlusso})",
            DigitPaErrorCodes.PPT_SEMANTICA
          )
        )
      case None =>
        Future.successful(())
    }
  }

  private def wrapInBundleMessage(ncefrr: NodoInviaFlussoRendicontazioneRisposta) = {
    for {
      respPayload <- XmlEnum.nodoInviaFlussoRendicontazioneRisposta2Str_nodoperpsp(ncefrr)
      _ <- XsdValid.checkOnly(respPayload, XmlEnum.NODO_INVIA_FLUSSO_RENDICONTAZIONE_RISPOSTA_NODOPERPSP, outputXsdValid)
      _ = log.debug("Valid Response envelope")
    } yield respPayload
  }

  def parseInput(payload: String, inputXsdValid: Boolean): Try[NodoInviaFlussoRendicontazione] = {
    log.info(FdrLogConstant.logSintattico(actorClassId))
    (for {
      _ <- XsdValid.checkOnly(payload, XmlEnum.NODO_INVIA_FLUSSO_RENDICONTAZIONE_NODOPERPSP, inputXsdValid)
      body <- XmlEnum.str2nodoInviaFlussoRendicontazione_nodoperpsp(payload)
      _ = log.debug("Request validated successfully")
    } yield body) recoverWith { case e =>
      log.warn(e, s"${e.getMessage}")
      val cfb = exception.DigitPaException(e.getMessage, DigitPaErrorCodes.PPT_SINTASSI_EXTRAXSD, e)
      Failure(cfb)
    }
  }

}
