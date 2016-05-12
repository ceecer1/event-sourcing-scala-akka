package com.rew3.es.organization.write

import java.util.UUID

import com.rew3.es.common.Id

object OrganizationId {
  def createRandom = OrganizationId(UUID.randomUUID().toString)
}

case class OrganizationId(override val value: String) extends AnyVal with Id[Organization]

