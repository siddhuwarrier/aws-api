package info.siddhuw

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * @author Siddhu Warrier
 */


package object auth {
  val UserResolutionTimeout = 1 second
  val CookieKey = "lockhart-aws-api"
}
