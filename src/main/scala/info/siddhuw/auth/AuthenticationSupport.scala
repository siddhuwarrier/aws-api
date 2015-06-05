package info.siddhuw.auth

import info.siddhuw.models.User
import info.siddhuw.models.daos.UserDao
import org.scalatra.ScalatraBase
import org.scalatra.auth.{ScentryConfig, ScentrySupport}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author Siddhu Warrier
 */


trait AuthenticationSupport extends ScentrySupport[User] {
  self: ScalatraBase =>

  override protected def registerAuthStrategies() {
    scentry.register(CookieStrategy.Name, app => new CookieStrategy(app))
    //fallback strategy if cookie auth fails
    scentry.register(UserPasswordStrategy.Name, app => new UserPasswordStrategy(app, username, password))
  }

  protected def username: String = ""

  protected def password: String = ""

  protected def fromSession = {
    case username: String =>
      UserDao.find(username) match {
        case Some(user) =>
          user
        case _ =>
          null
      }
  }

  protected def toSession = {
    case user: User => user.username
  }

  // Define type to avoid casting as (new ScentryConfig {}).asInstanceOf[ScentryConfiguration]
  type ScentryConfiguration = ScentryConfig

  protected def scentryConfig = {
    new ScentryConfig {}
  }
}

trait CookieSupport extends AuthenticationSupport {
  self: ScalatraBase =>

  before() {
    if(!isAuthenticated) {
      scentry.authenticate(CookieStrategy.Name)
    }
  }
}