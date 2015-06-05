package info.siddhuw.models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema

/**
 * @author Siddhu Warrier
 * @date 05/06/15.
 */

object APISchema extends Schema {
  val users = table[User]

  on(users) {
    u =>
      declare(u.username is unique)
  }
}
