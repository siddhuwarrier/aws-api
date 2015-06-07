package info.siddhuw.services

import com.amazonaws.regions.Regions
import info.siddhuw.models.AWSRegion
import org.scalatest.{ Matchers, FlatSpec }

/**
 * @author Siddhu Warrier
 */

class AWSServiceGetRegionsSpec extends FlatSpec with Matchers {
  "The Get Regions method in the AWS Service" should "return the full list of regions" in {
    val regions = new AWSService().regions(excludeChina = false, excludeGov = false)
    regions should equal(Regions.values().map(r ⇒ AWSRegion(r.getName)))
  }

  it should "exclude the China region if requested" in {
    val regions = new AWSService().regions(excludeChina = true, excludeGov = false)
    regions should not contain AWSRegion(Regions.CN_NORTH_1.getName)
  }

  it should "exclude the US Government cloud if requested" in {
    val regions = new AWSService().regions(excludeChina = false, excludeGov = true)
    regions should not contain AWSRegion(Regions.GovCloud.getName)
  }

  it should "exclude both China and the US Government cloud if requested" in {
    val regions = new AWSService().regions(excludeChina = true, excludeGov = true)
    regions should not contain AWSRegion(Regions.GovCloud.getName)
    regions should not contain AWSRegion(Regions.CN_NORTH_1.getName)
  }
}
