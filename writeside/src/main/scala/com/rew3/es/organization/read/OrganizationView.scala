/*
package com.rew3.es.organization.read

import akka.actor.ActorLogging
import akka.persistence.{SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer, PersistentView}

/**
 * Created by shishir on 9/22/15.
 */
class OrganizationView extends PersistentView with ActorLogging {

  override def persistenceId: String = "beta"
  override def viewId = "view-beta"

  def receive = {
    case "snap" =>
      log.info("************ snap ***********")
      println(s"view saving snapshot")
      saveSnapshot(numReplicated)
    case SnapshotOffer(metadata, snapshot: Int) =>
      log.info(s"************ SnapshotOffer $snapshot ***********")
      numReplicated = snapshot
      println(s"view received snapshot offer ${snapshot} (metadata = ${metadata})")
    case payload if isPersistent =>
      //log.info("************ payload persistent ***********")
      numReplicated += 1
      println(s"view replayed event ${payload} (num replicated = ${numReplicated})")
    case SaveSnapshotSuccess(metadata) =>
      log.info("************ SaveSnapshotSuccess(metadata) ***********")
      println(s"view saved snapshot (metadata = ${metadata})")
    case SaveSnapshotFailure(metadata, reason) =>
      log.info("************ SaveSnapshotFailure(metadata, reason) ***********")
      println(s"view snapshot failure (metadata = ${metadata}), caused by ${reason}")
    case payload =>
      log.info("************ payload ***********")
      println(s"view received other message ${payload}")
  }
}
*/
