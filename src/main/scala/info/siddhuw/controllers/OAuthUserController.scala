package info.siddhuw.controllers

import info.siddhuw.models.TwitterUser
import info.siddhuw.services.OAuthLoginService
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.{Ok, Unauthorized}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
 * @author Siddhu Warrier
 */
class OAuthUserController(loginService: OAuthLoginService[TwitterUser]) extends JsonController {
  import OAuthUserController._
  
  override protected implicit def jsonFormats: Formats = DefaultFormats
  
  get("/") {
    redirect(loginService.redirectUrl)
  }

  get("/callback") {
    val authorised = Await.result(loginService.authoriseUser(params), AuthorizationTimeout)

    authorised match {
      case Success(authorisedUser) =>
        //TODO return API token in Authorization Bearer header
      val welcomeMsg = s"Welcome ${authorisedUser.screenName}"
        Ok(welcomeMsg)
      case Failure(e) =>
        logger.error("Authorisation failed", e)
        halt(Unauthorized(e.getMessage))
    }
  }
}

object OAuthUserController {
  val AuthorizationTimeout = 20 seconds
}
