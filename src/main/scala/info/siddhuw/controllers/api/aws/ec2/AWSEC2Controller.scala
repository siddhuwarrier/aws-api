package info.siddhuw.controllers.api.aws.ec2

import info.siddhuw.controllers.JsonController
import info.siddhuw.services.AWSEC2BrowserService
import org.json4s.{ DefaultFormats, Formats }
import org.scalatra.{ BadRequest, CorsSupport }

/**
 * @author Siddhu Warrier
 */

class AWSEC2Controller(implicit val awsEc2Service: AWSEC2BrowserService) extends JsonController with CorsSupport {
  override protected implicit def jsonFormats: Formats = DefaultFormats

  before() {
    contentType = formats("json")
  }

  options("/*") {
    response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"))
  }

  get("/instances/count") {
    "count" -> awsEc2Service.count("us-east-1")
  }

  get("/instances") {
    val region = params.getOrElse("region", halt(BadRequest("msg" -> "param region required")))
    awsEc2Service.list(region)
  }
}
