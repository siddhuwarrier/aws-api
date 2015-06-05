package info.siddhuw.controllers

import org.json4s.{DefaultFormats, Formats}
import org.scalatra._

/**
 * @author Siddhu Warrier
 */

class UserController extends JsonController {
  protected implicit val jsonFormats: Formats = DefaultFormats

  post("/") {
    authenticate() match {
      case Some(user) =>
        //TODO looks like I'm hit by a Scalatra bug similar to this one inc codeplex: http://stackoverflow.com/questions/11533867/set-cookie-header-with-multiple-cookies
        //only the first cookie is being set; need to verify if this problem is just with the tests parsing the response, or with how the response is sent
        //println("I can haz cookies: " + cookies.get("lockhart-aws-api")
        cookies.delete(JettySessionCookieKey)

        response
      case _ =>
        halt(Unauthorized("Invalid username/password"))
    }
  }

  override def username: String = {
    (parsedBody \ "username").extractOpt[String].getOrElse("")
  }

  override def password: String = {
    (parsedBody \ "password").extractOpt[String].getOrElse("")
  }
}
