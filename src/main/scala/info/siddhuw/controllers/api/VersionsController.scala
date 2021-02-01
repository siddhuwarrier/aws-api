package info.siddhuw.controllers.api

import info.siddhuw.controllers.JsonController
import info.siddhuw.services.VersionsService
import net.logstash.logback.marker.Markers.appendEntries
import org.json4s.{ DefaultFormats, Formats }
import org.scalatra.swagger.{ Parameter, Swagger, SwaggerSupport, SwaggerSupportSyntax }
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters.MapHasAsJava
import scala.util.{ Failure, Success, Try }

class VersionsController(implicit val versionsService: VersionsService, implicit val swagger: Swagger) extends JsonController with SwaggerSupport {
  override protected implicit def jsonFormats: Formats = DefaultFormats
  implicit val applicationDescription: String = "The Microservice Version API"

  val logger = LoggerFactory.getLogger(classOf[VersionsController])

  before() {
    contentType = formats("json")
  }

  val versionsApiDocs: SwaggerSupportSyntax.OperationBuilder =
    (apiOperation[Map[String, String]]("versions")
      position 1
      summary "Get microservice version"
      description "This endpoint returns the last git commit that was deployed")

  // we register this to /versions in ScalatraBootstrap, hence the relative path here is /
  get("/", operation(versionsApiDocs)) {
    val logData = Map("endpoint" -> "GET /versions")

    "version" -> (Try(versionsService.getVersion()) match {
      case Success(version) ⇒ version
      case Failure(e) ⇒
        logger.error(appendEntries((logData + ("Action" -> "Get git commit version")).asJava), "Failed: ", e)
        "unknown"
    })
  }

}
