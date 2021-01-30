package info.siddhuw.controllers.api

import com.typesafe.config.ConfigFactory
import info.siddhuw.controllers.JsonController
import info.siddhuw.services.VersionsService
import net.logstash.logback.marker.Markers.appendEntries
import org.json4s.{ DefaultFormats, Formats }
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters.MapHasAsJava
import scala.util.{ Failure, Success, Try }

class VersionsController(implicit val versionsService: VersionsService) extends JsonController {
  override protected implicit def jsonFormats: Formats = DefaultFormats
  val logger = LoggerFactory.getLogger(classOf[VersionsController])

  before() {
    contentType = formats("json")
  }

  // we register this to /versions in ScalatraBootstrap, hence the relative path here is /
  get("/") {
    val logData = Map("endpoint" -> "GET /versions")

    "version" -> (Try(versionsService.getVersion()) match {
      case Success(version) ⇒ version
      case Failure(e) ⇒
        logger.error(appendEntries((logData + ("Action" -> "Get git commit version")).asJava), "Failed: ", e)
        "unknown"
    })
  }
}
