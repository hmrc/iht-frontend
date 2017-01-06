/*
 * Copyright 2017 HM Revenue & Customs
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
import iht.testhelpers.MockObjectBuilder._
import iht.utils.RegistrationKickOutHelper
import org.mockito.Matchers._
import play.api.i18n.Messages
import play.api.mvc.Result
import play.api.test.Helpers._

import scala.concurrent.Future

class KickoutControllerTest extends RegistrationControllerTest {

  before {
    mockCachingConnector = mock[CachingConnector]
  }

  "RegistrationKickoutControllerTest" must {
    "respond suitably to onPageLoad" in {
      val request = createFakeRequest(isAuthorised = true)
      val controller = new KickoutController{
        override val authConnector = createFakeAuthConnector(isAuthorised=true)
        override val metrics:Metrics = Metrics
        override val cachingConnector = mockCachingConnector
      }

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
        status(result) should be(OK)
        contentAsString(result).contains(Messages(kickout._2)) should be (true)
      }
    }

    "redirect to homepage on submit" in {
      val request = createFakeRequest(isAuthorised = true)
      val controller = new KickoutController{
        override val authConnector = createFakeAuthConnector(isAuthorised=true)
        override val metrics:Metrics = Metrics
        override val cachingConnector = mockCachingConnector
      }

      val result: Future[Result] = controller.onSubmit(request)
      status(result) should be(SEE_OTHER)
      redirectLocation(result).get should be("https://www.gov.uk/inheritance-tax")
    }
  }
}
