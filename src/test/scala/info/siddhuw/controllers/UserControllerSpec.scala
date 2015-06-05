package info.siddhuw.controllers

import java.net.HttpCookie
import java.util.UUID

import info.siddhuw.auth
import info.siddhuw.crypto.PasswordHasher
import info.siddhuw.models.APISchema._
import info.siddhuw.models.User
import info.siddhuw.utils.ApiTestSuite
import org.apache.commons.httpclient.HttpStatus
import org.json4s.{DefaultFormats, _}
import org.json4s.jackson.JsonMethods._
import org.scalatest.FlatSpec
import org.scalatra.{CookieOptions, Cookie}
import scala.collection.JavaConversions._

class UserControllerSpec extends FlatSpec with ApiTestSuite {
  protected implicit val jsonFormats = DefaultFormats
  val password = "pw1"
  val (pwHash, salt) = PasswordHasher.hash(password)
  val validUser = User("username", pwHash, salt, "api-key", "api-secret-key")
  val invalidUser = User("username", pwHash, salt, "api-key", "api-secret-key")

  addServlet(classOf[UserController], "/*")

  before {
    transaction {
      users.insert(validUser)
    }
  }

  after {
    transaction {
      users.deleteWhere(u => u.username === validUser.username)
    }
  }

  "The Auth Controller" should "return an API token for an authenticated user in a cookie" in {
    val requestJson = parse( s"""{"username": "${validUser.username}", "password":"$password"}""")
    postJson("/", requestJson) {
      status should equal(HttpStatus.SC_OK)

      val cookies = HttpCookie.parse(header("Set-Cookie"))
      cookies.size should equal(1)
      val actualCookie = cookies.get(0)

      actualCookie.getPath should equal("/")
      actualCookie.getName should equal(auth.CookieKey)
      actualCookie.getSecure should equal(true)
      //TODO fix it so that token is sent through
    }
  }

  it should s"not require username and password if ${auth.CookieKey} cookie has not expired" in {
    val requestJson = parse( s"""{"username": "${validUser.username}", "password":"$password"}""")
    postJson("/", requestJson) {
      status should equal(HttpStatus.SC_OK)
      val receivedCookie = HttpCookie.parse(header("Set-Cookie")).get(0)

      post(uri = "/", Array[Byte](), headers = Map("Cookie" -> buildReqCookie(receivedCookie))) {
        status should equal(HttpStatus.SC_OK)
      }
    }
  }

  it should "refresh token if user hits login page when already authenticated" in {
    val requestJson = parse( s"""{"username": "${validUser.username}", "password":"$password"}""")
    postJson("/", requestJson) {
      status should equal(HttpStatus.SC_OK)
      val receivedCookie = HttpCookie.parse(header("Set-Cookie")).get(0)

      post(uri = "/", Array[Byte](),
        headers = Map("Cookie" -> buildReqCookie(receivedCookie, maxAgeOpt = Some(receivedCookie.getMaxAge.toInt - 1)))) {
        status should equal(HttpStatus.SC_OK)

        val newCookie = HttpCookie.parse(header("Set-Cookie")).get(0)
        newCookie.getMaxAge should equal (receivedCookie.getMaxAge)
        newCookie.getValue should not equal receivedCookie.getValue
      }
    }
  }

  it should "set the token in Redis" in {
    pending
  }

  it should "return a 401 if the user's password is invalid" in {
    val requestJson = parse( s"""{"username": "${validUser.username}", "password":"invalid_pass"}"""")
    postJson("/", requestJson) {
      status should equal(HttpStatus.SC_UNAUTHORIZED)
      val cookies = HttpCookie.parse(header("Set-Cookie"))

      cookies.forall {
        _.getName == auth.CookieKey
      } should equal (false)
    }


  }

  it should "respond with a 401 if the user does not exist" in {
    pending
  }

  private def buildReqCookie(receivedCookie: HttpCookie, maxAgeOpt: Option[Int] = None): String = {
    val maxAge = maxAgeOpt match {
      case Some(max) => max
      case None =>  receivedCookie.getMaxAge.toInt
    }
    Cookie(receivedCookie.getName,
      receivedCookie.getValue)(CookieOptions(maxAge = maxAge, path = receivedCookie.getPath,
      secure = receivedCookie.getSecure)).toCookieString
  }
}
