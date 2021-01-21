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

package iht.controllers.application.exemptions.partner

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.testhelpers.TestHelper._
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import iht.utils.CommonHelper._
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class ExemptionPartnerNameControllerTest extends ApplicationControllerTest {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with ExemptionPartnerNameController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def partnerNameController = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def partnerNameControllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  val registrationDetailsWithNoIhtRef = CommonBuilder.buildRegistrationDetails copy (
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    ihtReference = None)

  "ExemptionPartnerNameController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = partnerNameControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = partnerNameControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "return OK on page load" in {

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(CommonBuilder.buildApplicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = partnerNameController.onPageLoad(createFakeRequest(isAuthorised = true))
      status(result) must be(OK)
      contentAsString(result) must include(messagesApi("page.iht.application.exemptions.partner.name.title"))
    }

    "respond with error when ApplicationDetails could not be retrieved on page load" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = None,
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = partnerNameController.onPageLoad(createFakeRequest(isAuthorised = true))
      status(result) must be(INTERNAL_SERVER_ERROR)
    }

    "save and redirect to spouse or civil partner exemptions overview page on successful page submit" in {
      val partnerExemptionValues = CommonBuilder.buildPartnerExemption

      val partnerForm = partnerExemptionNameForm.fill(partnerExemptionValues)
      implicit val request = createFakeRequest(isAuthorised = true).withFormUrlEncodedBody(partnerForm.data.toSeq: _*)

      val applicationDetails = Some(CommonBuilder.buildApplicationDetails.copy(allExemptions = Some
      (CommonBuilder.buildAllExemptions.copy(partner = Some(CommonBuilder.buildPartnerExemption)))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = applicationDetails,
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = partnerNameController.onSubmit(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(addFragmentIdentifierToUrl(routes.PartnerOverviewController.onPageLoad().url, ExemptionsPartnerNameID)))
    }

    "show relevant error message when page fails in validation while submission" in {
      val partnerExemptionValues = CommonBuilder.buildPartnerExemption.copy(firstName = Some(""), lastName = Some(""))

      val partnerForm = partnerExemptionNameForm.fill(partnerExemptionValues)
      implicit val request = createFakeRequest(isAuthorised = true).withFormUrlEncodedBody(partnerForm.data.toSeq: _*)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)

      val result = partnerNameController.onSubmit(request)
      status(result) must be(BAD_REQUEST)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      partnerNameController.onPageLoad(createFakeRequest()))
  }
}
