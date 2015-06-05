package info.siddhuw.auth

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

import info.siddhuw.models.User
import info.siddhuw.models.daos.UserDao
import org.scalatra.ScalatraBase
import org.scalatra.auth.ScentryStrategy

import scala.concurrent.{ExecutionContext, Await, Future}

/**
 * @author Siddhu Warrier
 */


class CookieStrategy(protected val app: ScalatraBase)(implicit ec: ExecutionContext) extends ScentryStrategy[User] {
  override def name = CookieStrategy.Name


  //TODO refresh token after authentication

  override def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse): Option[User] = {
    //TODO check tokens in redis
    app.cookies.get(CookieKey).flatMap(
      cookieUsername =>
        Await.result(Future(UserDao.find(cookieUsername)), UserResolutionTimeout)
    )
  }

  override def isValid(implicit request: HttpServletRequest) = {
    app.cookies.get(CookieKey).flatMap(Some(_)).isDefined
  }
}

object CookieStrategy {
  val Name = "Cookie"
}
