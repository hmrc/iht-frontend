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

package iht.controllers.application.status

import iht.constants.Constants
import iht.controllers.application.ApplicationControllerTest
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.testhelpers.MockObjectBuilder._
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import org.mockito.ArgumentMatchers._
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Request
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class ApplicationStatusControllerTest extends ApplicationControllerTest {


  def applicationStatusController = new ApplicationStatusController {
    def getView = (ihtReference, deceasedName, probateDetails) => (request: Request[_], formPartialRetriever: FormPartialRetriever) =>
      iht.views.html.application.status.in_review_application(ihtReference,
                                                      deceasedName,
                                                      probateDetails)(request, applicationMessages, formPartialRetriever)

    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = mockAuthConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def createMocksForRegistrationAndApplication(rd: RegistrationDetails, ad: ApplicationDetails) = {
    createMockToGetCaseDetails(mockIhtConnector, Future.successful(rd))
    createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(rd)))
    createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rd))
    createMockToGetApplicationDetails(mockIhtConnector, Some(ad))
    createMockToGetProbateDetails(mockIhtConnector)
    createMockToGetProbateDetailsFromCache(mockCachingConnector)
    createMockToStoreSingleValueInCache(
          cachingConnector = mockCachingConnector,
          singleValueFormKey = same(Constants.PDFIHTReference),
          singleValueReturn = CommonBuilder.DefaultIHTReference)
  }

  "ApplicationStatusController" must {
    "return the correct page on load" in {
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithDeceasedDetails
      val applicationDetails = CommonBuilder.buildApplicationDetailsWithAllAssets

      createMocksForRegistrationAndApplication(registrationDetails, applicationDetails)

      val result = applicationStatusController.onPageLoad("")(createFakeRequest())
      status(result) mustBe OK
      contentAsString(result) must include(messagesApi("page.iht.application.overview.inreview.browserTitle"))
    }
  }
}
