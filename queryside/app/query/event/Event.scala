package query.event

import models.{MetaInfo, Identifiable, MetaInfoAware}
import org.joda.time.DateTime
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import query.common.AccessType.AccessType

/**
 * Created by shishir on 9/24/15.
 */
case class Event(_id: Option[String] = Option(BSONObjectID.generate.stringify),
                 meta: Option[MetaInfo],
                 name: Option[String] = None,
                 access: Option[AccessType] = None,
                 title: Option[String] = None,
                 description: Option[String] = None,
                 isPhysical: Option[Boolean] = Some(true),
                 location: Option[CollaborationLocation] = None,
                 contacts: Option[CollaborationContact] = None,
                 startDateTime: Option[DateTime] = None,
                 endDateTime: Option[DateTime] = None
                  ) extends Identifiable with MetaInfoAware

case class CollaborationLocation(address1: Option[String] = None,
                                 address2: Option[String] = None,
                                 zip: Option[String] = None,
                                 city: Option[String] = None,
                                 state: Option[String] = None,
                                 country: Option[String] = None)

object CollaborationLocation {
  implicit val collaborationLocationFormat = Json.format[CollaborationLocation]
}

case class CollaborationContact(name: Option[String] = None,
                                email: Option[Seq[String]] = None,
                                phone: Option[Seq[String]] = None)

object CollaborationContact {
  implicit val collaborationContactFormat = Json.format[CollaborationContact]
}

