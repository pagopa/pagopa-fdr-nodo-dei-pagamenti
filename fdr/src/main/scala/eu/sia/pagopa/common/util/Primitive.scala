package eu.sia.pagopa.common.util

import eu.sia.pagopa.common.actor.PerRequestActor
import eu.sia.pagopa.ftpsender.actor.FtpRetryActorPerRequest
import eu.sia.pagopa.rendicontazioni.actor.rest.{ConvertFlussoRendicontazioneActorPerRequest, GetAllRevisionFdrActorPerRequest, NodoInviaFlussoRendicontazioneFTPActorPerRequest, RegisterFdrForValidationActorPerRequest}
import eu.sia.pagopa.rendicontazioni.actor.soap.{NodoChiediElencoFlussiRendicontazioneActorPerRequest, NodoChiediFlussoRendicontazioneActorPerRequest, NodoInviaFlussoRendicontazioneActor}

object Primitive {

  val soap: Map[String, (String, Boolean => Class[_ <: PerRequestActor])] = Map(
    "nodoChiediFlussoRendicontazione" -> ("Body/_/identificativoStazioneIntermediarioPA", _ => classOf[NodoChiediFlussoRendicontazioneActorPerRequest]),
    "nodoInviaFlussoRendicontazione" -> ("Body/_/identificativoCanale", _ => classOf[NodoInviaFlussoRendicontazioneActor]),
    "nodoChiediElencoFlussiRendicontazione" -> ("Body/_/identificativoStazioneIntermediarioPA", _ => classOf[NodoChiediElencoFlussiRendicontazioneActorPerRequest])
  )

  val rest: Map[String, (String, Boolean => Class[_ <: PerRequestActor])] = Map(
    "convertFlussoRendicontazione" -> ("convert/fdr3", _ => classOf[ConvertFlussoRendicontazioneActorPerRequest]),
    "nodoInviaFlussoRendicontazioneFTP" -> ("nodoInviaFlussoRendicontazioneFTP", _ => classOf[NodoInviaFlussoRendicontazioneFTPActorPerRequest]),
    "registerFdrForValidation" -> ("register-for-validation/fdr", _ => classOf[RegisterFdrForValidationActorPerRequest])
  )

  val restInternal: Map[String, (String, Boolean => Class[_ <: PerRequestActor])] = Map(
    "getAllRevisionFdr" -> ("internal/organizations/:organizationId/fdrs/:fdr", _ => classOf[GetAllRevisionFdrActorPerRequest])
  )

  val jobs: Map[String, (String, Boolean => Class[_ <: PerRequestActor])] = Map(
    "ftpUpload" -> ("ftpUpload", _ => classOf[FtpRetryActorPerRequest])
  )

  private val allPrimitives = Primitive.soap ++ Primitive.rest ++ Primitive.restInternal ++ Primitive.jobs

  def getActorClass(primitive: String): Class[_ <: PerRequestActor] = {
    allPrimitives(primitive)._2(false)
  }
}
