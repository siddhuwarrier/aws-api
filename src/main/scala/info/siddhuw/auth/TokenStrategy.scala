package info.siddhuw.auth

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

import com.google.common.net.HttpHeaders._
import info.siddhuw.models.DBUser
import info.siddhuw.models.daos.DBUserDAO
import info.siddhuw.services.JWTTokenService
import org.scalatra.ScalatraBase
import org.scalatra.auth.ScentryStrategy

/**
 * @author Siddhu Warrier
 */
class TokenStrategy(protected val app: ScalatraBase, val userDao: DBUserDAO) extends ScentryStrategy[DBUser] {
  val tokenService = new JWTTokenService(userDao)

  override def name = TokenStrategy.Name

  override def isValid(implicit request: HttpServletRequest) = {
    Option(app.request.getHeader(AUTHORIZATION)).flatMap(Some(_)).isDefined
  }

  override def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse): Option[DBUser] = {
    val jwtTokenOpt = getToken
    if (isValidToken(jwtTokenOpt)) {
      //TODO make async
      userDao.findById(tokenService.getUsername(jwtTokenOpt.get).get)
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
