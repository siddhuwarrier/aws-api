package info.siddhuw.models.daos

import info.siddhuw.models.APISchema._
import info.siddhuw.utils.DatabaseSupport
import info.siddhuw.utils.builders.DBUserBuilder
import org.scalatest.{ BeforeAndAfter, BeforeAndAfterAll, FlatSpec, Matchers }

/**
 * @author Siddhu Warrier
 */

class DBUserDAOFindSpec extends FlatSpec
    with Matchers
    with BeforeAndAfter
    with BeforeAndAfterAll
    with DatabaseSupport {
  override protected def beforeAll(): Unit = {
    initDb()
  }

  override protected def afterAll(): Unit = {
    destroyDb()
  }

  private val userDAO = new DBUserDAO
  private val userObjs = for (i ← 1 to 10) yield DBUserBuilder.build(username = s"Kim the ${i}th")

  before {
    userObjs.map {
      dbUsers.insert
    }
  }

  after {
    userObjs.map {
      dbUser ⇒
        dbUsers.deleteWhere(_.username === dbUser.username)
    }
  }

  "The DB User DAO" should "find an existing twitter user" in {
    userObjs.forall {
      dbUser ⇒
        userDAO.findById(dbUser.username).isDefined
    } should equal(true)
  }

  it should "return None if the twitter user is not found" in {
    userDAO.findById(DBUserBuilder.build().username) should equal(None)
  }
}
