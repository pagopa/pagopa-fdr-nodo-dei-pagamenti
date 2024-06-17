package eu.sia.pagopa.common.util

import akka.http.scaladsl.model.{ContentType => _}
import eu.sia.pagopa.common.json.model.{FlussiRendicontazioneEvent, IUVRendicontatiEvent}
import scalaxbmodel.flussoriversamento.CtFlussoRiversamento
import scalaxbmodel.nodoperpsp.NodoInviaFlussoRendicontazione

import java.time.LocalDateTime

object EventUtil {

  def createIUVRendicontatiEvent(
                             sessionId: String,
                             nifr: NodoInviaFlussoRendicontazione,
                             flussoRiversamento:CtFlussoRiversamento,
                             insertedTimestamp: LocalDateTime)={
    flussoRiversamento.datiSingoliPagamenti.map(dsp => {
      IUVRendicontatiEvent(
        dsp.identificativoUnivocoVersamento,
        flussoRiversamento.identificativoUnivocoRegolamento,
        dsp.singoloImportoPagato,
        Integer.parseInt(dsp.codiceEsitoSingoloPagamento.toString),
        dsp.dataEsitoSingoloPagamento.toGregorianCalendar.toZonedDateTime.toLocalDateTime,
        dsp.indiceDatiSingoloPagamento.get.toString(),
        nifr.identificativoFlusso,
        nifr.dataOraFlusso.toGregorianCalendar.toZonedDateTime.toLocalDateTime,
        nifr.identificativoDominio,
        nifr.identificativoPSP,
        nifr.identificativoIntermediarioPSP,
        sessionId,
        insertedTimestamp
      )
    })
  }

  def createFlussiRendicontazioneEvent(
                             nifr: NodoInviaFlussoRendicontazione,
                             flussoRiversamento:CtFlussoRiversamento,
                             insertedTimestamp: LocalDateTime)={
    FlussiRendicontazioneEvent(
      nifr.identificativoFlusso,
      nifr.dataOraFlusso.toGregorianCalendar.toZonedDateTime.toLocalDateTime,
      insertedTimestamp,
      flussoRiversamento.dataRegolamento.toGregorianCalendar.toZonedDateTime.toLocalDateTime,
      flussoRiversamento.identificativoUnivocoRegolamento,
      flussoRiversamento.numeroTotalePagamenti.intValue,
      flussoRiversamento.importoTotalePagamenti,
      nifr.identificativoDominio,
      nifr.identificativoPSP,
      nifr.identificativoIntermediarioPSP,
      s"${nifr.identificativoFlusso}${nifr.dataOraFlusso}${insertedTimestamp}",
      flussoRiversamento.datiSingoliPagamenti.map(dsp => dsp.dataEsitoSingoloPagamento.toGregorianCalendar.toZonedDateTime.toLocalDateTime.toString)
    )
  }
}
