package info.siddhuw.controllers.api

import com.google.common.io.Resources
import com.google.common.net.HttpHeaders._
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.jwk.{ JWKSelector, JWKSet, KeyType, OctetSequenceKey }
import com.nimbusds.jose.{ JWSAlgorithm, JWSHeader }
import com.nimbusds.jwt.{ JWTClaimsSet, SignedJWT }
import com.typesafe.config.{ Config, ConfigFactory }
import info.siddhuw.builders.DBUserBuilder
import info.siddhuw.models.DBUser
import info.siddhuw.models.daos.DBUserDAO
import info.siddhuw.services.JWTTokenService
import org.apache.http.HttpStatus._
import org.joda.time.DateTime
import org.joda.time.chrono.ISOChronology
import org.json4s.{ DefaultFormats, _ }
import org.json4s.jackson.JsonMethods._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{ BeforeAndAfterAll, FlatSpec }
import org.scalatra.test.scalatest.ScalatraSuite

/**
 * @author Siddhu Warrier
 */
class APIControllerSpec extends FlatSpec
    with ScalatraSuite
    with BeforeAndAfterAll
    with MockitoSugar {
  implicit def jsonFormats: Formats = DefaultFormats

  implicit val mockUserDao = mock[DBUserDAO]
  implicit val tokenService = new JWTTokenService(mockUserDao)

  addServlet(new APIController, "/api/*")

  "The API controller credentials endpoint" should "return the credentials of a logged in user" in {
    val dbUser = DBUserBuilder.build()
    when(mockUserDao.findById(dbUser.username)).thenReturn(Some(dbUser))

    get("/api/me", headers = Map(AUTHORIZATION -> ("Bearer " + tokenService.create(dbUser)))) {
      status should equal(SC_OK)
      parse(body).extract[DBUser] should equal(dbUser)
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
    val dbUser = DBUserBuilder.build()
    when(mockUserDao.findById(dbUser.username)).thenReturn(None)

    get("/api/me", headers = Map(AUTHORIZATION -> ("Bearer " + tokenService.create(dbUser)))) {
      status should equal(SC_UNAUTHORIZED)
    }
  }

  it should "respond with a 401 if the JWT token is expired" in {
    val dbUser = DBUserBuilder.build()
    val claimsSet = new JWTClaimsSet()
    val config = ConfigFactory.load("app")

    claimsSet.setSubject(dbUser.username)
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
