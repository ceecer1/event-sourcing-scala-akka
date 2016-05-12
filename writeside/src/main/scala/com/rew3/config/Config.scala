package com.rew3.config

import com.typesafe.config.ConfigFactory

/**
 * Created by shishir on 9/22/15.
 */
object Config {

  private val config = ConfigFactory.load()

  object Api {
    private val apiConfig = config.getConfig("api")

    val bindHost = apiConfig.getString("bind.host")
    val bindPort = apiConfig.getInt("bind.port")
  }

  object Events {
    private val eventsConfig = config.getConfig("events")

    val exchangeName = eventsConfig.getString("exchangeName")
  }

}
