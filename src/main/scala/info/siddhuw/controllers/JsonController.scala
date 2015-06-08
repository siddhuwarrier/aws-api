package info.siddhuw.controllers

import org.scalatra.ScalatraServlet
import org.scalatra.json.JacksonJsonSupport
import org.squeryl.PrimitiveTypeMode

/**
 * @author Siddhu Warrier
 */

trait JsonController extends ScalatraServlet
    with PrimitiveTypeMode
    with JacksonJsonSupport {
}
