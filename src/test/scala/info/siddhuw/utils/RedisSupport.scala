package info.siddhuw.utils

import org.scalatest.BeforeAndAfterAll
import redis.embedded.RedisServer

/**
 * @author Siddhu Warrier
 */


trait RedisSupport {
  self: BeforeAndAfterAll =>

  protected val redisServer = new RedisServer(6379)

  override protected def beforeAll(): Unit = {
    redisServer.start()
  }

  override protected def afterAll(): Unit = {
    redisServer.stop()
  }

}
