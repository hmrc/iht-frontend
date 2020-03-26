package connectors

import iht.models.ApplicantDetails
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.{HeaderCarrier, Upstream5xxResponse}
import utils.stubs.IhtStub
import utils.{IntegrationBaseSpec, TestDataUtil}

class IhtConnectorSpec extends IntegrationBaseSpec with MockitoSugar with TestDataUtil {

  implicit val headerCarrier = HeaderCarrier()
  implicit val request = createFakeRequest()

  val regDetails = testRegistrationDetails.copy(applicantDetails = Some(ApplicantDetails(role = Some("Lead Executor"), nino = Some("AA123456A"))))
  val applicationDetails = testApplicationDetails

  val connector = injectedIhtConnector

  "IhtConnector" when {
    "submitApplication is called" should {
      "handle a successful response from IHT" in {

        IhtStub.stubSuccessfulResponse()

        await(
          connector.submitApplication(
          regDetails.ihtReference.get,
          regDetails.applicantDetails.get.nino.get,
          applicationDetails
          )
        ) shouldBe Some("12345678")
      }

      "handle a 500 response from IHT" in {

        IhtStub.stubInternalServerError()

        val ex = intercept[Upstream5xxResponse] {
          await(
            connector.submitApplication(
              regDetails.ihtReference.get,
              regDetails.applicantDetails.get.nino.get,
              applicationDetails)
          )
        }
        ex.getMessage should contain
          "iht/AA123456A/ABC1234567890/application/submit' returned 500. Response body: 'No registration details found'"
      }

      "handle a 403 response from IHT" in {

        IhtStub.stubForbiddenResponse()

        await(
          connector.submitApplication(
            regDetails.ihtReference.get,
            regDetails.applicantDetails.get.nino.get,
            applicationDetails
          )
        ) shouldBe None
      }
    }
  }

}
