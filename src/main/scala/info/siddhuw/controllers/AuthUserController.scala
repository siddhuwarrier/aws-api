package info.siddhuw.controllers

import info.siddhuw.auth.LoginAuthenticationSupport
import info.siddhuw.models.daos.DBUserDAO
import info.siddhuw.services.JWTTokenService
import org.json4s.{ DefaultFormats, Formats }
import org.scalatra.{ CorsSupport, Unauthorized }

/**
 * @author Siddhu Warrier
 */

class AuthUserController(implicit val userDao: DBUserDAO) extends JsonController
    with LoginAuthenticationSupport
    with CorsSupport {
  val tokenService = new JWTTokenService(userDao)
  options("/*") {
    response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"))
  }

  override protected implicit def jsonFormats: Formats = DefaultFormats

  post("/") {
    authenticate() match {
      case Some(dbUser) ⇒
        val token = tokenService.create(dbUser)
        "token" -> token
      case None ⇒
        halt(Unauthorized("msg" -> "Invalid login/password"))
    }
  }

  override def username: String = {
    val x = (parsedBody \ "username").extractOrElse[String]("")
    x
  }
  override def password: String = (parsedBody \ "password").extractOrElse[String]("")
}
