package eu.sia.pagopa

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestActorRef
import com.azure.storage.blob.{BlobAsyncClient, BlobClientBuilder}
import com.azure.storage.blob.options.BlobParallelUploadOptions
import com.typesafe.config.ConfigFactory
import eu.sia.pagopa.common.json.model.FdREventToHistory
import eu.sia.pagopa.common.repo.Repositories
import eu.sia.pagopa.common.util.Constant
import eu.sia.pagopa.config.actor.FdRMetadataActor
import org.mockito.{ArgumentCaptor, MockedConstruction, Mockito}
import org.mockito.ArgumentMatchers.{any, anyString}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import reactor.core.publisher.Mono
import scalaxbmodel.nodoperpsp.NodoInviaFlussoRendicontazione

import java.time.{Instant, LocalDateTime}
import java.time.temporal.ChronoUnit
import scala.jdk.CollectionConverters._

class FdRMetadataActorUnitTests
  extends AnyWordSpec
    with Matchers
    with BeforeAndAfterAll {

  private implicit val system: ActorSystem = ActorSystem(
    "FdRMetadataActorTest",
    ConfigFactory.parseString("""
      azure-storage-blob {
        connection-string = "fake"
        fdr1-flows-container-name = "fdr1-flows"
      }
    """).withFallback(ConfigFactory.load())
  )

  private val insertedTimestamp =
    LocalDateTime.parse("2026-11-24T10:00:00")

  private def metadata(elaborate: Boolean): Map[String, String] =
    Map(
      "elaborate" -> elaborate.toString,
      "sessionId" -> "test-session-id",
      "insertedTimestamp" -> insertedTimestamp.toString,
      "serviceIdentifier" -> Constant.FDR_VERSION
    )

  private def captureUploadOptions(
                                    elaborate: Boolean
                                  ): BlobParallelUploadOptions = {

    val repositories =
      Mockito.mock(classOf[Repositories])

    val actorProps =
      Mockito.mock(classOf[ActorProps])

    val nifr =
      Mockito.mock(classOf[NodoInviaFlussoRendicontazione])

    Mockito.when(nifr.identificativoFlusso)
      .thenReturn("test-flow-id")

    val event = FdREventToHistory(
      sessionId = "test-session-id",
      nifr = nifr,
      soapRequest = "<xml>test payload</xml>",
      insertedTimestamp = insertedTimestamp,
      elaborate = elaborate,
      retry = 0
    )

    val blobClient =
      Mockito.mock(classOf[BlobAsyncClient])

    Mockito.when(blobClient.getAccountName)
      .thenReturn("test-account")

    Mockito.when(blobClient.getContainerName)
      .thenReturn("fdr1-flows")

    Mockito.when(
      blobClient.uploadWithResponse(
        any(classOf[BlobParallelUploadOptions])
      )
    ).thenReturn(Mono.empty())

    val construction = Mockito.mockConstruction(
      classOf[BlobClientBuilder],
      new MockedConstruction.MockInitializer[BlobClientBuilder] {

        override def prepare(
                              builder: BlobClientBuilder,
                              context: MockedConstruction.Context
                            ): Unit = {

          Mockito.when(builder.connectionString(anyString()))
            .thenReturn(builder)

          Mockito.when(builder.blobName(anyString()))
            .thenReturn(builder)

          Mockito.when(builder.containerName(anyString()))
            .thenReturn(builder)

          Mockito.when(builder.buildAsyncClient())
            .thenReturn(blobClient)
        }
      }
    )

    try {
      val actorRef = TestActorRef[FdRMetadataActor](
        Props(new FdRMetadataActor(repositories, actorProps))
      )

      try {
        val actor = actorRef.underlyingActor

        val method = classOf[FdRMetadataActor]
          .getDeclaredMethod(
            "saveBlob",
            classOf[FdREventToHistory],
            classOf[ActorSystem]
          )

        method.setAccessible(true)
        method.invoke(actor, event, system)

        val captor = ArgumentCaptor.forClass(
          classOf[BlobParallelUploadOptions]
        )

        Mockito.verify(blobClient)
          .uploadWithResponse(captor.capture())

        captor.getValue

      } finally {
        system.stop(actorRef)
      }

    } finally {
      construction.close()
    }
  }

  "FdRMetadataActor.saveBlob" must {

    "set processing tags when elaborate is true" in {

      val options = captureUploadOptions(true)
      val tags = options.getTags.asScala

      tags.size shouldBe 3
      tags("processingStatus") shouldBe "PENDING"
      tags("processingAttempt") shouldBe "0"
      tags.contains("processingUpdatedAt") shouldBe true
    }

    "not set processing tags when elaborate is false" in {

      val options = captureUploadOptions(false)

      assert(
        options.getTags == null ||
          options.getTags.isEmpty
      )
    }

    "preserve metadata when elaborate is true" in {

      val options = captureUploadOptions(true)

      options.getMetadata.asScala.toMap shouldBe metadata(true)
    }

    "preserve metadata when elaborate is false" in {

      val options = captureUploadOptions(false)

      options.getMetadata.asScala.toMap shouldBe metadata(false)
    }

    "prevent blob overwrite" in {

      Seq(true, false).foreach { elaborate =>

        val options = captureUploadOptions(elaborate)

        options.getRequestConditions.getIfNoneMatch shouldBe "*"
      }
    }

    "set processingUpdatedAt with seconds precision in UTC" in {

      val before =
        Instant.now().truncatedTo(ChronoUnit.SECONDS)

      val options = captureUploadOptions(true)

      val after =
        Instant.now().truncatedTo(ChronoUnit.SECONDS)

      val timestampValue =
        options.getTags.get("processingUpdatedAt")

      val timestamp = Instant.parse(timestampValue)

      timestamp.getNano shouldBe 0
      timestamp.isBefore(before) shouldBe false
      timestamp.isAfter(after) shouldBe false
      timestampValue.endsWith("Z") shouldBe true
    }
  }

  override protected def afterAll(): Unit = {
    try {
      akka.testkit.TestKit.shutdownActorSystem(system)
    } finally {
      super.afterAll()
    }
  }
}