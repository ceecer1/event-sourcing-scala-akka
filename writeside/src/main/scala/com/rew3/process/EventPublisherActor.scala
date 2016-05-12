package com.rew3.process

import akka.actor.Props
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.Request
import com.rew3.es.common.Event

object EventPublisherActor {
  def props = Props[EventPublisherActor]
}

class EventPublisherActor extends ActorPublisher[Event] {

  var eventCache: List[Event] = Nil

  context.system.eventStream.subscribe(self, classOf[Event])

  override def receive = {
    case Request(n) =>
      while (isActive && totalDemand > 0 && eventCache.nonEmpty) {
        val (head :: tail) = eventCache
        onNext(head)
        eventCache = tail
      }
    case event: Event =>
      if (isActive && totalDemand > 0)
        onNext(event)
      else
        eventCache :+= event
  }

}
