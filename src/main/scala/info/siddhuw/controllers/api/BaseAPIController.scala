package info.siddhuw.controllers.api

import info.siddhuw.auth.APIAuthenticationSupport
import info.siddhuw.controllers.JsonController
import info.siddhuw.controllers.api.BaseAPIController.{ TooManyRequestsMsg, UnauthorizedErrMsg }
import info.siddhuw.services.ThrottlingService
import net.logstash.logback.marker.Markers.appendEntries
import org.json4s.{ DefaultFormats, Formats }
import org.scalatra.{ CorsSupport, TooManyRequests, Unauthorized }
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.language.postfixOps

/**
 * @author Siddhu Warrier
 */

trait BaseAPIController extends JsonController
    with APIAuthenticationSupport
    with CorsSupport {
  private val logger = LoggerFactory.getLogger(classOf[BaseAPIController])

  implicit val throttlingService: ThrottlingService

  override protected implicit def jsonFormats: Formats = DefaultFormats

  val logData = Map("endpoint" -> "ANY authenticated endpoint")
  before() {
    contentType = formats("json")
    authenticate() match {
      case Some(user) ⇒
        throttlingService.consumeToken(user) match {
          case Right(retryAfter: FiniteDuration) ⇒
            logger.error(appendEntries(logData.asJava), s"Failed: $TooManyRequestsMsg")
            halt(
              TooManyRequests("msg" -> TooManyRequestsMsg,
                headers = Map("X-RateLimit-Remaining" -> retryAfter.toMillis.toString)))
          case _ ⇒
            logger.debug(appendEntries(logData.asJava), "Throttling not required")
        }
      case None ⇒
        logger.error(appendEntries(logData.asJava), s"Failed: $UnauthorizedErrMsg")
        halt(Unauthorized("msg" -> UnauthorizedErrMsg))
    }
  }

  options("/*") {
    response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"))
  }
}

object BaseAPIController {
  val UnauthorizedErrMsg = "You need to be logged in to view this endpoint"
  val TooManyRequestsMsg = "Too many requests"
}
