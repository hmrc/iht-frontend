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

package iht.controllers.registration

import iht.config.AppConfig
import iht.metrics.IhtMetrics

import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import iht.utils.RegistrationKickOutHelper
import org.mockito.ArgumentMatchers._
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class KickoutRegControllerTest extends RegistrationControllerTest with RegistrationKickOutHelper {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with KickoutRegController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  val appConfig = mockAppConfig

  "RegistrationKickoutControllerTest" must {
    "respond suitably to onPageLoad" in {
      val request = createFakeRequest(isAuthorised = true, authRetrieveNino = false)

      def controller = new TestController {
        override val authConnector = mockAuthConnector
        override lazy val metrics:IhtMetrics = mock[IhtMetrics]
        override val cachingConnector = mockCachingConnector
        override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
      }

      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithDeceasedDetails
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      Seq(
        (KickoutDeceasedDateOfDeathDateCapitalTax,
          "page.iht.registration.deceasedDateOfDeath.kickout.date.capital.tax.summary"),
        (KickoutDeceasedDateOfDeathDateOther,
          "page.iht.registration.deceasedDateOfDeath.kickout.date.other.summary"),
        (KickoutDeceasedDetailsLocationScotland,
          "page.iht.registration.deceasedDetails.kickout.location.summary"),
        (KickoutDeceasedDetailsLocationOther,
          "page.iht.registration.deceasedDetails.kickout.location.summary"),
        (KickoutApplicantDetailsProbateScotland,
          "page.iht.registration.applicantDetails.kickout.probate.summary"),
        (KickoutApplicantDetailsProbateNi,
          "page.iht.registration.applicantDetails.kickout.probate.summary"),
        (KickoutNotApplyingForProbate,
          "page.iht.registration.notApplyingForProbate.kickout.summary"),
        (KickoutNotAnExecutor,
          "page.iht.registration.notAnExecutor.kickout.p1")
      ).foreach{kickout=>
        createMockToGetSingleValueFromCache(mockCachingConnector, any(), Some(kickout._1))
        val result: Future[Result] = controller.onPageLoad(request)
        status(result) must be(OK)
        contentAsString(result).contains(messagesApi(kickout._2)) must be (true)
      }
    }

    "redirect to homepage on submit" in {
      val request = createFakeRequest(isAuthorised = true, authRetrieveNino = false)
      def controller = new TestController{
        override val authConnector = mockAuthConnector
        override lazy val metrics:IhtMetrics = mock[IhtMetrics]
        override val cachingConnector = mockCachingConnector
        override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
      }

      val result: Future[Result] = controller.onSubmit(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result).get must be("https://www.gov.uk/inheritance-tax")
    }
  }
}
