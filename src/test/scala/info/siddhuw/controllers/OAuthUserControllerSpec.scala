package info.siddhuw.controllers

import com.google.common.net.HttpHeaders._
import com.typesafe.config.ConfigFactory
import info.siddhuw.builders.TwitterUserBuilder
import info.siddhuw.models.TwitterUser
import info.siddhuw.models.daos.UserDao
import info.siddhuw.services.{ JWTTokenService, OAuthLoginService }
import org.apache.http.HttpStatus._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.mock.MockitoSugar
import org.scalatest.{ FlatSpec, Matchers }
import org.scalatra.test.scalatest.ScalatraSuite

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import com.google.common.net.HttpHeaders._
import scala.util.{ Failure, Try }

/**
 * TODO write these tests!
 *
 * @author Siddhu Warrier
 */

class OAuthUserControllerSpec extends FlatSpec
    with MockitoSugar
    with ScalatraSuite
    with Matchers {
  val config = ConfigFactory.load("app")

  val mockLoginService = mock[OAuthLoginService[TwitterUser]]
  val twitterUser = TwitterUserBuilder.build()
  val mockUserDao = mock[UserDao[TwitterUser]]
  when(mockUserDao.findById(twitterUser.screenName)).thenReturn(Some(twitterUser))
  val tokenService = new JWTTokenService(mockUserDao)
  val oauthUserController = new OAuthUserController(mockLoginService, tokenService)
  addServlet(oauthUserController, "/auth/*")

  //TODO investigate strange issue with happy path tests that I don't receive any logs for. Works fine when run.

  "The OAuth User Controller" should "redirect users with no JWT token through to the third-party auth provider" in {
    val expectedRedirectUrl = "http://evilkanievel.com"
    when(mockLoginService.redirectUrl).thenReturn(expectedRedirectUrl)
    get("/auth") {
      status should equal(SC_MOVED_TEMPORARILY)
      header(LOCATION) should equal(expectedRedirectUrl)
    }
  }

  "The OAuth User Controller callback endpoint" should "respond with a 401 if the authorisation fails" in {
    val requestParams = Map(config.getString("oauth.token") -> "token",
      config.getString("oauth.verifier") -> "verifier")
    when(mockLoginService.authoriseUser(requestParams)).thenReturn(Future(Failure(new IllegalArgumentException)))

    get("/auth/callback", requestParams) {
      status should equal(SC_UNAUTHORIZED)
      header(AUTHORIZATION) should equal(null)
    }
  }

  it should "respond with a 503 if the authorisation times out" in {
    val requestParams = Map(config.getString("oauth.token") -> "token",
      config.getString("oauth.verifier") -> "verifier")

    when(mockLoginService.authoriseUser(requestParams)).thenAnswer {
      new Answer[Future[Try[TwitterUser]]] {
        override def answer(invocationOnMock: InvocationOnMock): Future[Try[TwitterUser]] = Future {
          //sleep longer than the timeout
          Thread.sleep((config.getLong("auth.wait_time_sec") + 1 seconds).toMillis)
          null //return type does not matter
        }
      }
    }

    get("/auth/callback", requestParams) {
      status should equal(SC_SERVICE_UNAVAILABLE)
      header(AUTHORIZATION) should equal(null)
    }
  }
}
