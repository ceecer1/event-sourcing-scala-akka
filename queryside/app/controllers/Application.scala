package controllers

import actors.WebsocketEventPublisher
import models.SocialEvent
import play.api.libs.json.Json
import play.api.mvc.WebSocket.FrameFormatter
import play.api.mvc.{WebSocket, Action, Controller}
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

/**
 * Created by shishir on 9/24/15.
 */
object Application extends Controller {

  implicit val socialEventFormat = Json.format[SocialEvent]
  implicit val socialEventFrameFormatter = FrameFormatter.jsonFrame[SocialEvent]

  def index = Action.async {
    Future(Ok(Json.toJson("ok")))
  }

  def socialEvents(eventId: String) = WebSocket.acceptWithActor[String, SocialEvent] { request => out =>
    WebsocketEventPublisher.props(eventId, out)
  }

}
