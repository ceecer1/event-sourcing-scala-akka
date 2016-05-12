package com.rew3.es.organization.actor

import akka.actor.{Actor, Props}
import com.rew3.es.organization.actor.OrganizationManager.CollabOrganizationCreated
import com.rew3.es.organization.write.{OrganizationId, OrganizationProcessor, OrganizationProtocol}

object OrganizationManager{
  def props = Props[OrganizationManager]

  case class CollabOrganizationCreated(id: OrganizationId)
}

class OrganizationManager extends Actor {

  import OrganizationProtocol._

  override def receive = {
    case CreateOrganization =>
      val id = OrganizationId.createRandom
      context.actorOf(OrganizationProcessor.props(id), id.value)
      sender() ! CollabOrganizationCreated(id)
    case SendOrganizationCommands(id, command) =>
      context.child(id.value) match {
        case Some(eventActor) => {
          println(s"Found the organization living actor here: ${eventActor.path.name}")
          eventActor forward command
        }
        case None => {
          println(s"Dead organization actor here with id $id")
          sender() ! OrganizationDoesNotExist
        }
      }
  }

}
