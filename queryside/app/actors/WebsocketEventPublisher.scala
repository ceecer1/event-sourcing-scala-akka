package actors

import akka.actor._
import akka.stream.scaladsl.{ImplicitMaterializer, Sink, Source}
import config.Config
import global.Global
import io.scalac.amqp.Queue
import models.SocialEvent

object WebsocketEventPublisher {
  def props(eventId: String, out: ActorRef) =
    Props(new WebsocketEventPublisher(eventId, out))
}

class WebsocketEventPublisher(eventId: String, out: ActorRef) extends Actor with ActorLogging
  with ImplicitMaterializer {

  import context.dispatcher

  override def preStart() = {
    import Global.connection
    import Config.Events._

    val queue = Queue(name = eventId, durable = false, autoDelete = true)

    val bindFuture = for {
      _ <- connection.queueDeclare(queue)
      _ <- connection.queueBind(queue.name, exchangeName, "", Map("eventId" -> eventId))
    } yield ()

    bindFuture.map { _ =>
      Source.fromPublisher(connection.consume(queue.name))
        .map(_.message)
        .to(Sink.actorSubscriber(EventSubscriber.props(self)))
        .run()
    }.failed.map { ex =>
      log.error(ex, "Cannot bind queue to events from Social Events {}", eventId)
      context stop self
    }
  }

  override def receive = {
    case ev: SocialEvent if ev.eventId == eventId =>
      out ! ev
  }

}
