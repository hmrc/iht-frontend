package utils.stubs

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.test.Helpers.{FORBIDDEN, INTERNAL_SERVER_ERROR, OK}
import utils.IntegrationBaseSpec

object IhtStub extends IntegrationBaseSpec {

  def responseForSubmitApplication(status: Int, body: String): Unit = {
    stubPost("/iht/AA123456A/ABC1234567890/application/submit", status, body)
  }

  def stubSuccessfulResponse(): Unit = responseForSubmitApplication(OK, "Success response received: 12345678")

  def stubInternalServerError(): Unit = responseForSubmitApplication(INTERNAL_SERVER_ERROR, "No registration details found")

  def stubForbiddenResponse(): Unit = responseForSubmitApplication(FORBIDDEN, "Submitter is not the lead executor")

}
