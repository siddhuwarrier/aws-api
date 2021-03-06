package info.siddhuw.controllers.api.aws.ec2

import com.amazonaws.{ AmazonClientException, AmazonServiceException }
import com.amazonaws.regions.Regions
import com.google.common.base.CharMatcher
import com.google.common.net.HttpHeaders._
import info.siddhuw.controllers.AwsApiSwagger
import info.siddhuw.controllers.api.BaseAPIController
import info.siddhuw.models.APISchema._
import info.siddhuw.models.{ DBUser, EC2Instance }
import info.siddhuw.models.daos.DBUserDAO
import info.siddhuw.services.{ AWSEC2Service, JWTTokenService, ThrottlingService }
import info.siddhuw.utils.DatabaseSupport
import info.siddhuw.utils.builders.EC2InstanceBuilder
import info.siddhuw.utils.crypto.PasswordHasher
import org.json4s.jackson.JsonMethods._
import org.json4s.{ DefaultFormats, Formats }
import org.scalatest._
import org.scalatra.test.scalatest.ScalatraSuite
import org.apache.commons.httpclient.HttpStatus._
import org.mockito.Matchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatra.swagger.Swagger

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * @author Siddhu Warrier
 */

class AWSEC2ListInstancesControllerSpec extends AnyFeatureSpec
    with GivenWhenThen
    with BeforeAndAfterAll
    with BeforeAndAfter
    with DatabaseSupport
    with ScalatraSuite
    with MockitoSugar
    with Matchers {
  implicit val mockAwsEc2Service: AWSEC2Service = mock[AWSEC2Service]
  //mock out dependency that connects to the internet
  implicit val userDao: DBUserDAO = new DBUserDAO
  implicit val mockThrottlingService: ThrottlingService = mock[ThrottlingService]
  implicit val swagger: Swagger = new AwsApiSwagger
  addServlet(new AWSEC2Controller, "/api/aws/ec2/*")

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
    Mockito.reset(mockThrottlingService, mockAwsEc2Service)
  }

  Feature("Retrieve the list of AWS instances in a region") {
    Scenario("Retrieve the full list of active AWS instances") {
      Given("I have active EC2 instances")
      val expected = List(EC2InstanceBuilder.build)
      val region = Regions.EU_WEST_1.getName
      when(mockAwsEc2Service.list(region, activeOnly = true)).thenReturn(expected)

      And("I am logged in")
      loggedIn {
        (_: DBUser, token: String) ⇒

          When("I retrieve the list of AWS instances")
          get("/api/aws/ec2/instances", params = Map("region" -> region), headers = Map(AUTHORIZATION -> ("Bearer " + token))) {

            Then("I should receive a status code of 200")
            status should equal(SC_OK)

            And("I should receive a JSON with all of the expected instances")
            val actual = parse(body).extract[List[EC2Instance]]
            actual should equal(expected)
          }
      }
    }
    Scenario("Respond with 429 if the user has made more queries in a minute than the limit") {
      Given("I have active EC2 instances")
      val expected = List(EC2InstanceBuilder.build)
      val region = Regions.EU_WEST_1.getName
      when(mockAwsEc2Service.list(region, activeOnly = true)).thenReturn(expected)

      And("I am logged in")
      loggedIn {
        (dbUser: DBUser, token: String) ⇒

          And("I have already retrieved the list of AWS instances as many times than I am allowed to in a minute")
          val expectedWaitTime = Right(30 seconds)
          when(mockThrottlingService.consumeToken(dbUser)).thenReturn(expectedWaitTime)

          And("I retrieve the list of AWS instances again within the same minute")
          get("/api/aws/ec2/instances", params = Map("region" -> region), headers = Map(AUTHORIZATION -> ("Bearer " + token))) {

            Then("I should receive a status code of 429")
            status should equal(429)

            And("I should receive an error message")
            val errMsg = compact(render(parse(body) \ "msg"))
            CharMatcher.is('\"').trimFrom(errMsg) should equal(BaseAPIController.TooManyRequestsMsg)

            And("I should be told the number of seconds to wait in the `X-Rate-Limit-Retry-After-Seconds` header")
            header.get("X-RateLimit-Remaining") should equal(Some(expectedWaitTime.value.toMillis.toString))
          }
      }
    }

    Scenario("Respond with 400 if region query parameter not specified") {
      Given("I am logged in")
      loggedIn {
        (_: DBUser, token: String) ⇒

          When("I retrieve the list of AWS instances")
          get("/api/aws/ec2/instances", headers = Map(AUTHORIZATION -> ("Bearer " + token))) {
            Then("I should receive a status code of 400")
            status should equal(SC_BAD_REQUEST)

            And("I should receive an error message")
            val errMsg = compact(render(parse(body) \ "msg"))
            CharMatcher.is('\"').trimFrom(errMsg) should equal(AWSEC2Controller.RegionParamMissingErrMsg)
          }
      }
    }

    Scenario("Respond with 400 if region query parameter contains region that is not recognised by the EC2 service") {
      val invalidRegion = "invalid-region"
      when(mockAwsEc2Service.list(invalidRegion, activeOnly = true)).thenThrow(new IllegalArgumentException("Invalid region"))

      Given("I am logged in")
      loggedIn {
        (_: DBUser, token: String) ⇒

          When("I retrieve the list of AWS instances")
          get("/api/aws/ec2/instances", params = Map("region" -> invalidRegion), headers = Map(AUTHORIZATION -> ("Bearer " + token))) {

            Then("I should receive a status code of 400")
            status should equal(SC_BAD_REQUEST)

            And("I should receive an error message")
            val errMsg = compact(render(parse(body) \ "msg"))
            CharMatcher.is('\"').trimFrom(errMsg) should equal(AWSEC2Controller.RegionParamInvalidErrMsg)
          }
      }
    }

    Scenario("Respond with 401 if the user is not logged in") {
      val region = "some-region-it-does-not-matter-as-we-wont-be-performing-a-request"
      When("I retrieve the list of AWS instances")
      get("/api/aws/ec2/instances", params = Map("region" -> region)) {

        Then("I should receive a status code of 401")
        status should equal(SC_UNAUTHORIZED)

        And("I should receive an error message")
        val errMsg = compact(render(parse(body) \ "msg"))
        CharMatcher.is('\"').trimFrom(errMsg) should equal(BaseAPIController.UnauthorizedErrMsg)
      }
    }

    Scenario("respond with 500 if EC2 service returns null unexpectedly") {
      val region = Regions.AP_SOUTHEAST_1.getName
      when(mockAwsEc2Service.list(region, activeOnly = true)).thenReturn(null)

      Given("I am logged in")
      loggedIn {
        (_: DBUser, token: String) ⇒

          When("I retrieve the list of AWS instances")
          get("/api/aws/ec2/instances", params = Map("region" -> region), headers = Map(AUTHORIZATION -> ("Bearer " + token))) {
            Then("I should receive a status code of 500")
            status should equal(SC_INTERNAL_SERVER_ERROR)

            And("I should receive an error message")
            val errMsg = compact(render(parse(body) \ "msg"))
            CharMatcher.is('\"').trimFrom(errMsg) should equal(AWSEC2Controller.InternalServerErrMsg)
          }
      }
    }

    Scenario("Respond with 503 if failure retrieving instances from EC2") {
      val region = Regions.AP_SOUTHEAST_1.getName

      Given("I am logged in")
      loggedIn {
        (_: DBUser, token: String) ⇒

          When("I retrieve the list of AWS instances")
          And("the EC2 Service fails")
          when(mockAwsEc2Service.list(region, activeOnly = true)).thenThrow(new AmazonClientException("Mock AWS failure"))
          get("/api/aws/ec2/instances", params = Map("region" -> region), headers = Map(AUTHORIZATION -> ("Bearer " + token))) {
            Then("I should receive a status code of 503")
            status should equal(SC_SERVICE_UNAVAILABLE)

            And("I should receive an error message")
            val errMsg = compact(render(parse(body) \ "msg"))
            CharMatcher.is('\"').trimFrom(errMsg) should equal(AWSEC2Controller.UnableToReadFromEC2ErrMsg)
          }
      }
    }

    Scenario("Respond with 500 if the AWS credentials configured are invalid") {
      val invalidCredentialsException = new AmazonServiceException("Auth fail")
      invalidCredentialsException.setStatusCode(SC_UNAUTHORIZED)
      val region = Regions.EU_WEST_1.getName

      Given("I am logged in")
      loggedIn {
        (_: DBUser, token: String) ⇒
          When("I retrieve the list of AWS instances")
          And("The EC2 service fails due to bad credentials")
          when(mockAwsEc2Service.list(region, activeOnly = true)).thenThrow(invalidCredentialsException)
          get("/api/aws/ec2/instances", params = Map("region" -> region), headers = Map(AUTHORIZATION -> ("Bearer " + token))) {

            Then("I should receive a status code of 500")
            status should equal(SC_INTERNAL_SERVER_ERROR)

            Then("I should receive an error message")
            val errMsg = compact(render(parse(body) \ "msg"))
            CharMatcher.is('\"').trimFrom(errMsg) should equal(AWSEC2Controller.AWSCredentialsInvalidErrMsg)
          }
      }
    }
  }

  //TODO In the future, consider moving this into a shared trait as it is used by AWSControllerRegionSpec and this spec
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
