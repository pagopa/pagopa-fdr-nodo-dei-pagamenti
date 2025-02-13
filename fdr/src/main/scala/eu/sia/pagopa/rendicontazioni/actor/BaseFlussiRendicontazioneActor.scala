package eu.sia.pagopa.rendicontazioni.actor

import eu.sia.pagopa.Main.ConfigData
import eu.sia.pagopa.common.actor.NodoLogging
import eu.sia.pagopa.common.exception
import eu.sia.pagopa.common.exception.{DigitPaErrorCodes, DigitPaException}
import eu.sia.pagopa.common.repo.fdr.FdrRepository
import eu.sia.pagopa.common.repo.fdr.enums.RendicontazioneStatus
import eu.sia.pagopa.common.repo.fdr.model.{BinaryFile, Rendicontazione}
import eu.sia.pagopa.common.util._
import eu.sia.pagopa.common.util.xml.XsdValid
import eu.sia.pagopa.commonxml.XmlEnum
import eu.sia.pagopa.rendicontazioni.util.CheckRendicontazioni
import scalaxbmodel.flussoriversamento.CtFlussoRiversamento
import scalaxbmodel.nodoperpsp.NodoInviaFlussoRendicontazione

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait BaseFlussiRendicontazioneActor { this: NodoLogging =>

  def validateRendicontazione(
                               nifr: NodoInviaFlussoRendicontazione,
                               checkUTF8: Boolean,
                               inputXsdValid: Boolean,
                               fdrRepository: FdrRepository
                             )(implicit log: NodoLogger, ec: ExecutionContext) = {
    log.debug("Check 'Flusso riversamento' element validity")

    for {
      content <- Future.fromTry(StringUtils.getStringDecodedByString(nifr.xmlRendicontazione.toString, checkUTF8))
      r <- XsdValid.checkOnly(content, XmlEnum.FLUSSO_RIVERSAMENTO_FLUSSORIVERSAMENTO, inputXsdValid) match {
        case Success(_) =>
          log.debug("Saving valid report")

          val flussoRiversamento =
            XmlEnum.str2FlussoRiversamento_flussoriversamento(content).getOrElse(throw exception.DigitPaException(DigitPaErrorCodes.PPT_SINTASSI_XSD))

          if (flussoRiversamento.identificativoFlusso != nifr.identificativoFlusso) {
            throw exception.DigitPaException("Il campo [identificativoFlusso] non è uguale al campo dentro xml flusso riversamento [identificativoFlusso]", DigitPaErrorCodes.PPT_SEMANTICA)
          }

          val dataOraFlussoFlussoRiversamento = flussoRiversamento.dataOraFlusso.toGregorianCalendar.toZonedDateTime.toLocalDateTime
          val dataOraFlusso = nifr.dataOraFlusso.toGregorianCalendar.toZonedDateTime.toLocalDateTime

          if (
            dataOraFlussoFlussoRiversamento.getYear != dataOraFlusso.getYear ||
              dataOraFlussoFlussoRiversamento.getMonth != dataOraFlusso.getMonth ||
              dataOraFlussoFlussoRiversamento.getDayOfMonth != dataOraFlusso.getDayOfMonth ||
              dataOraFlussoFlussoRiversamento.getHour != dataOraFlusso.getHour ||
              dataOraFlussoFlussoRiversamento.getMinute != dataOraFlusso.getMinute ||
              dataOraFlussoFlussoRiversamento.getSecond != dataOraFlusso.getSecond
          ) {
            throw exception.DigitPaException("Il campo [dataOraFlusso] non è uguale al campo dentro xml flusso riversamento [dataOraFlusso]", DigitPaErrorCodes.PPT_SEMANTICA)
          }
          Future.successful(flussoRiversamento, content)

        case Failure(e) =>
          log.warn(e, "Invalid spill stream 'Flusso riversamento' element")
          val rendi = Rendicontazione(
            0,
            RendicontazioneStatus.INVALID,
            0,
            nifr.identificativoPSP,
            Some(nifr.identificativoIntermediarioPSP),
            Some(nifr.identificativoCanale),
            nifr.identificativoDominio,
            nifr.identificativoFlusso,
            LocalDateTime.parse(nifr.dataOraFlusso.toString, DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault())),
            None,
            None,
            Util.now()
          )
          fdrRepository
            .save(rendi)
            .recoverWith({ case e1 =>
              Future.failed(exception.DigitPaException(DigitPaErrorCodes.PPT_SYSTEM_ERROR, e1))
            })
            .flatMap(_ => {
              Future.failed(exception.DigitPaException(e.getMessage, DigitPaErrorCodes.PPT_SINTASSI_XSD, e))
            })
      }
    } yield r
  }

  def saveRendicontazione(
                          identificativoFlusso: String,
                          identificativoPSP: String,
                          identificativoIntermediarioPSP: String,
                          identificativoCanale: String,
                          identificativoDominio: String,
                          dataOraFlusso: javax.xml.datatype.XMLGregorianCalendar,
                          xmlRendicontazione: scalaxb.Base64Binary,
                          checkUTF8: Boolean,
                          flussoRiversamento: CtFlussoRiversamento,
                          fdrRepository: FdrRepository)(implicit log: NodoLogger, ec: ExecutionContext) = {

    for {
      r <- {
        val rendi = Rendicontazione(
          0,
          RendicontazioneStatus.VALID,
          0,
          identificativoPSP,
          Some(identificativoIntermediarioPSP),
          Some(identificativoCanale),
          identificativoDominio,
          identificativoFlusso,
          LocalDateTime.parse(dataOraFlusso.toString, DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault())),
          None,
          None,
          Util.now()
        )
        val content = StringUtils.getStringDecodedByString(xmlRendicontazione.toString, checkUTF8) match {
          case Success(c) => c
          case Failure(e) => throw new DigitPaException("Errore decodifica rendicontazione", DigitPaErrorCodes.PPT_SYSTEM_ERROR, e)
        }
        val bf = BinaryFile(0, xmlRendicontazione.length, Some(Util.gzipContent(content.getBytes)), None)
        fdrRepository
          .saveRendicontazioneAndBinaryFile(rendi, bf)
          .recoverWith({ case e =>
            Future.failed(exception.DigitPaException("Errore salvataggio rendicontazione", DigitPaErrorCodes.PPT_SYSTEM_ERROR, e))
          })
          .flatMap(data => {
            Future.successful((Constant.OK, data._1, None, flussoRiversamento))
          })
      }
    } yield r
  }

  def checks(ddataMap: ConfigData, nodoInviaFlussoRendicontazione: NodoInviaFlussoRendicontazione, checkPassword: Boolean, actorClassId: String)(implicit log: NodoLogger) = {
    log.info(FdrLogConstant.logSemantico(actorClassId) + " psp, broker, channel, password, ci")
    val paaa = for {
      (psp, canale) <- DDataChecks
        .checkPspIntermediarioPspCanale(
          log,
          ddataMap,
          Some(nodoInviaFlussoRendicontazione.identificativoPSP),
          nodoInviaFlussoRendicontazione.identificativoIntermediarioPSP,
          Some(nodoInviaFlussoRendicontazione.identificativoCanale),
          Some(nodoInviaFlussoRendicontazione.password),
          checkPassword
        )
        .map(pc => pc._1 -> pc._3)
      pa <- DDataChecks.checkPA(log, ddataMap, nodoInviaFlussoRendicontazione.identificativoDominio)
    } yield (pa, psp, canale)

    paaa.recoverWith({
      case ex: DigitPaException =>
        Failure(ex)
      case ex@_ =>
        Failure(exception.DigitPaException(ex.getMessage, DigitPaErrorCodes.PPT_SINTASSI_XSD, ex))
    })
  }

  def checkFormatoIdFlussoRendicontazione(identificativoFlusso: String, idPsp: String, actorClassId: String)(implicit log: NodoLogger) = {
    log.info(FdrLogConstant.logSemantico(actorClassId) + " checkFormatoIdFlussoRendicontazione")
    (for {
      _ <- CheckRendicontazioni.checkFormatoIdFlussoRendicontazione(identificativoFlusso, idPsp)
    } yield ()).recoverWith({
      case ex: DigitPaException =>
        Failure(ex)
      case ex@_ =>
        Failure(exception.DigitPaException(ex.getMessage, DigitPaErrorCodes.PPT_SINTASSI_XSD, ex))
    })
  }

}
