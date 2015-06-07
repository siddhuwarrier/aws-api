package info.siddhuw.services

import org.scalatest.{ Matchers, FlatSpec }
import org.scalatest.mock.MockitoSugar

/**
 * @author Siddhu Warrier
 */

class AWSEC2BrowserServiceListInstancesSpec extends FlatSpec
    with MockitoSugar
    with Matchers {
  "The AWS EC2 Browser Service" should "retrieve the list of instances for a region" in {
    println(new AWSEC2BrowserService().list("eu-west-1"))
  }

  it should "return an empty list if there are more instances than can be returned within the bounds" in {
    pending
  }

  it should "fail if the credentials are invalid" in {
    pending
  }

  it should "fail if the region requested for is invalid" in {
    pending
  }

  it should "fail if it was not possible to connect to AWS" in {
    pending
  }
}
