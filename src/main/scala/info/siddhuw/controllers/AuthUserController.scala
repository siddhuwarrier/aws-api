package info.siddhuw.controllers

import info.siddhuw.auth.LoginAuthenticationSupport
import info.siddhuw.controllers.AuthUserController._
import info.siddhuw.models.daos.DBUserDAO
import info.siddhuw.services.JWTTokenService
import net.logstash.logback.marker.Markers._
import org.json4s.{ DefaultFormats, Formats }
import org.scalatra.{ CorsSupport, Unauthorized }
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

/**
 * @author Siddhu Warrier
 */

class AuthUserController(implicit val userDao: DBUserDAO) extends JsonController
    with LoginAuthenticationSupport
    with CorsSupport {
  val tokenService = new JWTTokenService(userDao)
  val logger = LoggerFactory.getLogger(classOf[AuthUserController])

  options("/*") {
    response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"))
  }

  override protected implicit def jsonFormats: Formats = DefaultFormats

  post("/") {
    val logData = Map("endpoint" -> "POST /")

    authenticate() match {
      case Some(dbUser) ⇒

        logger.info(appendEntries(logData + ("Action" -> "Generate Token")), "Start")
        val token = tokenService.create(dbUser)
        logger.info(appendEntries(logData + ("Action" -> "Generate Token")), "Done")

        "token" -> token
      case None ⇒
        logger.error(appendEntries(logData), s"Failed: $UnauthorizedMsg")
        halt(Unauthorized("msg" -> UnauthorizedMsg))
    }
  }

  override def username: String = (parsedBody \ "username").extractOrElse[String]("")

  override def password: String = (parsedBody \ "password").extractOrElse[String]("")
}

object AuthUserController {
  val UnauthorizedMsg = "Invalid login/password"
}
