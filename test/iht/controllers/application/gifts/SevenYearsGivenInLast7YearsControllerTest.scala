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

package iht.controllers.application.gifts

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.testhelpers.{CommonBuilder, ContentChecker, MockFormPartialRetriever}
import iht.utils._
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever


class SevenYearsGivenInLast7YearsControllerTest  extends ApplicationControllerTest{

  protected abstract class TestController extends FrontendController(mockControllerComponents) with SevenYearsGivenInLast7YearsController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def sevenYearsGivenInLast7YearsController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def sevenYearsGivenInLast7YearsControllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  val allGifts=CommonBuilder.buildAllGifts
  val applicationDetails=CommonBuilder.buildApplicationDetails copy (allGifts = Some(allGifts))

  "SevenYearsGivenInLast7YearsController" must {

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = sevenYearsGivenInLast7YearsController.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to ida login page on Submit if the user is not logged in" in {
      val result = sevenYearsGivenInLast7YearsControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        storeAppDetailsInCache = true,
        saveAppDetails = true)

      val result = sevenYearsGivenInLast7YearsController.onPageLoad (createFakeRequest())
      status(result) mustBe OK
    }

    "display the question on the page" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      val regDetails = buildRegistrationDetailsWithDeceasedAndIhtRefDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = regDetails,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        storeAppDetailsInCache = true,
        saveAppDetails = true)

      val result = sevenYearsGivenInLast7YearsController.onPageLoad (createFakeRequest())
      status(result) mustBe OK
      ContentChecker.stripLineBreaks(contentAsString(result)) must include (messagesApi("page.iht.application.gifts.lastYears.question",
                                                        DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)))
    }

    "display the guidance on the page" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      val regDetails = buildRegistrationDetailsWithDeceasedAndIhtRefDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = regDetails,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        storeAppDetailsInCache = true,
        saveAppDetails = true)

      val result = sevenYearsGivenInLast7YearsController.onPageLoad (createFakeRequest())
      status(result) mustBe OK
      contentAsString(result) must include (messagesApi("page.iht.application.gifts.lastYears.description.p1"))
      ContentChecker.stripLineBreaks(contentAsString(result)) must include (messagesApi("page.iht.application.gifts.lastYears.description.p3",
                                                        DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)))
      contentAsString(result) must include (messagesApi("iht.estateReport.assets.money.lowerCaseInitial"))
      contentAsString(result) must include (messagesApi("iht.estateReport.gifts.stocksAndSharesListed"))
      contentAsString(result) must include (messagesApi("page.iht.application.gifts.lastYears.description.e3"))
      contentAsString(result) must include (messagesApi("page.iht.application.gifts.lastYears.description.e4"))
    }

    "save application and go to Seven Years To Trust page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allGifts= Some(CommonBuilder.buildAllGifts))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        storeAppDetailsInCache = true,
        saveAppDetails = true)

      val withSevenYearsGivenInLast7YearsValue = CommonBuilder.buildAllGifts.copy(isGivenInLast7Years = Some(false))

      val filledSevenYearsGivenInLast7YearsForm = giftSevenYearsGivenInLast7YearsForm.fill(withSevenYearsGivenInLast7YearsValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledSevenYearsGivenInLast7YearsForm.data.toSeq: _*)

      val result = sevenYearsGivenInLast7YearsController.onSubmit (request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be (Some(iht.controllers.application.gifts.routes.SevenYearsToTrustController.onPageLoad().url))
    }

    "display error if user submit the page without selecting the answer " in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allGifts= Some(CommonBuilder.buildAllGifts))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        storeAppDetailsInCache = true,
        saveAppDetails = true)

      val withSevenYearsGivenInLast7YearsValue = CommonBuilder.buildAllGifts.copy(isGivenInLast7Years = None)

      val filledSevenYearsGivenInLast7YearsForm = giftSevenYearsGivenInLast7YearsForm.fill(withSevenYearsGivenInLast7YearsValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledSevenYearsGivenInLast7YearsForm.data.toSeq: _*)

      val result = sevenYearsGivenInLast7YearsController.onSubmit()(request)
      status(result) must be (BAD_REQUEST)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      sevenYearsGivenInLast7YearsController.onPageLoad(createFakeRequest()))
  }
}
