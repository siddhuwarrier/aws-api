package info.siddhuw.models.daos

import info.siddhuw.models.TwitterUser
import info.siddhuw.models.APISchema._
import org.squeryl.PrimitiveTypeMode

/**
 * @author Siddhu Warrier
 */


trait TwitterUserDaoComponent extends UserDaoComponent[TwitterUser] {
   override def userDao: UserDao[TwitterUser] = new TwitterUserDao()

  class TwitterUserDao extends UserDao[TwitterUser] with PrimitiveTypeMode {
    override def findById(screenName: String): Option[TwitterUser] = {
      transaction {
        twitterUsers.where(_.screenName === screenName).singleOption
      }
    }
  }
}
