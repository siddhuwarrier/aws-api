package info.siddhuw.services

import com.amazonaws.regions.{ Region, Regions }
import com.amazonaws.services.ec2.AmazonEC2
import com.amazonaws.services.ec2.model.{ DescribeInstancesRequest, DescribeInstancesResult, Filter, Reservation }
import info.siddhuw.models.EC2Instance

import scala.collection.JavaConversions._

/**
 * @author Siddhu Warrier
 */

class AWSEC2Service(val ec2: AmazonEC2) {

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

    instanceResult.getReservations.flatMap {
      listInReservation
    }.toList
  }

  private def listInReservation(reservation: Reservation): List[EC2Instance] = {
    reservation.getInstances.map {
      EC2Instance.apply
    }.toList
  }

  private def describeInstances(regionName: String, activeOnly: Boolean): DescribeInstancesResult = {
    val filters = activeOnly match {
      case true ⇒
        createActiveFilter()
      case false ⇒
        Array[Filter]()
    }
    val region = Region.getRegion(Regions.fromName(regionName))
    ec2.setRegion(region)

    ec2.describeInstances(new DescribeInstancesRequest().withFilters(filters: _*))
  }

  private def createActiveFilter(): Array[Filter] = {
    Array(new Filter("instance-state-name", List("running", "stopped")))
  }
}
