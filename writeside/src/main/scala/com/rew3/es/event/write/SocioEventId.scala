package com.rew3.es.event.write

import java.util.UUID

import com.rew3.es.common.Id

object SocioEventId {
  def createRandom = SocioEventId(UUID.randomUUID().toString)
}

case class SocioEventId(override val value: String) extends AnyVal with Id[SocioEvent]

