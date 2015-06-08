package info.siddhuw.models

import com.amazonaws.services.ec2.model.{Instance => AWSInstanceModel}

/**
 * @author Siddhu Warrier
 */

case class EC2Instance(id: String, instanceType: String, state: String, availZone: String, elasticIP: String, privateIP: String)

object EC2Instance {
  def apply(inst: AWSInstanceModel): EC2Instance = {
    require(inst != null, "arg cannot be null")

    EC2Instance(inst.getInstanceId, inst.getInstanceType, inst.getState.getName,
      inst.getPlacement.getAvailabilityZone, inst.getPublicIpAddress, inst.getPrivateIpAddress)
  }
}

