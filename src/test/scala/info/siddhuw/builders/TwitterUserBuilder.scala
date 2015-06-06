package info.siddhuw.builders

import java.util.UUID

import info.siddhuw.models.TwitterUser

/**
 * @author Siddhu Warrier
 */

object TwitterUserBuilder {
  def build(screenName: String = UUID.randomUUID.toString): TwitterUser = {
    TwitterUser(screenName)
  }
}
