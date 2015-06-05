package info.siddhuw

import javax.servlet.ServletContext

import info.siddhuw.controllers.UserController
import info.siddhuw.models.APISchema
import org.scalatra.LifeCycle
import org.squeryl.adapters.H2Adapter
import org.squeryl.{PrimitiveTypeMode, Session, SessionFactory}


class ScalatraBootstrap extends LifeCycle with PrimitiveTypeMode {

  SessionFactory.concreteFactory = Some(() =>
    Session.create(java.sql.DriverManager.getConnection("jdbc:h2:mem:test1"), new H2Adapter))
  val session = SessionFactory.newSession

  override def destroy(context: ServletContext): Unit = {
    super.destroy(context)
    session.unbindFromCurrentThread
  }

  override def init(context: ServletContext) {
    session.bindToCurrentThread
    APISchema.create
    //users.insert(User(AuthController.APP_USERNAME, "blah", "bleeh"))

    context.mount(new UserController, "/auth/*")
  }
}

