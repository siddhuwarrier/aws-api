package info.siddhuw.models.daos

import info.siddhuw.models.APISchema._
import info.siddhuw.models.DBUser
import org.squeryl.PrimitiveTypeMode

/**
 * @author Siddhu Warrier
 */

class DBUserDAO extends PrimitiveTypeMode {
  def findById(username: String): Option[DBUser] = {
    transaction {
      dbUsers.where(_.username === username).singleOption
    }
  }
}
