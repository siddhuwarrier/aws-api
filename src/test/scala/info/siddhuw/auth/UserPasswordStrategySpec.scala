package info.siddhuw.auth

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import com.typesafe.config.ConfigFactory
import info.siddhuw.controllers.AuthUserController
import info.siddhuw.models.DBUser
import info.siddhuw.models.daos.DBUserDAO
import info.siddhuw.utils.builders.DBUserBuilder
import info.siddhuw.utils.crypto.PasswordHasher
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

class UserPasswordStrategySpec extends AnyFlatSpec
    with MockitoSugar
    with Matchers {
  val app = mock[AuthUserController]
  val mockUserDao = mock[DBUserDAO]

  implicit val httpResponse = mock[HttpServletResponse]
  implicit val httpRequest = mock[HttpServletRequest]
  val validPassword = "valid-password"

  val config = ConfigFactory.load("app")

  "The UserPassword Authentication Strategy" should "authenticate a user with the right username and password credentials" in {
    val (hash, salt) = PasswordHasher.hash(validPassword)
    val dbUser = DBUserBuilder.build(pwHash = hash, salt = salt)
    when(mockUserDao.findById(dbUser.username)).thenReturn(Option(dbUser))

    val userPasswordStrategy = new UserPasswordStrategy(app, mockUserDao, dbUser.username, validPassword)
    userPasswordStrategy.authenticate() should equal(Some(dbUser))
  }

  it should "fail to authenticate a user if the user's password is incorrect" in {
    val invalidPassword = validPassword + "_someextrachars"
    val (hash, salt) = PasswordHasher.hash(validPassword)
    val dbUser = DBUserBuilder.build(pwHash = hash, salt = salt)

    when(mockUserDao.findById(dbUser.username)).thenReturn(Option(dbUser))

    val userPasswordStrategy = new UserPasswordStrategy(app, mockUserDao, dbUser.username, invalidPassword)
    userPasswordStrategy.authenticate() should equal(None)
  }

  it should "fail to authenticate a user if the user cannot be found in the DB" in {
    val dbUser = DBUserBuilder.build()

    when(mockUserDao.findById(dbUser.username)).thenReturn(None)

    val userPasswordStrategy = new UserPasswordStrategy(app, mockUserDao, dbUser.username, "some-password")
    userPasswordStrategy.authenticate() should equal(None)
  }

  it should "fail to authenticate a user if the DB operation takes longer than the DB wait time" in {
    val (hash, salt) = PasswordHasher.hash(validPassword)
    val dbUser = DBUserBuilder.build(pwHash = hash, salt = salt)

    when(mockUserDao.findById(dbUser.username)).thenAnswer(
      new Answer[Option[DBUser]] {
        override def answer(invocationOnMock: InvocationOnMock): Option[DBUser] = {
          Thread.sleep((config.getLong("db.wait_time_sec") + 1 seconds).toMillis)
          Some(dbUser)
        }
      })

    val userPasswordStrategy = new UserPasswordStrategy(app, mockUserDao, dbUser.username, validPassword)
    userPasswordStrategy.authenticate() should equal(None)
  }

  it should "mark an authentication request as invalid if password is null" in {
    val userPasswordStrategy = new UserPasswordStrategy(app, mockUserDao, "username", null)
    userPasswordStrategy.isValid should equal(false)
  }

  it should "mark an authentication request as invalid if password is empty" in {
    val userPasswordStrategy = new UserPasswordStrategy(app, mockUserDao, "username", "   ")
    userPasswordStrategy.isValid should equal(false)
  }

  it should "mark an authentication request as invalid if username is null" in {
    val userPasswordStrategy = new UserPasswordStrategy(app, mockUserDao, null, "password")
    userPasswordStrategy.isValid should equal(false)
  }

  it should "mark an authentication request as invalid if username is empty" in {
    val userPasswordStrategy = new UserPasswordStrategy(app, mockUserDao, "  ", "password")
    userPasswordStrategy.isValid should equal(false)
  }
}
