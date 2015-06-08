package info.siddhuw.utils

import com.typesafe.config.ConfigFactory
import info.siddhuw.models.APISchema
import org.scalatest.BeforeAndAfterAll
import org.squeryl.adapters.H2Adapter
import org.squeryl.{ PrimitiveTypeMode, Session, SessionFactory }

/**
 * @author Siddhu Warrier
 */

trait DatabaseSupport extends PrimitiveTypeMode {
  self: BeforeAndAfterAll ⇒
  val config = ConfigFactory.load("app")

  SessionFactory.concreteFactory = Some(() ⇒
    Session.create(java.sql.DriverManager.getConnection(config.getString("db.url")), new H2Adapter))
  val session = SessionFactory.newSession

  protected def initDb(): Unit = {
    session.bindToCurrentThread
    APISchema.create
  }

  protected def destroyDb(): Unit = {
    APISchema.drop
    session.unbindFromCurrentThread
  }
}
