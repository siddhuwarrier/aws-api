package info.siddhuw.auth

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

import com.typesafe.config.ConfigFactory
import info.siddhuw.crypto.PasswordHasher
import info.siddhuw.models.DBUser
import info.siddhuw.models.daos.DBUserDAO
import net.logstash.logback.marker.Markers._
import org.scalatra.ScalatraBase
import org.scalatra.auth.ScentryStrategy
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.language.postfixOps
import scala.util.{ Success, Failure, Try }

/**
 * @author Siddhu Warrier
 */

class UserPasswordStrategy(protected val app: ScalatraBase, val userDao: DBUserDAO,
    username: String, password: String)(implicit ec: ExecutionContext) extends ScentryStrategy[DBUser] {
  val logger = LoggerFactory.getLogger(classOf[UserPasswordStrategy])
  val config = ConfigFactory.load("app")

  override def name: String = UserPasswordStrategy.Name

  override def isValid(implicit request: HttpServletRequest) = {
    username != null && password != null && !username.trim.isEmpty && !password.trim.isEmpty
  }

  override def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse): Option[DBUser] = {
    val logData = Map("action" -> "authenticate")
    logger.info(appendEntries(logData), "Start")

    Try(Await.result(authenticateAgainstDB(username, password), config.getLong("db.wait_time_sec") seconds)) match {
      case Failure(e) ⇒
        logger.error(appendEntries(logData), "Authentication failed", e)
        None
      case Success(dbUserOpt) ⇒
        logger.info(appendEntries(logData), "Done")
        dbUserOpt
    }
  }

  private def authenticateAgainstDB(username: String, password: String): Future[Option[DBUser]] = Future {
    val logData = Map("action" -> "authenticate-against-db")

    userDao.findById(username) match {
      case Some(user) if PasswordHasher.hash(password, user.salt) == user.pwHash ⇒
        Some(user)
      case _ ⇒
        None
    }
  }
}

object UserPasswordStrategy {
  val Name = "UserPassword"

}
