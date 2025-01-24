package eu.sia.pagopa.common.repo.re.model

import play.api.libs.json._
import scalaxbmodel.flussoriversamento.{CtDatiSingoliPagamenti, CtFlussoRiversamento, CtIstitutoMittente, CtIstitutoRicevente, G, Number1u460, Number1u461, StTipoIdentificativoUnivoco, StTipoIdentificativoUnivocoPersG, StVersioneOggetto}

import java.time.LocalDateTime
import javax.xml.datatype.XMLGregorianCalendar



case class Fdr1EventHub(
                         sessionId: String,
                         insertedTimestamp: LocalDateTime,
                         flussoRiversamento: CtFlussoRiversamento,
                         flowId: String,
                         flowDate: XMLGregorianCalendar,
                         creditorInstitution:String,
                         psp:String,
                         brokerPsp:String
                       ) {

  def getSessionId(): String = sessionId
  def getInsertedTimestamp(): LocalDateTime = insertedTimestamp
  def getFlussoRiversamento(): CtFlussoRiversamento = flussoRiversamento
  def getFlowId(): String = flowId
  def getFlowDate(): XMLGregorianCalendar = flowDate
  def getCreditorInstitution(): String = creditorInstitution
  def getPsp(): String = psp
  def getBrokerPsp(): String = brokerPsp


//  def toJson(implicit format: OFormat[Fdr1EventHub]): JsValue = Json.toJson(this)



}

//// Serializzatori impliciti
//object JsonFormats {
//  // Format per LocalDateTime
////  implicit val localDateTimeFormat: Format[LocalDateTime] = Format(
////    Reads.localDateTimeReads("yyyy-MM-dd'T'HH:mm:ss"),
////    Writes.localDateTimeWrites("yyyy-MM-dd'T'HH:mm:ss")
////  )
//
//  // Format per XMLGregorianCalendar
//  implicit val xmlGregorianCalendarFormat: Format[XMLGregorianCalendar] = new Format[XMLGregorianCalendar] {
//    override def writes(calendar: XMLGregorianCalendar): JsValue = JsString(calendar.toString)
//
//    override def reads(json: JsValue): JsResult[XMLGregorianCalendar] =
//      JsError("Deserialization of XMLGregorianCalendar is not supported")
//  }
//
//
//  implicit val ctStSoggettoVersioneOggetto: Format[StVersioneOggetto] = new Format[StVersioneOggetto] {
//    override def writes(obj: StVersioneOggetto): JsValue = JsString(obj.toString)
//
//    override def reads(json: JsValue): JsResult[StVersioneOggetto] = json match {
//      case JsString("1.0") => JsSuccess(Number1u460)
//      case JsString("1.1") => JsSuccess(Number1u461)
//      case JsString(unknown) => JsError(s"Unknown value for StVersioneOggetto: $unknown")
//      case _ => JsError("Invalid type for StVersioneOggetto, expected JsString")
//    }
//  }
//
//  implicit val ctIstitutoMittenteFormat: Format[StTipoIdentificativoUnivoco] = new Format[StTipoIdentificativoUnivoco] {
//    override def writes(obj: StTipoIdentificativoUnivoco): JsValue = JsString(obj.toString)
//
//    override def reads(json: JsValue): JsResult[StTipoIdentificativoUnivoco] = json match {
//      case JsString(value) =>
//        StTipoIdentificativoUnivoco.values.find(_.toString == value) match {
//          case Some(v) => JsSuccess(v)
//          case None => JsError(s"Tipo sconosciuto per StTipoIdentificativoUnivoco: $value")
//        }
//      case _ => JsError("Formato JSON non valido per StTipoIdentificativoUnivoco")
//    }
//  }
//
//  implicit val stTipoIdentificativoUnivocoPersGformat: Format[StTipoIdentificativoUnivocoPersG] = new Format[StTipoIdentificativoUnivocoPersG] {
//    override def writes(o: StTipoIdentificativoUnivocoPersG): JsValue = JsString(o.toString)
//
//    override def reads(json: JsValue): JsResult[StTipoIdentificativoUnivocoPersG] = json match {
//      case JsString(value) =>
//        values.find(_.toString == value) match {
//          case Some(v) => JsSuccess(v)
//          case None => JsError(s"Tipo sconosciuto per StTipoIdentificativoUnivocoPersG: $value")
//        }
//      case _ => JsError("Formato JSON non valido per StTipoIdentificativoUnivocoPersG")
//    }
//
//    lazy val values: Seq[StTipoIdentificativoUnivocoPersG] = Seq(G)
//  }
//
//  implicit val ctIstitutoRiceventeFormat: OFormat[CtIstitutoRicevente] = Json.format[CtIstitutoRicevente]
//  implicit val ctDatiSingoliPagamentiFormat: Format[CtDatiSingoliPagamenti] = Json.format[CtDatiSingoliPagamenti]
//
//
//  // Format per CtFlussoRiversamento
//  implicit val ctFlussoRiversamentoFormat: OFormat[CtFlussoRiversamento] = Json.format[CtFlussoRiversamento]
//
//  // Format per Fdr1EventHub
//  implicit val fdr1EventHubFormat: OFormat[Fdr1EventHub] = Json.format[Fdr1EventHub]
//}
