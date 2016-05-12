package models

import play.api.libs.json.JsValue

case class SocialEvent(eventId: String, eventType: String, data: JsValue)
