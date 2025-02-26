package eu.sia.pagopa.rendicontazioni.actor.rest

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import eu.sia.pagopa.{ActorProps, BootstrapUtil}
import eu.sia.pagopa.common.actor.{HttpFdrServiceManagement, PerRequestActor}
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
import eu.sia.pagopa.common.util.DDataChecks.checkPsp
import eu.sia.pagopa.common.util._
import eu.sia.pagopa.common.util.xml.XmlUtil
import eu.sia.pagopa.commonxml.XmlEnum
import eu.sia.pagopa.config.actor.{FdRMetadataActor, ReActor}
import eu.sia.pagopa.rendicontazioni.actor.BaseFlussiRendicontazioneActor
import eu.sia.pagopa.rendicontazioni.util.CheckRendicontazioni
import org.slf4j.MDC
import scalaxbmodel.flussoriversamento.{CtDatiSingoliPagamenti, CtFlussoRiversamento, CtIdentificativoUnivoco, CtIdentificativoUnivocoPersonaG, CtIstitutoMittente, CtIstitutoRicevente, Number1u461}
import scalaxbmodel.nodoperpsp.NodoInviaFlussoRendicontazione
import spray.json._

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}
import javax.xml.datatype.DatatypeFactory
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}


case class RegisterFdrForValidationActorPerRequest(repositories: Repositories, actorProps: ActorProps)
  extends PerRequestActor with BaseFlussiRendicontazioneActor with ReUtil {

  var req: RestRequest = _
  var replyTo: ActorRef = _

  var reFlow: Option[Re] = None

  val reActor = actorProps.routers(BootstrapUtil.actorRouter(BootstrapUtil.actorClassId(classOf[ReActor])))

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
          flowAction = Some("registerFdrForValidation")
        )
      )

      (for {
        _ <- Future.successful(())
        _ = log.debug(FdrLogConstant.logSintattico(actorClassId))
        (flowId, pspId, organizationId, flowTimestamp) <- Future.fromTry(parseInput(req))

        re_ = Re(
          psp = Some(pspId),
          idDominio = Some(organizationId),
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
          flowName = Some(flowId),
          flowAction = Some(req.primitive)
        )
        _ = reFlow = Some(re_)

        _ = log.debug(FdrLogConstant.logGeneraPayload(s"registerFdrForValidation REST"))

        (persistenceOutcome) <- saveRendicontazione(
          flowId,
          pspId,
          organizationId,
          DatatypeFactory.newInstance().newXMLGregorianCalendar(flowTimestamp),
          repositories.fdrRepository
        )

        _ = if (persistenceOutcome == Constant.KO) {
          throw RestException("Error saving fdr on Db", Constant.HttpStatusDescription.INTERNAL_SERVER_ERROR, StatusCodes.InternalServerError.intValue)
        } else {
          Future.successful(())
        }
      } yield RestResponse(req.sessionId, Some(GenericResponse(GenericResponseOutcome.OK.toString).toJson.toString), StatusCodes.OK.intValue, reFlow, req.testCaseId, None))
        .recoverWith({
          case rex: RestException =>
            Future.successful(generateResponse(Some(rex)))
          case cause: Throwable =>
            val pmae = RestException(DigitPaErrorCodes.description(DigitPaErrorCodes.PPT_SYSTEM_ERROR), StatusCodes.InternalServerError.intValue, cause)
            Future.successful(generateResponse(Some(pmae)))
        }).map(res => {
          callTrace(traceInterfaceRequest, reActor, req, reFlow.get, req.reExtra)
          res.throwable match {
            case Some(ex) => log.error(FdrLogConstant.logEndKO(actorClassId, Some(ex)))
            case None => log.info(FdrLogConstant.logEndOK(actorClassId))
          }
          replyTo ! res
          complete()
        })
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

  private def parseInput(restRequest: RestRequest) = {
    Try({
      val parsedRequest = if (restRequest.payload.isEmpty) {
        Failure(RestException("Invalid request", Constant.HttpStatusDescription.BAD_REQUEST, StatusCodes.BadRequest.intValue))
      } else {
        JsonValid.check(restRequest.payload.get, JsonEnum.REGISTER_FOR_VALIDATION) match {
          case Success(_) =>
            val obj = restRequest.payload.get.parseJson.convertTo[RegisterFdrForValidationRequest]
            Success(obj)
          case Failure(e) =>
            if (e.getMessage.contains("flowId")) {
              Failure(RestException("Invalid flowId", "", StatusCodes.BadRequest.intValue, e))
            } else if (e.getMessage.contains("pspId")) {
              Failure(RestException("Invalid pspId", "", StatusCodes.BadRequest.intValue, e))
            } else if (e.getMessage.contains("organizationId")) {
              Failure(RestException("Invalid organizationId", "", StatusCodes.BadRequest.intValue, e))
            } else if (e.getMessage.contains("flowTimestamp")) {
              Failure(RestException("Invalid flowTimestamp", "", StatusCodes.BadRequest.intValue, e))
            } else {
              Failure(RestException("Invalid request", "", StatusCodes.BadRequest.intValue, e))
            }
        }
      }

      val pspId = parsedRequest.get.pspId
      val organizationId = parsedRequest.get.organizationId
      val flowId = parsedRequest.get.flowId
      MDC.put(Constant.MDCKey.FDR, flowId)
      val flowTimestamp = parsedRequest.get.flowTimestamp
      checkPsp(log, ddataMap, pspId) match {
        case Success(value) => value
        case Failure(e: DigitPaException) =>
          throw RestException(e.getMessage, Constant.HttpStatusDescription.BAD_REQUEST, StatusCodes.BadRequest.intValue)
        case _ =>
          throw RestException("Error during check psp", Constant.HttpStatusDescription.INTERNAL_SERVER_ERROR, StatusCodes.InternalServerError.intValue)
      }
      CheckRendicontazioni.checkFormatoIdFlussoRendicontazione(flowId, pspId) match {
        case Success(value) => value
        case Failure(e: DigitPaException) =>
          throw RestException(e.getMessage, Constant.HttpStatusDescription.BAD_REQUEST, StatusCodes.BadRequest.intValue)
        case _ =>
          throw RestException("Error during check fdr format", Constant.HttpStatusDescription.INTERNAL_SERVER_ERROR, StatusCodes.InternalServerError.intValue)
      }
      (flowId, pspId, organizationId, flowTimestamp)
    })
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

  private def makeFailureResponse(sessionId: String, tcid: Option[String], restException: RestException, re: Option[Re]): RestResponse = {
    import spray.json._
    log.error(restException, s"Errore generico: ${restException.message}")
    val err = GenericResponse(GenericResponseOutcome.KO.toString).toJson.toString()
    RestResponse(sessionId, Some(err), restException.statusCode, re, tcid, Some(restException))
  }

  private def generateResponse(exception: Option[RestException]) = {
    log.debug(FdrLogConstant.logGeneraPayload(actorClassId + "Risposta"))
    val httpStatusCode = exception.map(_.statusCode).getOrElse(StatusCodes.OK.intValue)
    log.debug(s"Generazione risposta $httpStatusCode")
    val errorCause = exception.map(_.message).getOrElse("Generic error")
    val responsePayload = exception.map(v => GenericResponse(errorCause).toJson.toString())
    RestResponse(req.sessionId, responsePayload, httpStatusCode, reFlow, req.testCaseId, exception)
  }

}
