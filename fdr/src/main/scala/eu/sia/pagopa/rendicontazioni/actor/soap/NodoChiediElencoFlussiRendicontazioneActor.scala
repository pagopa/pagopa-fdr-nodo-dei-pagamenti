package eu.sia.pagopa.rendicontazioni.actor.soap

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import eu.sia.pagopa.{ActorProps, BootstrapUtil}
import eu.sia.pagopa.common.actor.{HttpSoapServiceManagement, PerRequestActor}
import eu.sia.pagopa.common.enums.EsitoRE
import eu.sia.pagopa.common.exception
import eu.sia.pagopa.common.exception.{DigitPaErrorCodes, DigitPaException}
import eu.sia.pagopa.common.message._
import eu.sia.pagopa.common.repo.Repositories
import eu.sia.pagopa.common.repo.re.model.Re
import eu.sia.pagopa.common.util._
import eu.sia.pagopa.common.util.xml.XmlUtil.XsdDatePattern
import eu.sia.pagopa.common.util.xml.{XmlUtil, XsdValid}
import eu.sia.pagopa.commonxml.XmlEnum
import eu.sia.pagopa.config.actor.ReActor
import eu.sia.pagopa.rendicontazioni.actor.soap.response.NodoChiediElencoFlussiRendicontazioneResponse
import scalaxbmodel.nodoperpa.{NodoChiediElencoFlussiRendicontazione, NodoChiediElencoFlussiRendicontazioneRisposta, TipoElencoFlussiRendicontazione, TipoIdRendicontazione}

import java.time.LocalDateTime
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

case class NodoChiediElencoFlussiRendicontazioneActorPerRequest(repositories: Repositories, actorProps: ActorProps)
    extends PerRequestActor
      with ReUtil
      with NodoChiediElencoFlussiRendicontazioneResponse {

  var req: SoapRequest = _
  var replyTo: ActorRef = _
  val inputXsdValid: Boolean = DDataChecks.getConfigurationKeys(ddataMap, "validate_input").toBoolean
  val outputXsdValid: Boolean = DDataChecks.getConfigurationKeys(ddataMap, "validate_output").toBoolean

  private val dayLimit: Long = Try(context.system.settings.config.getLong(s"chiediElencoFlussiRendicontazioneDayLimit")).getOrElse(90)
  private val callNexiToo: Boolean = Try(context.system.settings.config.getBoolean(s"callNexiToo")).getOrElse(false)

  var re: Option[Re] = None

  val RESPONSE_NAME = "nodoChiediElencoFlussiRendicontazioneRisposta"

  val reActor = actorProps.routers(BootstrapUtil.actorRouter(BootstrapUtil.actorClassId(classOf[ReActor])))

  override def receive: Receive = { case soapRequest: SoapRequest =>
    req = soapRequest
    replyTo = sender()

    re = Some(
      Re(
        componente = Componente.NDP_FDR.toString,
        categoriaEvento = CategoriaEvento.INTERNO.toString,
        sessionId = Some(req.sessionId),
        esito = Some(EsitoRE.CAMBIO_STATO.toString),
        tipoEvento = Some(actorClassId),
        sottoTipoEvento = SottoTipoEvento.INTERN.toString,
        insertedTimestamp = soapRequest.timestamp,
        erogatore = Some(FaultId.NODO_DEI_PAGAMENTI_SPC),
        businessProcess = Some(actorClassId),
        erogatoreDescr = Some(FaultId.NODO_DEI_PAGAMENTI_SPC),
        flowAction = Some(req.primitive)
      )
    )
    log.debug(FdrLogConstant.logSintattico(actorClassId))
    val pipeline = for {

      ncefr <- Future.fromTry(parseInput(soapRequest))

      _ = re = re.map(r =>
        r.copy(
          idDominio = ncefr.identificativoDominio,
          psp = ncefr.identificativoPSP,
          fruitore = Some(ncefr.identificativoStazioneIntermediarioPA),
          stazione = Some(ncefr.identificativoStazioneIntermediarioPA),
          esito = Some(EsitoRE.RICEVUTA.toString)
        )
      )

      _ = log.debug(FdrLogConstant.logSemantico(actorClassId))
      (staz, psp) <- checks(ncefr)

      _ = re = re.map(r => r.copy(fruitoreDescr = Some(staz.stationCode), pspDescr = psp.flatMap(_.description)))

      rendicontazioni <- findRendicontazioni(ncefr)

      rendicontazioniFiltered = rendicontazioni.groupBy(_._1).map(a => a._2.maxBy(_._2)(Ordering.by(_.toString)))
      _ = log.debug(s"${rendicontazioniFiltered.size} reporting found on db")

      rendicontazioniNexi <- if( callNexiToo ) {
        (for {
          _ <- Future.successful(())

          response <- HttpSoapServiceManagement.createRequestSoapAction(
            req.sessionId,
            req.testCaseId,
            req.primitive,
            SoapReceiverType.NEXI.toString,
            req.payload,
            actorProps,
            re.get
          )

          ncefrResponse <- if( response.payload.isDefined ) {
            parseResponseNexi(response.payload.get) match {
              case Success(v) => Future.successful(v)
              case Failure(e) => {
                log.error(e, e.getMessage)
                Future.successful(None)
              }
            }
          } else {
            Future.successful(None)
          }

          flussiResponseNexi <- if( ncefrResponse.isDefined ) {
            for {
              _ <- Future.successful(())
              _ = ncefrResponse.get.fault.map(v=> log.warn(s"Outcome by ${SoapReceiverType.NEXI.toString}: faultCode=[${v.faultCode}, faultString=[${v.faultString}], description=[${v.description}]"))
            } yield ncefrResponse.get.elencoFlussiRendicontazione
          } else {
            Future.successful(None)
          }

          tipiIdRendicontazioni = if ( flussiResponseNexi.isDefined) {
            val flussiTrovati = flussiResponseNexi.get.idRendicontazione
            log.debug(s"Returned ${flussiTrovati.size} reportings by ${SoapReceiverType.NEXI.toString}")
            flussiTrovati
          } else {
            log.info(s"No reportings returned by ${SoapReceiverType.NEXI.toString}")
            Nil
          }
        } yield tipiIdRendicontazioni).recoverWith({
          case e: Throwable => {
            log.error(e, s"Error calling ${req.primitive} Nexi")
            Future.successful(Nil)
          }
        })
      } else {
        Future.successful(Nil)
      }

      elencoFlussiRendicontazione <- createTipoElencoFlussiRendicontazione(rendicontazioniFiltered.toSeq, rendicontazioniNexi)

      ncrfrResponse = NodoChiediElencoFlussiRendicontazioneRisposta(None, elencoFlussiRendicontazione)

      _ = log.debug(FdrLogConstant.logGeneraPayload(RESPONSE_NAME))
      env <- Future.fromTry(wrapInBundleMessage(ncrfrResponse))
    } yield SoapResponse(soapRequest.sessionId, Some(env), StatusCodes.OK.intValue, re, soapRequest.testCaseId, None)

    pipeline.recover({
      case e: DigitPaException =>
        log.warn(e, FdrLogConstant.logGeneraPayload(s"negative $RESPONSE_NAME, [${e.getMessage}]"))
        errorHandler(req.sessionId, req.testCaseId, outputXsdValid, e, re)
      case e: Throwable =>
        log.warn(e, FdrLogConstant.logGeneraPayload(s"negative $RESPONSE_NAME, [${e.getMessage}]"))
        errorHandler(req.sessionId, req.testCaseId, outputXsdValid, DigitPaErrorCodes.PPT_SYSTEM_ERROR, re)
    }) map (sr => {
      callTrace(traceInterfaceRequest, reActor, soapRequest, re.get, soapRequest.reExtra)
      logEndProcess(sr)
      replyTo ! sr
      complete()
    })
  }

  override def actorError(e: DigitPaException): Unit = {
    actorError(req, replyTo, ddataMap, e, re)
  }

  private def callTrace(callback: (ActorRef, SoapRequest, Re, ReExtra) => Unit,
                        reActor: ActorRef, soapRequest: SoapRequest, re: Re,
                        reExtra: ReExtra): Unit = {
    Future {
      callback(reActor, soapRequest, re, reExtra)
    }.recover {
      case e: Throwable =>
        log.error(e, s"Execution error in ${callback.getClass.getSimpleName}")
    }
  }

  private def parseInput(br: SoapRequest): Try[NodoChiediElencoFlussiRendicontazione] = {
    (for {
      _ <- XsdValid.checkOnly(br.payload, XmlEnum.NODO_CHIEDI_ELENCO_FLUSSI_RENDICONTAZIONE_NODOPERPA, inputXsdValid)
      body <- XmlEnum.str2nodoChiediElencoFlussiRendicontazione_nodoperpa(br.payload)
    } yield body) recoverWith { case e =>
      log.error(e, e.getMessage)
      Failure(exception.DigitPaException(e.getMessage, DigitPaErrorCodes.PPT_SINTASSI_EXTRAXSD))
    }
  }

  private def parseResponseNexi(payloadResponse: String): Try[Option[NodoChiediElencoFlussiRendicontazioneRisposta]] = {
    log.debug(FdrLogConstant.logSintattico(s"${SoapReceiverType.NEXI.toString} $RESPONSE_NAME"))
    (for {
      _ <- XsdValid.checkOnly(payloadResponse, XmlEnum.NODO_CHIEDI_ELENCO_FLUSSI_RENDICONTAZIONE_RISPOSTA_NODOPERPA, inputXsdValid)
      body <- XmlEnum.str2nodoChiediElencoFlussiRendicontazioneResponse_nodoperpa(payloadResponse)
    } yield Some(body)) recoverWith { case e =>
      Failure(e)
    }
  }

  private def createTipoElencoFlussiRendicontazione(rendicontazioni: Seq[(String, LocalDateTime)], rendicontazioniNexi: Seq[Option[TipoIdRendicontazione]]) = {
    log.debug(s"Merge the list of streams by ${SoapReceiverType.NEXI.toString} with ours")
    val tipiIdRendi = (rendicontazioniNexi ++ rendicontazioni.map(rendi => {
      Some(TipoIdRendicontazione(rendi._1, XmlUtil.StringXMLGregorianCalendarDate.format(rendi._2, XsdDatePattern.DATE_TIME)))
    })).distinct
    Future.successful(Some(TipoElencoFlussiRendicontazione(tipiIdRendi.size, tipiIdRendi)))
  }

  private def findRendicontazioni(ncefr: NodoChiediElencoFlussiRendicontazione) = {
    log.debug("Looking for valid reports on db")
    val idjIdIntPA = ddataMap.creditorInstitutionBrokers(ncefr.identificativoIntermediarioPA).brokerCode
    val idStazioniInt = ddataMap.stations.filter(_._2.brokerCode == idjIdIntPA).map(_._2.stationCode).toSeq
    val paStazPa =
      ddataMap.creditorInstitutionStations.filter(spa => idStazioniInt.contains(spa._2.stationCode)).map(_._2.creditorInstitutionCode).toSeq
    val domini = ddataMap.creditorInstitutions
      .filter(p => ncefr.identificativoDominio.forall(d => p._1 == d))
      .filter(pa => {
        paStazPa.contains(pa._2.creditorInstitutionCode)
      })
      .keys
      .toSeq
    repositories.fdrRepository.findRendicontazioni(domini, ncefr.identificativoPSP, dayLimit)
  }

  private def checks(ncefr: NodoChiediElencoFlussiRendicontazione) = {
    val identificativoDominio = ncefr.identificativoDominio
    Future.fromTry({
      for {
        staz <-
          if (identificativoDominio.isDefined) {
            DDataChecks
              .checkPaIntermediarioPaStazione(log, ddataMap, identificativoDominio.get, ncefr.identificativoIntermediarioPA, ncefr.identificativoStazioneIntermediarioPA, None, Some(ncefr.password))
              .map(_._3)
          } else {
            DDataChecks.checkIntermediarioPaStazionePassword(log, ddataMap, ncefr.identificativoIntermediarioPA, ncefr.identificativoStazioneIntermediarioPA, ncefr.password).map(_._2)
          }
        psp <-
          if (ncefr.identificativoPSP.isDefined) {
            DDataChecks.checkPsp(log, ddataMap, ncefr.identificativoPSP.get).map(p => Some(p))
          } else {
            Success(None)
          }
      } yield (staz, psp)
    })
  }

  private def wrapInBundleMessage(ncefrr: NodoChiediElencoFlussiRendicontazioneRisposta) = {
    for {
      respPayload <- XmlEnum.nodoChiediElencoFlussiRendicontazioneRisposta2Str_nodoperpa(ncefrr)
      _ <- XsdValid.checkOnly(respPayload, XmlEnum.NODO_CHIEDI_ELENCO_FLUSSI_RENDICONTAZIONE_RISPOSTA_NODOPERPA, outputXsdValid)
      _ = log.debug("Valid response")
    } yield respPayload
  }
}
