package info.siddhuw.controllers.api.aws.ec2

import org.scalatest.{ Matchers, FlatSpec }

/**
 * @author Siddhu Warrier
 */

class AWSEC2ListInstancesControllerSpec extends FlatSpec with Matchers {
  "The AWS EC2 List Instances endpoint" should "return a JSONified list of EC2 instances for the region specified" in {
    pending
  }

  it should "respond with a 400 if the region specified is invalid" in {
    pending
  }

  it should "default the start parameter to 0" in {
    pending
  }

  it should "default the end parameter to 5" in {
    pending
  }

  //TODO respond with a 500 instead?
  it should "return an empty list if there was a failure retrieving instances from EC2" in {
    pending
  }
}
