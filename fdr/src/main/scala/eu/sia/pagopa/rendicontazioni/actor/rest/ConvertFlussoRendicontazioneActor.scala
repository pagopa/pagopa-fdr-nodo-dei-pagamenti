package eu.sia.pagopa.rendicontazioni.actor.rest

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import eu.sia.pagopa.{ActorProps, BootstrapUtil}
import eu.sia.pagopa.common.actor.PerRequestActor
import eu.sia.pagopa.common.enums.EsitoRE
import eu.sia.pagopa.common.exception.{DigitPaErrorCodes, DigitPaException, RestException}
import eu.sia.pagopa.common.json.model.FdREventToHistory
import eu.sia.pagopa.common.json.model.rendicontazione._
import eu.sia.pagopa.common.json.{JsonEnum, JsonValid}
import eu.sia.pagopa.common.message._
import eu.sia.pagopa.common.repo.Repositories
import eu.sia.pagopa.common.repo.re.model.Re
import eu.sia.pagopa.common.util.Util.ungzipContent
import eu.sia.pagopa.common.util._
import eu.sia.pagopa.common.util.xml.XmlUtil
import eu.sia.pagopa.commonxml.XmlEnum
import eu.sia.pagopa.config.actor.{FdRMetadataActor, ReActor}
import eu.sia.pagopa.rendicontazioni.actor.BaseFlussiRendicontazioneActor
import org.slf4j.MDC
import scalaxbmodel.flussoriversamento.{CtDatiSingoliPagamenti, CtFlussoRiversamento, CtIdentificativoUnivoco, CtIdentificativoUnivocoPersonaG, CtIstitutoMittente, CtIstitutoRicevente, Number1u461}
import scalaxbmodel.nodoperpsp.NodoInviaFlussoRendicontazione
import spray.json._

import java.time.LocalDateTime
import javax.xml.datatype.DatatypeFactory
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

case class ConvertFlussoRendicontazioneActorPerRequest(repositories: Repositories, actorProps: ActorProps)
  extends PerRequestActor with BaseFlussiRendicontazioneActor with ReUtil {

  var req: RestRequest = _
  var replyTo: ActorRef = _

  private var _psp: String = _
  private var _organizationId: String = _
  private var _fdr: String = _
  private var _rev: Integer = _
  private var _retry: Integer = _

  private var uncompressedPayload: Array[Byte] = _
  val inputXsdValid: Boolean = Try(DDataChecks.getConfigurationKeys(ddataMap, "validate_input").toBoolean).getOrElse(false)

  var reFlow: Option[Re] = None

  private val fdrMetadataActor = actorProps.routers(BootstrapUtil.actorRouter(BootstrapUtil.actorClassId(classOf[FdRMetadataActor])))
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
        _ = log.debug(FdrLogConstant.logSintattico(actorClassId))
        flow <- Future.fromTry(parseInput(req))

        re_ = Re(
          psp = Some(flow.pspDomainId),
          idDominio = Some(flow.orgDomainId),
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
          BigDecimal(flow.computedTotAmount),
          flow.paymentList.map(p => {
            CtDatiSingoliPagamenti(
              p.iuv,
              p.iur,
              Some(BigInt.int2bigInt(p.transferId)),
              BigDecimal(p.amount),
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

        (esito, _, _, _) <- saveRendicontazione(
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

        _ = if (esito == Constant.KO) {
          throw RestException("Error saving fdr on Db", Constant.HttpStatusDescription.INTERNAL_SERVER_ERROR, StatusCodes.InternalServerError.intValue)
        } else {
          // WIP testing addition to blob
          Future {
            actorProps.routers(BootstrapUtil.actorRouter(BootstrapUtil.actorClassId(classOf[FdRMetadataActor])))
              .tell(
                FdREventToHistory(
                  sessionId = req.sessionId,
                  nifr = nifr,
                  soapRequest = req.payload.get,
                  insertedTimestamp = LocalDateTime.now(),
                  elaborate = true,
                  retry = 0
                ),
                null)
          }
          // end testing
          Future.successful(())
        }
      } yield RestResponse(req.sessionId, Some(GenericResponse(GenericResponseOutcome.OK.toString).toJson.toString), StatusCodes.OK.intValue, reFlow, req.testCaseId, None) )
        .recoverWith({
          case rex: RestException =>
            Future.successful(generateResponse(Some(rex)))
          case cause: Throwable =>
            val pmae = RestException(DigitPaErrorCodes.description(DigitPaErrorCodes.PPT_SYSTEM_ERROR), StatusCodes.InternalServerError.intValue, cause)
            Future.successful(generateResponse(Some(pmae)))
        }).map( res => {
          callTrace(traceInterfaceRequest, reActor, req, reFlow.get, req.reExtra)
          logEndProcess(res)
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
      if (restRequest.payload.isEmpty) {
        Failure(RestException("Invalid request", Constant.HttpStatusDescription.BAD_REQUEST, StatusCodes.BadRequest.intValue))
      } else {
        var decompressedPayload = "";
        decompressedPayload = ungzipContent(restRequest.payload.get.getBytes).toString
        JsonValid.check(decompressedPayload, JsonEnum.CONVERT_FLOW) match {
          case Success(_) =>
            Success(Flow.read(decompressedPayload.parseJson))
          case Failure(e) =>
            if (e.getMessage.contains("fdr")) {
              Failure(RestException("Invalid fdr", "", StatusCodes.BadRequest.intValue, e))
            } else if (e.getMessage.contains("pspId")) {
              Failure(RestException("Invalid pspId", "", StatusCodes.BadRequest.intValue, e))
            } else if (e.getMessage.contains("organizationId")) {
              Failure(RestException("Invalid organizationId", "", StatusCodes.BadRequest.intValue, e))
            } else if (e.getMessage.contains("retry")) {
              Failure(RestException("Invalid retry", "", StatusCodes.BadRequest.intValue, e))
            } else if (e.getMessage.contains("revision")) {
              Failure(RestException("Invalid revision", "", StatusCodes.BadRequest.intValue, e))
            } else {
              Failure(RestException("Invalid request", "", StatusCodes.BadRequest.intValue, e))
            }
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

  private def generateResponse(exception: Option[RestException]) = {
    log.debug(FdrLogConstant.logGeneraPayload(actorClassId + "Risposta"))
    val httpStatusCode = exception.map(_.statusCode).getOrElse(StatusCodes.OK.intValue)
    log.debug(s"Generazione risposta $httpStatusCode")
    val responsePayload = exception.map(v => GenericResponse(GenericResponseOutcome.KO.toString).toJson.toString())
    RestResponse(req.sessionId, responsePayload, httpStatusCode, reFlow, req.testCaseId, exception)
  }

}
