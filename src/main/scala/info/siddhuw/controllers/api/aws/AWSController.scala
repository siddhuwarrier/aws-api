package info.siddhuw.controllers.api.aws

import info.siddhuw.controllers.api.BaseAPIController
import info.siddhuw.controllers.api.aws.AWSController._
import info.siddhuw.models.daos.DBUserDAO
import info.siddhuw.services.AWSService
import net.logstash.logback.marker.Markers._
import org.scalatra.Unauthorized
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

/**
 * @author Siddhu Warrier
 */

class AWSController(implicit val awsService: AWSService, implicit val userDao: DBUserDAO) extends BaseAPIController {
  val logger = LoggerFactory.getLogger(classOf[AWSController])

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
