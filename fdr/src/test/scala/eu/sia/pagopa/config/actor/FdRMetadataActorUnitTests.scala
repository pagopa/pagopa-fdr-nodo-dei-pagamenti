package eu.sia.pagopa

import com.azure.core.util.BinaryData
import eu.sia.pagopa.config.actor.FdRMetadataActor
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.jdk.CollectionConverters._

class FdRMetadataActorUnitTests extends AnyWordSpec with Matchers {

  private val binaryData = BinaryData.fromString("test payload")

  private def metadata(elaborate: Boolean): Map[String, String] =
    Map(
      "elaborate" -> elaborate.toString,
      "sessionId" -> "test-session-id",
      "insertedTimestamp" -> "2026-11-24T10:00:00",
      "serviceIdentifier" -> "FDR1"
    )

  "FdRMetadataActor.buildUploadOptions" must {

    "set processing tags when elaborate is true" in {
      val options = FdRMetadataActor.buildUploadOptions(
        binaryData,
        metadata(true),
        true
      )

      val tags = options.getTags.asScala

      tags.size shouldBe 3
      tags("processingStatus") shouldBe "PENDING"
      tags("processingAttempt") shouldBe "0"
      tags.contains("processingUpdatedAt") shouldBe true
    }

    "not set processing tags when elaborate is false" in {
      val options = FdRMetadataActor.buildUploadOptions(
        binaryData,
        metadata(false),
        false
      )

      assert(options.getTags == null || options.getTags.isEmpty)
    }

    "preserve metadata when elaborate is true" in {
      val expectedMetadata = metadata(true)

      val options = FdRMetadataActor.buildUploadOptions(
        binaryData,
        expectedMetadata,
        true
      )

      options.getMetadata.asScala.toMap shouldBe expectedMetadata
    }

    "preserve metadata when elaborate is false" in {
      val expectedMetadata = metadata(false)

      val options = FdRMetadataActor.buildUploadOptions(
        binaryData,
        expectedMetadata,
        false
      )

      options.getMetadata.asScala.toMap shouldBe expectedMetadata
    }

    "prevent blob overwrite" in {
      Seq(true, false).foreach { elaborate =>
        val options = FdRMetadataActor.buildUploadOptions(
          binaryData,
          metadata(elaborate),
          elaborate
        )

        options.getRequestConditions.getIfNoneMatch shouldBe "*"
      }
    }

    "set processingUpdatedAt with seconds precision in UTC" in {
      val before = Instant.now().truncatedTo(ChronoUnit.SECONDS)

      val options = FdRMetadataActor.buildUploadOptions(
        binaryData,
        metadata(true),
        true
      )

      val after = Instant.now().truncatedTo(ChronoUnit.SECONDS)

      val timestampValue =
        options.getTags.get("processingUpdatedAt")

      val timestamp = Instant.parse(timestampValue)

      timestamp.getNano shouldBe 0
      timestamp.isBefore(before) shouldBe false
      timestamp.isAfter(after) shouldBe false
      timestampValue.endsWith("Z") shouldBe true
    }
  }
}