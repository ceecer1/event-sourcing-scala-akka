package com.rew3.es.organization.request

import akka.actor.{Actor, ActorRef, Props, ReceiveTimeout}
import com.rew3.es.controllers.EventApi.CreateEventResponseData
import com.rew3.es.organization.actor.OrganizationManager
import com.rew3.es.organization.write.OrganizationProtocol
import org.json4s.Formats
import spray.http.StatusCodes
import spray.httpx.Json4sSupport
import spray.routing.RequestContext

import scala.concurrent.duration.Duration

object CreateOrganizationRequestActor {
  def props(ctx: RequestContext,
            organizationManager: ActorRef)
           (implicit json4sFormats: Formats,
            timeout: Duration) =
    Props(new CreateOrganizationRequestActor(ctx, organizationManager))
}

class CreateOrganizationRequestActor(ctx: RequestContext, organizationManager: ActorRef)(implicit override val json4sFormats: Formats,
    timeout: Duration) extends Actor with Json4sSupport {

  organizationManager ! OrganizationProtocol.CreateOrganization

  context.setReceiveTimeout(timeout)

  override def receive = {
    case OrganizationManager.CollabOrganizationCreated(id) =>
      ctx.complete(StatusCodes.Created, CreateEventResponseData(id.value))
      context stop self
    case ReceiveTimeout =>
      ctx.complete(StatusCodes.RequestTimeout)
      context stop self
  }

}
