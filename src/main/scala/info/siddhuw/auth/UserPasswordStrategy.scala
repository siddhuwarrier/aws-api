package info.siddhuw.auth

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.typesafe.scalalogging.LazyLogging
import info.siddhuw.crypto.PasswordHasher
import info.siddhuw.models.User
import info.siddhuw.models.daos.UserDao
import org.scalatra.auth.ScentryStrategy
import org.scalatra.{CookieOptions, ScalatraBase}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
/**
 * @author Siddhu Warrier
 */


class UserPasswordStrategy(protected val app: ScalatraBase, username: String, password: String)
                          (implicit ec: ExecutionContext) extends ScentryStrategy[User]
with LazyLogging {

  override def name: String = UserPasswordStrategy.Name

  override def isValid(implicit request: HttpServletRequest) = {
    !username.trim.isEmpty && !password.trim.isEmpty
  }

  override def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse): Option[User] = {
    Await.result(doAuthenticate(username, password), UserResolutionTimeout)
  }

  override def afterAuthenticate(winningStrategy: String, user: User)(implicit request: HttpServletRequest, response: HttpServletResponse): Unit = {
    //TODO switch to generating token

    app.cookies.set(CookieKey, user.username)(CookieOptions(path = "/", maxAge = Duration(1, DAYS).toSeconds.toInt,
      secure = true))

    logger.debug("Cookies: " + app.cookies.get(CookieKey))
  }

  private def doAuthenticate(username: String, password: String): Future[Option[User]] = Future {
    UserDao.find(username) match {
      case Some(user) if PasswordHasher.hash(password, user.salt) == user.pwHash =>
        Some(user)
      case _ =>
        None
    }
  }
}

object UserPasswordStrategy {
  val Name = "UserPassword"

}
