package info.siddhuw.metrics

import com.codahale.metrics.health.HealthCheck.Result
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ServiceHealthCheckSpec extends AnyFlatSpec with Matchers {
  "ServiceHealthCheck" should "always return true if the application is reachable" in {
    new ServiceHealthCheck().check() should equal(Result.healthy())
  }
}
