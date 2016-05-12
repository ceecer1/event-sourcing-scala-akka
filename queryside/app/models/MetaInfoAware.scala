package models

trait MetaInfoAware {
  def meta: Option[MetaInfo]
}
