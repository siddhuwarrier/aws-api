package info.siddhuw.services

import java.util.Date
import java.util.concurrent.TimeoutException

import com.google.common.io.Resources
import com.nimbusds.jose.crypto._
import com.nimbusds.jose.jwk._
import com.nimbusds.jose.{ JWSAlgorithm, JWSHeader }
import com.nimbusds.jwt.{ JWTClaimsSet, ReadOnlyJWTClaimsSet, SignedJWT }
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import info.siddhuw.models.TwitterUser
import info.siddhuw.models.daos.UserDao
import org.joda.time.DateTime
import org.joda.time.chrono.ISOChronology

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }
import scala.language.postfixOps
import scala.util.Try

/**
 * @author Siddhu Warrier
 */
class JWTTokenService(userDao: UserDao[TwitterUser]) extends LazyLogging {

  val config = ConfigFactory.load("app")

  def create(user: TwitterUser) = {
    val claimsSet = createClaimsSet(user)

    val signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claimsSet)
    signedJWT.sign(new MACSigner(getSharedSecret))
    signedJWT.serialize()
  }

  def isValid(jwtTokenStr: String): Boolean = {
    try {
      val jwsToken = SignedJWT.parse(jwtTokenStr)
      jwsToken.verify(new MACVerifier(getSharedSecret)) && verifyClaims(jwsToken.getJWTClaimsSet)
    } catch {
      case t: Throwable ⇒
        logger.error("Failed to check validity", t)
        false
    }
  }

  def getScreenName(jwtTokenStr: String): Try[String] = Try(SignedJWT.parse(jwtTokenStr).getJWTClaimsSet.getSubject)

  private def verifyClaims(claimsSet: ReadOnlyJWTClaimsSet): Boolean = {
    val expiryDt = new DateTime(claimsSet.getExpirationTime, ISOChronology.getInstanceUTC)

    claimsSet.getIssuer == config.getString("auth.jwt.issuer") &&
      isNotExpired(expiryDt) &&
      isTokenFromAuthorisedUser(claimsSet.getSubject)
  }

  private def isNotExpired(expiryDt: DateTime): Boolean = {
    expiryDt.isAfter(DateTime.now(ISOChronology.getInstanceUTC))
  }

  private def isTokenFromAuthorisedUser(screenName: String): Boolean = {
    val dbWaitTime = config.getLong("db.wait_time_sec") seconds

    try {
      //async DB op
      Await.result(Future(userDao.findById(screenName)), dbWaitTime) match {
        case Some(_) ⇒
          true
        case None ⇒
          false
      }
    } catch {
      case _: TimeoutException ⇒
        false
    }
  }

  private def getSharedSecret: Array[Byte] = {
    val octetSeqKeySet = JWKSet.load(Resources.getResource(config.getString("auth.jwt.hmac.keyfile")))

    val selector = new JWKSelector
    selector.setKeyType(KeyType.OCT)
    val octetSeqKey = selector.select(octetSeqKeySet).get(0).asInstanceOf[OctetSequenceKey]

    octetSeqKey.toByteArray
  }

  private def createClaimsSet(user: TwitterUser): JWTClaimsSet = {
    val claimsSet = new JWTClaimsSet()

    claimsSet.setSubject(user.screenName)
    claimsSet.setIssuer(config.getString("auth.jwt.issuer"))
    claimsSet.setExpirationTime(getExpirationTime)

    claimsSet
  }

  private def getExpirationTime: Date = {
    val validityTime = config.getLong("auth.jwt.token_validity_min") minutes
    val now = DateTime.now(ISOChronology.getInstanceUTC)

    now.plus(validityTime.toMillis).toDate
  }

}
