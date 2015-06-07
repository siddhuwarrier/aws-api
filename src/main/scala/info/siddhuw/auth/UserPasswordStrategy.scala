package info.siddhuw.auth

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

import com.typesafe.scalalogging.LazyLogging
import info.siddhuw.crypto.PasswordHasher
import info.siddhuw.models.DBUser
import info.siddhuw.models.daos.DBUserDAO
import org.scalatra.ScalatraBase
import org.scalatra.auth.ScentryStrategy

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
/**
 * @author Siddhu Warrier
 */

class UserPasswordStrategy(protected val app: ScalatraBase, val userDao: DBUserDAO,
  username: String, password: String)(implicit ec: ExecutionContext) extends ScentryStrategy[DBUser]
    with LazyLogging {
  override def name: String = UserPasswordStrategy.Name

  override def isValid(implicit request: HttpServletRequest) = {
    !username.trim.isEmpty && !password.trim.isEmpty
  }

  override def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse): Option[DBUser] = {
    authenticateAgainstDB(username, password)
  }

  //TODO make async
  private def authenticateAgainstDB(username: String, password: String): Option[DBUser] = {
    userDao.findById(username) match {
      case Some(user) if PasswordHasher.hash(password, user.salt) == user.pwHash ⇒
        Some(user)
      case None ⇒
        None
    }
  }
}

object UserPasswordStrategy {
  val Name = "UserPassword"

}
