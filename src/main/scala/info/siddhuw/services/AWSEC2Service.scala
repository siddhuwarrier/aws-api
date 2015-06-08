package info.siddhuw.services

import com.amazonaws.AmazonClientException
import com.amazonaws.regions.{ Region, Regions }
import com.amazonaws.services.ec2.AmazonEC2
import com.amazonaws.services.ec2.model.{ DescribeInstancesRequest, DescribeInstancesResult, Filter, Reservation }
import com.typesafe.config.ConfigFactory
import info.siddhuw.models.EC2Instance

import scala.jdk.CollectionConverters._
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * @author Siddhu Warrier
 */

class AWSEC2Service(val ec2: AmazonEC2)(implicit ec: ExecutionContext) {
  val config = ConfigFactory.load("app")

  /**
   * List Amazon EC2 instances
   *
   * @param region AWS region
   * @param activeOnly If this is set to true, only 'running' and 'stopped' instances are returned. (default: true)
   * @return the list of AWS EC2 instances of type EC2Instance
   */
  def list(region: String, activeOnly: Boolean = true): List[EC2Instance] = {
    val filter = new Filter()
    val instanceResult = describeInstances(region, activeOnly)

    instanceResult.getReservations.asScala.flatMap {
      listInReservation
    }.toList
  }

  private def listInReservation(reservation: Reservation): List[EC2Instance] = {
    reservation.getInstances.asScala.map {
      EC2Instance.apply
    }.toList
  }

  private def describeInstances(regionName: String, activeOnly: Boolean): DescribeInstancesResult = {
    val filters = if (activeOnly) {
      createActiveFilter()
    } else {
      Array[Filter]()
    }
    val region = Region.getRegion(Regions.fromName(regionName))
    ec2.setRegion(region)

    val awsTimeout = config.getLong("aws.wait_time_sec") seconds

    Try(Await.result(Future(ec2.describeInstances(new DescribeInstancesRequest().withFilters(filters: _*))), awsTimeout)) match {
      case Success(result) ⇒
        result
      case Failure(e) ⇒
        throw new AmazonClientException(e)
    }
  }

  private def createActiveFilter(): Array[Filter] = {
    Array(new Filter("instance-state-name", List("running", "stopped").asJava))
  }
}
