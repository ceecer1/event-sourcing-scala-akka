package config

import com.typesafe.config.ConfigFactory

object Config {

  private val config = ConfigFactory.load()

  object Events {
    private val eventsConfig = config.getConfig("events")

    val exchangeName = eventsConfig.getString("exchangeName")
  }

  object Social {
    private val socialConfig = config.getConfig("social")

    val apiUrl = socialConfig.getString("apiUrl")
  }

}
