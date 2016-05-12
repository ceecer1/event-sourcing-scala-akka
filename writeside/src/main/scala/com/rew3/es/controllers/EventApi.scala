package com.rew3.es.controllers

import akka.actor.{ActorRef, ActorRefFactory}
import com.rew3.es.controllers.EventApi.{ChangeDescription, ChangeTitle, EventCmd}
import com.rew3.es.event.request.{CreateEventRequestActor, EventCommandRequestActor}
import com.rew3.es.event.write.SocioEventId
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.DefaultFormats
import spray.httpx.Json4sSupport
import spray.routing.Route

import scala.concurrent.duration._

object EventApi {
  case class CreateEventResponseData(id: String)
  case class EventCmd(title: String, description: String, eventStart: Long, eventEnd: Long, eventDate: Long,
                    eventType: String, street: String, city: String, state: String, zip: String, country: String,
                    eventLink: String)
  case class ChangeTitle(version: Long, title: String)
  case class ChangeDescription(version: Long, description: String)
}

case class EventApi(eventManager: ActorRef, actorRefFactory: ActorRefFactory) extends Json4sSupport with ParseHeader {

  import com.rew3.es.event.write.EventProtocol._

  override implicit val json4sFormats = DefaultFormats

  implicit val timeout = 2.seconds

  //def eventManager: ActorRef

  override val routes =
    pathPrefix("api") {
      (pathPrefix("event" / Segment) & get)(handleGetById) ~
        (pathPrefix("event") & post) {
            // the header is passed in containing the content type
            // we match the header using a case statement, and depending
            // on the content type we return a specific object
          parseDomainModel { h => getRequestedCommand(h)
             match {
              case c: String if c == "New" => pathEndOrSingleSlash (handleCreate)
              case c: String if c == "Create" => (path (Segment) & entity (as[EventCmd] ) ) (handleNewEvent)
              case c: String if c == "ChangeTitle" => (path (Segment)
                                                          & entity (as[ChangeTitle] ) ) (handleEventTitleChange)
              case c: String if c == "ChangeDescription" => (path (Segment)
                                                          & entity (as[ChangeDescription] ) ) (handleDescriptionChange)
            }
          }
        }
    }

  private def handleCreate: Route = { ctx =>
    actorRefFactory.actorOf(CreateEventRequestActor.props(ctx, eventManager))
    /*ctx.complete(HttpResponse(StatusCodes.OK, "ok"))*/
  }

  private def handleGetById(id: String): Route = { ctx =>
    println("\n\ninside get event")
    val props = EventCommandRequestActor.props(ctx, eventManager, SocioEventId(id), GetEvent(id, 0L))
    actorRefFactory.actorOf(props)
  }

  private def handleEventTitleChange(id: String, cmd: ChangeTitle): Route = { ctx =>
    println("\n\ninside change title")
    val props = EventCommandRequestActor.props(ctx, eventManager, SocioEventId(id),
      ChangeEventTitle(id, cmd.version, cmd.title))
    actorRefFactory.actorOf(props)
  }

  private def handleDescriptionChange(id: String, cmd: ChangeDescription): Route = { ctx =>
    println("\n\ninside change description")
    val props = EventCommandRequestActor.props(ctx, eventManager, SocioEventId(id),
      ChangeEventDescription(id, cmd.version, cmd.description))
    actorRefFactory.actorOf(props)
  }

  private def handleNewEvent(id: String, cmd: EventCmd): Route = { ctx =>
    println("\n\ninside new event")
    val props = EventCommandRequestActor.props(ctx, eventManager, SocioEventId(id),
      CreateEvent(id, 0L, cmd.title, cmd.description,
      new DateTime(DateTimeZone.UTC).plusDays(2).withTimeAtStartOfDay().getMillis ,
        new DateTime(DateTimeZone.UTC).plusDays(6).getMillis,
        new DateTime(DateTimeZone.UTC).plusDays(4).getMillis, cmd.eventType, true, 2, 3, 4, 5, cmd.street,
        cmd.city, cmd.state, cmd.zip, cmd.country, cmd.eventLink, 1L))
    actorRefFactory.actorOf(props)
  }

}
