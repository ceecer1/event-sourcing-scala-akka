package com.rew3.es.event.actor

import akka.actor.{Actor, ActorLogging, Props}
import com.rew3.es.event.actor.EventManager.CollabEventCreated
import com.rew3.es.event.write._

object EventManager {
  def props = Props[EventManager]

  case class CollabEventCreated(id: SocioEventId)
  case class ReturnedEvent(e: SocioEvent)
}

class EventManager extends Actor with ActorLogging {

  import EventProtocol._

  override def receive = {
    case CreateEvent =>
      val id = SocioEventId.createRandom
      context.actorOf(EventProcessor.props(id), id.value)
      sender() ! CollabEventCreated(id)
    case SendEventCommands(id, command) =>
      log.info("\n\nInside SendEventCommands in EventManager")
      context.child(id.value) match {
        case Some(eventActor) => {
          println(s"Found the event living actor here: ${eventActor.path.name}")
          eventActor forward command
        }
        case None => {
          println(s"Dead event actor here with id $id")
          sender() ! EventDoesNotExist
        }
      }
  }

}
