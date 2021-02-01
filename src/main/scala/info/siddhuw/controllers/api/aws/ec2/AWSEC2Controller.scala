package info.siddhuw.controllers.api.aws.ec2

import com.amazonaws.{ AmazonClientException, AmazonServiceException }
import info.siddhuw.auth.APIAuthenticationSupport
import info.siddhuw.controllers.JsonController
import info.siddhuw.controllers.api.BaseAPIController
import info.siddhuw.controllers.api.aws.ec2.AWSEC2Controller._
import info.siddhuw.models.daos.DBUserDAO
import info.siddhuw.services.{ AWSEC2Service, ThrottlingService }
import net.logstash.logback.marker.Markers._
import org.json4s.{ DefaultFormats, Formats }
import org.scalatra._
import org.slf4j.{ Logger, LoggerFactory }
import org.apache.http.HttpStatus._

import scala.jdk.CollectionConverters._

/**
 * @author Siddhu Warrier
 */

class AWSEC2Controller(implicit val awsEc2Service: AWSEC2Service, implicit val userDao: DBUserDAO, implicit val throttlingService: ThrottlingService) extends BaseAPIController {
  val logger: Logger = LoggerFactory.getLogger(classOf[AWSEC2Controller])

  get("/instances") {
    val logData = Map("endpoint" -> "GET /instances")

    val region = params.getOrElse("region", halt(BadRequest("msg" -> RegionParamMissingErrMsg)))

    try {
      logger.info(appendEntries((logData + ("Action" -> "List instances")).asJava), "Start")
      val instances = awsEc2Service.list(region, activeOnly = true)
      Option(instances).getOrElse(halt(InternalServerError("msg" -> InternalServerErrMsg)))
      logger.info(appendEntries((logData + ("Action" -> "List instances")).asJava), "Done")

      instances
    } catch {
      case e: AmazonServiceException if e.getStatusCode == SC_UNAUTHORIZED ⇒
        logger.error(appendEntries((logData + ("Action" -> "List instances")).asJava), "Failed: ", e)
        halt(InternalServerError("msg" -> AWSCredentialsInvalidErrMsg))

      case e: AmazonClientException ⇒
        logger.error(appendEntries((logData + ("Action" -> "List instances")).asJava), "Failed: ", e)
        halt(ServiceUnavailable("msg" -> UnableToReadFromEC2ErrMsg))

      case e: IllegalArgumentException ⇒
        logger.error(appendEntries((logData + ("Action" -> "List instances")).asJava), "Failed: ", e)
        halt(BadRequest("msg" -> RegionParamInvalidErrMsg))
    }
  }
}

object AWSEC2Controller {
  val RegionParamMissingErrMsg = "query param 'region' is missing"
  val RegionParamInvalidErrMsg = "query param 'region' is invalid"
  val UnableToReadFromEC2ErrMsg = "Unable to read from EC2. Please try again later."
  val AWSCredentialsInvalidErrMsg = "The AWS credentials configured on the server are invalid. Please contact customer support."
  val InternalServerErrMsg = "Internal Server Error. Please contact customer support."
}
