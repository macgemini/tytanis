package configs

import com.typesafe.config.ConfigFactory

/**
  * Created by mac on 28.02.17.
  */
case class Config(trackerUrl: String)

object Config{
  def apply(): Config = {
    val configuration = ConfigFactory.load()
    val trackerUrl = configuration.getString("trackerUrl")
    new Config(trackerUrl)
  }
}
