package info.siddhuw.controllers.api.aws

import info.siddhuw.controllers.api.BaseAPIController
import info.siddhuw.controllers.api.BaseAPIController._
import info.siddhuw.models.daos.DBUserDAO
import info.siddhuw.services.{ AWSService, ThrottlingService }
import net.logstash.logback.marker.Markers._
import org.scalatra.Unauthorized
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters._

/**
 * @author Siddhu Warrier
 */

class AWSController(implicit val awsService: AWSService, implicit val userDao: DBUserDAO, implicit val throttlingService: ThrottlingService) extends BaseAPIController {
  val logger = LoggerFactory.getLogger(classOf[AWSController])

  get("/regions") {
    val logData = Map("endpoint" -> "GET /regions")
    authenticate() match {
      case Some(_) ⇒
        logger.info(appendEntries((logData + ("Action" -> "Retrieve regions")).asJava), "Start")
        val regions = awsService.regions()
        logger.info(appendEntries((logData + ("Action" -> "Retrieve regions")).asJava), "Done")

        regions
      case None ⇒
        logger.error(appendEntries(logData.asJava), s"Failed: $UnauthorizedErrMsg")
        halt(Unauthorized("msg" -> UnauthorizedErrMsg))
    }
  }
}
