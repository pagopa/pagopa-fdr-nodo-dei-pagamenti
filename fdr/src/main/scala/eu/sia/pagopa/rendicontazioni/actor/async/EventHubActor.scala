package eu.sia.pagopa.rendicontazioni.actor.async

import eu.sia.pagopa.ActorProps
import eu.sia.pagopa.common.actor.BaseActor
import eu.sia.pagopa.common.json.model.IUVRendicontatiEvent
import eu.sia.pagopa.common.repo.Repositories

final case class EventHubActor(repositories: Repositories, actorProps: ActorProps) extends BaseActor{

  override def receive: Receive = {
    case event: IUVRendicontatiEvent =>
      log.debug(s"IUVRendicontatiEvent arrived ${event.iuv}")
    case events: Seq[IUVRendicontatiEvent] =>
      log.debug(s"IUVRendicontatiEvent Seq arrived ${events.size}")
  }
}
