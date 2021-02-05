/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import iht.models.ApplicantDetails
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
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

        val ex = intercept[UpstreamErrorResponse] {
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
