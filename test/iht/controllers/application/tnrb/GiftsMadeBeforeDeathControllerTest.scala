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
import iht.views.html.application.tnrb.gifts_made_before_death
import org.joda.time.LocalDate
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class GiftsMadeBeforeDeathControllerTest  extends ApplicationControllerTest{

  protected abstract class TestController extends FrontendController(mockControllerComponents) with GiftsMadeBeforeDeathController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val giftsMadeBeforeDeathView: gifts_made_before_death = app.injector.instanceOf[gifts_made_before_death]
  }

  def giftsMadeBeforeDeathController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def giftsMadeBeforeDeathControllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
//    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  "GiftsMadeBeforeDeathController" must {

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = giftsMadeBeforeDeathController.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to ida login page on Submit if the user is not logged in" in {
      val result = giftsMadeBeforeDeathController.onSubmit(createFakeRequest(isAuthorised = false))
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

      val result = giftsMadeBeforeDeathController.onPageLoad (createFakeRequest())
      status(result) mustBe OK
    }

    "show predeceased name on page load" in {
      val firstName = CommonBuilder.firstNameGenerator
      val secondName = CommonBuilder.surnameGenerator
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(increaseIhtThreshold =
        Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(firstName),
          lastName = Some(secondName))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val result = giftsMadeBeforeDeathController.onPageLoad (createFakeRequest())
      status(result) mustBe OK
      ContentChecker.stripLineBreaks(contentAsString(result)) must include(messagesApi("iht.estateReport.tnrb.giftsMadeBeforeDeath.question",
        s"$firstName $secondName"))
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

      val withGiftsMadeBeforeDeathValue = CommonBuilder.buildTnrbEligibility.copy(isGiftMadeBeforeDeath = Some(false))

      val filledGiftsMadeBeforeDeathForm = giftMadeBeforeDeathForm.fill(withGiftsMadeBeforeDeathValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledGiftsMadeBeforeDeathForm.data.toSeq: _*).withMethod("POST")

      val result = giftsMadeBeforeDeathController.onSubmit (request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be(Some(routes.TnrbOverviewController.onPageLoad.url + "#" + mockAppConfig.TnrbGiftsGivenAwayID))
    }

    "go to KickOut page if gifts were given away in last 7 years " in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(widowCheck= Some(CommonBuilder.buildWidowedCheck))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val withGiftsMadeBeforeDeathValue = CommonBuilder.buildTnrbEligibility.copy(isGiftMadeBeforeDeath = Some(true))

      val filledGiftsMadeBeforeDeathForm = giftMadeBeforeDeathForm.fill(withGiftsMadeBeforeDeathValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledGiftsMadeBeforeDeathForm.data.toSeq: _*).withMethod("POST")

      val result = giftsMadeBeforeDeathController.onSubmit (request)
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

      val withGiftsMadeBeforeDeathValue = CommonBuilder.buildTnrbEligibility.copy(isGiftMadeBeforeDeath = Some(false))

      val filledGiftsMadeBeforeDeathForm = giftMadeBeforeDeathForm.fill(withGiftsMadeBeforeDeathValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledGiftsMadeBeforeDeathForm.data.toSeq: _*).withMethod("POST")

      val result = giftsMadeBeforeDeathController.onSubmit (request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be(Some(routes.TnrbSuccessController.onPageLoad.url))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      giftsMadeBeforeDeathController.onPageLoad(createFakeRequest()))
  }
}
