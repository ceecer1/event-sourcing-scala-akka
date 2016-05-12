package com.rew3.es.event.request

import akka.actor.{Actor, ActorRef, Props, ReceiveTimeout}
import com.rew3.es.controllers.EventApi.CreateEventResponseData
import com.rew3.es.event.actor.EventManager
import com.rew3.es.event.write.EventProtocol
import org.json4s.Formats
import spray.http.StatusCodes
import spray.httpx.Json4sSupport
import spray.routing.RequestContext

import scala.concurrent.duration.Duration

object CreateEventRequestActor {
  def props(ctx: RequestContext,
            eventManager: ActorRef)
           (implicit json4sFormats: Formats,
            timeout: Duration) =
    Props(new CreateEventRequestActor(ctx, eventManager))
}

class CreateEventRequestActor(ctx: RequestContext, eventManager: ActorRef)(implicit override val json4sFormats: Formats,
    timeout: Duration) extends Actor with Json4sSupport {

  eventManager ! EventProtocol.CreateEvent

  context.setReceiveTimeout(timeout)

  override def receive = {
    case EventManager.CollabEventCreated(id) =>
      ctx.complete(StatusCodes.Created, CreateEventResponseData(id.value))
      context stop self
    case ReceiveTimeout =>
      ctx.complete(StatusCodes.RequestTimeout)
      context stop self
  }

}
