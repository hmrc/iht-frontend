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

package iht.controllers.application.tnrb

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.forms.TnrbForms._
import iht.testhelpers.{CommonBuilder, ContentChecker}
import iht.views.html.application.tnrb.permanent_home
import org.joda.time.LocalDate
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class PermanentHomeControllerTest  extends ApplicationControllerTest{

  protected abstract class TestController extends FrontendController(mockControllerComponents) with PermanentHomeController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val permanentHomeView: permanent_home = app.injector.instanceOf[permanent_home]
  }

  def permanentHomeController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def permanentHomeControllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
//    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  "PermanentHomeController" must {

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = permanentHomeController.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to ida login page on Submit if the user is not logged in" in {
      val result = permanentHomeController.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(widowCheck= Some(CommonBuilder.buildWidowedCheck))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val result = permanentHomeController.onPageLoad (createFakeRequest())
      status(result) mustBe OK
    }

    "show predeceased name on page load" in {

      val firstName = CommonBuilder.firstNameGenerator
      val surname = CommonBuilder.surnameGenerator
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(increaseIhtThreshold =
        Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(firstName), lastName = Some(surname))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val result = permanentHomeController.onPageLoad (createFakeRequest())
      status(result) mustBe OK
      ContentChecker.stripLineBreaks(contentAsString(result)) must include(messagesApi("iht.estateReport.tnrb.permanentHome.question",
        s"$firstName $surname"))
    }

    "save application and go to Tnrb Overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(increaseIhtThreshold =
        Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(CommonBuilder.firstNameGenerator),
          lastName = Some(CommonBuilder.surnameGenerator))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val withPermanentHomeValue = CommonBuilder.buildTnrbEligibility

      val filledPermanentHomeForm = partnerLivingInUkForm.fill(withPermanentHomeValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPermanentHomeForm.data.toSeq: _*).withMethod("POST")

      val result = permanentHomeController.onSubmit (request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be(Some(routes.TnrbOverviewController.onPageLoad.url + "#" + appConfig.TnrbSpousePermanentHomeInUKID))
    }

    "go to KickOut page when Partner Home not in the UK " in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(widowCheck= Some(CommonBuilder.buildWidowedCheck))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val withPermanentHomeValue = CommonBuilder.buildTnrbEligibility.copy(isPartnerLivingInUk = Some(false))

      val filledPermanentHomeForm = partnerLivingInUkForm.fill(withPermanentHomeValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPermanentHomeForm.data.toSeq: _*).withMethod("POST")

      val result = permanentHomeController.onSubmit (request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be(Some(iht.controllers.application.routes.KickoutAppController.onPageLoad.url))
    }

    "go to successful Tnrb page on submit when its satisfies happy path" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(increaseIhtThreshold =
        Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(CommonBuilder.firstNameGenerator),
          lastName = Some(CommonBuilder.surnameGenerator),
          dateOfMarriage= Some(new LocalDate(1984, 12, 11)))),
          widowCheck = Some(CommonBuilder.buildWidowedCheck))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val withPermanentHomeValue = CommonBuilder.buildTnrbEligibility.copy(isPartnerLivingInUk = Some(true))

      val filledPermanentHomeForm = partnerLivingInUkForm.fill(withPermanentHomeValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPermanentHomeForm.data.toSeq: _*).withMethod("POST")

      val result = permanentHomeController.onSubmit (request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be(Some(routes.TnrbSuccessController.onPageLoad.url))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      permanentHomeController.onPageLoad(createFakeRequest()))
  }
}
