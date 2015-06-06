package info.siddhuw.controllers

import javax.servlet.http.HttpServletRequest

import com.google.common.net.HttpHeaders._
import com.typesafe.config.ConfigFactory
import info.siddhuw.models.TwitterUser
import info.siddhuw.services.{ JWTTokenService, OAuthLoginService }
import org.json4s.{ DefaultFormats, Formats }
import org.scalatra.{ Ok, ServiceUnavailable, Unauthorized }

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{ Try, Failure, Success }

/**
 * @author Siddhu Warrier
 */
class OAuthUserController(loginService: OAuthLoginService[TwitterUser],
    tokenService: JWTTokenService) extends JsonController {

  val appConfig = ConfigFactory.load("app")
  override protected implicit def jsonFormats: Formats = DefaultFormats

  get("/") {
    if (isAuthorised) {
      Ok()
    } else {
      redirect(loginService.redirectUrl)
    }
  }

  get("/callback") {
    authorise match {
      case Success(authorisedUser) ⇒
        val token = tokenService.create(authorisedUser)
        Ok(headers = Map(AUTHORIZATION -> ("Bearer " + token)))

      case Failure(e) ⇒
        //FIXME: Do we want to be leaking exception messages out to the user? Likely not.
        logger.error("Authorisation failed", e)
        e match {
          case t: RuntimeException ⇒
            halt(Unauthorized(e.getMessage))
          case _ ⇒
            halt(ServiceUnavailable(e.getMessage))
        }
    }
  }

  private def isAuthorised(implicit request: HttpServletRequest): Boolean = {
    request.headers.get(AUTHORIZATION) match {
      case Some(authorizationHeader) ⇒
        tokenService.isValid(authorizationHeader.replaceAll("Bearer", "").trim)
      case None ⇒
        false
    }
  }

  private def authorise(implicit request: HttpServletRequest): Try[TwitterUser] = {
    try {
      val authorised = Await.result(loginService.authoriseUser(params), appConfig.getLong("auth.wait_time_sec") seconds)
      authorised
    } catch {
      //likely timeout!
      case e: Throwable ⇒
        Failure(e)
    }
  }

}
