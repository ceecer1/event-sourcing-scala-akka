package com.rew3

import akka.actor.ActorSystem
import akka.io.IO
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import com.google.common.net.MediaType
import com.rew3.config.Config
import com.rew3.es.common.Event
import com.rew3.process.{EventPublisherActor, ProcessManager}
import com.rew3.service.ApiService
import io.scalac.amqp.{Message, Headers, Exchange, Connection}
import org.json4s.{DefaultFormats, Extraction}
import org.json4s.native.JsonMethods._
import org.slf4j.LoggerFactory
import spray.can.Http

import scala.util.{Failure, Success}

/**
 * Created by shishir on 8/27/15.
 */
object Boot {

  implicit val system = ActorSystem("EventSourcedSystem")
  import system.dispatcher

  val log = LoggerFactory.getLogger(Boot.getClass)

  implicit val formats = DefaultFormats

  def main(args: Array[String]) = {
    setupRestApi()
    setupEventStream()
  }

  private def setupRestApi() = {
    val processManager = system.actorOf(ProcessManager.props, "process-manager")
    val apiServiceActor = system.actorOf(ApiService.props(processManager), "api-service")
    IO(Http).tell(Http.Bind(apiServiceActor, "0.0.0.0", 8081), apiServiceActor)
  }

  private def setupEventStream() = {
    import Config.Events._

    val connection = Connection()
    val exchange = Exchange(exchangeName, Headers, durable = false)

    connection.exchangeDeclare(exchange) onComplete {
      case Success(_) =>
        Source.actorPublisher[Event](EventPublisherActor.props)
          .map(toMessage)
          .to(Sink.fromSubscriber(connection.publish(exchange = exchangeName, "")))
          .run()(ActorMaterializer())
      case Failure(ex) =>
        log.error("Cannot create exchange", ex)
        sys.exit(1)
    }
  }

  private def toMessage(event: Event) = {
    val serialized = compact(render(Extraction.decompose(event)))
    Message(
      body = ByteString(serialized),
      contentType = Some(MediaType.JSON_UTF_8),
      contentEncoding = Some("UTF-8"),
      headers = Map(
        "eventId" -> event.id,
        "type" -> event.getClass.getSimpleName))
  }
}