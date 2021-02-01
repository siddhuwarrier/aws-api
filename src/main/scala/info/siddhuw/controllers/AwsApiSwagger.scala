package info.siddhuw.controllers

import org.scalatra.swagger._
import org.scalatra.{CorsSupport, ScalatraServlet}

class ResourcesApp(implicit val swagger: Swagger) extends ScalatraServlet
    with NativeSwaggerBase with CorsSupport

object AwsApiInfo extends ApiInfo(
  "The AWS EC2 API Wrapper API",
  "Docs for the AWS EC2 Wrapper API",
  "http://www.siddhuw.info",
  ContactInfo("Siddhu Warrier", "http://www.siddhuw.info", "me@siddhuw.info"),
  LicenseInfo("Apache License", "https://www.apache.org/licenses/LICENSE-2.0.txt"))

class AwsApiSwagger extends Swagger(Swagger.SpecVersion, "0.1.0", AwsApiInfo) {
}
