package info.siddhuw.controllers.api

import info.siddhuw.auth.AuthenticationSupport
import info.siddhuw.controllers.JsonController
import info.siddhuw.models.TwitterUser
import info.siddhuw.models.daos.UserDao
import info.siddhuw.services.JWTTokenService
import org.json4s.{ DefaultFormats, Formats }
import org.scalatra.Unauthorized

/**
 * @author Siddhu Warrier
 */

class APIController(implicit val tokenService: JWTTokenService,
    implicit val userDao: UserDao[TwitterUser]) extends JsonController with AuthenticationSupport {
  override protected implicit def jsonFormats: Formats = DefaultFormats

  before() {
    contentType = formats("json")
  }

  get("/me") {
    authenticate() match {
      case Some(screenName) ⇒
        TwitterUser(screenName)
      case None ⇒
        halt(Unauthorized("Unauthorized request"))
    }
  }
}
