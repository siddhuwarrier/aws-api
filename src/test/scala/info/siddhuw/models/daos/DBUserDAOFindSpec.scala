package info.siddhuw.models.daos

import info.siddhuw.builders.DBUserBuilder
import info.siddhuw.utils.DatabaseSupport
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfter, Matchers, FlatSpec }
import info.siddhuw.models.APISchema._

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
  private val userObjs = List.fill(10)(DBUserBuilder.build())

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
