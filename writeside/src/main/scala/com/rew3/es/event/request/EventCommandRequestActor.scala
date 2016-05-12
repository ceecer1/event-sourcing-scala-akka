package com.rew3.es.event.request

import akka.actor.{Actor, ActorRef, Props, ReceiveTimeout}
import com.rew3.es.common.{CommandAccepted, CommandRejected}
import com.rew3.es.event.write.EventProtocol.{EventCommand, EventDoesNotExist}
import com.rew3.es.event.write.{EventProtocol, SocioEvent, SocioEventId}
import org.json4s.Formats
import spray.http.StatusCodes
import spray.httpx.Json4sSupport
import spray.routing.RequestContext

import scala.concurrent.duration.Duration

object EventCommandRequestActor {
  def props(ctx: RequestContext,
            eventManager: ActorRef,
            eventId: SocioEventId,
            command: EventCommand)
           (implicit json4sFormats: Formats,
            timeout: Duration) =
    Props(new EventCommandRequestActor(ctx, eventManager, eventId, command))
}

class EventCommandRequestActor(
    ctx: RequestContext,
    eventManager: ActorRef,
    eventId: SocioEventId,
    command: EventCommand)(
    implicit override val json4sFormats: Formats,
    timeout: Duration)
  extends Actor with Json4sSupport {

  eventManager ! EventProtocol.SendEventCommands(eventId, command)

  context.setReceiveTimeout(timeout)

  override def receive = {
    case ev: SocioEvent => ctx.complete(StatusCodes.OK, ev)
    case CommandAccepted => ctx.complete(StatusCodes.Accepted)
    case CommandRejected(violation) =>
      ctx.complete(StatusCodes.BadRequest, violation)
      context stop self
    case EventDoesNotExist =>
      ctx.complete(StatusCodes.NotFound)
      context stop self
    case ReceiveTimeout =>
      ctx.complete(StatusCodes.RequestTimeout)
      context stop self
  }

}
