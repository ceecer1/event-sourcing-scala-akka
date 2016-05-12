package query.blog

import models.{MetaInfoAware, Identifiable, MetaInfo}
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID

case class Blog (_id: Option[String] = Option(BSONObjectID.generate.stringify),
                  meta: Option[MetaInfo],
                  title: Option[String] = None,
                  body: Option[String] = None) extends Identifiable with MetaInfoAware

object Blog{
  implicit  val format = Json.format[Blog]
}
