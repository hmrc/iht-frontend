/*
 * Copyright 2019 HM Revenue & Customs
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

package iht.controllers.registration

import iht.connector.CachingConnector
import iht.metrics.Metrics
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder}
import iht.testhelpers.MockObjectBuilder._
import iht.utils.RegistrationKickOutHelper
import org.mockito.ArgumentMatchers._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class KickoutRegControllerTest extends RegistrationControllerTest {

  "RegistrationKickoutControllerTest" must {
    "respond suitably to onPageLoad" in {
      val request = createFakeRequest(isAuthorised = true, authRetrieveNino = false)

      def controller = new KickoutRegController{
        override val authConnector = mockAuthConnector
        override lazy val metrics:Metrics = mock[Metrics]
        override val cachingConnector = mockCachingConnector
        override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
      }

      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithDeceasedDetails
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      Seq(
        (RegistrationKickOutHelper.KickoutDeceasedDateOfDeathDateCapitalTax,
          "page.iht.registration.deceasedDateOfDeath.kickout.date.capital.tax.summary"),
        (RegistrationKickOutHelper.KickoutDeceasedDateOfDeathDateOther,
          "page.iht.registration.deceasedDateOfDeath.kickout.date.other.summary"),
        (RegistrationKickOutHelper.KickoutDeceasedDetailsLocationScotland,
          "page.iht.registration.deceasedDetails.kickout.location.summary"),
        (RegistrationKickOutHelper.KickoutDeceasedDetailsLocationOther,
          "page.iht.registration.deceasedDetails.kickout.location.summary"),
        (RegistrationKickOutHelper.KickoutApplicantDetailsProbateScotland,
          "page.iht.registration.applicantDetails.kickout.probate.summary"),
        (RegistrationKickOutHelper.KickoutApplicantDetailsProbateNi,
          "page.iht.registration.applicantDetails.kickout.probate.summary")
      ).foreach{kickout=>
        createMockToGetSingleValueFromCache(mockCachingConnector, any(), Some(kickout._1))
        val result: Future[Result] = controller.onPageLoad(request)
        status(result) must be(OK)
        contentAsString(result).contains(messagesApi(kickout._2)) must be (true)
      }
    }

    "redirect to homepage on submit" in {
      val request = createFakeRequest(isAuthorised = true, authRetrieveNino = false)
      def controller = new KickoutRegController{
        override val authConnector = mockAuthConnector
        override lazy val metrics:Metrics = mock[Metrics]
        override val cachingConnector = mockCachingConnector
      }

      val result: Future[Result] = controller.onSubmit(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result).get must be("https://www.gov.uk/inheritance-tax")
    }
  }
}
