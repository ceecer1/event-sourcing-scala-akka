package com.rew3.service

import akka.actor._
import com.rew3.es.controllers.{OrganizationApi, EventApi}
import com.rew3.es.event.actor.EventManager
import com.rew3.es.organization.actor.OrganizationManager
import spray.http.{HttpResponse, HttpRequest}
import spray.routing.HttpService
import spray.routing.directives.LogEntry

/**
 * Created by shishir on 9/9/15.
 */

object ApiService {
  def props(processManager: ActorRef) = Props(new ApiService(processManager))
}

class ApiService(val processManager: ActorRef) extends Actor with HttpService {

  override val actorRefFactory = context

  val eventManager = context.actorOf(EventManager.props)
  val organizationManager = context.actorOf(OrganizationManager.props)

  def requestMethodAndResponseStatusAsInfo(req: HttpRequest): Any => Option[LogEntry] = {
    case res: HttpResponse => Some(LogEntry(req.method + ":" + req.uri + ":" + res.message.status, akka.event.Logging.DebugLevel))
    case _ => None
  }

  def receive: akka.actor.Actor.Receive = runRoute(logRequestResponse(requestMethodAndResponseStatusAsInfo _)(
      new EventApi(eventManager, actorRefFactory).routes) ~
      new OrganizationApi(organizationManager, actorRefFactory).routes)

}
