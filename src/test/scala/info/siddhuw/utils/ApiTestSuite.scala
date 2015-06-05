package info.siddhuw.utils

import info.siddhuw.models.APISchema
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.scalatest.BeforeAndAfter
import org.scalatra.test.scalatest.ScalatraSuite
import org.squeryl.adapters.H2Adapter
import org.squeryl.{AbstractSession, PrimitiveTypeMode, Session, SessionFactory}

/**
 * @author Siddhu Warrier
 * @date 05/06/15.
 */

trait ApiTestSuite extends ScalatraSuite with PrimitiveTypeMode with BeforeAndAfter {

  var session: AbstractSession = _

  override def beforeAll() = {
    super.beforeAll()
    initSession()
    initTestDb()
  }

  private def initSession(): Unit = {
    SessionFactory.concreteFactory = Some(() =>
      Session.create(java.sql.DriverManager.getConnection("jdbc:h2:mem:test1"), new H2Adapter))
    session = SessionFactory.newSession
  }

  private def initTestDb(): Unit = {
    transaction {
      APISchema.create
    }
  }

  override def afterAll() = {
    session.close
    super.afterAll()
  }

  def postJson[A](uri: String, body: JValue, headers: Map[String, String] = Map())(f: => A): A = {
    post(uri, compact(render(body)).getBytes("utf-8"), Map("Content-Type" -> "application/json") ++ headers)(f)
  }
}
