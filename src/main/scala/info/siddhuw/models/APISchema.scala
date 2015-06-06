package info.siddhuw.models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema

/**
 * @author Siddhu Warrier
 * @date 05/06/15.
 */

object APISchema extends Schema {
  val twitterUsers = table[TwitterUser]("twitter_user")

  on(twitterUsers) {
    u ⇒
      declare(u.screenName is unique)
  }
}
