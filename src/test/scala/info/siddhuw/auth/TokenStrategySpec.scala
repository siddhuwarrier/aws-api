package info.siddhuw.auth

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import com.google.common.net.HttpHeaders._
import com.typesafe.config.{ Config, ConfigFactory }
import info.siddhuw.controllers.AuthUserController
import info.siddhuw.models.DBUser
import info.siddhuw.models.daos.DBUserDAO
import info.siddhuw.services.JWTTokenService
import info.siddhuw.utils.builders.DBUserBuilder
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * @author Siddhu Warrier
 */
class TokenStrategySpec extends AnyFlatSpec
    with MockitoSugar
    with Matchers {
  val app: AuthUserController = mock[AuthUserController]
  val config: Config = ConfigFactory.load("app")
  val mockUserDao: DBUserDAO = mock[DBUserDAO]

  implicit val httpResponse: HttpServletResponse = mock[HttpServletResponse]
  implicit val httpRequest: HttpServletRequest = mock[HttpServletRequest]

  when(app.request).thenReturn(httpRequest)

  "The Token Strategy" should "be marked invalid if there is no authorization header set" in {
    val tokenStrategy = new TokenStrategy(app, mockUserDao)
    tokenStrategy.isValid should equal(false)
  }

  it should "authenticate a user if the JWT token provided is valid and the user it was generated for exists in the DB" in {
    val dbUser = DBUserBuilder.build()
    when(mockUserDao.findById(dbUser.username)).thenReturn(Some(dbUser))

    val token = new JWTTokenService(mockUserDao).create(dbUser)
    when(httpRequest.getHeader(AUTHORIZATION)).thenReturn("Bearer " + token)

    val tokenStrategy = new TokenStrategy(app, mockUserDao)
    tokenStrategy.authenticate() should equal(Some(dbUser))
  }

  it should "fail to authenticate a user if the valid JWT token is associated with a user not in DB" in {
    val dbUser = DBUserBuilder.build()
    when(mockUserDao.findById(dbUser.username)).thenReturn(None)

    val token = new JWTTokenService(mockUserDao).create(dbUser)
    when(httpRequest.getHeader(AUTHORIZATION)).thenReturn("Bearer " + token)

    val tokenStrategy = new TokenStrategy(app, mockUserDao)
    tokenStrategy.authenticate() should equal(None)
  }

  it should "fail to authenticate a user if the JWT token is invalid" in {
    when(httpRequest.getHeader(AUTHORIZATION)).thenReturn("Bearer " + "this ain't no JWT token")

    val tokenStrategy = new TokenStrategy(app, mockUserDao)
    tokenStrategy.authenticate() should equal(None)
  }

  it should "fail to authenticate a user if retrieving his credentials from the DB takes too long" in {
    val dbUser = DBUserBuilder.build()
    when(mockUserDao.findById(dbUser.username))
      .thenReturn(Some(dbUser)) //when invoked by the JWTTokenService
      .thenAnswer(
        new Answer[Option[DBUser]] {
          override def answer(invocationOnMock: InvocationOnMock): Option[DBUser] = {
            Thread.sleep((config.getLong("db.wait_time_sec") + 1 seconds).toMillis)
            Some(dbUser)
          }
        })

    val token = new JWTTokenService(mockUserDao).create(dbUser)
    when(httpRequest.getHeader(AUTHORIZATION)).thenReturn("Bearer " + token)

    val tokenStrategy = new TokenStrategy(app, mockUserDao)
    tokenStrategy.authenticate() should equal(None)
  }
}
