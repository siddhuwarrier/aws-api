package info.siddhuw.models

import com.amazonaws.services.ec2.model.{ Instance, InstanceState, Placement }
import info.siddhuw.utils.builders.EC2InstanceBuilder
import org.mockito.Mockito._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

/**
 * @author Siddhu Warrier
 */

class EC2InstanceFactorySpec extends AnyFlatSpec
    with Matchers
    with MockitoSugar {
  "The EC2 instance factory" should "build an EC2 Instance from an AWS instance result" in {
    val expected = EC2InstanceBuilder.build
    val mockInstance = mock[Instance]
    when(mockInstance.getInstanceId).thenReturn(expected.id)
    when(mockInstance.getInstanceType).thenReturn(expected.instanceType)
    val mockInstanceState = mock[InstanceState]
    when(mockInstanceState.getName).thenReturn(expected.state)
    when(mockInstance.getState).thenReturn(mockInstanceState)
    val mockPlacement = mock[Placement]
    when(mockPlacement.getAvailabilityZone).thenReturn(expected.availZone)
    when(mockInstance.getPlacement).thenReturn(mockPlacement)
    when(mockInstance.getPublicIpAddress).thenReturn(expected.elasticIP)
    when(mockInstance.getPrivateIpAddress).thenReturn(expected.privateIP)

    EC2Instance(mockInstance) should equal(expected)
  }

  it should "not build an EC2 instance if the AWS instance result does not exist" in {
    an[IllegalArgumentException] should be thrownBy EC2Instance(null)
  }
}
