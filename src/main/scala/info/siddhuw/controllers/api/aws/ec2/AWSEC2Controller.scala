package info.siddhuw.controllers.api.aws.ec2

import com.amazonaws.{ AmazonServiceException, AmazonClientException }
import info.siddhuw.auth.APIAuthenticationSupport
import info.siddhuw.controllers.JsonController
import info.siddhuw.controllers.api.BaseAPIController
import info.siddhuw.controllers.api.aws.ec2.AWSEC2Controller._
import info.siddhuw.models.daos.DBUserDAO
import info.siddhuw.services.AWSEC2Service
import net.logstash.logback.marker.Markers._
import org.json4s.{ DefaultFormats, Formats }
import org.scalatra._
import org.slf4j.LoggerFactory
import org.apache.http.HttpStatus._

import scala.collection.JavaConversions._

/**
 * @author Siddhu Warrier
 */

class AWSEC2Controller(implicit val awsEc2Service: AWSEC2Service, implicit val userDao: DBUserDAO) extends BaseAPIController {
  val logger = LoggerFactory.getLogger(classOf[AWSEC2Controller])

  get("/instances") {
    val logData = Map("endpoint" -> "GET /instances")

    authenticate() match {
      case Some(_) ⇒
        val region = params.getOrElse("region", halt(BadRequest("msg" -> RegionParamMissingErrMsg)))

        try {
          logger.info(appendEntries(logData + ("Action" -> "List instances")), "Start")
          val instances = awsEc2Service.list(region, activeOnly = true)
          Option(instances).getOrElse(halt(InternalServerError("msg" -> InternalServerErrMsg)))
          logger.info(appendEntries(logData + ("Action" -> "List instances")), "Done")

          instances
        } catch {
          case e: AmazonServiceException if e.getStatusCode == SC_UNAUTHORIZED ⇒
            logger.error(appendEntries(logData + ("Action" -> "List instances")), "Failed: ", e)
            halt(InternalServerError("msg" -> AWSCredentialsInvalidErrMsg))

          case e: AmazonClientException ⇒
            logger.error(appendEntries(logData + ("Action" -> "List instances")), "Failed: ", e)
            halt(ServiceUnavailable("msg" -> UnableToReadFromEC2ErrMsg))

          case e: IllegalArgumentException ⇒
            logger.error(appendEntries(logData + ("Action" -> "List instances")), "Failed: ", e)
            halt(BadRequest("msg" -> RegionParamInvalidErrMsg))
        }

      case None ⇒
        logger.error(appendEntries(logData), s"Failed: $UnauthorizedErrMsg")
        halt(Unauthorized("msg" -> UnauthorizedErrMsg))
    }
  }
}

object AWSEC2Controller {
  val RegionParamMissingErrMsg = "query param 'region' is missing"
  val RegionParamInvalidErrMsg = "query param 'region' is invalid"
  val UnableToReadFromEC2ErrMsg = "Unable to read from EC2. Please try again later."
  val AWSCredentialsInvalidErrMsg = "The AWS credentials configured on the server are invalid. Please contact customer support."
  val InternalServerErrMsg = "Internal Server Error. Please contact customer support."
  val UnauthorizedErrMsg = "You need to be logged in to view this endpoint"
}
