package info.siddhuw.services

import info.siddhuw.models.APISchema._
import info.siddhuw.models.TwitterUser
import info.siddhuw.models.daos.TwitterUserDaoComponent
import info.siddhuw.utils.{DatabaseSupport, RedisSupport}
import org.joda.time.DateTime
import org.joda.time.chrono.ISOChronology
import org.owasp.esapi.crypto.CryptoToken
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FlatSpec, Matchers}
import redis.clients.jedis.Jedis

/**
 * @author Siddhu Warrier
 */


class JedisRedisServiceIntegrationTest extends FlatSpec
with Matchers
with BeforeAndAfterAll
with DatabaseSupport
with RedisSupport {
  private val twitterUser = TwitterUser("kim.kipling")
  private val userDao = new TwitterUserDaoComponent {}.userDao


  override protected def beforeAll(): Unit = {
    super.beforeAll()
    initDb()
  }

  override protected def afterAll(): Unit = {
    destroyDb()
    super.afterAll()
  }

  private val jedisService = new JedisRedisService(config.getString("redis.host"), config.getInt("redis.port"), userDao)

  "The Jedis Redis service" should "create a new token valid for a defined number of minutes" in {
    val token = jedisService.setTokenFor(twitterUser)

    token should not equal null
    val rebuilt = new CryptoToken(token)
    rebuilt.getUserAccountName should equal(twitterUser.screenName)
    rebuilt.getExpirationDate.getTime should be > DateTime.now().withChronology(ISOChronology.getInstanceUTC).getMillis
  }

  it should "validate a token that hasn't expired and is associated with an authorised user" in {
    try {
      transaction {
        twitterUsers.insert(twitterUser)
      }
      val token = jedisService.setTokenFor(twitterUser)
      jedisService.isTokenValid(token) should equal (true)
    }
    finally {
      transaction {
        twitterUsers.deleteWhere(_.screenName === twitterUser.screenName)
      }
    }
  }

  it should "mark token as invalid if it isn't associated with an authorised user" in {
    val token = jedisService.setTokenFor(twitterUser)
    jedisService.isTokenValid(token) should equal (false)
  }

  it should "mark a token as invalid if it has expired" in {
    val token = jedisService.setTokenFor(twitterUser)

    val jedis = new Jedis(config.getString("redis.host"), config.getInt("redis.port"))

  }
}
