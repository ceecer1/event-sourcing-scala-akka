package com.rew3.es.controllers

import spray.http.{ContentType, HttpHeaders}
import spray.routing
import spray.routing.HttpService

/**
 * Created by shishir on 9/8/15.
 */
trait ParseHeader extends HttpService {

  def parseDomainModel = {
    headerValue({
      case x@HttpHeaders.`Content-Type`(value) => Some(value)
      case default => None
    })
    }

  def getRequestedCommand(h: ContentType) = h.mediaType.value.split(";")(1).split("=")(1)

  val routes: routing.Route
}

