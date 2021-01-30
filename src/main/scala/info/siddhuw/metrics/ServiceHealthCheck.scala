package info.siddhuw.metrics

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheck.Result

class ServiceHealthCheck extends HealthCheck {
  override def check(): HealthCheck.Result = Result.healthy()
}
