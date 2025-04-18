package eu.sia.pagopa.common.json.schema.drafts

import eu.sia.pagopa.common.json.schema.internal.draft4.{ SchemaReads4, SchemaWrites4 }
import eu.sia.pagopa.common.json.schema.internal.validators.DefaultFormats
import eu.sia.pagopa.common.json.schema.{ JsonSource, SchemaConfigOptions, SchemaFormat, SchemaType, SchemaVersion }

trait Version4 extends SchemaVersion with SchemaReads4 with SchemaWrites4

object Version4 extends Version4 { self =>
  val SchemaUrl = "http://json-schema.org/draft-04/schema#"
  val schemaLocation: String = SchemaUrl
  lazy val Schema: SchemaType =
    JsonSource.schemaFromUrl(self.getClass.getResource("/draft-schemas/json-schema-draft-04.json")).getOrElse(throw new RuntimeException("Could not read schema file json-schema-draft-04.json."))
  val options: SchemaConfigOptions = new SchemaConfigOptions {
    override def supportsExternalReferences: Boolean = true
    override def formats: Map[String, SchemaFormat] = DefaultFormats.formats
  }
  def apply(schemaOptions: SchemaConfigOptions): Version4 = {
    new Version4 {
      val schemaLocation: String = SchemaUrl
      override def options: SchemaConfigOptions = schemaOptions
    }
  }
}
