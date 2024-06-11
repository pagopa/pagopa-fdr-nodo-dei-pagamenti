package eu.sia.pagopa.common.json.model

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.{JsonInclude, JsonPropertyOrder}
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import eu.sia.pagopa.common.repo.util.YNBoolean
import eu.sia.pagopa.common.repo.util.YNBoolean.YNBoolean

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

object UseCase {
  val nodoInviaRT = "24"
  val nd = "n/d" //not usually used

}

object TransactionEvent {
  val BIZ_EVENT_MODEL_1 = "1"
  val BIZ_EVENT_MODEL_2 = "2"
  val VERSION = "2"
}

class Event()

@JsonPropertyOrder(Array("IUV", "IUV", "IMPORTO", "COD_ESITO", "DATA_ESITO_SINGOLO_PAGAMENTO", "IDSP", "ID_FLUSSO", "DATA_ORA_FLUSSO", "ID_DOMINIO", "PSP", "INT_PSP", "UNIQUE_ID", "INSERTED_TIMESTAMP"))
@JsonInclude(Include.NON_ABSENT)
case class IUVRendicontatiEvent(
                             iuv: String,
                             iur: String,
                             importo: BigDecimal,
                             codEsito: Integer,
                             dataEsitoSingoloPagamento: LocalDateTime,
                             idPsp: String,
                             idFlusso: String,
                             dataOraFlusso: LocalDateTime,
                             idDominio: String,
                             psp: String,
                             intPsp: String,
                             uniqueId: String,
                             insertedTimestamp: LocalDateTime
) extends Event {

}

@JsonPropertyOrder(Array("ID_FLUSSO", "DATA_ORA_FLUSSO", "INSERTED_TIMESTAMP", "DATA_REGOLAMENTO", "CAUSALE", "NUM_PAGAMENTI", "SOMMA_VERSATA", "ID_DOMINIO", "PSP", "INT_PSP", "UNIQUE_ID", "ALL_DATES"))
@JsonInclude(Include.NON_ABSENT)
case class FlussiRendicontazioneEvent(
                                  idFlusso: String,
                                  dataOraFlusso: LocalDateTime,
                                  insertedTimestamp: LocalDateTime,
                                  dataRegolamento: LocalDateTime,
                                  causale: String,
                                  numPagamenti: Integer,
                                  sommaVersata: BigDecimal,
                                  idDominio: String,
                                  psp: String,
                                  intPsp: String,
                                  uniqueId: String,
                                  allDates: String
                           ) extends Event {

}
