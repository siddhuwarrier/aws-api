package info.siddhuw.services

import info.siddhuw.builders.TwitterUserBuilder
import org.scalatest.mock.MockitoSugar
import org.scalatest.{ Matchers, FlatSpec }
import org.scribe.model.{ Response, OAuthRequest }
import org.mockito.Mockito._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

/**
 * @author Siddhu Warrier
 */

class TwitterCredentialRetrievalServiceSpec extends FlatSpec
    with MockitoSugar
    with Matchers {
  "The Twitter OAuth Credentials Retrieval Service" should "retrieve the user's credentials from Twitter" in {
    val twitterUser = TwitterUserBuilder.build()
    val mockResponse = mock[Response]
    when(mockResponse.getBody).thenReturn(s"""{"screen_name" : "${twitterUser.screenName}"}""")

    val mockOAuthRequest = mock[OAuthRequest]
    when(mockOAuthRequest.send()).thenReturn(mockResponse)

    val credService = new TwitterCredentialRetrievalServiceComponent {}.credentialRetrievalService

    Await.result(credService.getCredentials(mockOAuthRequest), 1 second) should equal(twitterUser.screenName)
  }

  it should "fail gracefully if the JSON received from Twitter isn't valid" in {
    val twitterUser = TwitterUserBuilder.build()
    val mockResponse = mock[Response]
    when(mockResponse.getBody).thenReturn("<xml-tag>Twitter no longer loves JSON</xml-tag>")

    val mockOAuthRequest = mock[OAuthRequest]
    when(mockOAuthRequest.send()).thenReturn(mockResponse)

    val credService = new TwitterCredentialRetrievalServiceComponent {}.credentialRetrievalService

    credService.getCredentials(mockOAuthRequest) onComplete {
      case _: Success[String] ⇒
        fail("Expected failure")
      case t: Failure[String] ⇒
        t.get should equal(null)
    }
  }
}
