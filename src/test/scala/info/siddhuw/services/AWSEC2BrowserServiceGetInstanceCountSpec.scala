package info.siddhuw.services

import org.scalatest.{ Matchers, FlatSpec }

/**
 * @author Siddhu Warrier
 */

class AWSEC2BrowserServiceGetInstanceCountSpec extends FlatSpec with Matchers {
  "The AWS EC2 Browser Service" should "retrieve the number of instances across all regions" in {
    println(new AWSEC2BrowserService().count("us-east-1"))
  }

  it should "fail if the credentials are invalid" in {
    pending
  }

  it should "fail if it is not possible to connect to the AWS API" in {
    pending
  }
}
