package info.siddhuw.controllers.api.aws

import info.siddhuw.auth.APIAuthenticationSupport
import info.siddhuw.controllers.JsonController
import info.siddhuw.models.daos.DBUserDAO
import info.siddhuw.services.AWSService
import org.json4s.{ DefaultFormats, Formats }
import org.scalatra.{ Unauthorized, CorsSupport }
import org.slf4j.LoggerFactory
import net.logstash.logback.marker.Markers._
import scala.collection.JavaConversions._
import AWSController._

/**
 * @author Siddhu Warrier
 */

class AWSController(implicit val awsService: AWSService, implicit val userDao: DBUserDAO) extends JsonController
    with APIAuthenticationSupport
    with CorsSupport {
  override protected implicit def jsonFormats: Formats = DefaultFormats

  val logger = LoggerFactory.getLogger(classOf[AWSController])

  before() {
    contentType = formats("json")
  }

  options("/*") {
    response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"))
  }

  get("/regions") {
    val logData = Map("endpoint" -> "GET /regions")
    authenticate() match {
      case Some(_) ⇒
        logger.info(appendEntries(logData + ("Action" -> "Retrieve regions")), "Start")
        val regions = awsService.regions()
        logger.info(appendEntries(logData + ("Action" -> "Retrieve regions")), "Done")

        regions
      case None ⇒
        logger.error(appendEntries(logData), s"Failed: $UnauthorizedMsg")
        halt(Unauthorized("msg" -> UnauthorizedMsg))
    }
  }
}

object AWSController {
  val UnauthorizedMsg = "You need to be logged in to view this endpoint"
}
