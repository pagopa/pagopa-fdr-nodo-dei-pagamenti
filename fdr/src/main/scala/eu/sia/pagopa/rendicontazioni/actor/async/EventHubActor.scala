package eu.sia.pagopa.rendicontazioni.actor.async

import eu.sia.pagopa.ActorProps
import eu.sia.pagopa.common.actor.BaseActor
import eu.sia.pagopa.common.json.model.{FdREvent, IUVRendicontatiEvent}
import eu.sia.pagopa.common.repo.Repositories
import eu.sia.pagopa.common.util.{EventUtil, FdrLogConstant}
import eu.sia.pagopa.common.util.azurehubevent.sdkazureclient.{AzureFlussiRendicontazioneProducer, AzureIuvRendicontatiProducer}

final case class EventHubActor(repositories: Repositories, actorProps: ActorProps) extends BaseActor {

  def generateIUVRendicontati(event: FdREvent) = {
    log.info("Generating generateIUVRendicontati")
    EventUtil.createIUVRendicontatiEvent(
      event.sessionId,
      event.nifr,
      event.flussoRiversamento,
      event.insertedTimestamp
    )
      .map( event => {
        AzureIuvRendicontatiProducer.send(log, event)
      })
    log.info("Generated generateIUVRendicontati")
  }

  def generateFlussiRendicontazione(event: FdREvent) = {
    log.info("Generating generateFlussiRendicontazione")
    AzureFlussiRendicontazioneProducer.send(log, EventUtil.createFlussiRendicontazioneEvent(
      event.sessionId,
      event.nifr,
      event.flussoRiversamento,
      event.insertedTimestamp
    ))
    log.info("Generated generateFlussiRendicontazione")
  }


  override def receive: Receive = {
    case event: FdREvent =>
      log.debug(s"FdREvent arrived ${event}")
      generateIUVRendicontati(event)
      generateFlussiRendicontazione(event)
    case event: IUVRendicontatiEvent =>
      log.debug(s"IUVRendicontatiEvent arrived ${event.iuv}")
    case events: Seq[IUVRendicontatiEvent] =>
      log.debug(s"IUVRendicontatiEvent Seq arrived ${events.size}")
    case s: String =>
      log.debug(s"String arrived ${s}")
    case _ =>
      log.error(s"""########################
                   |EVH ACT unmanaged message type
                   |########################""".stripMargin)
  }

}
