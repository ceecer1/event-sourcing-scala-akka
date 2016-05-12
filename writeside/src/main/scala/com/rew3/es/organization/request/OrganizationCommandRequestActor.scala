package com.rew3.es.organization.request

import akka.actor.{Actor, ActorRef, Props, ReceiveTimeout}
import com.rew3.es.common.{CommandAccepted, CommandRejected}
import com.rew3.es.event.write.EventProtocol.{ErrorMessage, EventDoesNotExist}
import com.rew3.es.organization.write.OrganizationProtocol.OrganizationCommand
import com.rew3.es.organization.write.{Organization, OrganizationId, OrganizationProtocol}
import org.json4s.Formats
import spray.http.StatusCodes
import spray.httpx.Json4sSupport
import spray.routing.RequestContext

import scala.concurrent.duration.Duration

object OrganizationCommandRequestActor {
  def props(ctx: RequestContext,
            organizationManager: ActorRef,
            orgId: OrganizationId,
            command: OrganizationCommand)
           (implicit json4sFormats: Formats,
            timeout: Duration) =
    Props(new OrganizationCommandRequestActor(ctx, organizationManager, orgId, command))
}

class OrganizationCommandRequestActor(
    ctx: RequestContext,
    organizationManager: ActorRef,
    orgId: OrganizationId,
    command: OrganizationCommand)(
    implicit override val json4sFormats: Formats,
    timeout: Duration)
  extends Actor with Json4sSupport {

  organizationManager ! OrganizationProtocol.SendOrganizationCommands(orgId, command)

  context.setReceiveTimeout(timeout)

  override def receive = {
    case org: Organization => ctx.complete(StatusCodes.OK, org)
    case CommandAccepted =>
      ctx.complete(StatusCodes.Accepted)
      context stop self
    case CommandRejected(violation) =>
      val message = "rule violated"
      println(message)
      ctx.complete(StatusCodes.BadRequest, ErrorMessage(message))
      context stop self
    case EventDoesNotExist =>
      ctx.complete(StatusCodes.NotFound)
      context stop self
    case ReceiveTimeout =>
      ctx.complete(StatusCodes.RequestTimeout)
      context stop self
  }

}
