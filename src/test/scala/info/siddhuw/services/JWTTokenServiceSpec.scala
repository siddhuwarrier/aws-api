package info.siddhuw.services

import info.siddhuw.builders.TwitterUserBuilder
import info.siddhuw.models.TwitterUser
import info.siddhuw.models.daos.UserDao
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.mock.MockitoSugar
import org.scalatest.{ FlatSpec, Matchers }

/**
 * @author Siddhu Warrier
 */

class JWTTokenServiceSpec extends FlatSpec
    with MockitoSugar
    with Matchers {
  "The JWT service" should "generate an HMAC-signed JWT token from the user's credentials" in {
    val twitterUser = TwitterUserBuilder.build()
    val mockUserDao = mock[UserDao[TwitterUser]]
    when(mockUserDao.findById(twitterUser.screenName)).thenReturn(Some(twitterUser))
    val jwtTokenService = new JWTTokenService(mockUserDao)

    val jwtToken = jwtTokenService.create(twitterUser)
    jwtTokenService.isValid(jwtToken) should equal(true)
    verify(mockUserDao).findById(twitterUser.screenName)
  }
  it should "not validate a generated JWT token if the user sending the token is no longer authorised" in {
    val twitterUser = TwitterUserBuilder.build()
    val mockUserDao = mock[UserDao[TwitterUser]]
    when(mockUserDao.findById(anyString)).thenReturn(None)
    val jwtTokenService = new JWTTokenService(mockUserDao)

    val jwtToken = jwtTokenService.create(twitterUser)
    jwtTokenService.isValid(jwtToken) should equal(false)
  }

  it should "not validate a generated JWT token if the DB operation times out" in {
    val twitterUser = TwitterUserBuilder.build()
    val mockUserDao = mock[UserDao[TwitterUser]]
    when(mockUserDao.findById(anyString)).thenAnswer {
      new Answer[Option[TwitterUser]] {
        override def answer(invocationOnMock: InvocationOnMock): Option[TwitterUser] = {
          Thread.sleep(10000)
          None
        }
      }
    }
    val jwtTokenService = new JWTTokenService(mockUserDao)

    val jwtToken = jwtTokenService.create(twitterUser)
    jwtTokenService.isValid(jwtToken) should equal(false)
  }

  it should "not validate a JWT token that is invalid" in {
    val mockUserDao = mock[UserDao[TwitterUser]]
    val jwtTokenService = new JWTTokenService(mockUserDao)
    jwtTokenService.isValid("this ain't no JWT token") should equal(false)
    verifyZeroInteractions(mockUserDao)
  }

  it should "not retrieve a username from a JWT token that is invalid" in {
    val jwtTokenService = new JWTTokenService(mock[UserDao[TwitterUser]])
    jwtTokenService.getScreenName("this ain't no JWT token").isFailure should equal(true)
  }

  it should "retrieve username from a valid JWT token" in {
    val twitterUser = TwitterUserBuilder.build()
    val jwtTokenService = new JWTTokenService(mock[UserDao[TwitterUser]])

    val jwtToken = jwtTokenService.create(twitterUser)
    jwtTokenService.getScreenName(jwtToken).get should equal(twitterUser.screenName)
  }
}
