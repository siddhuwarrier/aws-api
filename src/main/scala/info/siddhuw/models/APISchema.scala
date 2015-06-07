package info.siddhuw.models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema

/**
 * @author Siddhu Warrier
 */

object APISchema extends Schema {
  val dbUsers = table[DBUser]("db_user")
  on(dbUsers) {
    u ⇒
      declare(u.username is unique)
  }
}
