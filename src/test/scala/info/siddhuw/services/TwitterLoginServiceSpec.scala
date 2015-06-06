package info.siddhuw.services

import info.siddhuw.models.TwitterUser
import info.siddhuw.models.daos.{ UserDaoComponent, UserDao }
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.tags.Slow
import org.scalatest.{ Matchers, FlatSpec }
import org.scalatest.mock.MockitoSugar
import org.scribe.model.OAuthRequest
import org.scribe.oauth.OAuthService

import org.mockito.Mockito._
import org.mockito.Matchers._

import scala.concurrent.{ ExecutionContext, Future, Await }
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author Siddhu Warrier
 */

@Slow
class TwitterLoginServiceSpec extends FlatSpec
    with Matchers
    with MockitoSugar {

  "The Twitter login service" should "return the authorised twitter user if the login is successful" in {
    val twitterUser = new TwitterUser("test-twitter-user")
    val mockOAuthService = mock[OAuthService]
    val mockTwitterUserDao = buildMockTwitterDao(Some(twitterUser))
    val mockTwitterCredentialRetrievalService = buildMockTwitterCredRetrievalService(twitterUser)

    val twitterLoginService = new TwitterLoginServiceComponent with MockUserDaoComponent with MockCredentialRetrievalServiceComponent {
      override val oauthService: OAuthService = mockOAuthService
      override val mockUserDao: UserDao[TwitterUser] = mockTwitterUserDao
      override val mockCredentialRetrievalService: CredentialRetrievalService = mockTwitterCredentialRetrievalService
    }.loginService

    val params = Map("oauth_token" -> "afssfsg", "oauth_verifier" -> "hahahaha")

    val authorised = Await.result(twitterLoginService.authoriseUser(params), 2 seconds)
    authorised.isSuccess should equal(true)
    authorised.get should equal(twitterUser)
  }

  it should "fail gracefully if the user declined to authorise the app" in {
    val mockOAuthService = mock[OAuthService]
    val mockTwitterUserDao = mock[UserDao[TwitterUser]]
    val mockTwitterCredentialRetrievalService = mock[CredentialRetrievalService]

    val twitterLoginService = new TwitterLoginServiceComponent with MockUserDaoComponent with MockCredentialRetrievalServiceComponent {
      override val oauthService: OAuthService = mockOAuthService
      override val mockUserDao: UserDao[TwitterUser] = mockTwitterUserDao
      override val mockCredentialRetrievalService: CredentialRetrievalService = mockTwitterCredentialRetrievalService
    }.loginService

    val params = Map("denied" -> "computer says no")

    val authorised = Await.result(twitterLoginService.authoriseUser(params), 2 seconds)
    authorised.isFailure should equal(true)
  }

  it should "fail gracefully if the user's twitter handle is not pre-authorised to access the app" in {
    val twitterUser = new TwitterUser("test-twitter-user")
    val mockOAuthService = mock[OAuthService]
    val mockTwitterCredentialRetrievalService = buildMockTwitterCredRetrievalService(twitterUser)
    val mockTwitterUserDao = buildMockTwitterDao(None)

    val twitterLoginService = new TwitterLoginServiceComponent with MockUserDaoComponent with MockCredentialRetrievalServiceComponent {
      override val oauthService: OAuthService = mockOAuthService
      override val mockUserDao: UserDao[TwitterUser] = mockTwitterUserDao
      override val mockCredentialRetrievalService: CredentialRetrievalService = mockTwitterCredentialRetrievalService
    }.loginService

    val params = Map("oauth_token" -> "afssfsg", "oauth_verifier" -> "hahahaha")

    val authorised = Await.result(twitterLoginService.authoriseUser(params), 2 seconds)
    authorised.isFailure should equal(true)
  }

  it should "fail gracefully if Twitter takes too long to respond" in {
    val mockOAuthService = mock[OAuthService]
    val mockTwitterCredentialRetrievalService = buildFailingMockTwitterCredRetrievalService
    val mockTwitterUserDao = mock[UserDao[TwitterUser]]

    val twitterLoginService = new TwitterLoginServiceComponent with MockUserDaoComponent with MockCredentialRetrievalServiceComponent {
      override val oauthService: OAuthService = mockOAuthService
      override val mockUserDao: UserDao[TwitterUser] = mockTwitterUserDao
      override val mockCredentialRetrievalService: CredentialRetrievalService = mockTwitterCredentialRetrievalService
    }.loginService

    val params = Map("oauth_token" -> "afssfsg", "oauth_verifier" -> "hahahaha")

    val authorised = Await.result(twitterLoginService.authoriseUser(params), 2 seconds)
    authorised.isFailure should equal(true)
  }

  trait MockUserDaoComponent extends UserDaoComponent[TwitterUser] {
    val mockUserDao: UserDao[TwitterUser]

    override def userDao = mockUserDao
  }

  trait MockCredentialRetrievalServiceComponent extends CredentialRetrievalServiceComponent {
    val mockCredentialRetrievalService: CredentialRetrievalService

    override def credentialRetrievalService = mockCredentialRetrievalService
  }

  private def buildMockTwitterDao(twitterUserOpt: Option[TwitterUser]) = {
    val mockTwitterUserDao = mock[UserDao[TwitterUser]]
    when(mockTwitterUserDao.findById(anyString)).thenReturn(twitterUserOpt)
    mockTwitterUserDao
  }

  private def buildMockTwitterCredRetrievalService(twitterUser: TwitterUser) = {
    val mockTwitterCredRetrievalService = mock[CredentialRetrievalService]

    when(mockTwitterCredRetrievalService.getCredentials(any[OAuthRequest])(any[ExecutionContext]))
      .thenReturn(Future(""""username":"${twitterUser.screenName}""""))

    mockTwitterCredRetrievalService
  }

  private def buildFailingMockTwitterCredRetrievalService = {
    val mockTwitterCredRetrievalService = mock[CredentialRetrievalService]
    when(mockTwitterCredRetrievalService.getCredentials(any[OAuthRequest])(any[ExecutionContext]))
      .thenAnswer(new Answer[Future[String]] {

        override def answer(invocationOnMock: InvocationOnMock): Future[String] = Future {
          Thread.sleep(1200000) //sleep for 20 minutes; we should time out before then for sure
          ""
        }
      })
    mockTwitterCredRetrievalService
  }
}
