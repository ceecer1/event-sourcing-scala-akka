package com.rew3.process

import akka.actor.{Actor, ActorLogging, Props}

/**
 * Created by shishir on 9/9/15.
 */
object ProcessManager {
  def props = Props[ProcessManager]

}

class ProcessManager extends Actor with ActorLogging {

  /*val eventManager = context.actorOf(EventManager.props, "event-manager")
  val organizationManager = context.actorOf(OrganizationManager.props, "organization-manager")*/

  override def receive = {
    case "check" => log.info("process check")
    case "test" => log.info("test check")
  }

}
