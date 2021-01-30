package info.siddhuw.services

import com.typesafe.config.ConfigFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class VersionsServiceSpec extends AnyFlatSpec with Matchers {
  "VersionsService" should "get the git commit ID from a git.properties file in the classpath" in {
    val expectedVersion = ConfigFactory.load("git.properties").getString("git.commit.id.full")
    new VersionsService().getVersion() should equal(expectedVersion)
  }
}
