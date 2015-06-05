package info.siddhuw.models.daos

import info.siddhuw.models.APISchema._
import info.siddhuw.models.User
import org.squeryl.PrimitiveTypeMode

/**
 * @author Siddhu Warrier
 */


object UserDao extends PrimitiveTypeMode {
  def find(username: String): Option[User] = {
    transaction {
      users.where(_.username === username).singleOption
    }
  }
}
