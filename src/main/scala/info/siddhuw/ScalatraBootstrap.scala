package info.siddhuw

import javax.servlet.ServletContext
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.ec2.AmazonEC2Client
import com.typesafe.config.ConfigFactory
import info.siddhuw.controllers.{ AuthUserController, AwsApiSwagger, ResourcesApp }
import info.siddhuw.controllers.api.VersionsController
import info.siddhuw.controllers.api.aws.AWSController
import info.siddhuw.controllers.api.aws.ec2.AWSEC2Controller
import info.siddhuw.metrics.ServiceHealthCheck
import info.siddhuw.models.daos.DBUserDAO
import info.siddhuw.services.{ AWSEC2Service, AWSService, JWTTokenService, ThrottlingService, VersionsService }
import org.scalatra.LifeCycle
import org.scalatra.metrics.MetricsBootstrap
import org.scalatra.metrics.MetricsSupportExtensions.metricsSupportExtensions
import org.scalatra.swagger.{ Swagger, SwaggerEngine }
import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.{ PrimitiveTypeMode, Session, SessionFactory }

import scala.concurrent.ExecutionContext.Implicits.global

class ScalatraBootstrap extends LifeCycle with PrimitiveTypeMode with MetricsBootstrap {
  val conf = ConfigFactory.load("app")

  SessionFactory.concreteFactory = Some(() ⇒ {
    Session.create(
      java.sql.DriverManager.getConnection(
        conf.getString("db.url"),
        conf.getString("db.username"),
        conf.getString("db.password")),
      new PostgreSqlAdapter)
  })
  val session = SessionFactory.newSession

  override def destroy(context: ServletContext): Unit = {
    super.destroy(context)
    shutdownDb()
  }

  override def init(context: ServletContext) {
    initDb()

    implicit val dbUserDao: DBUserDAO = new DBUserDAO
    implicit val jwtTokenService: JWTTokenService = new JWTTokenService(dbUserDao)
    implicit val awsService: AWSService = new AWSService
    implicit val awsEc2Service: AWSEC2Service = new AWSEC2Service(new AmazonEC2Client())
    implicit val versionsService: VersionsService = new VersionsService
    implicit val throttlingService: ThrottlingService = new ThrottlingService
    // swagger
    implicit val swagger: Swagger = new AwsApiSwagger

    healthCheckRegistry.register("service", new ServiceHealthCheck)

    // TODO restrict CORS allowed origins?
    context.setInitParameter("org.scalatra.cors.allowCredentials", "false")
    context.setInitParameter("org.scalatra.cors.enable", "true")
    context.setInitParameter("org.scalatra.cors.allowedOrigins", "*")

    context.mount(new AuthUserController, "/auth", "auth")
    context.mount(new VersionsController, "/versions", "versions")
    context.mount(new AWSController, "/api/aws", "aws")
    context.mount(new AWSEC2Controller, "/api/aws/ec2", name = "ec2")
    context.mountHealthCheckServlet("/health")

    // swagger docs
    context.mount(new ResourcesApp, "/api-explorer")
  }

  private def initDb(): Unit = {
    session.bindToCurrentThread
  }

  private def shutdownDb(): Unit = {
    session.unbindFromCurrentThread
  }
}

