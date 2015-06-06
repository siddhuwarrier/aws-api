package info.siddhuw

import javax.servlet.ServletContext

import com.typesafe.config.ConfigFactory
import info.siddhuw.controllers.OAuthUserController
import info.siddhuw.models.APISchema
import info.siddhuw.models.daos.TwitterUserDaoComponent
import info.siddhuw.services.{TwitterCredentialRetrievalServiceComponent, TwitterLoginServiceComponent}
import org.scalatra.LifeCycle
import org.scribe.builder.ServiceBuilder
import org.scribe.builder.api.TwitterApi
import org.scribe.oauth.OAuthService
import org.squeryl.adapters.{PostgreSqlAdapter, H2Adapter}
import org.squeryl.{PrimitiveTypeMode, Session, SessionFactory}


class ScalatraBootstrap extends LifeCycle with PrimitiveTypeMode {
  val conf = ConfigFactory.load("app")

  SessionFactory.concreteFactory = Some(() => {
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
    shutdownDb
  }

  override def init(context: ServletContext) {
    initDb

    val loginService = new TwitterLoginServiceComponent with TwitterUserDaoComponent with TwitterCredentialRetrievalServiceComponent {
      override val oauthService: OAuthService = new ServiceBuilder()
        .provider(classOf[TwitterApi])
        .apiKey(conf.getString("twitter.apikey"))
        .apiSecret(conf.getString("twitter.apisecret"))
        .callback(conf.getString("twitter.callback"))
        .build()
    }.loginService
    context.mount(new OAuthUserController(loginService), "/auth/*")
  }

  private def initDb: Unit = {
    session.bindToCurrentThread
  }

  private def shutdownDb = {
    session.unbindFromCurrentThread
  }
}

