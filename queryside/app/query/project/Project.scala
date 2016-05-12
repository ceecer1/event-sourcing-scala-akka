package query.project

import models.{MetaInfoAware, Identifiable, MetaInfo}
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import query.common.AccessType.AccessType
import query.event.CollaborationContact


case class Tag(id: Option[String] = None,
               tag: Option[String])

object Tag {
  implicit val semiTagFormat = Json.format[Tag]
}

case class Data(tags: Tag,
                title: String,
                description: String,
                contacts: CollaborationContact)

object Data{
  implicit val dataFormat = Json.format[Data]
}

case class Project(_id: Option[String] = Option(BSONObjectID.generate.stringify),
                   meta: Option[MetaInfo],
                   projectUser: Option[Seq[String]] = None,
                   ownerId: Option[String] = None,
                   name: Option[String] = None,
                   data: Option[Data] = None,
                   creatorId: Option[String] = None,
                   access: Option[AccessType] = None   //Flags.Access.Open or Flags.Access.Closed
                    ) extends Identifiable with MetaInfoAware
