package info.siddhuw.controllers

import info.siddhuw.auth.LoginAuthenticationSupport
import info.siddhuw.controllers.AuthUserController._
import info.siddhuw.models.InputUser
import info.siddhuw.models.daos.DBUserDAO
import info.siddhuw.services.JWTTokenService
import net.logstash.logback.marker.Markers._
import org.json4s.{ DefaultFormats, Formats }
import org.scalatra.swagger.{ Swagger, SwaggerSupport, SwaggerSupportSyntax }
import org.scalatra.{ CorsSupport, Unauthorized }
import org.slf4j.{ Logger, LoggerFactory }

import scala.jdk.CollectionConverters._

/**
 * @author Siddhu Warrier
 */

class AuthUserController(implicit val userDao: DBUserDAO, implicit val swagger: Swagger) extends JsonController
    with LoginAuthenticationSupport
    with SwaggerSupport
    with CorsSupport {
  implicit val applicationDescription: String = "The User Authentication API"

  val tokenService = new JWTTokenService(userDao)
  val logger: Logger = LoggerFactory.getLogger(classOf[AuthUserController])

  options("/*") {
    response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"))
  }

  override protected implicit def jsonFormats: Formats = DefaultFormats

  val authDocs: SwaggerSupportSyntax.OperationBuilder =
    (apiOperation[String]("auth")
      summary "POST credentials to get JWT Token to use the API"
      description "This endpoint takes a username and password and returns a JWT Token if valid"
      parameters (
        bodyParam[InputUser]("user-info"),
        headerParam[String]("Content-Type").allowableValues("application/json")))

  post("/", operation(authDocs)) {
    val logData = Map("endpoint" -> "POST /")

    authenticate() match {
      case Some(dbUser) ⇒

        logger.info(appendEntries((logData + ("Action" -> "Generate Token")).asJava), "Start")
        val token = tokenService.create(dbUser)
        logger.info(appendEntries((logData + ("Action" -> "Generate Token")).asJava), "Done")

        "token" -> token
      case None ⇒
        logger.error(appendEntries(logData.asJava), s"Failed: $UnauthorizedMsg")
        halt(Unauthorized("msg" -> UnauthorizedMsg))
    }
  }

  override def username: String = (parsedBody \ "username").extractOrElse[String]("")

  override def password: String = (parsedBody \ "password").extractOrElse[String]("")
}

object AuthUserController {
  val UnauthorizedMsg = "Invalid login/password"
}
