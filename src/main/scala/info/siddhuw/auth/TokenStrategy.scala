package info.siddhuw.auth

import javax.servlet.http.{ HttpServletResponse, HttpServletRequest }

import info.siddhuw.models.TwitterUser
import info.siddhuw.models.daos.UserDao
import info.siddhuw.services.JWTTokenService
import org.scalatra.ScalatraBase
import org.scalatra.auth.ScentryStrategy
import com.google.common.net.HttpHeaders._

/**
 * @author Siddhu Warrier
 */
class TokenStrategy(protected val app: ScalatraBase, val userDao: UserDao[TwitterUser]) extends ScentryStrategy[String] {
  val tokenService = new JWTTokenService(userDao)

  override def name = TokenStrategy.Name

  override def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse): Option[String] = {
    val jwtTokenOpt = getToken
    if (isValidToken(jwtTokenOpt)) {
      //do not need to check for success or failure here as I know the token is valid when this method is invoked.
      Some(tokenService.getScreenName(jwtTokenOpt.get).get)
    } else {
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
