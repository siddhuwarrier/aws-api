package info.siddhuw.auth

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

import com.google.common.net.HttpHeaders._
import com.typesafe.config.ConfigFactory
import info.siddhuw.models.DBUser
import info.siddhuw.models.daos.DBUserDAO
import info.siddhuw.services.JWTTokenService
import net.logstash.logback.marker.Markers._
import org.scalatra.ScalatraBase
import org.scalatra.auth.ScentryStrategy
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._
import scala.concurrent.{ Future, ExecutionContext, Await }
import scala.util.{ Failure, Success, Try }
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * @author Siddhu Warrier
 */
class TokenStrategy(protected val app: ScalatraBase, val userDao: DBUserDAO)(implicit val ec: ExecutionContext) extends ScentryStrategy[DBUser] {
  val tokenService = new JWTTokenService(userDao)
  val logger = LoggerFactory.getLogger(classOf[TokenStrategy])
  val config = ConfigFactory.load("app")

  override def name = TokenStrategy.Name

  override def isValid(implicit request: HttpServletRequest) = {
    Option(app.request.getHeader(AUTHORIZATION)).flatMap(Some(_)).isDefined
  }

  override def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse): Option[DBUser] = {
    val logData = Map("action" -> "authenticate")
    logger.info(appendEntries(logData), "Start")

    val jwtTokenOpt = getToken
    if (isValidToken(jwtTokenOpt)) {
      val usernameInToken = tokenService.getUsername(jwtTokenOpt.get).get

      Try(Await.result(Future(userDao.findById(usernameInToken)), config.getLong("db.wait_time_sec") seconds)) match {
        case Success(user) ⇒
          logger.info(appendEntries(logData), "Done")
          user
        case Failure(e) ⇒
          logger.error(appendEntries(logData), "Failed", e)
          None
      }
    } else {
      logger.error(appendEntries(logData), "Failed: Invalid JWT token")
      None
    }
  }

  private def getToken(implicit request: HttpServletRequest): Option[String] = {
    Option(app.request.getHeader(AUTHORIZATION)) match {
      case Some(headerVal) ⇒
        Some(headerVal.replace("Bearer", "").trim)
      case None ⇒
        None
    }
  }

  private def isValidToken(authorizationHeaderOpt: Option[String]): Boolean = {
    authorizationHeaderOpt match {
      case None ⇒
        false
      case Some(headerVal) ⇒
        tokenService.isValid(headerVal)
    }
  }

}

object TokenStrategy {
  val Name = "JWTTokenStrategy"
}
