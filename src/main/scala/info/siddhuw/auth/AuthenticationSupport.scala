package info.siddhuw.auth

import info.siddhuw.models.DBUser
import info.siddhuw.models.daos.DBUserDAO
import org.scalatra.ScalatraBase
import org.scalatra.auth.{ScentryConfig, ScentrySupport}

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author Siddhu Warrier
 */

trait AuthenticationSupport extends ScentrySupport[DBUser] {
  self: ScalatraBase ⇒
  type ScentryConfiguration = ScentryConfig

  implicit val userDao: DBUserDAO

  protected def fromSession = {
    case username: String ⇒
      userDao.findById(username).orNull
  }

  protected def toSession = {
    case user: DBUser ⇒
      user.username
  }

  protected def scentryConfig = {
    new ScentryConfig {}
  }

  protected def username: String = ""
  protected def password: String = ""
}

trait LoginAuthenticationSupport extends AuthenticationSupport {
  self: ScalatraBase ⇒

  override protected def registerAuthStrategies() {
    scentry.register(TokenStrategy.Name, app ⇒ new TokenStrategy(app, userDao))
    scentry.register(UserPasswordStrategy.Name, app ⇒ new UserPasswordStrategy(app, userDao, username, password))
  }
}

trait APIAuthenticationSupport extends AuthenticationSupport {
  self: ScalatraBase ⇒

  override protected def registerAuthStrategies() {
    scentry.register(TokenStrategy.Name, app ⇒ new TokenStrategy(app, userDao))
  }
}