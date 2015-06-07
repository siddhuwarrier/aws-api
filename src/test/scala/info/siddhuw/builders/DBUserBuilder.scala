package info.siddhuw.builders

import java.util.UUID

import info.siddhuw.models.DBUser

/**
 * @author Siddhu Warrier
 */

object DBUserBuilder {
  def build(username: String = UUID.randomUUID.toString): DBUser = {
    DBUser(username, UUID.randomUUID.toString, UUID.randomUUID.toString)
  }
}
