package info.siddhuw.controllers

import com.typesafe.scalalogging.LazyLogging
import org.scalatra.ScalatraServlet
import org.scalatra.json.JacksonJsonSupport
import org.squeryl.PrimitiveTypeMode

/**
 * @author Siddhu Warrier
 */

trait JsonController extends ScalatraServlet
    with LazyLogging
    with PrimitiveTypeMode
    with JacksonJsonSupport {
}
