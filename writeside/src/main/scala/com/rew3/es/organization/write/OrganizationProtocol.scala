package com.rew3.es.organization.write

import com.rew3.es.common.{Event, Command}
import com.rew3.es.Address

/**
 * This object contains all the commands and events as well as other messages that the event aggregate may process.
 */
object OrganizationProtocol {

  /**
  * These are the sealed commands of every action that happens with an event. Commands can be rejected.
  */
  sealed trait OrganizationCommand extends Command {
    def id: String
    def expectedVersion: Long
  }

  case class SendOrganizationCommands(orgId: OrganizationId, command: OrganizationCommand) extends Command
//if event does not exist
  case object OrganizationDoesNotExist

  final case class CreateOrganization(id: String, expectedVersion: Long = -1L, name: String, description: String,
                               category: String, website: String, addresses: Seq[Address], contactPersons: Seq[String],
                                      street: String, city: String, stateOrProvince: String, zip: String,
                                      country: String) extends OrganizationCommand
  final case class ChangeOrganizationName(id: String, expectedVersion: Long,
                                           name: String) extends OrganizationCommand
  final case class ChangeOrganizationDescription(id: String, expectedVersion: Long,
                                                 description: String) extends OrganizationCommand
  final case class ChangeOrganizationCategory(id: String, expectedVersion: Long,
                                              category: String) extends OrganizationCommand
  final case class ChangeOrganizationAddress(id: String, expectedVersion: Long,
                                             street: String, city: String, stateOrProvince: String,
                                         country: String, zip: String) extends OrganizationCommand
  final case class ChangeOrganizationWebsite(id: String, expectedVersion: Long,
                                             website: String) extends OrganizationCommand
  final case class ValidateOrganization(id: String, expectedVersion: Long,
                                             validated: Boolean) extends OrganizationCommand
  final case class GetOrganization(id: String, expectedVersion: Long) extends OrganizationCommand

  /**
   * These are the resulting events from commands if the commands were not rejected. They will be journaled
   * in the event store and cannot be rejected.
  */
  sealed trait OrganizationEvent extends Event {
    def id: String
    def version: Long
  }

  final case class OrganizationCreated(id: String, version: Long, title: String, description: String,
                                eventStart: Long, eventEnd: Long, eventDate: Long, eventType: String, access: Boolean,
                                maxParticipants: Int, recurrenceGroupId: Int, recurrenceInterval: Int,
                                recurrenceTimes: Int, street: String, city: String, stateOrProvince: String,
                                zip: String, country: String, eventLink: String,
                                cancelledDate: Long) extends OrganizationEvent
  final case class OrganizationTitleChanged(id: String, version: Long, name: String) extends OrganizationEvent
  final case class OrganizationDescriptionChanged(id: String, version: Long,
                                                  description: String) extends OrganizationEvent
  final case class OrganizationCategoryChanged(id: String, version: Long, category: String) extends OrganizationEvent
  final case class OrganizationAddressChanged(id: String, version: Long,
                                              street: String, city: String, stateOrProvince: String,
                                       country: String, zip: String) extends OrganizationEvent
  final case class OrganizationWebsiteChanged(id: String, version: Long, website: String) extends OrganizationEvent
  final case class OrganizationValidated(id: String, version: Long, validated: Boolean) extends OrganizationEvent

  final case class SubOrganizationCreated(id: String, version: Long,
                                          parentOrganizationId: String) extends OrganizationEvent
  final case class OrganizationUserInvited(id: String, version: Long, userId: String) extends OrganizationEvent
  final case class OrganizationInvitationAccepted(id: String, version: Long,
                                                  eventId: String, userId: String) extends OrganizationEvent
  final case class OrganizationInvitationDeclined(id: String, version: Long,
                                                  eventId: String, userId: String) extends OrganizationEvent
  final case class OrganizationJoinRequested(id: String, version: Long, userIds: Seq[String]) extends OrganizationEvent

  final case class UsersOrganizationJoinRequested(id: String, version: Long, eventId: String) extends OrganizationEvent
  final case class UserJoinOrganizationRequestAccepted(id: String, version: Long,
                                                       eventId: String, userId: String) extends OrganizationEvent
  final case class UserJoinOrganizationRequestDeclined(id: String, version: Long,
                                                       eventId: String, userId: String) extends OrganizationEvent
  //final case class OrganizationPrivilegeSet(id: String, version: Long, maybeAccess: Boolean) extends OrganizationEvent
  final case class OrganizationLeft(id: String, version: Long, eventId: String, userId: String)
  final case class OrganizationFollowed(id: String, version: Long, eventId: String,
                                        userId: String) extends OrganizationEvent
  final case class OrganizationUnfollowed(id: String, version: Long, eventId: String,
                                          userId: String) extends OrganizationEvent

  final case class ErrorMessage(data: String)
  //final case class GetOrganization(id: String)
  case object SnapshotEventedOrganizations

}
