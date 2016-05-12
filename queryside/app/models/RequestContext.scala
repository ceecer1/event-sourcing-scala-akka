package models

import play.api.i18n.Lang
import play.api.libs.json._

case class RequestContext(member: String,
                          user: String,
                          memberId: Option[String] = None,
                          lang: Lang = Lang("fr"),
                          command: String = null,
                          eTag: String = null ) {

  def withCommand(command: String) = this.copy(command = command)
  def withETag(eTag: String) = this.copy(eTag = eTag)
}


object RequestContext {

  implicit object LangFormat extends Format[Lang] {
    def reads(json: JsValue): JsResult[Lang] = (
      (json \ "lang").validate[String].map { lang =>
        Lang(lang)
      }
      )

    def writes(lang: Lang): JsValue = JsObject(Seq(
      "lang" -> JsString(lang.language)
    ))
  }
  implicit val format = Json.format[RequestContext]
}