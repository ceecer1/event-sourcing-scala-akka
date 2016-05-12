/*
package com.rew3.es.controllers

import scala.concurrent.duration._

import akka.actor._
import akka.persistence._

object ViewExample extends App {
  case object SaveSnapshot
  class ExamplePersistentActor extends PersistentActor with ActorLogging {
    override def persistenceId = "beta"

    var count = 1

    def receiveCommand: Receive = {
      case payload: String => {
        println(s"persistentActor received ${payload} (nr = ${count})")
        persist(payload + count) { evt =>
          count += 1
        }

        if ((count % 5) == 0) {
          self ! SaveSnapshot
        }
      }

      case SaveSnapshot =>
        deleteSnapshots(SnapshotSelectionCriteria.Latest)
        saveSnapshot(count)

    }

    def receiveRecover: Receive = {
      case SnapshotOffer(metadata, snapshot: Int) =>
        log.info(s"************ SnapshotOffer $snapshot ***********")
        count = snapshot
      case x: String => {
        println("*********** Inside RECEIVE RECOVER ********** " + x)
        count += 1
      }
      case RecoveryCompleted => println("*********** READY READY READY ********** ")

    }
  }

  class ExampleView extends PersistentView with ActorLogging {
    private var numReplicated = 0

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

  val system = ActorSystem("example")

  val persistentActor = system.actorOf(Props(classOf[ExamplePersistentActor]))
  //val view = system.actorOf(Props(classOf[ExampleView]))

  import system.dispatcher

  system.scheduler.schedule(Duration.Zero, 2.seconds, persistentActor, "scheduled")
  //system.scheduler.schedule(Duration.Zero, 10.seconds, view, "snap")
}*/
