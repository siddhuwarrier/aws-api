package info.siddhuw

import javax.servlet.ServletContext

import com.typesafe.config.ConfigFactory
import info.siddhuw.controllers.AuthUserController
import info.siddhuw.controllers.api.aws.AWSController
import info.siddhuw.controllers.api.aws.ec2.AWSEC2Controller
import info.siddhuw.models.daos.DBUserDAO
import info.siddhuw.services.{ AWSEC2BrowserService, AWSService, JWTTokenService }
import org.scalatra.LifeCycle
import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.{ PrimitiveTypeMode, Session, SessionFactory }

class ScalatraBootstrap extends LifeCycle with PrimitiveTypeMode {
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

    implicit val dbUserDao = new DBUserDAO
    implicit val jwtTokenService = new JWTTokenService(dbUserDao)
    implicit val awsService = new AWSService
    implicit val awsEc2Service = new AWSEC2BrowserService //build the AWS Java SDK here

    context.mount(new AuthUserController, "/auth/*")
    context.mount(new AWSController, "/api/aws/*")
    context.mount(new AWSEC2Controller, "/api/aws/ec2/*")
  }

  private def initDb(): Unit = {
    session.bindToCurrentThread
  }

  private def shutdownDb(): Unit = {
    session.unbindFromCurrentThread
  }
}

