package info.siddhuw.controllers.api.aws

import info.siddhuw.controllers.api.BaseAPIController
import info.siddhuw.controllers.api.BaseAPIController._
import info.siddhuw.models.AWSRegion
import info.siddhuw.models.daos.DBUserDAO
import info.siddhuw.services.{ AWSService, ThrottlingService }
import net.logstash.logback.marker.Markers._
import org.apache.http.HttpStatus
import org.scalatra.Unauthorized
import org.scalatra.swagger.{ Operation, ResponseMessage, Swagger, SwaggerSupport, SwaggerSupportSyntax }
import org.slf4j.{ Logger, LoggerFactory }

import scala.jdk.CollectionConverters._

/**
 * @author Siddhu Warrier
 */

class AWSController(implicit val awsService: AWSService,
    implicit val userDao: DBUserDAO,
    implicit val throttlingService: ThrottlingService,
    implicit val swagger: Swagger) extends BaseAPIController with SwaggerSupport {
  implicit val applicationDescription: String = "The AWS API"
  val logger: Logger = LoggerFactory.getLogger(classOf[AWSController])

  val getRegionsDocs: Operation =
    (apiOperation[AWSRegion]("regions")
      summary "GET list of available AWS regions"
      position 2
      description "This endpoint lists the available AWS regions you can query against."
      authorizations "Authorization"
      responseMessages (
        ResponseMessage(200, "Successfully retrieved").model[AWSRegion],
        ResponseMessage(429, "Too many requests. Check the X-RateLimit-Remaining header to figure out how long to wait for."),
        ResponseMessage(401, "Invalid JWT token")))

  get("/regions", operation(getRegionsDocs)) {
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
