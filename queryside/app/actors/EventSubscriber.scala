package actors

import akka.actor._
import akka.stream.actor.ActorSubscriberMessage.{OnComplete, OnError, OnNext}
import akka.stream.actor.{ActorSubscriber, RequestStrategy}
import io.scalac.amqp.Message
import models.SocialEvent
import play.api.libs.json.Json


object EventSubscriber {
  def props(publisher: ActorRef) = Props(new EventSubscriber(publisher))
}

class EventSubscriber(publisher: ActorRef) extends ActorSubscriber with ActorLogging {

  override val requestStrategy = new RequestStrategy {
    override def requestDemand(remainingRequested: Int) =
      Math.max(remainingRequested, 1)
  }

  override def receive = {
    case OnNext(message: Message) =>
      val socialEvent =
        SocialEvent(
          eventId = message.headers("eventId"),
          eventType = message.headers("type"),
          data = Json.parse(message.body.mkString))
      publisher ! socialEvent
    case OnComplete =>
      log.info("Social event stream completed")
      context unwatch publisher
      publisher ! PoisonPill
      context stop self
    case OnError(cause) =>
      log.error(cause, "Subscriber error occurred")
      context unwatch publisher
      publisher ! PoisonPill
      context stop self
  }

}
