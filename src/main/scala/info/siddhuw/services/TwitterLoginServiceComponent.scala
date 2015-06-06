package info.siddhuw.services

import java.util.concurrent.TimeoutException
import javax.security.auth.login.LoginException

import com.typesafe.scalalogging.LazyLogging
import info.siddhuw.models.TwitterUser
import info.siddhuw.models.daos.UserDaoComponent
import org.scribe.model.{OAuthRequest, Token, Verb, Verifier}
import org.scribe.oauth.OAuthService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}


/**
 * @author Siddhu Warrier
 */


/**
 * Service to log in using Twitter as the 3rd party OAuth solution and a DB-based authorisation mechanism.
 */
trait TwitterLoginServiceComponent extends OAuthLoginServiceComponent[TwitterUser] with LazyLogging {
  this: UserDaoComponent[TwitterUser] with CredentialRetrievalServiceComponent =>

  val oauthService: OAuthService

  override def loginService: OAuthLoginService[TwitterUser] = new TwitterLoginService

  import TwitterLoginService._
  class TwitterLoginService(implicit ec: ExecutionContext) extends OAuthLoginService[TwitterUser] {

    override def redirectUrl: String = oauthService.getAuthorizationUrl(oauthService.getRequestToken)

    override def authoriseUser(redirectParams: Map[String, String]): Future[Try[TwitterUser]] = {
      getParams(redirectParams) match {
        case Success((oauthVerifier, oauthToken)) =>
          val twitterReq = new OAuthRequest(Verb.GET, CredentialsApiEndpoint)
          oauthService.signRequest(accessToken(oauthVerifier, oauthToken), twitterReq)
          try {
            val screenName = Await.result(credentialRetrievalService.getCredentials(twitterReq), TwitterTimeoutInterval)
            doAuthorise(screenName)
          }
          catch {
            case e: TimeoutException =>
              Future(Failure(e))
          }

        case Failure(t) =>
          logger.error("Failed to retrieve parameters", t)
          Future(Failure(new RuntimeException("User did not authorise the application")))
      }
    }

    private def getParams(params: Map[String, String]): Try[(String, String)] = {
      Try((params(OAuthVerifier), params(OAuthToken)))
    }

    private def accessToken(oauthVerifier: String, oauthToken: String): Token = {
      val verifier = new Verifier(oauthVerifier)
      val requestToken = new Token(oauthToken, oauthVerifier)
      oauthService.getAccessToken(requestToken, verifier)
    }

    private def doAuthorise(screenName: String): Future[Try[TwitterUser]] = Future {
      userDao.findById(screenName) match {
        case Some(twitterUser) =>
          Success(twitterUser)
        case _ =>
          Failure(new LoginException(s"user $screenName not authorised to use this application"))
      }
    }
  }

  object TwitterLoginService {
    val CredentialsApiEndpoint = "https://api.twitter.com/1.1/account/verify_credentials.json"
    val OAuthToken = "oauth_token"
    val OAuthVerifier = "oauth_verifier"
    val TwitterTimeoutInterval = 10 seconds
  }
}
