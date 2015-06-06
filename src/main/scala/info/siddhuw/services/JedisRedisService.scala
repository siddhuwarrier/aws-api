package info.siddhuw.services

import java.util.Date

import com.typesafe.config.ConfigFactory
import info.siddhuw.models.TwitterUser
import info.siddhuw.models.daos.UserDao
import org.joda.time.DateTime
import org.joda.time.chrono.ISOChronology
import org.owasp.esapi.crypto.CryptoToken
import redis.clients.jedis.Jedis

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.language.postfixOps

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Redis Service using the Jedis client. I'd have liked to use the Scala client by Debasish Ghosh, but Maven Central
 * seems to have trouble getting the JAR through to me.
 *
 * We use Redis for storing time-limited API tokens for authorised clients.
 *
 * @author Siddhu Warrier
 */
class JedisRedisService(redisHost: String, redisPort: Int, userDao: UserDao[TwitterUser]) extends RedisService {
  import JedisRedisService._

  val config = ConfigFactory.load("app")
  val jedis = new Jedis(redisHost, redisPort)

  override def setTokenFor(user: TwitterUser): String = {
    val token = generateApiToken(user)
    jedis.set(user.screenName, token)
    token
  }

  override def isTokenValid(token: String): Boolean = {
    val cryptoToken = new CryptoToken(token)
    if (!cryptoToken.isExpired) {
      Await.result(Future(userDao.findById(cryptoToken.getUserAccountName)), DbWaitTime) match {
        case Some(_) =>
          return true
        case None =>
          return false
      }
    }

    false
  }

  override def expire(token: String): Unit = {
    val expireInMs = 1
    val cryptoToken = new CryptoToken(token)
    cryptoToken.setExpiration(DateTime.now().withChronology(ISOChronology.getInstanceUTC).plusMillis(expireInMs))
    Thread.sleep(expireInMs)
  }

  private def generateApiToken(user: TwitterUser): String = {
    val cryptoToken = new CryptoToken()
    cryptoToken.setExpiration(DateTime.now().withChronology(ISOChronology.getInstanceUTC).plusMinutes(TokenValidityMin))
    cryptoToken.setUserAccountName(user.screenName)
    cryptoToken.getToken
  }

  implicit def dateTimeToDate(dateTime: DateTime): Date = {
    new Date(dateTime.getMillis)
  }
}

object JedisRedisService {
  val TokenValidityMin = 60
  val DbWaitTime = 2 seconds
}
