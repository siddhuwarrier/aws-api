package info.siddhuw.services

import org.json4s.JsonAST.JString
import org.json4s.jackson.JsonMethods._
import org.scribe.model.OAuthRequest

import scala.concurrent.{ExecutionContext, Future}

/**
 * @author Siddhu Warrier
 */


trait TwitterCredentialRetrievalServiceComponent extends CredentialRetrievalServiceComponent {
  override def credentialRetrievalService = new TwitterCredentialRetrievalService

  class TwitterCredentialRetrievalService extends CredentialRetrievalService {
    import TwitterCredentialRetrievalService._

    override def getCredentials(signedRequest: OAuthRequest)(implicit ec: ExecutionContext): Future[String] = Future {
      val body = signedRequest.send().getBody
      val JString(screenName) = parse(body) \ ScreenName
      screenName
    }
  }

  object TwitterCredentialRetrievalService {
    val ScreenName = "screen_name"
  }
}
