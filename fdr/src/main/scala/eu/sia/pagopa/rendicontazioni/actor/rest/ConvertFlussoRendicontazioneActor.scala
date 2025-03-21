package eu.sia.pagopa.rendicontazioni.actor.rest

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import eu.sia.pagopa.common.actor.PerRequestActor
import eu.sia.pagopa.common.enums.EsitoRE
import eu.sia.pagopa.common.exception.{DigitPaErrorCodes, DigitPaException, RestException}
import eu.sia.pagopa.common.json.model.rendicontazione._
import eu.sia.pagopa.common.json.model.{Error, FdREventToHistory}
import eu.sia.pagopa.common.json.{JsonEnum, JsonValid}
import eu.sia.pagopa.common.message._
import eu.sia.pagopa.common.repo.Repositories
import eu.sia.pagopa.common.repo.fdr.enums.RendicontazioneStatus
import eu.sia.pagopa.common.repo.fdr.model.Rendicontazione
import eu.sia.pagopa.common.repo.re.model.Re
import eu.sia.pagopa.common.util.Util.ungzipContent
import eu.sia.pagopa.common.util._
import eu.sia.pagopa.common.util.xml.XmlUtil
import eu.sia.pagopa.commonxml.XmlEnum
import eu.sia.pagopa.config.actor.{FdRMetadataActor, ReActor}
import eu.sia.pagopa.rendicontazioni.actor.BaseFlussiRendicontazioneActor
import eu.sia.pagopa.{ActorProps, BootstrapUtil}
import org.slf4j.MDC
import scalaxbmodel.flussoriversamento.{CtDatiSingoliPagamenti, CtFlussoRiversamento, CtIdentificativoUnivoco, CtIdentificativoUnivocoPersonaG, CtIstitutoMittente, CtIstitutoRicevente, Number1u461}
import scalaxbmodel.nodoperpsp.NodoInviaFlussoRendicontazione
import spray.json._

import javax.xml.datatype.DatatypeFactory
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{Failure, Success}

case class ConvertFlussoRendicontazioneActor(repositories: Repositories, actorProps: ActorProps)
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
          flowAction = Some(req.primitive)
        )
      )

      (for {
        _ <- Future.successful(())
        _ = log.debug(FdrLogConstant.logStart(actorClassId))
        flow <- Future.fromTry(parseInput(req))

        re_ = Re(
          psp = Some(flow.sender.pspId),
          idDominio = Some(flow.receiver.organizationId),
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
          flowName = Some(flow.name),
          flowAction = Some(req.primitive)
        )
        _ = reFlow = Some(re_)

        _ = log.debug(FdrLogConstant.logGeneraPayload(s"nodoInviaFlussoRendicontazione SOAP"))
        flussoRiversamento = CtFlussoRiversamento(
          Number1u461,
          flow.name,
          DatatypeFactory.newInstance().newXMLGregorianCalendar(flow.date),
          flow.regulation,
          DatatypeFactory.newInstance().newXMLGregorianCalendar(flow.regulationDate),
          CtIstitutoMittente(
            CtIdentificativoUnivoco(
              flow.sender._type match {
                case SenderTypeEnum.ABI_CODE => scalaxbmodel.flussoriversamento.A
                case SenderTypeEnum.BIC_CODE => scalaxbmodel.flussoriversamento.B
                case _ => scalaxbmodel.flussoriversamento.GValue
              },
              flow.sender.id
            ),
            Some(flow.sender.pspName)
          ),
          Some(flow.bicCodePouringBank),
          CtIstitutoRicevente(
            CtIdentificativoUnivocoPersonaG(
              scalaxbmodel.flussoriversamento.G,
              flow.receiver.id
            ),
            Some(flow.receiver.organizationName)
          ),
          BigDecimal(flow.computedTotPayments),
          BigDecimal(flow.computedSumPayments),
          flow.paymentList.map(p => {
            CtDatiSingoliPagamenti(
              p.iuv,
              p.iur,
              Some(BigInt.int2bigInt(p.transferId)),
              BigDecimal(p.pay),
              p.payStatus match {
                case PayStatusEnum.NO_RPT => scalaxbmodel.flussoriversamento.Number9
                case PayStatusEnum.REVOKED => scalaxbmodel.flussoriversamento.Number3
                case PayStatusEnum.STAND_IN => scalaxbmodel.flussoriversamento.Number4
                case PayStatusEnum.STAND_IN_NO_RPT => scalaxbmodel.flussoriversamento.Number8
                case _ => scalaxbmodel.flussoriversamento.Number0
              },
              DatatypeFactory.newInstance().newXMLGregorianCalendar(p.payDate)
            )
          })
        )
        flussoRiversamentoEncoded <- Future.fromTry(XmlEnum.FlussoRiversamento2Str_flussoriversamento(flussoRiversamento))
        flussoRiversamentoBase64 = XmlUtil.StringBase64Binary.encodeBase64(flussoRiversamentoEncoded)

        nifr = NodoInviaFlussoRendicontazione(
          flow.sender.pspId,
          flow.sender.pspBrokerId,
          flow.sender.channelId,
          flow.sender.password,
          flow.receiver.organizationId,
          flow.name,
          DatatypeFactory.newInstance().newXMLGregorianCalendar(flow.date),
          flussoRiversamentoBase64
        )

        (_, rendicontazioneSaved, _, _) <- saveRendicontazione(
          flow.name,
          flow.sender.pspId,
          flow.sender.pspBrokerId,
          flow.sender.channelId,
          flow.receiver.organizationId,
          DatatypeFactory.newInstance().newXMLGregorianCalendar(flow.date),
          flussoRiversamentoBase64,
          checkUTF8,
          flussoRiversamento,
          repositories.fdrRepository
        )

        _ = reFlow = reFlow.map(r => r.copy(status = Some("PUBLISHED")))
        _ = callTrace(traceInternalRequest, reActor, restRequest, reFlow.get, restRequest.reExtra)
        sr = RestResponse(req.sessionId, Some(GenericResponse(GenericResponseOutcome.OK.toString).toJson.toString), StatusCodes.OK.intValue, reFlow, req.testCaseId, None)
      } yield (sr, nifr, flussoRiversamentoEncoded, rendicontazioneSaved))
        .recoverWith({
          case rex: RestException =>
            Future.successful(generateErrorResponse(Some(rex)))
          case dex: DigitPaException =>
            val pmae = RestException(dex.message, StatusCodes.BadRequest.intValue)
            Future.successful(generateErrorResponse(Some(pmae)))
          case cause: Throwable =>
            val pmae = RestException(DigitPaErrorCodes.description(DigitPaErrorCodes.PPT_SYSTEM_ERROR), StatusCodes.InternalServerError.intValue, cause)
            Future.successful(generateErrorResponse(Some(pmae)))
        }).map {
          case sr: RestResponse =>
            callTrace(traceInterfaceRequest, reActor, req, reFlow.get, req.reExtra)
            logEndProcess(sr)
            replyTo ! sr
            complete()
          case (sr: RestResponse, nifr: NodoInviaFlussoRendicontazione, flussoRiversamentoEncoded: String, rendicontazioneSaved: Rendicontazione) =>
            callTrace(traceInterfaceRequest, reActor, req, reFlow.get, req.reExtra)
            logEndProcess(sr)

            Future {
              if (rendicontazioneSaved.stato.equals(RendicontazioneStatus.VALID)) {
                // send data to history
                actorProps.routers(BootstrapUtil.actorRouter(BootstrapUtil.actorClassId(classOf[FdRMetadataActor])))
                  .tell(
                    FdREventToHistory(
                      sessionId = req.sessionId,
                      nifr = nifr,
                      soapRequest = flussoRiversamentoEncoded,
                      insertedTimestamp = rendicontazioneSaved.insertedTimestamp,
                      elaborate = true,
                      retry = 0
                    ),
                    null)
              }
            }
            replyTo ! sr
            complete()
        }
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
    if (restRequest.payload.isEmpty) {
      Failure(RestException("Invalid request", Constant.HttpStatusDescription.BAD_REQUEST, StatusCodes.BadRequest.intValue))
    } else {
      ungzipContent(restRequest.payload.get.getBytes) match {
        case Success(content) =>
          val decompressedPayload = content.toString
          JsonValid.check(decompressedPayload, JsonEnum.CONVERT_FLOW) match {
            case Success(_) => Success(Flow.read(decompressedPayload.parseJson))
            case Failure(e) => Failure(RestException("Invalid FdR 3 flow JSON: " + e.getMessage, "", StatusCodes.BadRequest.intValue, e))
          }
        case Failure(e) => Failure(RestException("Error during request content unzip: " + e.getMessage, "", StatusCodes.BadRequest.intValue, e))
      }
    }
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

  private def generateErrorResponse(exception: Option[RestException]) = {
    log.debug(FdrLogConstant.logGeneraPayload(actorClassId + "Risposta"))
    val httpStatusCode = exception.map(_.statusCode).getOrElse(StatusCodes.OK.intValue)
    log.debug(s"Generating response $httpStatusCode")
    val payload = exception.map(v => Error(v.getMessage).toJson.toString())
    RestResponse(req.sessionId, payload, httpStatusCode, reFlow, req.testCaseId, exception)
  }

}
