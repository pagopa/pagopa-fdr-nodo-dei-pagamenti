package eu.sia.pagopa.rendicontazioni.actor.rest

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import eu.sia.pagopa.{ActorProps, BootstrapUtil}
import eu.sia.pagopa.common.actor.PerRequestActor
import eu.sia.pagopa.common.enums.EsitoRE
import eu.sia.pagopa.common.exception
import eu.sia.pagopa.common.exception.{DigitPaErrorCodes, DigitPaException, RestException}
import eu.sia.pagopa.common.json.model.{Error, FdREventToHistory}
import eu.sia.pagopa.common.json.model.rendicontazione._
import eu.sia.pagopa.common.json.{JsonEnum, JsonValid}
import eu.sia.pagopa.common.message._
import eu.sia.pagopa.common.repo.Repositories
import eu.sia.pagopa.common.repo.fdr.enums.RendicontazioneStatus
import eu.sia.pagopa.common.repo.fdr.model.Rendicontazione
import eu.sia.pagopa.common.repo.re.model.Re
import eu.sia.pagopa.common.util._
import eu.sia.pagopa.commonxml.XmlEnum
import eu.sia.pagopa.rendicontazioni.actor.BaseFlussiRendicontazioneActor
import eu.sia.pagopa.rendicontazioni.actor.async.FdREventActor
import eu.sia.pagopa.rendicontazioni.util.CheckRendicontazioni
import org.slf4j.MDC
import scalaxbmodel.flussoriversamento.CtFlussoRiversamento
import scalaxbmodel.nodoperpsp.NodoInviaFlussoRendicontazione
import spray.json._

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}
import java.util.UUID
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

case class NodoInviaFlussoRendicontazioneFTPActorPerRequest(repositories: Repositories, actorProps: ActorProps)
  extends PerRequestActor with BaseFlussiRendicontazioneActor with ReUtil {

  var req: RestRequest = _
  var replyTo: ActorRef = _

  var reFlow: Option[Re] = None

  val checkUTF8: Boolean = context.system.settings.config.getBoolean("bundle.checkUTF8")

  override def receive: Receive = {
    case restRequest: RestRequest =>
      replyTo = sender()
      req = restRequest

      reFlow = Some(
        Re(
          componente = Componente.NDP_FDR.toString,
          categoriaEvento = CategoriaEvento.INTERNO.toString,
          sessionId = Some(req.sessionId),
          payload = None,
          esito = Some(EsitoRE.CAMBIO_STATO.toString),
          tipoEvento = Some(actorClassId),
          sottoTipoEvento = SottoTipoEvento.INTERN.toString,
          insertedTimestamp = restRequest.timestamp,
          erogatore = Some(Componente.NDP_FDR.toString),
          businessProcess = Some(actorClassId),
          erogatoreDescr = Some(Componente.NDP_FDR.toString),
          flowAction = Some("nodoInviaFlussoRendicontazioneFTP")
        )
      )

      (for {
        _ <- Future.successful(())
        _ = log.info(FdrLogConstant.logSintattico(actorClassId))
        (nifrSoap, xmlPayload) <- Future.fromTry(parseInput(req))

        _ = MDC.put(Constant.MDCKey.FDR, nifrSoap.identificativoFlusso)
        now = Util.now()
        re_ = Re(
          idDominio = Some(nifrSoap.identificativoDominio),
          psp = Some(nifrSoap.identificativoPSP),
          componente = Componente.NDP_FDR.toString,
          categoriaEvento = CategoriaEvento.INTERNO.toString,
          tipoEvento = Some(actorClassId),
          sottoTipoEvento = SottoTipoEvento.INTERN.toString,
          fruitore = Some(nifrSoap.identificativoCanale),
          erogatore = Some(Componente.NDP_FDR.toString),
          canale = Some(nifrSoap.identificativoCanale),
          esito = Some(EsitoRE.RICEVUTA.toString),
          sessionId = Some(req.sessionId),
          insertedTimestamp = now,
          businessProcess = Some(actorClassId),
          erogatoreDescr = Some(Componente.NDP_FDR.toString),
          flowName = Some(nifrSoap.identificativoFlusso),
          flowAction = Some("nodoInviaFlussoRendicontazioneFTP")
        )
        _ = reFlow = Some(re_)

        _ = log.info(FdrLogConstant.logSemantico(actorClassId))
        (pa, psp, canale) <- Future.fromTry(checks(ddataMap, nifrSoap, true, actorClassId))

        _ <- Future.fromTry(checkFormatoIdFlussoRendicontazione(nifrSoap.identificativoFlusso, nifrSoap.identificativoPSP, actorClassId))

        _ = reFlow = reFlow.map(r => r.copy(fruitoreDescr = canale.flatMap(c => c.description), pspDescr = psp.flatMap(p => p.description)))

        _ = log.debug("Check duplicates on db")
        _ <- checksDB(nifrSoap)

        dataOraFlussoNew = LocalDateTime.parse(nifrSoap.dataOraFlusso.toString, DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault()))
        oldRendi <- repositories.fdrRepository.findRendicontazioniByIdFlusso(
          nifrSoap.identificativoPSP,
          nifrSoap.identificativoFlusso,
          LocalDateTime.of(dataOraFlussoNew.getYear, 1, 1, 0, 0, 0),
          LocalDateTime.of(dataOraFlussoNew.getYear, 12, 31, 23, 59, 59)
        )

        _ = log.debug("Check dataOraFlusso new with dataOraFlusso old")
        _ <- oldRendi match {
          case Some(old) =>
            val idDominioNew = nifrSoap.identificativoDominio
            if (dataOraFlussoNew.isAfter(old.dataOraFlusso)) {
              log.debug("Check idDominio new with idDominio old")
              if (idDominioNew == old.dominio) {
                Future.successful(())
              } else {
                Future.failed(
                  new RestException(DigitPaErrorCodes.description(
                    DigitPaErrorCodes.PPT_SEMANTICA),
                    s"Il file con identificativoFlusso [${nifrSoap.identificativoFlusso}] ha idDominio old[${old.dominio}], idDominio new [$idDominioNew]",
                    StatusCodes.BadRequest.intValue)
                )
              }
            } else {
              Future.failed(
                new RestException(DigitPaErrorCodes.description(
                  DigitPaErrorCodes.PPT_SEMANTICA),
                  s"Esiste già un file con identificativoFlusso [${nifrSoap.identificativoFlusso}] più aggiornato con dataOraFlusso [${old.dataOraFlusso.toString}]",
                  StatusCodes.BadRequest.intValue)
              )
            }
          case None =>
            Future.successful(())
        }

        (flussoRiversamento, flussoRiversamentoContent) <- validateRendicontazione(nifrSoap, checkUTF8, false, repositories.fdrRepository)
        (_, rendicontazioneSaved, _, _) <- saveRendicontazione(
          nifrSoap.identificativoFlusso,
          nifrSoap.identificativoPSP,
          nifrSoap.identificativoIntermediarioPSP,
          nifrSoap.identificativoCanale,
          nifrSoap.identificativoDominio,
          nifrSoap.dataOraFlusso,
          nifrSoap.xmlRendicontazione,
          checkUTF8,
          flussoRiversamento,
          repositories.fdrRepository
        )

        _ = reFlow = reFlow.map(r => r.copy(status = Some("PUBLISHED")))
        _ = traceInternalRequest(restRequest, reFlow.get, restRequest.reExtra, actorProps.rePayloadContainerBlobFunction, ddataMap)
        rr = RestResponse(req.sessionId, Some(GenericResponse(GenericResponseOutcome.OK.toString).toJson.toString), StatusCodes.OK.intValue, reFlow, req.testCaseId, None)
      } yield (rr, nifrSoap, flussoRiversamento, rendicontazioneSaved))
        .recoverWith({
          case rex: RestException =>
            Future.successful(generateErrorResponse(Some(rex)))
          case dex: DigitPaException =>
            val pmae = RestException(dex.message, StatusCodes.BadRequest.intValue)
            Future.successful(generateErrorResponse(Some(pmae)))
          case cause: Throwable =>
            val pmae = RestException(DigitPaErrorCodes.description(DigitPaErrorCodes.PPT_SYSTEM_ERROR), StatusCodes.InternalServerError.intValue, cause)
            Future.successful(generateErrorResponse(Some(pmae)))
      }).map { case (rr: RestResponse, nifr: NodoInviaFlussoRendicontazione, rendicontazioneSaved: Rendicontazione) =>
          traceInterfaceRequest(req, reFlow.get, req.reExtra, actorProps.rePayloadContainerBlobFunction, ddataMap)
          log.info(FdrLogConstant.logEnd(actorClassId))
          replyTo ! rr

          if (rendicontazioneSaved.stato.equals(RendicontazioneStatus.VALID)) {
            // send data to history
            actorProps.routers(BootstrapUtil.actorRouter(BootstrapUtil.actorClassId(classOf[FdREventActor])))
              .tell(
                FdREventToHistory(
                  sessionId = req.sessionId,
                  nifr = nifr,
                  soapRequest = req.payload.get,
                  insertedTimestamp = rendicontazioneSaved.insertedTimestamp,
                  elaborate = true,
                  retry = 0
                ),
                replyTo)
          }

          complete()
        }
      }
//  }

  private def parseInput(restRequest: RestRequest) = {
    Try({
      val nfrReq = if( restRequest.payload.isEmpty ) {
        throw RestException("Invalid request", Constant.HttpStatusDescription.BAD_REQUEST, StatusCodes.BadRequest.intValue)
      } else {
        JsonValid.check(restRequest.payload.get, JsonEnum.INVIA_FLUSSO_FTP) match {
          case Success(_) =>
            val obj = restRequest.payload.get.parseJson.convertTo[NodoInviaFlussoRendicontazioneFTPReq]
            val nodoInviaFlussoRendicontazione = XmlEnum.str2nodoInviaFlussoRendicontazione_nodoperpsp(obj.content) match {
              case Success(value) => value
              case Failure(e) =>
                log.warn(e, s"${e.getMessage}")
                throw RestException("Invalid content", "", StatusCodes.BadRequest.intValue, e)
            }
            nodoInviaFlussoRendicontazione
          case Failure(e) =>
            if (e.getMessage.contains("content")) {
              throw RestException("Invalid content", "", StatusCodes.BadRequest.intValue, e)
            } else {
              throw RestException("Invalid request", "", StatusCodes.BadRequest.intValue, e)
            }
        }
      }
      (nfrReq, restRequest.payload.get)
    })
  }

  override def actorError(dpe: DigitPaException): Unit = {
    actorError(replyTo, req, dpe, reFlow)
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

  def actorError(replyTo: ActorRef, req: RestRequest, dpe: DigitPaException, re: Option[Re]): Unit = {
    MDC.put(Constant.MDCKey.SESSION_ID, req.sessionId)
    val dpa = RestException(dpe.getMessage, StatusCodes.InternalServerError.intValue, dpe)
    val response = makeFailureResponse(req.sessionId, req.testCaseId, dpa, re)
    replyTo ! response
  }

  private def makeFailureResponse(sessionId: String, tcid: Option[String], restException: RestException, re: Option[Re]): RestResponse = {
    import spray.json._
    log.error(restException, s"Errore generico: ${restException.message}")
    val err = GenericResponse(GenericResponseOutcome.KO.toString).toJson.toString()
    RestResponse(sessionId, Some(err), restException.statusCode, re, tcid, Some(restException))
  }

  private def generateErrorResponse(exception: Option[RestException]) = {
    log.info(FdrLogConstant.logGeneraPayload(actorClassId + "Risposta"))
    val httpStatusCode = exception.map(_.statusCode).getOrElse(StatusCodes.OK.intValue)
    log.debug(s"Generating response $httpStatusCode")
    val payload = exception.map(v => Error(v.getMessage).toJson.toString())
    RestResponse(req.sessionId, payload, httpStatusCode, reFlow, req.testCaseId, exception)
  }

}
