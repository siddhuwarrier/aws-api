package info.siddhuw.utils.builders

import java.util.UUID

import info.siddhuw.models.EC2Instance

/**
 * @author Siddhu Warrier
 */

object EC2InstanceBuilder {
  def build: EC2Instance = {
    EC2Instance(UUID.randomUUID.toString, "t1.micro", "running", "test-avail-zone", "173.22.22.22", "10.10.1.1")
  }
}
