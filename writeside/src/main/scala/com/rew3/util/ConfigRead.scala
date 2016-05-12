package com.rew3.util

import com.typesafe.config.ConfigFactory

/**
 * Created by shishir on 8/27/15.
 */
object ConfigRead {

  private val config = ConfigFactory.load()

  val clusterName = config.getString("host.name")

}
