package info.siddhuw.services

import org.scribe.oauth.OAuthService

import scala.concurrent.Future
import scala.util.Try

/**
 * @author Siddhu Warrier
 */


/**
 * An OAuth Login Service that simplifies the process of:
 * (a) authenticating using a third-party OAuth service
 * (b) authorising using local mechanism
 *
 * This is an example of the Cake pattern: http://www.cakesolutions.net/teamblogs/2011/12/19/cake-pattern-in-depth
 */
trait OAuthLoginServiceComponent[T] {

  def loginService: OAuthLoginService[T]
}

trait OAuthLoginService[T] {
  def redirectUrl: String
  def authoriseUser(oauthRedirectParams: Map[String, String]): Future[Try[T]]
}
