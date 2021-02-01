package info.siddhuw.controllers

import org.scalatra.ScalatraServlet
import org.scalatra.json.JacksonJsonSupport
import org.slf4j.{ Logger, LoggerFactory }
import org.squeryl.PrimitiveTypeMode

/**
 * @author Siddhu Warrier
 */

trait JsonController extends ScalatraServlet
    with PrimitiveTypeMode
    with JacksonJsonSupport {
  private val logger: Logger = LoggerFactory.getLogger(classOf[JsonController])
  before() {
    contentType = formats("json")
  }
}
