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
import iht.models.application.exemptions.PartnerExemption
import iht.testhelpers.CommonBuilder
import iht.testhelpers.TestHelper._
import iht.utils.CommonHelper._
import iht.views.html.application.exemption.partner.partner_nino
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController



class PartnerNinoControllerTest extends ApplicationControllerTest{

  protected abstract class TestController extends FrontendController(mockControllerComponents) with PartnerNinoController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val partnerNinoView: partner_nino = app.injector.instanceOf[partner_nino]
  }

  def partnerNinoController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def partnerNinoControllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  "PartnerNinoController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = partnerNinoControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = partnerNinoControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
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

      val result = partnerNinoController.onPageLoad (createFakeRequest())
      status(result) mustBe (OK)
      contentAsString(result) must include (messagesApi("iht.estateReport.exemptions.partner.returnToAssetsLeftToSpouse"))
      contentAsString(result) must include (messagesApi("iht.saveAndContinue"))

    }

    "save application and go to Partner Overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(partner = Some(PartnerExemption(
          Some(true), Some(true), None, None, None, None, Some(1000))))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val partnerNino = CommonBuilder.buildPartnerExemption.copy(isAssetForDeceasedPartner = Some(true))

      val filledPartnerNinoForm = partnerNinoForm.fill(partnerNino)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPartnerNinoForm.data
        .toSeq: _*)

      val result = partnerNinoController.onSubmit(request)
      status(result) mustBe (SEE_OTHER)
      redirectLocation(result) must be(Some(addFragmentIdentifierToUrl(routes.PartnerOverviewController.onPageLoad().url, ExemptionsPartnerNinoID)))
    }

    "display error validation message when incomplete form is submitted" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(partner = Some(PartnerExemption(
          None, Some(true), None, None, None, None, Some(1000))))))

      val filledPartnerNinoForm = partnerNinoForm.fill(CommonBuilder.buildPartnerExemption.
        copy(nino = Some("")))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPartnerNinoForm.data.toSeq: _*)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = partnerNinoController.onSubmit()(request)
      status(result) must be (BAD_REQUEST)
      contentAsString(result) must include("a problem")
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      partnerNinoController.onPageLoad(createFakeRequest()))
  }
}
