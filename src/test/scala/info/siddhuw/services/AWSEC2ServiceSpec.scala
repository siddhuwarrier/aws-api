package info.siddhuw.services

import com.amazonaws.AmazonClientException
import com.amazonaws.regions.{ Region, Regions }
import com.amazonaws.services.ec2.AmazonEC2
import com.amazonaws.services.ec2.model._
import com.typesafe.config.ConfigFactory
import info.siddhuw.models.EC2Instance
import info.siddhuw.utils.builders.EC2InstanceBuilder
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

import scala.jdk.CollectionConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * @author Siddhu Warrier
 */

class AWSEC2ServiceSpec extends AnyFlatSpec
    with MockitoSugar
    with Matchers {
  "The AWS EC2 Browser Service list instances method" should "retrieve the list of active instances for a specified region" in {
    val expected = List.fill(10)(EC2InstanceBuilder.build)
    val expectedRequestWithFilters = new DescribeInstancesRequest()
      .withFilters(new Filter("instance-state-name", List("running", "stopped").asJava))
    val mockEC2 = mock[AmazonEC2]
    val mockDescribeInstancesResult = buildMockResult(expected)
    when(mockEC2.describeInstances(expectedRequestWithFilters)).thenReturn(mockDescribeInstancesResult)

    val awsEC2Service = new AWSEC2Service(mockEC2)
    val region = Regions.US_EAST_1.getName
    val actual = awsEC2Service.list(region)

    actual should equal(expected)
    verify(mockEC2).setRegion(Region.getRegion(Regions.US_EAST_1))
    verify(mockEC2).describeInstances(expectedRequestWithFilters)
  }

  it should "retrieve the list of all (active and inactive) instances for a specified region" in {
    val expected = List.fill(10)(EC2InstanceBuilder.build)
    val expectedRequest = new DescribeInstancesRequest()
    val mockEC2 = mock[AmazonEC2]
    val mockDescribeInstancesResult = buildMockResult(expected)
    when(mockEC2.describeInstances(expectedRequest)).thenReturn(mockDescribeInstancesResult)

    val awsEC2Service = new AWSEC2Service(mockEC2)
    val region = Regions.US_EAST_1.getName
    val actual = awsEC2Service.list(region, activeOnly = false)

    actual should equal(expected)
    verify(mockEC2).setRegion(Region.getRegion(Regions.US_EAST_1))
    verify(mockEC2).describeInstances(expectedRequest)
  }

  it should "fail if the region provided is invalid" in {
    val mockEC2 = mock[AmazonEC2]

    val awsEC2Service = new AWSEC2Service(mockEC2)

    an[IllegalArgumentException] should be thrownBy awsEC2Service.list("this ain't an amazon region")
    verifyZeroInteractions(mockEC2)
  }

  it should "fail if querying EC2 fails" in {
    val mockEC2 = mock[AmazonEC2]
    doThrow(new AmazonClientException("Failure")).when(mockEC2).describeInstances(any[DescribeInstancesRequest])

    val awsEC2Service = new AWSEC2Service(mockEC2)

    an[AmazonClientException] should be thrownBy awsEC2Service.list(Regions.US_EAST_1.getName)
  }

  it should "fail if querying EC2 times out" in {
    val result = buildMockResult(List(EC2InstanceBuilder.build))
    val mockEC2 = mock[AmazonEC2]
    val conf = ConfigFactory.load("app")

    when(mockEC2.describeInstances(any[DescribeInstancesRequest])).thenAnswer(new Answer[DescribeInstancesResult] {
      override def answer(invocationOnMock: InvocationOnMock): DescribeInstancesResult = {
        Thread.sleep((conf.getLong("aws.wait_time_sec") + 1 seconds).toMillis)
        result
      }
    })

    val awsEC2Service = new AWSEC2Service(mockEC2)
    an[AmazonClientException] should be thrownBy awsEC2Service.list(Regions.US_EAST_1.getName)
  }

  private def buildMockResult(expectedInstances: List[EC2Instance]): DescribeInstancesResult = {
    val mockDescribeInstancesResult = mock[DescribeInstancesResult]
    val mockReservation = mock[Reservation]
    when(mockDescribeInstancesResult.getReservations).thenReturn(List(mockReservation).asJava)

    val mockInstances = expectedInstances.map {
      expected ⇒
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

        mockInstance
    }

    when(mockReservation.getInstances).thenReturn(mockInstances.asJava)
    mockDescribeInstancesResult
  }
}
