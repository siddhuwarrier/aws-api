package info.siddhuw.controllers.api

import com.typesafe.config.ConfigFactory
import info.siddhuw.controllers.AwsApiSwagger
import info.siddhuw.services.VersionsService
import info.siddhuw.utils.JsonScalatraSuite
import org.apache.commons.httpclient.HttpStatus.SC_OK
import org.json4s.JString
import org.json4s.jackson.JsonMethods.{ compact, parse, render }
import org.mockito.Mockito.when
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{ BeforeAndAfterAll, GivenWhenThen }
import org.scalatestplus.mockito.MockitoSugar
import org.scalatra.swagger.Swagger

class VersionsControllerSpec extends AnyFeatureSpec
    with JsonScalatraSuite
    with GivenWhenThen
    with BeforeAndAfterAll
    with MockitoSugar
    with Matchers {
  implicit val versionsService = mock[VersionsService]
  implicit val swagger: Swagger = new AwsApiSwagger

  addServlet(new VersionsController, "/versions/*")

  Feature("Get service version") {
    Scenario("Version present in git.properties") {
      Given("I have the git commit ID in git.properties")
      when(versionsService.getVersion()) thenReturn "burak-crush-pineapple"

      When("I make a GET request to /versions")
      get("/versions") {

        Then("I should receive a status code of 200")
        status should equal(SC_OK)

        And("the version should be as expected")
        val version = (parse(body) \ "version").asInstanceOf[JString].s
        version should equal("burak-crush-pineapple")
      }
    }

    Scenario("Versions service fails to get commit") {
      Given("The Versions service blows up trying to get git commit")
      when(versionsService.getVersion()) thenThrow (new IllegalArgumentException("burak-crush-labdrador"))

      When("I make a GET request to /versions")
      get("/versions") {

        Then("I should receive a status code of 200")
        status should equal(SC_OK)

        And("the version should be unknown")
        val version = (parse(body) \ "version").asInstanceOf[JString].s
        version should equal("unknown")
      }
    }
  }
}
