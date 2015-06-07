package info.siddhuw.services

import com.amazonaws.regions.Regions
import info.siddhuw.models.AWSRegion

/**
 * @author Siddhu Warrier
 */

class AWSService {

  /**
   * Gets the list of AWS regions.
   * @param excludeGov (default: true) Exclude the Government regions AWS are setting up as normal access
   *                   credentials will not work with these regions.
   * @param excludeChina (default: true) Exclude the China region, as normal access credentials appear not to
   *                     work with this region.
   */
  def regions(excludeGov: Boolean = true, excludeChina: Boolean = true): List[AWSRegion] = {
    Regions.values().filterNot {
      excludeGov && _ == Regions.GovCloud
    }.filterNot {
      excludeChina && _ == Regions.CN_NORTH_1
    }.map {
      region ⇒
        AWSRegion(region.getName)
    }.toList
  }
}
