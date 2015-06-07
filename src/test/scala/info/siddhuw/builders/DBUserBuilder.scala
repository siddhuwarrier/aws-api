package info.siddhuw.builders

import java.util.UUID

import info.siddhuw.models.DBUser

/**
 * @author Siddhu Warrier
 */

object DBUserBuilder {
  def build(username: String = "kim.young.well", pwHash: String = UUID.randomUUID.toString,
    salt: String = UUID.randomUUID.toString): DBUser = {
    DBUser(username, pwHash, salt)
  }
}
