/*
 * Copyright 2022 HM Revenue & Customs
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
import iht.models.application.exemptions.PartnerExemption
import iht.testhelpers.CommonBuilder
import iht.testhelpers.TestHelper._
import iht.utils.CommonHelper._
import iht.views.html.application.exemption.partner.partner_permanent_home_question
import iht.views.html.ihtHelpers.custom.name
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController



class PartnerPermanentHomeQuestionControllerTest extends ApplicationControllerTest{

  protected abstract class TestController extends FrontendController(mockControllerComponents) with PartnerPermanentHomeQuestionController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val partnerPermanentHomeQuestionView: partner_permanent_home_question = app.injector.instanceOf[partner_permanent_home_question]
    override val nameView: name = app.injector.instanceOf[name]
  }

  def partnerPermanentHomeQuestionController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector

  }

  def partnerPermanentHomeQuestionControllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  "PartnerPermanentHomeQuestionController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = partnerPermanentHomeQuestionControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = partnerPermanentHomeQuestionControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load, page contains Return link and Save button" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = partnerPermanentHomeQuestionController.onPageLoad (createFakeRequest())
      status(result) mustBe (OK)
      contentAsString(result) must include (messagesApi("iht.saveAndContinue"))

    }

    "save application and go to Exemptions Overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(partner = Some(PartnerExemption(
          Some(true), Some(true), None, None, None, None, Some(1000))))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val partnerPermanentHomeQuestion = CommonBuilder.buildPartnerExemption.copy(isAssetForDeceasedPartner = Some(true))

      val filledPartnerPermanentHomeQuestionForm = partnerPermanentHomeQuestionForm.fill(partnerPermanentHomeQuestion)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPartnerPermanentHomeQuestionForm.data
        .toSeq: _*).withMethod("POST")

      val result = partnerPermanentHomeQuestionController.onSubmit(request)
      status(result) mustBe (SEE_OTHER)
      redirectLocation(result) must be(Some(addFragmentIdentifierToUrl(routes.PartnerOverviewController.onPageLoad.url, ExemptionsPartnerHomeID)))
    }

    "display validation message when incomplete form is submitted" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(partner = Some(PartnerExemption(
          None, Some(true), None, None, None, None, Some(1000))))))

      val filledPartnerPermanentHomeQuestionForm = partnerPermanentHomeQuestionForm.fill(CommonBuilder.buildPartnerExemption.
        copy(isPartnerHomeInUK = None))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPartnerPermanentHomeQuestionForm.data.toSeq: _*).withMethod("POST")

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = partnerPermanentHomeQuestionController.onSubmit()(request)
      status(result) must be (BAD_REQUEST)
      contentAsString(result) must include (messagesApi("error.problem"))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      partnerPermanentHomeQuestionController.onPageLoad(createFakeRequest()))
  }
}
