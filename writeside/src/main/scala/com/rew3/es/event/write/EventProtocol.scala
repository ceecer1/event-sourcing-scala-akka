package com.rew3.es.event.write

import com.rew3.es.common.Command

/**
 * This object contains all the commands and events as well as other messages that the event aggregate may process.
 */
object EventProtocol {

  /**
  * These are the sealed commands of every action that happens with an event. Commands can be rejected.
  */
  sealed trait EventCommand extends Command {
    def id: String
    def expectedVersion: Long
  }

  case class SendEventCommands(eventId: SocioEventId, command: EventCommand) extends Command
//if event does not exist
  case object EventDoesNotExist

  final case class Evento(id: String, expectedVersion: Long = -1L, name: String, title: String) extends EventCommand

  final case class CreateEvent(id: String, expectedVersion: Long = -1L, title: String, description: String,
                               eventStart: Long, eventEnd: Long, eventDate: Long, eventType: String, access: Boolean,
                               maxParticipants: Int, recurrenceGroupId: Int, recurrenceInterval: Int,
                               recurrenceTimes: Int, street: String, city: String, stateOrProvince: String,
                               zip: String, country: String, eventLink: String,
                               cancelledDate: Long) extends EventCommand
  final case class ChangeEventTitle(id: String, expectedVersion: Long, title: String) extends EventCommand
  final case class ChangeEventDescription(id: String, expectedVersion: Long, description: String) extends EventCommand
  final case class ChangeEventType(id: String, expectedVersion: Long, eventType: String) extends EventCommand
  final case class ChangeEventAddress(id: String, expectedVersion: Long, street: String, city: String, stateOrProvince: String,
                                         country: String, zip: String) extends EventCommand
  final case class ChangeEventLink(id: String, expectedVersion: Long, eventLink: String) extends EventCommand
  final case class ChangeEventAccess(id: String, expectedVersion: Long, access: Boolean) extends EventCommand

  final case class GetEvent(id: String, expectedVersion: Long) extends EventCommand

  /**
   * These are the resulting events from commands if the commands were not rejected. They will be journaled
   * in the event store and cannot be rejected.
  */
  sealed trait EventedEvent extends com.rew3.es.common.Event {
    def id: String
    def version: Long
  }

  final case class EventCreated(id: String, version: Long, title: String, description: String,
                                eventStart: Long, eventEnd: Long, eventDate: Long, eventType: String, access: Boolean,
                                maxParticipants: Int, recurrenceGroupId: Int, recurrenceInterval: Int,
                                recurrenceTimes: Int, street: String, city: String, stateOrProvince: String,
                                zip: String, country: String, eventLink: String,
                                cancelledDate: Long) extends EventedEvent
  final case class EventTitleChanged(id: String, version: Long, title: String) extends EventedEvent
  final case class EventDescriptionChanged(id: String, version: Long, description: String) extends EventedEvent
  final case class EventTypeChanged(id: String, version: Long, eventType: String) extends EventedEvent
  final case class EventAddressChanged(id: String, version: Long, street: String, city: String, stateOrProvince: String,
                                       country: String, zip: String) extends EventedEvent
  final case class EventLinkChanged(id: String, version: Long, eventLink: String) extends EventedEvent
  final case class EventAccessChanged(id: String, version: Long, access: Boolean) extends EventedEvent

  final case class SubEventCreated(id: String, version: Long, parentEventId: String) extends EventedEvent
  final case class EventUserInvited(id: String, version: Long, userId: String) extends EventedEvent
  final case class EventInvitationAccepted(id: String, version: Long, eventId: String, userId: String) extends EventedEvent
  final case class EventInvitationDeclined(id: String, version: Long, eventId: String, userId: String) extends EventedEvent
  final case class EventJoinRequested(id: String, version: Long, userIds: Seq[String]) extends EventedEvent

  final case class UsersEventJoinRequested(id: String, version: Long, eventId: String) extends EventedEvent
  final case class UserJoinEventRequestAccepted(id: String, version: Long, eventId: String, userId: String) extends EventedEvent
  final case class UserJoinEventRequestDeclined(id: String, version: Long, eventId: String, userId: String) extends EventedEvent
  //final case class EventPrivilegeSet(id: String, version: Long, maybeAccess: Boolean) extends EventedEvent
  final case class EventLeft(id: String, version: Long, eventId: String, userId: String)
  final case class EventFollowed(id: String, version: Long, eventId: String, userId: String) extends EventedEvent
  final case class EventUnfollowed(id: String, version: Long, eventId: String, userId: String) extends EventedEvent

  final case class ErrorMessage(data: String)
  //final case class GetEvent(id: String)

  case object SnapshotEventedEvents
}
