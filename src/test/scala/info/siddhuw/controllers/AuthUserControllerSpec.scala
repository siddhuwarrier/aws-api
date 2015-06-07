package info.siddhuw.controllers

import com.google.common.base.CharMatcher
import info.siddhuw.builders.DBUserBuilder
import info.siddhuw.crypto.PasswordHasher
import info.siddhuw.models.APISchema._
import info.siddhuw.models.DBUser
import info.siddhuw.models.daos.DBUserDAO
import info.siddhuw.services.JWTTokenService
import info.siddhuw.utils.{ DatabaseSupport, JsonScalatraSuite }
import org.apache.commons.httpclient.HttpStatus._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.json4s.{ DefaultFormats, _ }
import org.scalatest._
import org.scalatest.mock.MockitoSugar

/**
 * @author Siddhu Warrier
 */

class AuthUserControllerSpec extends FeatureSpec
    with JsonScalatraSuite
    with BeforeAndAfterAll
    with BeforeAndAfter
    with GivenWhenThen
    with DatabaseSupport
    with MockitoSugar {
  val validPassword = "valid-password"
  val (validPwHash, salt) = PasswordHasher.hash(validPassword)
  val user = DBUser("username", validPwHash, salt)

  implicit val userDao = new DBUserDAO
  addServlet(new AuthUserController, "/auth/*")

  implicit def jsonFormats: Formats = DefaultFormats

  override def beforeAll(): Unit = {
    super.beforeAll() //The Embedded Jetty container is started up in ScalatraSuite's beforeAll. So we need to call it.
    initDb()
  }

  override def afterAll(): Unit = {
    destroyDb()
    super.afterAll()
  }

  after {
    dbUsers.deleteWhere(_.username === user.username)
  }

  feature("Authenticating a user") {
    scenario("Successful Authentication") {
      Given("I am a registered user")
      insert(user)

      When("I log in using my correct password")
      val loginJson = ("username" -> user.username) ~ ("password" -> validPassword)
      postJson("/auth", body = loginJson) {

        Then("I should receive a 200 response")
        status should be(SC_OK)

        And("I should have a signed JWT token sent to me in the body of the response")
        val bearerToken = compact(render(parse(body) \ "token"))
        new JWTTokenService(userDao).getUsername(bearerToken).get should equal(user.username)
      }
    }

    scenario("Invalid password") {
      Given("I am a registered user")
      insert(user)

      When("I log in using my correct password")
      val loginJson = ("username" -> user.username) ~ ("password" -> "invalidgghioadsghoisghiodshg")
      postJson("/auth", body = loginJson) {

        Then("I should receive a 401 response")
        status should be(SC_UNAUTHORIZED)

        And("an error message should be present in the body")
        val errMsg = compact(render(parse(body) \ "msg"))
        CharMatcher.is('\"').trimFrom(errMsg) should equal("Invalid login/password")
      }
    }

    scenario("Invalid username") {
      Given("I am a user that is not registered")
      val dbUser = DBUserBuilder.build()
      userDao.findById(dbUser.username) should equal(None) //verify that the user does not exist in DB

      When("I try to log in")
      val loginJson = ("username" -> user.username) ~ ("password" -> "doesntreallymatter")
      postJson("/auth", body = loginJson) {

        Then("I should receive a 401 response")
        status should be(SC_UNAUTHORIZED)

        And("an error message should be present in the body")
        val errMsg = compact(render(parse(body) \ "msg"))
        CharMatcher.is('\"').trimFrom(errMsg) should equal("Invalid login/password")
      }
    }
  }

  private def insert(user: DBUser): Unit = {
    transaction {
      dbUsers.insert(user)
    }
  }
}
