package info.siddhuw.services

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.{ Regions, Region }
import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.model.{ Filter, Reservation, DescribeInstancesResult }
import com.typesafe.config.ConfigFactory
import info.siddhuw.models.EC2Instance
import scala.collection.JavaConversions._

/**
 * @author Siddhu Warrier
 */

class AWSEC2BrowserService {
  val config = ConfigFactory.load("app")
  val awsCreds = new BasicAWSCredentials(config.getString("aws.access_key_id"), config.getString("aws.secret_access_key"))

  val ec2 = new AmazonEC2Client(awsCreds)

  /**
   * Frustratingly, Amazon EC2 does not provide a RESTful API (and uses a Query interface as described in this
   * possibly outdated blogpost: https://gehrcke.de/2009/06/aws-about-api/)
   *
   * As a result, the Java SDK is the only non-ridiculously-complicated way to query EC2, and this does not provide
   * ways in which to restrict the number of results received, for instance (beyond filtering by a bunch of criteria
   * as described in com.amazonaws.services.ec2.model.Filter). This leaves us with three ways to implement the pagination:
   * <ul>
   *   <li>Retrieve the full list of instances each time a page request is made by the client, and filter it.
   *   The advantages of this approach are that it separates concerns between server and client elegantly,
   *   keeps the server API stateless, gets the user the latest information, and avoids unnecessary complications in the client code. However, the
   *   disadvantage is the number of queries we will make to the AWS API and the fact that the data may change on
   *   the server-side in unpredictable ways between page loads.</li>
   *   <li>Maintain state on the server: Violates principles of stateless REST API. Therefore, discarded.</li>
   *   <li>Perform pagination on the client side: The advantages are that it reduces the number of requests
   *   made to AWS, is likely to result in a more responsive UI (as we're not talking of a clearly countable number
   *   of instances), and will result in paginated information being consistent. The disadvantage is that the user
   *   will not see the latest data unless he refreshes the page (given changing the page is similar to a refresh,
   *   this problem exists in the first of the options described above.</li>
   *
   *   Therefore, for the purposes of this exercise, I have decided to go with paginating results on the client-side
   *   and keeping the server API dumb.
   * </ul>
   *
   * @param region
   * @return
   */
  def list(region: String): List[EC2Instance] = {
    val filter = new Filter()
    val instanceResult = describeInstances(region)

    instanceResult.getReservations.flatMap {
      listInReservation
    }.toList
  }

  private def listInReservation(reservation: Reservation): List[EC2Instance] = {
    reservation.getInstances.map {
      EC2Instance.apply
    }.toList
  }

  def count(region: String): Int = {
    val instanceResult = describeInstances(region)

    //sum up all the instances within all of the reservations
    instanceResult.getReservations.foldLeft(0) {
      (num, reservation) ⇒
        num + reservation.getInstances.size()
    }
  }

  private def describeInstances(regionName: String): DescribeInstancesResult = {
    val region = Region.getRegion(Regions.fromName(regionName))
    ec2.setRegion(region)

    ec2.describeInstances()
  }
}
