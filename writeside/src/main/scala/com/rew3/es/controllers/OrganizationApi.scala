package com.rew3.es.controllers

import akka.actor.{ActorRef, ActorRefFactory}
import com.rew3.es.Address
import com.rew3.es.controllers.EventApi.ChangeDescription
import com.rew3.es.controllers.OrganizationApi.{OrgAddress, ChangeName, OrgCmd}
import com.rew3.es.organization.request.{CreateOrganizationRequestActor, OrganizationCommandRequestActor}
import com.rew3.es.organization.write.OrganizationId
import org.json4s.DefaultFormats
import spray.httpx.Json4sSupport
import spray.routing.Route

import scala.concurrent.duration._

object OrganizationApi {
  case class CreateEventResponseData(id: String)
  case class OrgCmd(name: String, description: String, category: String, website: String, addresses: Seq[OrgAddress],
                    contactPersons: Seq[String], primaryAddress: OrgAddress)
  case class ChangeName(version: Long, name: String)
  case class ChangeDescription(version: Long, description: String)
  case class OrgAddress(street: String, city: String, state: String, zip: String, country: String)

}

case class OrganizationApi(organizationManager: ActorRef, actorRefFactory: ActorRefFactory) extends Json4sSupport
                  with ParseHeader {

  import com.rew3.es.organization.write.OrganizationProtocol._

  override implicit val json4sFormats = DefaultFormats

  implicit val timeout = 2.seconds

  //def organizationManager: ActorRef

  override val routes =
    pathPrefix("api") {
      (pathPrefix("org" / Segment) & get)(handleGetById) ~
        (pathPrefix("org") & post) {
            // the header is passed in containing the content type
            // we match the header using a case statement, and depending
            // on the content type we return a specific object
          parseDomainModel { h => getRequestedCommand(h)
             match {
              case c: String if c == "New" => pathEndOrSingleSlash (handleCreate)
              case c: String if c == "Create" => (path (Segment) & entity (as[OrgCmd] ) ) (handleNewOrg)
              case c: String if c == "ChangeName" => (path (Segment)
                                                          & entity (as[ChangeName] ) ) (handleEventTitleChange)
              case c: String if c == "ChangeDescription" => (path (Segment)
                                                          & entity (as[ChangeDescription] ) ) (handleDescriptionChange)
            }
          }
        }
    }


  private def handleGetById(id: String): Route = { ctx =>
    println("\n\ninside get org")
    val props = OrganizationCommandRequestActor.props(ctx, organizationManager, OrganizationId(id),
                      GetOrganization(id, 0L))
    actorRefFactory.actorOf(props)
  }

  private def handleCreate: Route = { ctx =>
    println("\n\ninside handle create")
    actorRefFactory.actorOf(CreateOrganizationRequestActor.props(ctx, organizationManager))
  }

  private def handleEventTitleChange(id: String, cmd: ChangeName): Route = { ctx =>
    println("\n\ninside change name")
    val props = OrganizationCommandRequestActor.props(ctx, organizationManager, OrganizationId(id),
      ChangeOrganizationName(id, cmd.version, cmd.name))
    actorRefFactory.actorOf(props)
  }

  private def handleDescriptionChange(id: String, cmd: ChangeDescription): Route = { ctx =>
    println("\n\ninside change description")
    val props = OrganizationCommandRequestActor.props(ctx, organizationManager, OrganizationId(id),
      ChangeOrganizationDescription(id, cmd.version, cmd.description))
    actorRefFactory.actorOf(props)
  }

  private def handleNewOrg(id: String, cmd: OrgCmd): Route = { ctx =>
    println("\n\ninside new org")
    val props = OrganizationCommandRequestActor.props(ctx, organizationManager, OrganizationId(id),
      CreateOrganization(id, 0L, cmd.name, cmd.description, cmd.category, cmd.website, getAddresses(cmd.addresses),
        cmd.contactPersons, cmd.primaryAddress.street, cmd.primaryAddress.city, cmd.primaryAddress.state,
        cmd.primaryAddress.zip, cmd.primaryAddress.country))
    actorRefFactory.actorOf(props)
  }

  private def getAddresses(addresses: Seq[OrgAddress]): Seq[Address] = {
    val esAddresses = addresses.map(a =>
      new Address(a.street, a.city, a.state, a.zip, a.country))
    esAddresses
  }

}

/*(pathPrefix("game") & post) {
pathEndOrSingleSlash(handleCreate) ~
(path(Segment / "start") & entity(as[StartGameRequestData]))(handleStart) ~
path(Segment / "roll" / Segment)(handleRoll)
}*/
