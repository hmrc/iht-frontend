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

package iht.controllers.application.gifts

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.RegistrationDetails
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import iht.utils.CommonHelper
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._

/**
 *
 * Created by Vineet Tyagi on 14/01/16.
 *l
 */
class SevenYearsGivenInLast7YearsControllerTest  extends ApplicationControllerTest{

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def sevenYearsGivenInLast7YearsController = new SevenYearsGivenInLast7YearsController {
    override val authConnector = createFakeAuthConnector(isAuthorised=true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def sevenYearsGivenInLast7YearsControllerNotAuthorised = new SevenYearsGivenInLast7YearsController {
    override val authConnector = createFakeAuthConnector(isAuthorised=false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  val allGifts=CommonBuilder.buildAllGifts
  val applicationDetails=CommonBuilder.buildApplicationDetails copy (allGifts = Some(allGifts))

  "SevenYearsGivenInLast7YearsController" must {

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = sevenYearsGivenInLast7YearsController.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to ida login page on Submit if the user is not logged in" in {
      val result = sevenYearsGivenInLast7YearsControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
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
      status(result) shouldBe OK
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
      status(result) shouldBe OK
      contentAsString(result) should include (Messages("page.iht.application.gifts.lastYears.question",
                                                        CommonHelper.getDeceasedNameOrDefaultString(regDetails)))
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
      status(result) shouldBe OK
      contentAsString(result) should include (Messages("page.iht.application.gifts.lastYears.description.p1"))
      contentAsString(result) should include (Messages("page.iht.application.gifts.lastYears.description.p3",
                                                        CommonHelper.getDeceasedNameOrDefaultString(regDetails)))
      contentAsString(result) should include (Messages("iht.estateReport.assets.money.lowerCaseInitial"))
      contentAsString(result) should include (Messages("iht.estateReport.gifts.stocksAndSharesListed"))
      contentAsString(result) should include (Messages("page.iht.application.gifts.lastYears.description.e3"))
      contentAsString(result) should include (Messages("page.iht.application.gifts.lastYears.description.e4"))
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
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should be (Some(iht.controllers.application.gifts.routes.SevenYearsToTrustController.onPageLoad().url))
    }

    "display error if user submit the page without selecting the answer " in {

      val withSevenYearsGivenInLast7YearsValue = CommonBuilder.buildAllGifts.copy(isGivenInLast7Years = None)

      val filledSevenYearsGivenInLast7YearsForm = giftSevenYearsGivenInLast7YearsForm.fill(withSevenYearsGivenInLast7YearsValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledSevenYearsGivenInLast7YearsForm.data.toSeq: _*)

      val result = sevenYearsGivenInLast7YearsController.onSubmit()(request)
      status(result) should be (BAD_REQUEST)
    }
  }
}
