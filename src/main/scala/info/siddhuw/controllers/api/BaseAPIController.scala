package info.siddhuw.controllers.api

import info.siddhuw.auth.APIAuthenticationSupport
import info.siddhuw.controllers.JsonController
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.CorsSupport

/**
 * @author Siddhu Warrier
 */

trait BaseAPIController extends JsonController
    with APIAuthenticationSupport
    with CorsSupport {
  override protected implicit def jsonFormats: Formats = DefaultFormats

  before() {
    contentType = formats("json")
  }

  options("/*") {
    response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"))
  }
}
