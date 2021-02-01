package info.siddhuw.controllers.api.aws

import com.google.common.base.CharMatcher
import com.google.common.net.HttpHeaders._
import info.siddhuw.controllers.AwsApiSwagger
import info.siddhuw.controllers.api.BaseAPIController
import info.siddhuw.models.APISchema._
import info.siddhuw.models.daos.DBUserDAO
import info.siddhuw.models.{ AWSRegion, DBUser }
import info.siddhuw.services.{ AWSService, JWTTokenService, ThrottlingService }
import info.siddhuw.utils.DatabaseSupport
import info.siddhuw.utils.crypto.PasswordHasher
import org.apache.commons.httpclient.HttpStatus._
import org.json4s.jackson.JsonMethods._
import org.json4s.{ DefaultFormats, Formats }
import org.mockito.Mockito.when
import org.scalatest._
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatra.test.scalatest.ScalatraSuite
import org.mockito.Matchers.any
import org.mockito.Mockito
import org.scalatra.swagger.Swagger

import scala.language.postfixOps
import scala.concurrent.duration._

/**
 * @author Siddhu Warrier
 */

class AWSControllerRegionSpec extends AnyFeatureSpec
    with GivenWhenThen
    with BeforeAndAfterAll
    with BeforeAndAfter
    with DatabaseSupport
    with ScalatraSuite
    with MockitoSugar
    with Matchers {
  implicit val awsService: AWSService = new AWSService //AWS Service does not currently connect to the internet; so no need to mock
  implicit val userDao: DBUserDAO = new DBUserDAO
  implicit val mockThrottlingService: ThrottlingService = mock[ThrottlingService]
  implicit val swagger: Swagger = new AwsApiSwagger
  addServlet(new AWSController, "/api/aws/*")

  implicit def jsonFormats: Formats = DefaultFormats

  override def beforeAll(): Unit = {
    super.beforeAll() //The Embedded Jetty container is started up in ScalatraSuite's beforeAll. So we need to call it.
    initDb()
  }

  override def afterAll(): Unit = {
    destroyDb()
    super.afterAll()
  }

  before {
    when(mockThrottlingService.consumeToken(any[DBUser])).thenAnswer(_ ⇒ Left())
  }

  after {
    Mockito.reset(mockThrottlingService)
  }

  Feature("Retrieve the list of AWS regions") {
    Scenario("Retrieve the full list of regions, except for China and GovCloud") {
      Given("I am logged in")
      loggedIn {
        (_: DBUser, token: String) ⇒

          When("I retrieve the list of regions")
          get("/api/aws/regions", headers = Map(AUTHORIZATION -> ("Bearer " + token))) {

            Then("I should receive a status code of 200")
            status should equal(SC_OK)

            And("I should receive a JSON with all of the regions except China and GovCloud")
            val actualRegions = parse(body).extract[List[AWSRegion]]
            val expectedRegions = awsService.regions()

            actualRegions should equal(expectedRegions)
          }
      }
    }

    Scenario("Fail to retrieve full list if requests exceeded") {
      Given("I am logged in")
      loggedIn {
        (dbUser: DBUser, token: String) ⇒

          And("I have exceeded the rate limit allowed by the API")
          when(mockThrottlingService.consumeToken(dbUser)).thenReturn(Right(30 seconds))

          When("I retrieve the list of regions")
          get("/api/aws/regions", headers = Map(AUTHORIZATION -> ("Bearer " + token))) {

            Then("I should receive a status code of 429")
            status should equal(429)

            And("I should receive a JSON with all of the regions except China and GovCloud")
            val errMsg = compact(render(parse(body) \ "msg"))
            CharMatcher.is('\"').trimFrom(errMsg) should equal(BaseAPIController.TooManyRequestsMsg)
          }
      }
    }

    Scenario("Cannot retrieve AWS regions if not logged in") {
      Given("I am not logged in")
      When("I retrieve the list of AWS regions")
      get("/api/aws/regions") {
        Then("I should receive a status code of 401")
        status should equal(SC_UNAUTHORIZED)

        And("I should receive an error message")
        val errMsg = compact(render(parse(body) \ "msg"))
        CharMatcher.is('\"').trimFrom(errMsg) should equal(BaseAPIController.UnauthorizedErrMsg)
      }
    }
  }

  def loggedIn(func: (DBUser, String) ⇒ Unit): Unit = {
    //TODO maybe consider actually making a GET request to the /auth URL
    val password = "validPassword"
    val (pwHash, salt) = PasswordHasher.hash(password)
    val dbUser = DBUser("username", pwHash, salt)

    transaction {
      dbUsers.insert(dbUser)
    }
    func(dbUser, new JWTTokenService(userDao).create(dbUser))

    transaction {
      dbUsers.deleteWhere(_.username === dbUser.username)
    }
  }
}
