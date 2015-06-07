package info.siddhuw.services

import info.siddhuw.builders.DBUserBuilder
import info.siddhuw.models.DBUser
import info.siddhuw.models.daos.DBUserDAO
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
    val twitterUser = DBUserBuilder.build()
    val mockUserDao = mock[DBUserDAO]
    when(mockUserDao.findById(twitterUser.username)).thenReturn(Some(twitterUser))
    val jwtTokenService = new JWTTokenService(mockUserDao)

    val jwtToken = jwtTokenService.create(twitterUser)
    jwtTokenService.isValid(jwtToken) should equal(true)
    verify(mockUserDao).findById(twitterUser.username)
  }
  it should "not validate a generated JWT token if the user sending the token is no longer authorised" in {
    val twitterUser = DBUserBuilder.build()
    val mockUserDao = mock[DBUserDAO]
    when(mockUserDao.findById(anyString)).thenReturn(None)
    val jwtTokenService = new JWTTokenService(mockUserDao)

    val jwtToken = jwtTokenService.create(twitterUser)
    jwtTokenService.isValid(jwtToken) should equal(false)
  }

  it should "not validate a generated JWT token if the DB operation times out" in {
    val twitterUser = DBUserBuilder.build()
    val mockUserDao = mock[DBUserDAO]
    when(mockUserDao.findById(anyString)).thenAnswer {
      new Answer[Option[DBUser]] {
        override def answer(invocationOnMock: InvocationOnMock): Option[DBUser] = {
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
    val mockUserDao = mock[DBUserDAO]
    val jwtTokenService = new JWTTokenService(mockUserDao)
    jwtTokenService.isValid("this ain't no JWT token") should equal(false)
    verifyZeroInteractions(mockUserDao)
  }

  it should "not retrieve a username from a JWT token that is invalid" in {
    val jwtTokenService = new JWTTokenService(mock[DBUserDAO])
    jwtTokenService.getUsername("this ain't no JWT token").isFailure should equal(true)
  }

  it should "retrieve username from a valid JWT token" in {
    val twitterUser = DBUserBuilder.build()
    val jwtTokenService = new JWTTokenService(mock[DBUserDAO])

    val jwtToken = jwtTokenService.create(twitterUser)
    jwtTokenService.getUsername(jwtToken).get should equal(twitterUser.username)
  }
}
