package info.siddhuw.controllers.api

import info.siddhuw.auth.APIAuthenticationSupport
import info.siddhuw.controllers.JsonController
import info.siddhuw.models.daos.DBUserDAO
import info.siddhuw.services.JWTTokenService
import org.json4s.{ DefaultFormats, Formats }
import org.scalatra.{ CorsSupport, Unauthorized }

/**
 * @author Siddhu Warrier
 */

class APIController(implicit val tokenService: JWTTokenService,
  implicit val userDao: DBUserDAO) extends JsonController
    with APIAuthenticationSupport
    with CorsSupport {
  override protected implicit def jsonFormats: Formats = DefaultFormats

  before() {
    contentType = formats("json")
  }

  options("/*") {
    response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
  }

  get("/me") {
    authenticate() match {
      case Some(dbUser) ⇒
        dbUser

      case None ⇒
        halt(Unauthorized("msg" -> "Unauthorized to access this endpoint"))
    }
  }
}
