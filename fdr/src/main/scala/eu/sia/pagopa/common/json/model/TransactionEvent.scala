package eu.sia.pagopa.common.json.model

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.{JsonInclude, JsonProperty, JsonPropertyOrder}
import eu.sia.pagopa.common.json.model.rendicontazione.Sender
import spray.json.{DefaultJsonProtocol, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}

import java.time.LocalDateTime

class Event()

@JsonPropertyOrder(Array("IUV", "IUR", "IMPORTO", "COD_ESITO", "DATA_ESITO_SINGOLO_PAGAMENTO", "IDSP", "ID_FLUSSO", "DATA_ORA_FLUSSO", "ID_DOMINIO", "PSP", "INT_PSP", "UNIQUE_ID", "INSERTED_TIMESTAMP"))
@JsonInclude(Include.NON_ABSENT)
case class IUVRendicontatiEvent(
                             @JsonProperty("IUV") iuv: String,
                             @JsonProperty("IUR") iur: String,
                             @JsonProperty("IMPORTO") importo: BigDecimal,
                             @JsonProperty("COD_ESITO") codEsito: Integer,
                             @JsonProperty("DATA_ESITO_SINGOLO_PAGAMENTO") dataEsitoSingoloPagamento: LocalDateTime,
                             @JsonProperty("IDSP") idsp: String,
                             @JsonProperty("ID_FLUSSO") idFlusso: String,
                             @JsonProperty("DATA_ORA_FLUSSO") dataOraFlusso: LocalDateTime,
                             @JsonProperty("ID_DOMINIO") idDominio: String,
                             @JsonProperty("PSP") psp: String,
                             @JsonProperty("INT_PSP") intPsp: String,
                             @JsonProperty("UNIQUE_ID") uniqueId: String,
                             @JsonProperty("INSERTED_TIMESTAMP") insertedTimestamp: LocalDateTime
) extends Event with DefaultJsonProtocol {

}

@JsonPropertyOrder(Array("ID_FLUSSO", "DATA_ORA_FLUSSO", "INSERTED_TIMESTAMP", "DATA_REGOLAMENTO", "CAUSALE", "NUM_PAGAMENTI", "SOMMA_VERSATA", "ID_DOMINIO", "PSP", "INT_PSP", "UNIQUE_ID", "ALL_DATES"))
@JsonInclude(Include.NON_ABSENT)
case class FlussiRendicontazioneEvent(
                                       @JsonProperty("ID_FLUSSO") idFlusso: String,
                                       @JsonProperty("DATA_ORA_FLUSSO") dataOraFlusso: LocalDateTime,
                                       @JsonProperty("INSERTED_TIMESTAMP") insertedTimestamp: LocalDateTime,
                                       @JsonProperty("DATA_REGOLAMENTO") dataRegolamento: LocalDateTime,
                                       @JsonProperty("CAUSALE") causale: String,
                                       @JsonProperty("NUM_PAGAMENTI") numPagamenti: Integer,
                                       @JsonProperty("SOMMA_VERSATA") sommaVersata: BigDecimal,
                                       @JsonProperty("ID_DOMINIO") idDominio: String,
                                       @JsonProperty("PSP") psp: String,
                                       @JsonProperty("INT_PSP") intPsp: String,
                                       @JsonProperty("UNIQUE_ID") uniqueId: String,
                                       @JsonProperty("ALL_DATES") allDates: Seq[String]
                           ) extends Event {

}
