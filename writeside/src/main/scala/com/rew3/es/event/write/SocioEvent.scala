package com.rew3.es.event.write

import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, SnapshotOffer}
import com.rew3.es.common.{CommandRejected, CommandAccepted}
import com.rew3.es.{Address, CommonValidations, DomainValidation, ValidationKey}
import org.joda.time.{DateTime, DateTimeZone}

import scalaz.Scalaz._
import scalaz._

/**
 * An Event domain aggregate trait to be extended by ActiveEvent, InactiveEvent, etc.
 */
sealed trait SocioEvent {
  def id: String
  def version: Long
  def title: String
  def description: String
  def eventStart: Long
  def eventEnd: Long
  def eventDate: Long
  def eventType: String
  def access: Boolean
  def maxParticipants: Int
  def recurrenceGroupId: Int
  def recurrenceInterval: Int
  def recurrenceTimes: Int
  def address: Address
  def eventLink: String
  def cancelledDate: Long
}

/**
 * Companion object for Event abstraction that handles version validation.
 */
object SocioEvent {
  import EventProtocol._

  def requireVersion[A <: SocioEvent](e: A, cmd: EventCommand): DomainValidation[A] = {
    if (cmd.expectedVersion == e.version) e.successNel
    else s"$e version does not match $cmd version".failureNel
  }
}

/**
 * Validations trait for Event abstractions.
 */
trait EventValidations {
  case object IdRequired extends ValidationKey
  case object TitleRequired extends ValidationKey
  case object DescriptionRequired extends ValidationKey
  case object EventTypeRequired extends ValidationKey
  case object EventLinkRequired extends ValidationKey
  case object EventAccessRequired extends ValidationKey
  case object StartDateRequired extends ValidationKey
  case object AddressRequired extends ValidationKey

  def checkStartDate(d: Long): Validation[String, Long] = {
    val dt = new DateTime(d, DateTimeZone.UTC)
    println("*************************************************** " + dt)
    if (dt.getMillis != dt.withTimeAtStartOfDay.getMillis) s"start date $d must be start of day boundary".failure else d.success
  }

  def checkEndDate(sd: Long, ed: Long): Validation[String, Long] = {
    if (ed <= sd) s"end date $ed must be greater than start date $sd".failure else ed.success
  }

  def checkCancellDate(dd: Long, sd: Long): Validation[String, Long] =
    if (dd <= sd) s"cancell date $dd must be greater than start date $sd".failure else dd.success

  def checkActivateDate(sd: Long, dd: Long): Validation[String, Long] =
    if (sd <= dd) s"activate date $sd must be greater than deactivate date $dd".failure else sd.success

}

/**
 * Case class that represents the state of an active Event.
 */
case class ActiveEvent (
  id: String,
  //`tag`: String = "ActiveEvent",
  version: Long,
  title: String,
  description: String,
  eventStart: Long,
  eventEnd: Long,
  eventDate: Long,
  eventType: String,
  access: Boolean,
  maxParticipants: Int,
  recurrenceGroupId: Int,
  recurrenceInterval: Int,
  recurrenceTimes: Int,
  address: Address,
  eventLink: String,
  cancelledDate: Long) extends SocioEvent with EventValidations {
  import CommonValidations._

  def withTitle(title: String): DomainValidation[ActiveEvent] =
    checkString(title, TitleRequired) fold (f => f.failureNel, s => copy(version = version + 1, title = s).success)

  def withDescription(description: String): DomainValidation[ActiveEvent] =
    checkString(description, DescriptionRequired) fold (f => f.failureNel, s => copy(version = version + 1,
      description = s).success)

  def withEventType(eventType: String): DomainValidation[ActiveEvent] =
    checkString(eventType, EventTypeRequired) fold (f => f.failureNel, s => copy(version = version + 1,
      eventType = s).success)

  def withEventLink(eventLink: String): DomainValidation[ActiveEvent] =
    checkString(eventLink, EventLinkRequired) fold (f => f.failureNel, s => copy(version = version + 1,
      eventLink = s).success)

  def withEventAccess(access: Boolean): DomainValidation[ActiveEvent] =
    checkBoolean(access, EventAccessRequired) fold (f => f.failureNel, s => copy(version = version + 1,
      access = s).success)

  def withAddress(street: String, city: String, stateOrProvince: String, country: String,
      zip: String): DomainValidation[ActiveEvent] =
    Address.validate(street, city, stateOrProvince, country, zip) fold (f => f.failure, s => copy(version = version + 1,
      address = s).success)

  def withStartDate(startDate: Long): DomainValidation[ActiveEvent] =
    checkStartDate(startDate) fold (f => f.failureNel, s => copy(version = version + 1, eventStart = s).success)

  def cancell(cancelledDate: Long): DomainValidation[InactiveEvent] =
    checkCancellDate(cancelledDate, this.cancelledDate) fold (
      f => f.failureNel,
      s => InactiveEvent(this.id, this.version + 1, this.title, this.description, this.eventStart, this.eventEnd, this.eventDate,
        this.eventType, this.access, this.maxParticipants, this.recurrenceGroupId, this.recurrenceInterval, this.recurrenceTimes,
        this.address, this.eventLink, s).success)

}

/**
* Companion object to create ActiveEvent
*/
object ActiveEvent extends EventValidations {
  import CommonValidations._
  import EventProtocol._

  def create(cmd: CreateEvent): DomainValidation[ActiveEvent] =
    (checkString(cmd.id, IdRequired).toValidationNel |@|
      0L.successNel |@|
      checkString(cmd.title, TitleRequired).toValidationNel |@|
      checkString(cmd.description, DescriptionRequired).toValidationNel |@|
      checkStartDate(cmd.eventStart).toValidationNel |@|
      checkEndDate(cmd.eventStart, cmd.eventEnd).toValidationNel |@|
      cmd.eventDate.successNel |@|
      cmd.eventType.successNel |@|
      Address.validate(cmd.street, cmd.city, cmd.stateOrProvince, cmd.country, cmd.zip) |@|
      cmd.eventLink.successNel) { (id, v, t, d, es, ee, ed, et, a, el) =>
      ActiveEvent(id, v, t, d, es, ee, ed, et, cmd.access, cmd.maxParticipants, cmd.recurrenceGroupId,
        cmd.recurrenceInterval, cmd.recurrenceTimes, a, el, cmd.cancelledDate) }
}

/**
 * Case class that represents the state of an inactive Event.
 */
case class InactiveEvent (
  id: String,
  version: Long,
  title: String,
  description: String,
  eventStart: Long,
  eventEnd: Long,
  eventDate: Long,
  eventType: String,
  access: Boolean,
  maxParticipants: Int,
  recurrenceGroupId: Int,
  recurrenceInterval: Int,
  recurrenceTimes: Int,
  address: Address,
  eventLink: String,
  cancelledDate: Long) extends SocioEvent with EventValidations {

  def activate(startDate: Long): DomainValidation[ActiveEvent] =
    (checkStartDate(startDate).toValidationNel |@|
      checkActivateDate(startDate, this.cancelledDate).toValidationNel) {(s1, s2) =>
      ActiveEvent(this.id, this.version + 1, this.title, this.description, s2, this.eventEnd, this.eventDate,
        this.eventType, this.access, this.maxParticipants, this.recurrenceGroupId, this.recurrenceInterval, this.recurrenceTimes,
        this.address, this.eventLink, this.cancelledDate)
    }
}


/**
 * Case class the contains the in-memory current state of all [[SocioEvent]] aggregates.
 * @param Events Map[String, Event] that contains the current state of all [[SocioEvent]] aggregates.
 */
final case class EventState(Events: Map[String, SocioEvent] = Map.empty) {
  def update(e: SocioEvent) = copy(Events = Events + (e.id -> e))
  def get(id: String) = Events.get(id)
  def getActive(id: String) = get(id) map (_.asInstanceOf[ActiveEvent])
  def getActiveAll = Events map (_._2.asInstanceOf[ActiveEvent])
  def getInactive(id: String) = get(id) map (_.asInstanceOf[InactiveEvent])
}


object EventProcessor {
  def props(id: SocioEventId) = Props(new EventProcessor(id))

}


/**
 * The EventProcessor is responsible for maintaining  state changes for all [[SocioEvent]] aggregates. This particular
 * processor uses Akka-Persistence's [[PersistentActor]]. It receives Commands and if valid will persist the generated events,
 * afterwhich it will updated the current state of the [[SocioEvent]] being processed.
 */
class EventProcessor(id: SocioEventId) extends PersistentActor with ActorLogging {
  import EventProtocol._

  override def persistenceId = id.value

  var state = EventState()

  def updateState(evt: SocioEvent): Unit =
    state = state.update(evt)

  /**
   * These are the events that are recovered during journal recovery. They cannot fail and must be processed to recreate the current
   * state of the aggregate.
   */
  val receiveRecover: Receive = {
    case evt: EventCreated =>
      updateState(ActiveEvent(evt.id, evt.version, evt.title, evt.description, evt.eventStart,
        evt.eventEnd, evt.eventDate, evt.eventType, evt.access, evt.maxParticipants,
        evt.recurrenceGroupId, evt.recurrenceInterval, evt.recurrenceTimes,  Address(evt.street,
          evt.city, evt.stateOrProvince, evt.country, evt.zip), evt.eventLink,
        evt.cancelledDate))
    case evt: EventTitleChanged =>
      updateState(state.getActive(evt.id).get.copy(version = evt.version, title = evt.title))
    case evt: EventDescriptionChanged =>
      updateState(state.getActive(evt.id).get.copy(version = evt.version, description = evt.description))
    case evt: EventTypeChanged =>
      updateState(state.getActive(evt.id).get.copy(version = evt.version, eventType = evt.eventType))
    case evt: EventAddressChanged =>
      updateState(state.getActive(evt.id).get.copy(version = evt.version, address = Address(evt.street, evt.city,
        evt.stateOrProvince, evt.country, evt.zip)))
    case evt: EventLinkChanged =>
      updateState(state.getActive(evt.id).get.copy(version = evt.version, eventLink = evt.eventLink))
    case evt: EventAccessChanged =>
      updateState(state.getActive(evt.id).get.copy(version = evt.version, access = evt.access))
    case evt: EventUserInvited =>

    case evt: EventInvitationAccepted =>

    case evt: EventInvitationDeclined =>

    case evt: EventJoinRequested =>

    //case evt: EventGetListRequested =>

    case evt: UsersEventJoinRequested =>

    case evt: UserJoinEventRequestAccepted =>

    case evt: UserJoinEventRequestDeclined =>

    //case evt: EventPrivilegeSet =>

    case evt: EventLeft =>

    case evt: EventFollowed =>

    case evt: EventUnfollowed =>

    case evt: EventFollowed =>

    case SnapshotOffer(_, snapshot: EventState) => state = snapshot
  }

  /**
   * These are the commands that are requested. As command they can fail sending response back to the user. Each command will
   * generate one or more events to be journaled.
   */
  val receiveCommand: Receive = {
    case cmd: CreateEvent => create(cmd) fold(
      f => {
        log.info("\n\nEntered into wrong place")
        sender ! CommandRejected(s"error $f occurred on $cmd")
      },
      s => {
        log.info("\n\nEntered into right place")
        persist(EventCreated(s.id, s.version, s.title, s.description, s.eventStart, s.eventEnd, s.eventDate, s.eventType,
        s.access, s.maxParticipants, s.recurrenceGroupId, s.recurrenceInterval, s.recurrenceTimes, s.address.street, s.address.city,
        s.address.stateOrProvince, s.address.zip, s.address.country, s.eventLink,
        s.cancelledDate)) { event =>
          updateState(s)
          context.system.eventStream.publish(event)
          sender() ! CommandAccepted
      }})


    case cmd: ChangeEventTitle => changeEventTitle(cmd) fold (
        f => sender ! CommandRejected(s"error $f occurred on $cmd"),
        s => persist(EventTitleChanged(s.id, s.version, s.title)) { event =>
          updateState(s)
          context.system.eventStream.publish(event)
          sender() ! CommandAccepted
        })
    case cmd: ChangeEventDescription => changeFirstName(cmd) fold (
      f => sender ! CommandRejected(s"error $f occurred on $cmd"),
      s => persist(EventDescriptionChanged(s.id, s.version, s.description)) { event =>
        updateState(s)
        context.system.eventStream.publish(event)
        sender() ! CommandAccepted
      })
    case cmd: ChangeEventAddress => changeAddress(cmd) fold (
      f => sender ! ErrorMessage(s"error $f occurred on $cmd"),
      s => persist(EventAddressChanged(s.id, s.version, s.address.street, s.address.city, s.address.stateOrProvince,
        s.address.country, s.address.zip)) { event =>
          updateState(s)
          context.system.eventStream.publish(event)
      })
    case cmd: ChangeEventType => changeEventType(cmd) fold (
      f => sender ! ErrorMessage(s"error $f occurred on $cmd"),
      s => persist(EventTypeChanged(s.id, s.version, s.eventType)) { event =>
        updateState(s)
        context.system.eventStream.publish(event)
      })
    case cmd: ChangeEventLink => changeEventLink(cmd) fold (
      f => sender ! ErrorMessage(s"error $f occurred on $cmd"),
      s => persist(EventLinkChanged(s.id, s.version, s.eventLink)) { event =>
        updateState(s)
        context.system.eventStream.publish(event)
      })
    case cmd: ChangeEventAccess => changeEventAccess(cmd) fold (
      f => sender ! ErrorMessage(s"error $f occurred on $cmd"),
      s => persist(EventAccessChanged(s.id, s.version, s.access)) { event =>
        updateState(s)
        context.system.eventStream.publish(event)
      })
  /*  case cmd: ActivateEvent => activate(cmd) fold (
      f => sender ! ErrorMessage(s"error $f occurred on $cmd"),
      s => persist(EventActivated(s.id, s.version, s.startDate)) { event =>
        updateState(s)
        context.system.eventStream.publish(event)
      })*/

    case cmd: GetEvent   =>
      val eventOpt = state.get(cmd.id).getOrElse(CommandRejected(s"The requested event with $id does not exist"))
      sender ! eventOpt
      self ! SnapshotEventedEvents
    case SnapshotEventedEvents  => saveSnapshot(state)
    case "print"            => println("STATE: " + state)
  }

  def create(cmd: CreateEvent): DomainValidation[ActiveEvent] =
    state.get(cmd.id) match {
      case Some(evt) => s"Event for $cmd already exists".failureNel
      case None      => {
        log.info("\n\n\n\n Entered into event create method")
        ActiveEvent.create(cmd)
      }
    }

  def changeEventTitle(cmd: ChangeEventTitle): DomainValidation[ActiveEvent] =
    updateActive(cmd) { e => e.withTitle(cmd.title) }

  def changeFirstName(cmd: ChangeEventDescription): DomainValidation[ActiveEvent] =
    updateActive(cmd) { e => e.withDescription(cmd.description) }

  def changeAddress(cmd: ChangeEventAddress): DomainValidation[ActiveEvent] =
    updateActive(cmd) { e => e.withAddress(cmd.street, cmd.city, cmd.stateOrProvince, cmd.country, cmd.zip) }

  def changeEventType(cmd: ChangeEventType): DomainValidation[ActiveEvent] =
    updateActive(cmd) { e => e.withEventType(cmd.eventType) }

  def changeEventLink(cmd: ChangeEventLink): DomainValidation[ActiveEvent] =
    updateActive(cmd) { e => e.withEventLink(cmd.eventLink) }

  def changeEventAccess(cmd: ChangeEventAccess): DomainValidation[ActiveEvent] =
    updateActive(cmd) { e => e.withEventAccess(cmd.access) }

  def updateEvent[A <: SocioEvent](cmd: EventCommand)(fn: SocioEvent => DomainValidation[A]): DomainValidation[A] =
    state.get(cmd.id) match {
      case Some(evt) => SocioEvent.requireVersion(evt, cmd) fold (f => f.failure, s => fn(s))
      case None      => s"Event for $cmd does not exist".failureNel
    }

  def updateActive[A <: SocioEvent](cmd: EventCommand)(fn: ActiveEvent => DomainValidation[A]): DomainValidation[A] =
    updateEvent(cmd) {
      case evt: ActiveEvent => fn(evt)
      case evt              => s"$evt for $cmd is not active".failureNel
    }

  def updateInactive[A <: SocioEvent](cmd: EventCommand)(fn: InactiveEvent => DomainValidation[A]): DomainValidation[A] =
    updateEvent(cmd) {
      case evt: InactiveEvent => fn(evt)
      case evt                => s"$evt for $cmd is not inactive".failureNel
    }

}
