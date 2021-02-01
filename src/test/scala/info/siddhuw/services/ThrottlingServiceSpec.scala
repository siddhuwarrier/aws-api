package info.siddhuw.services

import com.typesafe.config.ConfigFactory
import info.siddhuw.utils.builders.DBUserBuilder
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ThrottlingServiceSpec extends AnyFlatSpec with Matchers {
  "The Throttle Service" should "return nothing if there are tokens to consume in the bucket" in {
    val throttleService = new ThrottlingService
    val user = DBUserBuilder.build("burak-crush-kim-jong-un")
    throttleService.consumeToken(user) match {
      case Right(_) ⇒ fail("Should not have returned a duration which indicates the amount of time left before I can query again")
      case _ ⇒ // nothing to do, this is what we expect; it returns a Unit
    }
  }

  it should "return the amount of time required to wait if the bucket is empty" in {
    val throttleService = new ThrottlingService
    val user = DBUserBuilder.build("burak-crush-kim-jong-un")
    (1 to ConfigFactory.load("app").getInt("rate_limit.bucket_capacity")) foreach (_ ⇒ throttleService.consumeToken(user))
    throttleService.consumeToken(user) match {
      case Left(_) ⇒ fail("Should have refused service coz no tokens left")
      case Right(duration) ⇒ duration should not be (null)
    }
  }
}
