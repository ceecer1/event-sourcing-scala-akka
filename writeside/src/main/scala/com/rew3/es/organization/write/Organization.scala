package com.rew3.es.organization.write

import akka.actor.{ActorLogging, Props, ReceiveTimeout}
import akka.persistence.{PersistentActor, SnapshotOffer}
import com.rew3.es.common.{CommandAccepted, CommandRejected}
import com.rew3.es.{Address, CommonValidations, DomainValidation, ValidationKey}

import scala.concurrent.duration._
import scalaz.Scalaz._
import scalaz._

/**
 * An Organization domain aggregate trait to be extended by ActiveOrganization, InactiveOrganization, etc.
 */
sealed trait Organization {
  def id: String
  def version: Long
  def name: String
  def description: String
  def category: String
  def website: String
  def addresses: Seq[Address]
  def validated: Boolean
  def primaryAddress: Address
  def contactPersons: Seq[String]
}

/**
 * Companion object for Organization abstraction that handles version validation.
 */
object Organization {
  import OrganizationProtocol._

  def requireVersion[A <: Organization](e: A, cmd: OrganizationCommand): DomainValidation[A] = {
    if (cmd.expectedVersion == e.version) e.successNel
    else s"$e version does not match $cmd version".failureNel
  }
}

/**
 * Validations trait for Organization abstractions.
 */
trait OrganizationValidations {
  case object IdRequired extends ValidationKey
  case object NameRequired extends ValidationKey
  case object DescriptionRequired extends ValidationKey
  case object CategoryRequired extends ValidationKey
  case object WebsiteRequired extends ValidationKey
  case object AddressRequired extends ValidationKey
  case object ValidateRequired extends ValidationKey


  def validateOrganization(validated: Boolean): Validation[String, Boolean] = {
    if (validated != true) s"$validated must be true in order to activate organization $validated".failure else
      validated.success
  }

}

/**
 * Case class that represents the state of an active Organization.
 */
case class ActiveOrganization (
  id: String,
  version: Long,
  name: String,
  description: String,
  category: String,
  website: String,
  addresses: Seq[Address],
  validated: Boolean,
  primaryAddress: Address,
  contactPersons: Seq[String]) extends Organization with OrganizationValidations {

  import CommonValidations._

  def withName(name: String): DomainValidation[ActiveOrganization] =
    checkString(name, NameRequired) fold(f => f.failureNel, s => copy(version = version + 1, name = s).success)

  def withDescription(description: String): DomainValidation[ActiveOrganization] =
    checkString(description, DescriptionRequired) fold(f => f.failureNel, s => copy(version = version + 1,
      description = s).success)

  def withCategory(category: String): DomainValidation[ActiveOrganization] =
    checkString(category, CategoryRequired) fold(f => f.failureNel, s => copy(version = version + 1,
      category = s).success)

  def withWebsite(website: String): DomainValidation[ActiveOrganization] =
    checkString(website, WebsiteRequired) fold(f => f.failureNel, s => copy(version = version + 1,
      website = s).success)

  def withPrimaryAddress(street: String, city: String, stateOrProvince: String, country: String,
                         zip: String): DomainValidation[ActiveOrganization] =
    Address.validate(street, city, stateOrProvince, country, zip) fold(f => f.failure, s => copy(version = version + 1,
      primaryAddress = s).success)

  def withOrganizationValidation(validated: Boolean): DomainValidation[ActiveOrganization] =
    checkBoolean(validated, ValidateRequired) fold (f => f.failureNel, s => copy(version = version + 1,
      validated = s).success)

}
/**
* Companion object to create ActiveOrganization
*/
object ActiveOrganization extends OrganizationValidations {
  import CommonValidations._
  import OrganizationProtocol._

  def create(cmd: CreateOrganization): DomainValidation[ActiveOrganization] =
    (checkString(cmd.id, IdRequired).toValidationNel |@|
      0L.successNel |@|
      checkString(cmd.name, NameRequired).toValidationNel |@|
      checkString(cmd.description, DescriptionRequired).toValidationNel |@|
      checkString(cmd.category, CategoryRequired).toValidationNel |@|
      checkString(cmd.website, WebsiteRequired).toValidationNel |@|
      Address.validate(cmd.street, cmd.city, cmd.stateOrProvince,
        cmd.country, cmd.zip)) { (id, v, n, d, c, w, paddr) =>
      ActiveOrganization(id, v, n, d, c, w, cmd.addresses, true, paddr, cmd.contactPersons)
    }
}

/**
 * Case class that represents the state of an inactive Organization.
 */
case class InactiveOrganization (
                                  id: String,
                                  version: Long,
                                  name: String,
                                  description: String,
                                  category: String,
                                  website: String,
                                  addresses: Seq[Address],
                                  validated: Boolean,
                                  primaryAddress: Address,
                                  contactPersons: Seq[String]) extends Organization with OrganizationValidations {

  def validate(validated: Boolean): DomainValidation[ActiveOrganization] =
    (validateOrganization(validated).toValidationNel |@|
      validateOrganization(validated).toValidationNel) { (s1, s2) =>
      ActiveOrganization(this.id, this.version + 1, this.name, this.description, this.category, this.website,
        this.addresses, s1, this.primaryAddress, this.contactPersons)
    }
}


/**
 * Case class the contains the in-memory current state of all [[Organization]] aggregates.
 * @param Organizations Map[String, Organization] that contains the current state of all [[Organization]] aggregates.
 */
final case class OrganizationState(Organizations: Map[String, Organization] = Map.empty) {
  def update(e: Organization) = copy(Organizations = Organizations + (e.id -> e))
  def get(id: String) = Organizations.get(id)
  def getActive(id: String) = get(id) map (_.asInstanceOf[ActiveOrganization])
  def getActiveAll = Organizations map (_._2.asInstanceOf[ActiveOrganization])
  def getInactive(id: String) = get(id) map (_.asInstanceOf[InactiveOrganization])
}


object OrganizationProcessor {
  def props(id: OrganizationId) = Props(new OrganizationProcessor(id))

}


/**
 * The OrganizationProcessor is responsible for maintaining  state changes for all [[Organization]] aggregates. This particular
 * processor uses Akka-Persistence's [[PersistentActor]]. It receives Commands and if valid will persist the generated events,
 * afterwhich it will updated the current state of the [[Organization]] being processed.
 */
class OrganizationProcessor(id: OrganizationId) extends PersistentActor with ActorLogging {
  import OrganizationProtocol._

  override def persistenceId = id.value

  context.setReceiveTimeout(20000 milliseconds)

  var state = OrganizationState()

  def updateState(evt: Organization): Unit =
    state = state.update(evt)

  /**
   * These are the events that are recovered during journal recovery. They cannot fail and must be processed to recreate the current
   * state of the aggregate.
  */


  val receiveRecover: Receive = {
    case evt: OrganizationCreated =>
      updateState(ActiveOrganization(evt.id, evt.version, evt.title, evt.description, evt.title,
      evt.eventLink, Nil, true, Address(evt.street,
          evt.city, evt.stateOrProvince, evt.country, evt.zip), Nil))
    case evt: OrganizationTitleChanged =>
      updateState(state.getActive(evt.id).get.copy(version = evt.version, name = evt.name))
    case evt: OrganizationDescriptionChanged =>
      updateState(state.getActive(evt.id).get.copy(version = evt.version, description = evt.description))
    case evt: OrganizationCategoryChanged=>
      updateState(state.getActive(evt.id).get.copy(version = evt.version, category = evt.category))
    case evt: OrganizationAddressChanged =>
      updateState(state.getActive(evt.id).get.copy(version = evt.version, primaryAddress = Address(evt.street, evt.city,
        evt.stateOrProvince, evt.country, evt.zip)))
    case evt: OrganizationWebsiteChanged =>
      updateState(state.getActive(evt.id).get.copy(version = evt.version, website = evt.website))
    case evt: OrganizationValidated =>
      updateState(state.getActive(evt.id).get.copy(version = evt.version, validated = evt.validated))
    case evt: OrganizationUserInvited =>

    case evt: OrganizationInvitationAccepted =>

    case evt: OrganizationInvitationDeclined =>

    case evt: OrganizationJoinRequested =>

    //case evt: OrganizationGetListRequested =>

    case evt: UsersOrganizationJoinRequested =>

    case evt: UserJoinOrganizationRequestAccepted =>

    case evt: UserJoinOrganizationRequestDeclined =>

    //case evt: OrganizationPrivilegeSet =>

    case evt: OrganizationLeft =>

    case evt: OrganizationFollowed =>

    case evt: OrganizationUnfollowed =>

    case evt: OrganizationFollowed =>

    case SnapshotOffer(_, snapshot: OrganizationState) => state = snapshot

  }

  /**
   * These are the commands that are requested. As command they can fail sending response back to the user. Each command will
   * generate one or more events to be journaled.
   */
  val receiveCommand: Receive = {
    case cmd: CreateOrganization => create(cmd) fold (
      f => sender ! ErrorMessage(s"error $f occurred on $cmd"),
      s => persist(OrganizationCreated(s.id, s.version, s.name, s.description, 1L, 1L, 1L, "type",
        s.validated, 1, 1, 1, 1, s.primaryAddress.street, s.primaryAddress.city,
        s.primaryAddress.stateOrProvince, s.primaryAddress.zip, s.primaryAddress.country, s.website,
        1L)) { event =>
          updateState(s)
          context.system.eventStream.publish(event)
          sender() ! CommandAccepted
        })

    case cmd: ChangeOrganizationName => changeOrganizationName(cmd) fold (
      f => sender ! ErrorMessage(s"error $f occurred on $cmd"),
      s => persist(OrganizationTitleChanged(s.id, s.version, s.name)) { event =>
        updateState(s)
        context.system.eventStream.publish(event)
        sender() ! CommandAccepted
      })
    case cmd: ChangeOrganizationDescription => changeFirstName(cmd) fold (
      f => sender ! ErrorMessage(s"error $f occurred on $cmd"),
      s => persist(OrganizationDescriptionChanged(s.id, s.version, s.description)) { event =>
        updateState(s)
        context.system.eventStream.publish(event)
        sender() ! CommandAccepted
      })
    case cmd: ChangeOrganizationAddress => changeAddress(cmd) fold (
      f => sender ! ErrorMessage(s"error $f occurred on $cmd"),
      s => persist(OrganizationAddressChanged(s.id, s.version, s.primaryAddress.street, s.primaryAddress.city,
        s.primaryAddress.stateOrProvince, s.primaryAddress.country, s.primaryAddress.zip)) { event =>
          updateState(s)
          context.system.eventStream.publish(event)
      })
    case cmd: ChangeOrganizationCategory => changeOrganizationType(cmd) fold (
      f => sender ! ErrorMessage(s"error $f occurred on $cmd"),
      s => persist(OrganizationCategoryChanged(s.id, s.version, s.category)) { event =>
        updateState(s)
        context.system.eventStream.publish(event)
      })
    case cmd: ChangeOrganizationWebsite => changeOrganizationLink(cmd) fold (
      f => sender ! ErrorMessage(s"error $f occurred on $cmd"),
      s => persist(OrganizationWebsiteChanged(s.id, s.version, s.website)) { event =>
        updateState(s)
        context.system.eventStream.publish(event)
      })
    case cmd: ValidateOrganization => changeOrganizationAccess(cmd) fold (
      f => sender ! ErrorMessage(s"error $f occurred on $cmd"),
      s => persist(OrganizationValidated(s.id, s.version, s.validated)) { event =>
        updateState(s)
        context.system.eventStream.publish(event)
      })
  /*  case cmd: ActivateOrganization => activate(cmd) fold (
      f => sender ! ErrorMessage(s"error $f occurred on $cmd"),
      s => persist(OrganizationActivated(s.id, s.version, s.startDate)) { event =>
        updateState(s)
        context.system.eventStream.publish(event)
      })*/

    case cmd: GetOrganization   =>
      /*val orgOpt = state.get(cmd.id).getOrElse(CommandRejected(s"The requested organization with $id does not exist"))*/
      val orgOpt = CommandRejected(s"The requested organization with $id does not exist")
      sender ! orgOpt
      self ! SnapshotEventedOrganizations
    case SnapshotEventedOrganizations  => saveSnapshot(state)
    case "print"            => println("STATE: " + state)

    case ReceiveTimeout =>
      // To turn it off
      //throw new RuntimeException("Receive timed out")
      println("receive timed out and shutting down")
      //context.stop(self)
      context.setReceiveTimeout(Duration.Undefined)
  }

  def create(cmd: CreateOrganization): DomainValidation[ActiveOrganization] =
    state.get(cmd.id) match {
      case Some(evt) => s"Organization for $cmd already exists".failureNel
      case None      => ActiveOrganization.create(cmd)
    }

  def changeOrganizationName(cmd: ChangeOrganizationName): DomainValidation[ActiveOrganization] =
    updateActive(cmd) { e => e.withName(cmd.name) }

  def changeFirstName(cmd: ChangeOrganizationDescription): DomainValidation[ActiveOrganization] =
    updateActive(cmd) { e => e.withDescription(cmd.description) }

  def changeAddress(cmd: ChangeOrganizationAddress): DomainValidation[ActiveOrganization] =
    updateActive(cmd) { e => e.withPrimaryAddress(cmd.street, cmd.city, cmd.stateOrProvince, cmd.country, cmd.zip) }

  def changeOrganizationType(cmd: ChangeOrganizationCategory): DomainValidation[ActiveOrganization] =
    updateActive(cmd) { e => e.withCategory(cmd.category) }

  def changeOrganizationLink(cmd: ChangeOrganizationWebsite): DomainValidation[ActiveOrganization] =
    updateActive(cmd) { e => e.withWebsite(cmd.website) }

  def changeOrganizationAccess(cmd: ValidateOrganization): DomainValidation[ActiveOrganization] =
    updateActive(cmd) { e => e.withOrganizationValidation(cmd.validated) }

  def updateOrganization[A <: Organization](cmd: OrganizationCommand)(fn: Organization => DomainValidation[A]): DomainValidation[A] =
    state.get(cmd.id) match {
      case Some(evt) => Organization.requireVersion(evt, cmd) fold (f => f.failure, s => fn(s))
      case None      => s"Organization for $cmd does not exist".failureNel
    }

  def updateActive[A <: Organization](cmd: OrganizationCommand)(fn: ActiveOrganization => DomainValidation[A]): DomainValidation[A] =
    updateOrganization(cmd) {
      case evt: ActiveOrganization => fn(evt)
      case evt              => s"$evt for $cmd is not active".failureNel
    }

  def updateInactive[A <: Organization](cmd: OrganizationCommand)(fn: InactiveOrganization => DomainValidation[A]): DomainValidation[A] =
    updateOrganization(cmd) {
      case evt: InactiveOrganization => fn(evt)
      case evt                => s"$evt for $cmd is not inactive".failureNel
    }

}
