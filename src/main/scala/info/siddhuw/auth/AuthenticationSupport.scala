package info.siddhuw.auth

import info.siddhuw.models.TwitterUser
import info.siddhuw.models.daos.UserDao
import org.scalatra.ScalatraBase
import org.scalatra.auth.{ ScentryConfig, ScentrySupport }

/**
 * @author Siddhu Warrier
 */

trait AuthenticationSupport extends ScentrySupport[String] {
  self: ScalatraBase ⇒

  type ScentryConfiguration = ScentryConfig

  implicit val userDao: UserDao[TwitterUser]

  override protected def registerAuthStrategies() {
    scentry.register(TokenStrategy.Name, app ⇒ new TokenStrategy(app, userDao))
  }

  protected def fromSession = {
    case screenName: String ⇒
      screenName
  }

  protected def toSession = {
    case screenName: String ⇒
      screenName
  }

  protected def scentryConfig = {
    new ScentryConfig {}
  }
}