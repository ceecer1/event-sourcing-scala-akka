package query.common

import play.api.libs.json._

object AccessType extends Enumeration {
  type AccessType = Value
  val PRIVATE_OPEN, PUBLIC_CLOSE = Value

  implicit val readReferenceType: Reads[AccessType] = EnumUtils.enumReads(AccessType)
  implicit val writeReferenceType: Writes[AccessType] = EnumUtils.enumWrites
}

object EnumUtils {

  def enumReads[E <: Enumeration](enum: E): Reads[E#Value] = new Reads[E#Value] {
    def reads(json: JsValue): JsResult[E#Value] = json match {
      case JsString(s) => {
        try {
          JsSuccess(enum.withName(s))
        } catch {
          case _: NoSuchElementException => JsError(s"Enumeration expected of type: '${enum.getClass}', but it does not appear to contain the value: '$s'")
        }
      }
      case _ => JsError("String value expected")
    }
  }

  def enumWrites[E <: Enumeration]: Writes[E#Value] = new Writes[E#Value] {
    def writes(v: E#Value): JsValue = JsString(v.toString)
  }

  def enumFormat[E <: Enumeration](enum: E): Format[E#Value] = {
    Format(enumReads(enum), enumWrites)
  }
}