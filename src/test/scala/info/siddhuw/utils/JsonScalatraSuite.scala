package info.siddhuw.utils

import org.json4s.JValue
import org.json4s.jackson.JsonMethods._
import org.scalatra.test.scalatest.ScalatraSuite

/**
 * @author Siddhu Warrier
 */

trait JsonScalatraSuite extends ScalatraSuite {
  def postJson[A](uri: String, body: JValue, headers: Map[String, String] = Map())(f: ⇒ A): A =
    post(uri, compact(render(body)).getBytes("utf-8"), Map("Content-Type" -> "application/json") ++ headers)(f)
}
