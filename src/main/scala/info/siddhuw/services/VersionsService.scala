package info.siddhuw.services

import com.typesafe.config.ConfigFactory

class VersionsService {
  def getVersion(): String = {
    val gitConfig = ConfigFactory.load("git.properties")
    Option(gitConfig.getString("git.commit.id.full")).getOrElse("unknown")
  }

}
