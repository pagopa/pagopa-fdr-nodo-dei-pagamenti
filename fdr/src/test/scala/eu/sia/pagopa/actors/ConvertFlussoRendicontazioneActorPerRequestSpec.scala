package eu.sia.pagopa.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.{Http, HttpsConnectionContext}
import akka.stream.Materializer
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.{Config, ConfigFactory}
import eu.sia.pagopa.ActorProps
import eu.sia.pagopa.RepositoriesUtil.fdrRepository
import eu.sia.pagopa.common.actor.ActorUtility
import eu.sia.pagopa.common.util.NodoLogger
import eu.sia.pagopa.common.json.model.rendicontazione.{Convert, Flow, FlowPayment, PayStatusEnum, Payment, Receiver, Sender, SenderTypeEnum}
import eu.sia.pagopa.common.message._
import eu.sia.pagopa.common.repo.{DBComponent, Repositories}
import eu.sia.pagopa.common.repo.fdr.FdrRepository
import eu.sia.pagopa.common.repo.fdr.enums.RendicontazioneStatus
import eu.sia.pagopa.common.repo.fdr.model.{BinaryFile, Rendicontazione}
import eu.sia.pagopa.common.util.NodoLogger
import eu.sia.pagopa.rendicontazioni.actor.rest.ConvertFlussoRendicontazioneActorPerRequest
import eu.sia.pagopa.testutil.GzipUtil
import it.pagopa.config.{ConfigDataV1, CreditorInstitution}
import org.mockito.MockitoSugar
import org.mockito.MockitoSugar.mock
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike
import slick.jdbc.{JdbcBackend, JdbcProfile}
import spray.json._

import java.time.{Instant, ZoneId}
import java.util.Base64
import scala.concurrent.{ExecutionContext, Future}



object JsonProtocol extends DefaultJsonProtocol {
  // 1. Integer
  implicit val integerFormat: JsonFormat[Integer] = new JsonFormat[Integer] {
    def write(obj: Integer): JsValue = JsNumber(obj)  // Scala converte automaticamente
    def read(json: JsValue): Integer = json match {
      case JsNumber(num) => Integer.valueOf(num.intValue)  // Corretto: intValue senza parentesi
      case _ => throw DeserializationException("Expected number for Integer")
    }
  }

  // 2. BigDecimal
  implicit val javaBigDecimalFormat: JsonFormat[java.math.BigDecimal] =
    new JsonFormat[java.math.BigDecimal] {
      def write(obj: java.math.BigDecimal): JsValue = JsNumber(obj.doubleValue)
      def read(json: JsValue): java.math.BigDecimal = json match {
        case JsNumber(num) => java.math.BigDecimal.valueOf(num.doubleValue)
        case _ => throw DeserializationException("Expected number for BigDecimal")
      }
    }

  // 3. Enum
  implicit val senderTypeEnumFormat: JsonFormat[SenderTypeEnum.Value] = enumFormat(SenderTypeEnum)
  implicit val payStatusEnumFormat: JsonFormat[PayStatusEnum.Value] = enumFormat(PayStatusEnum)

  // 4. Class
  implicit val paymentFormat: JsonFormat[Payment] = jsonFormat7(Payment.apply _)
  implicit val flowPaymentFormat: JsonFormat[FlowPayment] = jsonFormat7(FlowPayment.apply _)
  implicit val senderFormat: JsonFormat[Sender] = jsonFormat7(Sender.apply _)
  implicit val receiverFormat: JsonFormat[Receiver] = jsonFormat3(Receiver.apply _)
  implicit val flowFormat: JsonFormat[Flow] = new JsonFormat[Flow] {
    def write(flow: Flow): JsValue = JsObject(
      "name" -> JsString(flow.name),
      "date" -> JsString(flow.date),
      "revision" -> flow.revision.toJson,
      "status" -> flow.status.toJson,
      "computedTotPayments" -> JsNumber(flow.computedTotPayments),
      "computedSumPayments" -> JsNumber(flow.computedSumPayments),
      "regulationDate" -> JsString(flow.regulationDate),
      "regulation" -> JsString(flow.regulation),
      "sender" -> flow.sender.toJson,
      "receiver" -> flow.receiver.toJson,
      "bicCodePouringBank" -> flow.bicCodePouringBank.toJson,
      "created" -> flow.created.toJson,
      "updated" -> flow.updated.toJson,
      "paymentList" -> flow.paymentList.toJson
    )

    def read(json: JsValue): Flow = json.asJsObject.getFields(
      "name", "date", "revision", "status", "computedTotPayments",
      "computedSumPayments", "regulationDate", "regulation", "sender",
      "receiver", "bicCodePouringBank", "created", "updated", "paymentList"
    ) match {
      case Seq(
      JsString(name), JsString(date), revision, status,
      JsNumber(computedTotPayments), JsNumber(computedSumPayments),
      JsString(regulationDate), JsString(regulation), sender,
      receiver, bicCodePouringBank, created, updated, paymentList
      ) => Flow(
        name, date, revision.convertTo[Option[Int]], status.convertTo[Option[String]],
        Integer.valueOf(computedTotPayments.intValue), computedSumPayments.doubleValue,
        regulationDate, regulation, sender.convertTo[Sender], receiver.convertTo[Receiver],
        bicCodePouringBank.convertTo[Option[String]], created.convertTo[Option[String]],
        updated.convertTo[Option[String]], paymentList.convertTo[Seq[FlowPayment]]
      )
      case _ => throw DeserializationException("Flow expected")
    }
  }
  implicit val convertFormat: JsonFormat[Convert] = jsonFormat2(Convert.apply)

  private def enumFormat[T <: Enumeration](enum: T): JsonFormat[T#Value] =
    new JsonFormat[T#Value] {
      def write(obj: T#Value): JsValue = JsString(obj.toString)
      def read(json: JsValue): T#Value = json match {
        case JsString(txt) => enum.withName(txt)
        case other => throw DeserializationException(s"Expected enum value, got $other")
      }
    }
}

class ConvertFlussoRendicontazioneActorPerRequestSpec
  extends TestKit(ActorSystem("TestSystem", ConfigFactory.load()))
    with ImplicitSender
    with AnyWordSpecLike
    with BeforeAndAfterAll
    with MockitoSugar {

  import JsonProtocol._

  type DDataMap = ConfigDataV1

  implicit val ec: ExecutionContext = ExecutionContext.global

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "ConvertFlussoRendicontazioneActorPerRequest" should {
    "not forward to Nexi when reportingFtpEnabled is false" in {
      // Mocks
      val testProbe = TestProbe()
      val mockRepositories = mockRepositoriesWithEmptyFdr()

      val mockActorProps = ActorProps(
        http = Http(system),
        httpsConnectionContext = mock[HttpsConnectionContext],
        actorMaterializer = Materializer(system),
        actorUtility = mock[ActorUtility],
        routers = Map.empty,
        actorClassId = "test-actor",
        cacertsPath = "",
        ddataMap = mock[ConfigDataV1]
      )

      val actorRef = system.actorOf(
        Props(new ConvertFlussoRendicontazioneActorPerRequest(mockRepositories, mockActorProps) {
          override val ddataMap: DDataMap = ConfigDataV1(
            version = "1.0",
            creditorInstitutions = Map("dominioTest" -> CreditorInstitution(
              creditorInstitutionCode = "dominioTest",
              enabled = true,
              businessName = Some("Test Business"),
              description = Some("Test Description"),
              address = None, // o Some(CreditorInstitutionAddress(...)) se hai questa case class
              pspPayment = false,
              reportingFtp = false,
              reportingZip = false
            )),
            creditorInstitutionBrokers = Map.empty,
            stations = Map.empty,
            creditorInstitutionStations = Map.empty,
            encodings = Map.empty,
            creditorInstitutionEncodings = Map.empty,
            ibans = Map.empty,
            creditorInstitutionInformations = Map.empty,
            psps = Map.empty,
            pspBrokers = Map.empty,
            paymentTypes = Map.empty,
            pspChannelPaymentTypes = Map.empty,
            plugins = Map.empty,
            pspInformationTemplates = Map.empty,
            pspInformations = Map.empty,
            channels = Map.empty,
            cdsServices = Map.empty,
            cdsSubjects = Map.empty,
            cdsSubjectServices = Map.empty,
            cdsCategories = Map.empty,
            configurations = Map.empty,
            ftpServers = Map.empty,
            languages = Map.empty,
            gdeConfigurations = Map.empty,
            metadataDict = Map.empty
          )
          override val checkUTF8: Boolean = false
        })
      )

      val jsonFlow = Flow(
        name = "flow123",
        date = "2025-01-01",
        revision = None,
        status = None,
        computedTotPayments = 1,
        computedSumPayments = 1.00,
        regulationDate = "2025-01-02",
        regulation = "reg",
        sender = Sender(
          _type = SenderTypeEnum.ABI_CODE,
          id = "senderId",
          pspId = "psp",
          pspName = "pspName",
          pspBrokerId = "broker",
          channelId = "channel",
          password = Some("pwd")
        ),
        receiver = Receiver("dominioTest", "ReceiverName", "OrganizationName"),
        bicCodePouringBank = Some("bic"),
        created = None,
        updated = None,
        paymentList = Seq(FlowPayment("iuv", "iur", 1, 1.00, "2025-01-01", PayStatusEnum.EXECUTED, 1))
      ).toJson.prettyPrint

      val zipped = GzipUtil.gzip(jsonFlow)
      val base64Payload = Base64.getEncoder.encodeToString(zipped)

      val convert = Convert(payload = base64Payload, encoding = None)
      val convertJson = convert.toJson.compactPrint

      val restRequest = RestRequest(
        sessionId = "testSession",
        payload = Some(convertJson),
        queryParameters = Nil,
        pathParams = Map.empty,
        callRemoteAddress = "localhost",
        primitive = "nodoInviaFlussoRendicontazione",
        timestamp = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime,
        reExtra = ReExtra(),
        testCaseId = None
      )

      actorRef.tell(restRequest, testProbe.ref)

      val response = testProbe.expectMsgType[RestResponse]
      assert(response.statusCode == 200)
    }
  }

  /*private def mockRepositoriesWithEmptyFdr(): Repositories = {
    import slick.jdbc.H2Profile

    val testProfile = H2Profile
    val mockDb = mock[JdbcBackend#DatabaseDef]
    val mockConfig = mock[Config]
    val mockLogger = mock[NodoLogger]

    trait TestDBComponent extends DBComponent {
      override val driver = testProfile
    }

    val mockFdrRepo = new FdrRepository(testProfile, mockDb) with TestDBComponent {
      override def saveRendicontazione(rendicontazione: Rendicontazione)
                                      (implicit log: NodoLogger): Future[Rendicontazione] = {
        Future.successful(rendicontazione.copy(
          stato = RendicontazioneStatus.VALID,
          insertedTimestamp = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime
        ))
      }
    }
    new Repositories(mockConfig, mockLogger) {
      // Sovrascrivi il lazy val per restituire il mock
      override lazy val fdrRepository: FdrRepository = mockFdrRepo
    }
  }*/
  private def mockRepositoriesWithEmptyFdr(): Unit = {

    // Profili finti per test
    val testProfile: JdbcProfile = H2Profile
    val testDb: JdbcBackend#DatabaseDef = mock[JdbcBackend#DatabaseDef]

    // Mock degli altri repository
    val mockBinaryFileRepo = mock[BinaryFileRepository]
    val mockFtpFileRepo = mock[FtpFileRepository]
    val mockSchedulerRepo = mock[SchedulerFireCheckRepository]

    // Implementazione custom del FdrRepository con salvataggio "finto"
    class TestFdrRepository(val driver: JdbcProfile, db: JdbcBackend#DatabaseDef)
      extends FdrRepository(driver, db) with DBComponent {

      override def saveRendicontazione(rendicontazione: Rendicontazione)
                                      (implicit log: NodoLogger): Future[Rendicontazione] = {
        Future.successful(rendicontazione.copy(
          stato = RendicontazioneStatus.VALID,
          insertedTimestamp = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime
        ))
      }
    }

    // Istanza corretta e sicura del repository FDR
    val mockFdrRepo = new TestFdrRepository(testProfile, testDb)

    // Override del metodo createRepository nella spec
    override def createRepository(): FdrRepository = mockFdrRepo
    override def createBinaryFileRepository(): BinaryFileRepository = mockBinaryFileRepo
    override def createFtpFileRepository(): FtpFileRepository = mockFtpFileRepo
    override def createSchedulerFireCheckRepository(): SchedulerFireCheckRepository = mockSchedulerRepo
  }
}