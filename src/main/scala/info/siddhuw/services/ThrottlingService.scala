package info.siddhuw.services

import com.typesafe.config.ConfigFactory
import info.siddhuw.models.DBUser
import info.siddhuw.services.ThrottlingService.DEFAULT_BANDWIDTH_LIMIT
import io.github.bucket4j.{ Bandwidth, Bucket, Bucket4j }
import org.slf4j.LoggerFactory

import java.util.concurrent.ConcurrentHashMap
import scala.collection._
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.jdk.DurationConverters._
import scala.language.postfixOps

class ThrottlingService {
  // throttling cache represented as a concurrent map in memory. TODO replace with Redis
  val throttlingCache: concurrent.Map[String, Bucket] = new ConcurrentHashMap[String, Bucket]().asScala

  def consumeToken(user: DBUser): Either[Unit, FiniteDuration] = {
    val bucket = throttlingCache.getOrElseUpdate(user.username, initBucket())
    val probe = bucket.tryConsumeAndReturnRemaining(1)
    if (probe.isConsumed) {
      // successful; does not matter when it refills
      return Left()
    }

    Right(probe.getNanosToWaitForRefill nanos)
  }

  private def initBucket(): Bucket = {
    Bucket4j.builder()
      .addLimit(DEFAULT_BANDWIDTH_LIMIT)
      .build()
  }
}

object ThrottlingService {
  // the default bandwidth limit is set to 100 requests for a minute.
  val DEFAULT_BANDWIDTH_LIMIT: Bandwidth = Bandwidth.simple(
    ConfigFactory load "app" getInt "rate_limit.bucket_capacity",
    (1 minute) toJava)
}
