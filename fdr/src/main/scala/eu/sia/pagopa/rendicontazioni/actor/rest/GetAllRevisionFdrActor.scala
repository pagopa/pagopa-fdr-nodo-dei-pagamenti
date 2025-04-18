package eu.sia.pagopa.rendicontazioni.actor.rest

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import com.nimbusds.jose.util.StandardCharset
import eu.sia.pagopa.{ActorProps, BootstrapUtil}
import eu.sia.pagopa.common.actor.PerRequestActor
import eu.sia.pagopa.common.enums.EsitoRE
import eu.sia.pagopa.common.exception.{DigitPaErrorCodes, DigitPaException, RestException}
import eu.sia.pagopa.common.json.model.Error
import eu.sia.pagopa.common.json.model.rendicontazione.GetXmlRendicontazioneResponse
import eu.sia.pagopa.common.message._
import eu.sia.pagopa.common.repo.Repositories
import eu.sia.pagopa.common.repo.fdr.model.{BinaryFile, Rendicontazione}
import eu.sia.pagopa.common.repo.re.model.Re
import eu.sia.pagopa.common.util.DDataChecks.checkPA
import eu.sia.pagopa.common.util._
import eu.sia.pagopa.config.actor.ReActor
import org.slf4j.MDC
import spray.json._

import java.util.Base64
import scala.concurrent.Future
import scala.language.postfixOps

case class GetAllRevisionFdrActorPerRequest(repositories: Repositories, actorProps: ActorProps) extends PerRequestActor with ReUtil {

  var req: RestRequest = _
  var replyTo: ActorRef = _

  var reFlow: Option[Re] = None

  val reActor = actorProps.routers(BootstrapUtil.actorRouter(BootstrapUtil.actorClassId(classOf[ReActor])))

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
          flowAction = Some(req.primitive)
        )
      )

      (for {
        _ <- Future.successful(())
        _ = log.debug(FdrLogConstant.logSintattico(actorClassId))

        organizationId = req.pathParams("organizationId")
        fdr = req.pathParams("fdr")
        _ = MDC.put(Constant.MDCKey.FDR, fdr)

        re_ = Re(
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
          flowName = Some(fdr),
          flowAction = Some(req.primitive),
          idDominio = Some(organizationId)
        )
        _ = reFlow = Some(re_)

        _ <- Future.fromTry(checkPA(log, ddataMap, organizationId))

        rendicontazioneOpt <- checkFlussoRendicontazione(fdr, organizationId)
        rendicontazione <- if( rendicontazioneOpt.isEmpty ) {
          log.error(s"FdR $fdr unknown")
          Future.failed(RestException("FdR unknown or not available", Constant.HttpStatusDescription.NOT_FOUND, StatusCodes.NotFound.intValue))
        } else {
          Future.successful(rendicontazioneOpt.get)
        }

        _ = reFlow = reFlow.map(r =>
          r.copy(
            psp = Some(rendicontazione.psp),
            esito = Some(EsitoRE.RICEVUTA.toString),
            canale = rendicontazione.canale
          )
        )

        binaryFileOption <- rendicontazione.fk_binary_file match {
          case Some(fk) =>
            repositories.fdrRepository.binaryFileById(fk)
          case None =>
            Future.failed(RestException("FdR XML not found", Constant.HttpStatusDescription.NOT_FOUND, StatusCodes.NotFound.intValue))
        }

        _ = log.debug("Make response with reporting")
        rendicontazioneDb <- elaboraRisposta(binaryFileOption)
      } yield RestResponse(req.sessionId, Some(GetXmlRendicontazioneResponse(rendicontazioneDb).toJson.compactPrint),
        StatusCodes.OK.intValue, reFlow, req.testCaseId, None) )
        .recoverWith({
          case rex: RestException =>
            Future.successful(generateErrorResponse(Some(rex)))
          case rex: DigitPaException =>
            Future.successful(generateErrorResponseFromSoap(Some(rex)))
          case cause: Throwable =>
            val pmae = RestException(DigitPaErrorCodes.description(DigitPaErrorCodes.PPT_SYSTEM_ERROR), StatusCodes.InternalServerError.intValue, cause)
            Future.successful(generateErrorResponse(Some(pmae)))
      }).map( res => {
        callTrace(traceInterfaceRequest, reActor, req, reFlow.get, req.reExtra)
        logEndProcess(res)
        replyTo ! res
        complete()
      })
  }

  private def checkFlussoRendicontazione(idFlusso: String, idDominio: String): Future[Option[Rendicontazione]] = {
    repositories.fdrRepository.getRendicontazioneValidaByIfFlusso(idFlusso, Some(idDominio), None)
  }

  override def actorError(dpe: DigitPaException): Unit = {
    actorError(replyTo, req, dpe, reFlow)
  }

  def actorError(replyTo: ActorRef, req: RestRequest, dpe: DigitPaException, re: Option[Re]): Unit = {
    MDC.put(Constant.MDCKey.SESSION_ID, req.sessionId)
    val dpa = RestException(dpe.getMessage, StatusCodes.InternalServerError.intValue, dpe)
    val response = makeFailureResponse(req.sessionId, req.testCaseId, dpa, re)
    replyTo ! response
  }

  private def callTrace(callback: (ActorRef, RestRequest, Re, ReExtra) => Unit,
                        reActor: ActorRef, restRequest: RestRequest, re: Re,
                        reExtra: ReExtra): Unit = {
    Future {
      callback(reActor, restRequest, re, reExtra)
    }.recover {
      case e: Throwable =>
        log.error(e, s"Execution error in ${callback.getClass.getSimpleName}")
    }
  }

  private def makeFailureResponse(sessionId: String, tcid: Option[String], restException: RestException, re: Option[Re]): RestResponse = {
    import spray.json._
    log.error(restException, s"Generic error: ${restException.message}")
    val err = Error(restException.message).toJson.toString()
    RestResponse(sessionId, Some(err), restException.statusCode, re, tcid, Some(restException))
  }

  private def generateErrorResponse(exception: Option[RestException]) = {
    log.debug(FdrLogConstant.logGeneraPayload(actorClassId + "Response"))
    val httpStatusCode = exception.map(_.statusCode).getOrElse(StatusCodes.OK.intValue)
    log.debug(s"Generating response $httpStatusCode")
    val payload = exception.map(v => Error(v.getMessage).toJson.toString())
    RestResponse(req.sessionId, payload, httpStatusCode, reFlow, req.testCaseId, exception)
  }

  private def generateErrorResponseFromSoap(exception: Option[Exception]) = {
    log.debug(FdrLogConstant.logGeneraPayload(actorClassId + "Response"))
    val httpStatusCode = StatusCodes.BadRequest.intValue
    log.debug(s"Generating response $httpStatusCode")
    val payload = exception.map(v => Error(v.getMessage).toJson.toString())
    RestResponse(req.sessionId, payload, httpStatusCode, reFlow, req.testCaseId, exception)
  }

  private def elaboraRisposta(binaryFileOption: Option[BinaryFile]): Future[String] = {
      if (binaryFileOption.isDefined) {
          Future.successful(new String(Base64.getEncoder.encode(binaryFileOption.get.fileContent.get), StandardCharset.UTF_8))
      } else {
        Future.failed(RestException("FdR XML not found", Constant.HttpStatusDescription.NOT_FOUND, StatusCodes.NotFound.intValue))
      }
  }

}
