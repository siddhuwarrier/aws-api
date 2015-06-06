package info.siddhuw.controllers.api

import com.google.common.io.Resources
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.{ JWSAlgorithm, JWSHeader }
import com.nimbusds.jose.jwk.{ OctetSequenceKey, KeyType, JWKSelector, JWKSet }
import com.nimbusds.jwt.{ SignedJWT, JWTClaimsSet }
import com.typesafe.config.{ Config, ConfigFactory }
import info.siddhuw.builders.TwitterUserBuilder
import info.siddhuw.models.TwitterUser
import info.siddhuw.models.daos.UserDao
import info.siddhuw.services.JWTTokenService
import org.joda.time.DateTime
import org.joda.time.chrono.ISOChronology
import org.mockito.Mockito._
import org.scalatest.{ BeforeAndAfterAll, FlatSpec }
import org.scalatest.mock.MockitoSugar
import org.scalatra.test.scalatest.ScalatraSuite
import org.apache.http.HttpStatus._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.DefaultFormats
import com.google.common.net.HttpHeaders._

/**
 * @author Siddhu Warrier
 */
class APIControllerSpec extends FlatSpec
    with ScalatraSuite
    with BeforeAndAfterAll
    with MockitoSugar {
  implicit def jsonFormats: Formats = DefaultFormats

  implicit val mockUserDao = mock[UserDao[TwitterUser]]
  implicit val tokenService = new JWTTokenService(mockUserDao)

  addServlet(new APIController, "/api/*")

  "The API controller credentials endpoint" should "return the credentials of a logged in user" in {
    val twitterUser = TwitterUserBuilder.build()
    when(mockUserDao.findById(twitterUser.screenName)).thenReturn(Some(twitterUser))

    get("/api/me", headers = Map(AUTHORIZATION -> ("Bearer " + tokenService.create(twitterUser)))) {
      status should equal(SC_OK)
      parse(body).extract[TwitterUser] should equal(twitterUser)
    }
  }

  it should "respond with a 401 if the request has no Authorization header" in {
    get("/api/me") {
      status should equal(SC_UNAUTHORIZED)
    }
  }

  it should "respond with a 401 if the request's Authorization header holds an invalid JWT token" in {
    get("/api/me", headers = Map(AUTHORIZATION -> "not a jwt token")) {
      status should equal(SC_UNAUTHORIZED)
    }
  }

  it should "respond with a 401 if the request's Authorization header holds a JWT token for an unauthorised user" in {
    val twitterUser = TwitterUserBuilder.build()
    when(mockUserDao.findById(twitterUser.screenName)).thenReturn(None)

    get("/api/me", headers = Map(AUTHORIZATION -> ("Bearer " + tokenService.create(twitterUser)))) {
      status should equal(SC_UNAUTHORIZED)
    }
  }

  it should "respond with a 401 if the JWT token is expired" in {
    val twitterUser = TwitterUserBuilder.build()
    val claimsSet = new JWTClaimsSet()
    val config = ConfigFactory.load("app")

    claimsSet.setSubject(twitterUser.screenName)
    claimsSet.setIssuer(config.getString("auth.jwt.issuer"))
    claimsSet.setExpirationTime(DateTime.now(ISOChronology.getInstanceUTC).toDate)

    val signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claimsSet)
    signedJWT.sign(new MACSigner(getSharedSecret(config)))
    val jwtToken = signedJWT.serialize()

    get("/api/me", headers = Map(AUTHORIZATION -> ("Bearer " + jwtToken))) {
      status should equal(SC_UNAUTHORIZED)
    }

  }

  private def getSharedSecret(config: Config): Array[Byte] = {
    val octetSeqKeySet = JWKSet.load(Resources.getResource(config.getString("auth.jwt.hmac.keyfile")))

    val selector = new JWKSelector
    selector.setKeyType(KeyType.OCT)
    val octetSeqKey = selector.select(octetSeqKeySet).get(0).asInstanceOf[OctetSequenceKey]

    octetSeqKey.toByteArray
  }
}
