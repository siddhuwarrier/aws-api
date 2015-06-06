package info.siddhuw.models.daos

import info.siddhuw.builders.TwitterUserBuilder
import info.siddhuw.utils.DatabaseSupport
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfter, Matchers, FlatSpec}
import info.siddhuw.models.APISchema._

/**
 * @author Siddhu Warrier
 */


class TwitterUserDaoFindIntegrationTest extends FlatSpec
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

  private val twitterUserDao = new TwitterUserDaoComponent {}.userDao
  private val twitterUserObjs = List.fill(10)(TwitterUserBuilder.build())

  before {
    twitterUserObjs.map {
      twitterUsers.insert
    }
  }

  after {
    twitterUserObjs.map {
      twitterUser =>
      twitterUsers.deleteWhere(_.screenName === twitterUser.screenName)
    }
  }

  "The Twitter User DAO" should "find an existing twitter user" in {
    twitterUserObjs.forall {
      twitterUser =>
        twitterUserDao.findById(twitterUser.screenName).isDefined
    } should equal (true)
  }

  it should "return None if the twitter user is not found" in {
    twitterUserDao.findById(TwitterUserBuilder.build().screenName) should equal (None)
  }
}
