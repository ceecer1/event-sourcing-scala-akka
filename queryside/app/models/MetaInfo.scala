package models

import org.joda.time.DateTime
import play.api.libs.json._

case class MetaInfo(_version: Long = 0L,
                    _created: Option[DateTime] = None, //insert date
                    _createdBy: Option[String] = None, //inserting user
                    _lastModified: Option[DateTime] = None,
                    _modifiedBy: Option[String] = None,
                    _owner: Option[String] = None,
                    _member: Option[String] = None,
                    _master: Option[String] = None)

object MetaInfo {

  implicit val format = Json.format[MetaInfo]
  def create(implicit context: RequestContext) = {
    MetaInfo(_owner = Some(context.user), _member = Some(context.member))
  }
}
