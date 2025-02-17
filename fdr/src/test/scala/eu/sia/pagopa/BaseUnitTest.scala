package eu.sia.pagopa

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.Logging
import akka.testkit.{ImplicitSender, TestKit}
import com.azure.core.util.BinaryData
import com.typesafe.config.{Config, ConfigFactory}
import eu.sia.pagopa.Main.ConfigData
import eu.sia.pagopa.common.message._
import eu.sia.pagopa.common.repo.fdr.FdrRepository
import eu.sia.pagopa.common.repo.{DBComponent, Repositories}
import eu.sia.pagopa.common.util.xml.XmlUtil
import eu.sia.pagopa.common.util._
import eu.sia.pagopa.commonxml.XmlEnum
import eu.sia.pagopa.rendicontazioni.actor.rest.NodoInviaFlussoRendicontazioneFTPActorPerRequest
import eu.sia.pagopa.rendicontazioni.actor.soap.{NodoChiediElencoFlussiRendicontazioneActorPerRequest, NodoChiediFlussoRendicontazioneActorPerRequest, NodoInviaFlussoRendicontazioneActor}
import eu.sia.pagopa.testutil._
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.FileSystemResourceAccessor
import liquibase.{Contexts, Liquibase}
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{Assertion, BeforeAndAfterAll}
import scalaxbmodel.nodoperpa.{NodoChiediElencoFlussiRendicontazioneRisposta, NodoChiediFlussoRendicontazioneRisposta}
import scalaxbmodel.nodoperpsp.NodoInviaFlussoRendicontazioneRisposta
import slick.dbio.{DBIO, DBIOAction, Streaming}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.{H2Profile, JdbcBackend}
import slick.sql.SqlStreamingAction
import slick.util.AsyncExecutor

import java.io.File
import java.time.format.DateTimeFormatter
import java.util.UUID
import scala.concurrent.duration.{Duration, DurationInt, FiniteDuration}
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.util.Try

object RepositoriesUtil {
  var fdrRepository: FdrRepository = _

  def getFdrRepository(implicit ec: ExecutionContext): FdrRepository = {
    if (fdrRepository != null) fdrRepository
    else {
      fdrRepository = FdrRepository(H2Profile, DBUtils.initDB("fdr"))
      fdrRepository
    }
  }
}

//@org.scalatest.Ignore
abstract class BaseUnitTest()
    extends TestKit({
      val config =
        """
        scope = test
        forwarder {
            subscriptionKey=key
        }
        blobstorage-dispatcher {
            type = Dispatcher
            executor = "thread-pool-executor"
            thread-pool-executor {
              fixed-pool-size = 16
            }
            throughput = 1
          }
        eventhub-dispatcher {
            type = Dispatcher
            executor = "thread-pool-executor"
            thread-pool-executor {
               fixed-pool-size = 16
            }
            throughput = 1
        }
        azure-hub-event {
          azure-sdk-client {
            re-event {
              client-timeoput-ms = 5000
              event-hub-name = "fdr-re"
              connection-string = "fake"
            }
            blob-re {
              enabled  = false
              container-name = "payload"
              connection-string = "fake"
            }
          }
        }
        azure-storage-blob {
            enabled  = false
            container-name = "xmlsharefile"
            connection-string = "fake"
        }
        config.http.connect-timeout = 1
        bundleTimeoutSeconds = 120
        bundle.checkUTF8 = false
        routing.useMetrics = false
        akka {
          loggers = ["akka.event.slf4j.Slf4jLogger"]
          loglevel = "INFO"
          logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
        }
        nexi {
            nodoChiediElencoFlussiRendicontazione {
              url="http://localhost:8080/webservices/input"
            }
            nodoChiediFlussoRendicontazione {
              url="http://localhost:8080/webservices/input"
            }
            timeoutSeconds=60
        }
        fdr{
            internalGetWithRevision {
                url="http://localhost:8080/fdr/service-internal/v1/internal/organizations/ndp/fdrs/{fdr}/revisions/{revision}/psps/{pspId}"
                method="GET"
            }
            internalGetFdrPayment {
                url="http://localhost:8080/fdr/service-internal/v1/internal/organizations/ndp/fdrs/{fdr}/revisions/{revision}/psps/{pspId}/payments"
                method="GET"
            }
            subscriptionKey=""
            timeoutSeconds=60
        }
        callNexiToo=true
        limitjobs = true
        config.ftp.connect-timeout = "1000"
        slick.dbs.default.db.numThreads=20
        slick.dbs.default.db.maxConnections=20
        configScheduleMinutes=1
        coordinatedShutdown=true
        waitAsyncProcesses=true
        reBufferSize=1
        reFlushIntervalSeconds=1
    """
      ActorSystem("testSystem", ConfigFactory.parseString(config))
    })
    with AnyWordSpecLike
    with ImplicitSender
    with should.Matchers
    with BeforeAndAfterAll {

  override def afterAll() {

    Thread.sleep(2000)
    TestKit.shutdownActorSystem(system)
  }

  override def beforeAll() {
    import slick.jdbc.H2Profile.api._
    fdrRepository.db.run(sql"update SCHEDULER_FIRE_CHECK set STATUS = 'WAIT_TO_NEXT_FIRE'".as[Long])
  }

  system.registerOnTermination(() => {
    //    Thread.sleep(5000)
    //    fdrRepository.db.close()
  })

  implicit val log: NodoLogger = new NodoLogger(Logging(system, getClass.getCanonicalName))
  implicit val ec: ExecutionContext = system.dispatcher

  val fdrRepository: FdrRepository = RepositoriesUtil.getFdrRepository

  val actorUtility = new ActorUtilityTest()
  val reFunction = (a: ReRequest, b: NodoLogger, c: ConfigData) => {
    Future.successful(())
  }
  val containerBlobFunction = (a: String, m: Map[String, String], b: BinaryData, c: NodoLogger) => {
    Future.successful(())
  }

  val certPath = s"${new File(".").getCanonicalPath}/localresources/cacerts"

  class RepositoriesTest(override val config: Config, override val log: NodoLogger) extends Repositories(config, log) {
    override lazy val fdrRepository: FdrRepository = RepositoriesUtil.getFdrRepository
  }

  val repositories = new RepositoriesTest(system.settings.config, log)

//  val props = ActorProps(null, null, null, actorUtility, Map(), containerBlobFunction, containerBlobFunction, "", certPath, TestItems.ddataMap)
  val props = ActorProps(null, null, null, actorUtility, Map(), "", certPath, TestItems.ddataMap)

  val mockActor = system.actorOf(Props.create(classOf[MockActor]), s"mock")

  val singletesttimeout: FiniteDuration = 1000.seconds

  def payload(testfile: String): String = {
    SpecsUtils.loadTestXML(s"$testfile.xml")
  }

  def initDB(schema: String, folder: String, additionalContexts: Seq[String] = Seq()): JdbcBackend.DatabaseDef = {

    val path = System.getProperty("user.dir")
    val scriptpath = s"$path/liquibase/changelog/$folder/"
    val scriptFolder = new File(scriptpath)
    val changelogMaster = s"./db.changelog-master.xml"

    val db = Database.forURL(
      s"jdbc:h2:$path/target/$schema;DB_CLOSE_ON_EXIT=FALSE;AUTO_SERVER=TRUE;INIT=CREATE SCHEMA IF NOT EXISTS $schema\\;SET SCHEMA $schema;",
      driver = "org.h2.Driver",
      executor = AsyncExecutor("test1", minThreads = 10, queueSize = 1000, maxConnections = 10, maxThreads = 10)
    )
    val database =
      DatabaseFactory.getInstance.findCorrectDatabaseImplementation(new JdbcConnection(db.source.createConnection()))
    val resourceAccessorOnline = new FileSystemResourceAccessor(scriptFolder)
    val liqui = new Liquibase(changelogMaster, resourceAccessorOnline, database)
    liqui.update(new Contexts(("default" + additionalContexts.mkString(",", ",", ""))))
    liqui.close()
    db
  }

  def inviaFlussoRendicontazionePayload(
                                         psp: String = TestItems.PSP,
                                         brokerPsp: String = TestItems.intPSP,
                                         channel: String = TestItems.canale,
                                         channelPwd: String = TestItems.canalePwd,
                                         pa: String = TestItems.PA,
                                         idFlussoReq: Option[String] = None,
                                         dateReq: Option[String],
                                         dataOraFlussoBusta: Option[String],
                                         dataOraFlussoAllegato: Option[String],
                                         istitutoMittente: Option[String],
                                         flussoNonValido: Boolean = false,
                                         denominazioneMittente: String
                                       ) = {
    val date = dateReq.getOrElse(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(Util.now()))
    val _dataOraFlussoBusta = dataOraFlussoBusta.getOrElse(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss").format(Util.now()))
    val _dataOraFlussoAllegato = dataOraFlussoAllegato.getOrElse(_dataOraFlussoBusta)
    //    val idflusso = idFlussoReq.getOrElse(s"${date}${psp}-${RandomStringUtils.randomNumeric(9)}")
    val rendi = if (flussoNonValido) {
      "flusso non valido"
    } else {
      SpecsUtils
        .loadTestXML("/requests/rendicontazione.xml")
        .replace("{istitutoMittente}", istitutoMittente.getOrElse(TestItems.PSP))
        .replace("{idflusso}", idFlussoReq.getOrElse(""))
        .replace("{dataoraflussoallegato}", _dataOraFlussoAllegato)
        .replace("{date}", date)
        .replace("{denominazioneMittente}", denominazioneMittente)
    }
    val payloadNew = payload("/requests/nodoInviaFlussoRendicontazione")
      .replace("{psp}", psp)
      .replace("{brokerPsp}", brokerPsp)
      .replace("{channel}", channel)
      .replace("{channelPwd}", channelPwd)
      .replace("{pa}", pa)
      .replace("{idflusso}", idFlussoReq.getOrElse(""))
      .replace("{dataoraflussobusta}", _dataOraFlussoBusta)
      .replace("{rendicontazione}", XmlUtil.StringBase64Binary.encodeBase64ToString(rendi.getBytes))
    payloadNew
  }

  def chiediFlussoRendicontazionePayload(
                                          idFlusso: String,
                                          psp: String = TestItems.PSP,
                                          brokerPsp: String = TestItems.intPSP,
                                          channel: String = TestItems.canale,
                                          channelPwd: String = TestItems.canalePwd,
                                          pa: String = TestItems.PA,
                                          brokerPa: String = TestItems.testIntPA,
                                          station: String = TestItems.stazione,
                                          stationPwd: String = TestItems.stazionePwd
                                        ) = {

    payload("/requests/nodoChiediFlussoRendicontazione")
      .replace("{psp}", psp)
      .replace("{brokerPsp}", brokerPsp)
      .replace("{channel}", channel)
      .replace("{channelPwd}", channelPwd)
      .replace("{pa}", pa)
      .replace("{brokerPa}", brokerPa)
      .replace("{station}", station)
      .replace("{stationPwd}", stationPwd)
      .replace("{idflusso}", idFlusso)
  }

  def chiediElencoFlussiRendicontazionePayload(
                                                psp: String = TestItems.PSP,
                                                brokerPsp: String = TestItems.intPSP,
                                                channel: String = TestItems.canale,
                                                channelPwd: String = TestItems.canalePwd,
                                                pa: String = TestItems.PA,
                                                brokerPa: String = TestItems.testIntPA,
                                                station: String = TestItems.stazione,
                                                stationPwd: String = TestItems.stazionePwd
                                              ) = {

    payload("/requests/nodoChiediElencoFlussiRendicontazione")
      .replace("{psp}", psp)
      .replace("{brokerPsp}", brokerPsp)
      .replace("{channel}", channel)
      .replace("{channelPwd}", channelPwd)
      .replace("{pa}", pa)
      .replace("{brokerPa}", brokerPa)
      .replace("{station}", station)
      .replace("{stationPwd}", stationPwd)
  }

  def inviaFlussoRendicontazione(
                                  testCase: Option[String] = None,
                                  pa: String = TestItems.PA,
                                  idFlusso: Option[String] = None,
                                  date: Option[String] = None,
                                  dataOraFlussoBusta: Option[String] = None,
                                  dataOraFlussoAllegato: Option[String] = None,
                                  istitutoMittente: Option[String] = None,
                                  flussoNonValido: Boolean = false,
                                  responseAssert: NodoInviaFlussoRendicontazioneRisposta => Assertion = (_) => assert(true),
                                  denominazioneMittente: Option[String] = Option("Banca Sella")
                                ): NodoInviaFlussoRendicontazioneRisposta = {
    val act =
      system.actorOf(
        Props.create(classOf[NodoInviaFlussoRendicontazioneActor], repositories, props.copy(actorClassId = "nodoInviaFlussoRendicontazione", routers = Map("ftp-senderRouter" -> mockActor))),
        s"nodoInviaFlussoRendicontazione${Util.now()}"
      )
    val soapres = askActor(
      act,
      SoapRequest(
        UUID.randomUUID().toString,
        inviaFlussoRendicontazionePayload(
          pa = pa,
          idFlussoReq = idFlusso,
          dateReq = date,
          dataOraFlussoBusta = dataOraFlussoBusta,
          dataOraFlussoAllegato = dataOraFlussoAllegato,
          istitutoMittente = istitutoMittente,
          flussoNonValido = flussoNonValido,
          denominazioneMittente = denominazioneMittente.get
        ),
        TestItems.testPDD,
        "nodoInviaFlussoRendicontazione",
        "test",
        Util.now(),
        ReExtra(),
        testCase
      )
    )
    assert(soapres.payload.isDefined)
    val actRes: Try[NodoInviaFlussoRendicontazioneRisposta] = XmlEnum.str2nodoInviaFlussoRendicontazioneResponse_nodoperpsp(soapres.payload.get)
    assert(actRes.isSuccess)
    responseAssert(actRes.get)
    actRes.get
  }

  def chiediFlussoRendicontazione(
                                   idFlusso: String,
                                   testCase: Option[String] = None,
                                   responseAssert: NodoChiediFlussoRendicontazioneRisposta => Assertion = (_) => assert(true)
                                 ): NodoChiediFlussoRendicontazioneRisposta = {
    val act =
      system.actorOf(Props.create(classOf[NodoChiediFlussoRendicontazioneActorPerRequest], repositories, props.copy(actorClassId = "nodoChiediFlussoRendicontazione")), s"nodoChiediFlussoRendicontazione${Util.now()}")
    val soapres =
      askActor(act, SoapRequest(UUID.randomUUID().toString, chiediFlussoRendicontazionePayload(idFlusso), TestItems.testPDD, "nodoChiediFlussoRendicontazione", "test", Util.now(), ReExtra(), testCase))
    assert(soapres.payload.isDefined)
    val actRes: Try[NodoChiediFlussoRendicontazioneRisposta] = XmlEnum.str2nodoChiediFlussoRendicontazioneResponse_nodoperpa(soapres.payload.get)
    assert(actRes.isSuccess)
    responseAssert(actRes.get)
    actRes.get
  }

  def chiediElencoFlussiRendicontazione(
                                         testCase: Option[String] = None,
                                         responseAssert: NodoChiediElencoFlussiRendicontazioneRisposta => Assertion = (_) => assert(true)
                                       ): NodoChiediElencoFlussiRendicontazioneRisposta = {
    val act =
      system.actorOf(
        Props.create(classOf[NodoChiediElencoFlussiRendicontazioneActorPerRequest], repositories, props.copy(actorClassId = "nodoChiediElencoFlussiRendicontazione")),
        s"nodoChiediElencoFlussiRendicontazione${Util.now()}"
      )
    val soapres =
      askActor(act, SoapRequest(UUID.randomUUID().toString, chiediElencoFlussiRendicontazionePayload(), TestItems.testPDD, "nodoChiediElencoFlussiRendicontazione", "test", Util.now(), ReExtra(), testCase))
    assert(soapres.payload.isDefined)
    val actRes: Try[NodoChiediElencoFlussiRendicontazioneRisposta] = XmlEnum.str2nodoChiediElencoFlussiRendicontazioneResponse_nodoperpa(soapres.payload.get)
    assert(actRes.isSuccess)
    responseAssert(actRes.get)
    actRes.get
  }

  def notifyFlussoRendicontazione(
                         payload: Option[String],
                         testCase: Option[String] = Some("OK"),
                         responseAssert: (String, Int) => Assertion = (_, _) => assert(true),
                         newdata: Option[ConfigData] = None
                       ): Future[String] = {
    val p = Promise[Boolean]()
    val notifyFlussoRendicontazione =
      system.actorOf(
        Props.create(classOf[NotifyFlussoRendicontazioneTest], p, repositories, props.copy(actorClassId = "notifyFlussoRendicontazione", ddataMap = newdata.getOrElse(TestDData.ddataMap))),
        s"notifyFlussoRendicontazione${Util.now()}"
      )

    val restResponse = askActor(
      notifyFlussoRendicontazione,
      RestRequest(
        UUID.randomUUID().toString,
        payload,
        Nil,
        Map(),
        TestItems.testPDD,
        "notifyFlussoRendicontazione",
        Util.now(),
        ReExtra(),
        testCase
      )
    )
    assert(restResponse.payload.isDefined)
    responseAssert(restResponse.payload.get, restResponse.statusCode)
    p.future.map(_ => restResponse.payload.get)
  }

  def registerFdrForValidation(
                                   payload: Option[String],
                                   testCase: Option[String] = Some("OK"),
                                   responseAssert: (String, Int) => Assertion = (_, _) => assert(true),
                                   newdata: Option[ConfigData] = None
                                 ): Future[String] = {
    val p = Promise[Boolean]()
    val registerFdrForValidation =
      system.actorOf(
        Props.create(classOf[RegisterFdrForValidationTest], p, repositories, props.copy(actorClassId = "registerFdrForValidation", ddataMap = newdata.getOrElse(TestDData.ddataMap))),
        s"registerFdrForValidation${Util.now()}"
      )

    val restResponse = askActor(
      registerFdrForValidation,
      RestRequest(
        UUID.randomUUID().toString,
        payload,
        Nil,
        Map(),
        TestItems.testPDD,
        "registerFdrForValidation",
        Util.now(),
        ReExtra(),
        testCase
      )
    )
    assert(restResponse.payload.isDefined)
    responseAssert(restResponse.payload.get, restResponse.statusCode)
    p.future.map(_ => restResponse.payload.get)
  }

  def nodoInviaFlussoRendicontazioneFTP(
                                         payload: Option[String],
                                         testCase: Option[String] = None,
                                         responseAssert: (String, Int) => Assertion = (_, _) => assert(true),
                                         newdata: Option[ConfigData] = None
                                ): Future[String] = {

    val p = Promise[Boolean]()
    val nodoInviaFlussoRendicontazioneFTP =
      system.actorOf(
        Props.create(classOf[NodoInviaFlussoRendicontazioneFTPTest], p, repositories, props.copy(actorClassId = "nodoInviaFlussoRendicontazioneFTP", ddataMap = newdata.getOrElse(TestDData.ddataMap))),
        s"nodoInviaFlussoRendicontazioneFTP${Util.now()}"
      )

    val restResponse = askActor(
      nodoInviaFlussoRendicontazioneFTP,
      RestRequest(
        UUID.randomUUID().toString,
        payload,
        Nil,
        Map(),
        TestItems.testPDD,
        "nodoInviaFlussoRendicontazioneFTP",
        Util.now(),
        ReExtra(),
        testCase
      )
    )
    assert(restResponse.payload.isDefined)
    responseAssert(restResponse.payload.get, restResponse.statusCode)
    p.future.map(_ => restResponse.payload.get)
  }

  def getAllRevisionFdr(
                         organizationId: String,
                         fdr: String,
                         testCase: Option[String] = Some("OK"),
                         responseAssert: (String, Int) => Assertion = (_, _) => assert(true),
                         newdata: Option[ConfigData] = None
                       ): Future[String] = {
    val p = Promise[Boolean]()
    val getAllRevisionFdr =
      system.actorOf(
        Props.create(classOf[GetAllRevisionFdrTest], p, repositories, props.copy(actorClassId = "getAllRevisionFdr", ddataMap = newdata.getOrElse(TestDData.ddataMap))),
        s"getAllRevisionFdr${Util.now()}"
      )

    val restResponse = askActor(
      getAllRevisionFdr,
      RestRequest(
        UUID.randomUUID().toString,
        None,
        Nil,
        Map("organizationId" -> organizationId, "fdr" -> fdr),
        TestItems.testPDD,
        "getAllRevisionFdr",
        Util.now(),
        ReExtra(),
        testCase
      )
    )
    assert(restResponse.payload.isDefined)
    responseAssert(restResponse.payload.get, restResponse.statusCode)
    p.future.map(_ => restResponse.payload.get)
  }


  def await[T](f: Future[T]): T = {
    Await.result(f, Duration.Inf)
  }

  def askActor(actor: ActorRef, soapRequest: SoapRequest) = {
    import akka.pattern.ask
    Await.result(actor.ask(soapRequest)(singletesttimeout).mapTo[SoapResponse], Duration.Inf)
  }

  def askActor(actor: ActorRef, restRequest: RestRequest) = {
    import akka.pattern.ask
    Await.result(actor.ask(restRequest)(singletesttimeout).mapTo[RestResponse], Duration.Inf)
  }

  def runQuery[T](repo: DBComponent, action: DBIOAction[Vector[T], Streaming[T], slick.dbio.Effect]) = {
    Await.result(repo.db.run(action), Duration.Inf).head
  }

  def runQueryList[T](repo: DBComponent, action: SqlStreamingAction[Vector[T], T, slick.dbio.Effect]) = {
    Await.result(repo.db.run(action), Duration.Inf)
  }

  def runAction[M](repo: DBComponent, action: DBIO[M]): M = {
    Await.result(repo.runAction(action), Duration.Inf)
  }

}
