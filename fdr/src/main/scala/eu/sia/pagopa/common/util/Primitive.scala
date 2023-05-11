package eu.sia.pagopa.common.util

import eu.sia.pagopa.common.actor.PerRequestActor
import eu.sia.pagopa.ftpsender.actor.FtpRetryActorPerRequest
import eu.sia.pagopa.rendicontazioni.actor.rest.ChiediFlussoRendicontazioneActorPerRequest
import eu.sia.pagopa.rendicontazioni.actor.soap.{NodoChiediElencoFlussiRendicontazioneActorPerRequest, NodoChiediFlussoRendicontazioneActorPerRequest, NodoFlussiRendicontazioneActorPerRequest}

object Primitive {

  val soap: Map[String, (String, Boolean => Class[_ <: PerRequestActor])] = Map(
    "nodoChiediFlussoRendicontazione" -> ("Body/_/identificativoStazioneIntermediarioPA", _ => classOf[NodoChiediFlussoRendicontazioneActorPerRequest]),
    "nodoInviaFlussoRendicontazione" -> ("Body/_/identificativoCanale", _ => classOf[NodoFlussiRendicontazioneActorPerRequest]),
    "nodoChiediElencoFlussiRendicontazione" -> ("Body/_/identificativoStazioneIntermediarioPA", _ => classOf[NodoChiediElencoFlussiRendicontazioneActorPerRequest])
  )

  val rest: Map[String, (String, Boolean => Class[_ <: PerRequestActor])] = Map(
    //FIXME: trovare un nome più congruo
    "chiediFlussoRendicontazione" -> ("chiediFlussoRendicontazione", _ => classOf[ChiediFlussoRendicontazioneActorPerRequest])
  )

  val jobs: Map[String, (String, Boolean => Class[_ <: PerRequestActor])] = Map(
    "ftpUpload" -> ("ftpUpload", _ => classOf[FtpRetryActorPerRequest])
  )

  val allPrimitives = Primitive.soap ++ Primitive.rest ++ Primitive.jobs

  def getActorClass(primitive: String): Class[_ <: PerRequestActor] = {
    allPrimitives(primitive)._2(false)
  }
}
