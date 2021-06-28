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

package iht.controllers.application.assets.money

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.testhelpers.CommonBuilder
import iht.testhelpers.TestHelper._
import iht.utils._
import iht.views.html.application.asset.money.money_deceased_own
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

/**
 * Created by jennygj on 17/06/16.
 */
class MoneyDeceasedOwnControllerTest extends ApplicationControllerTest {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with MoneyDeceasedOwnController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val moneyDeceasedOwnView: money_deceased_own = app.injector.instanceOf[money_deceased_own]
  }

  lazy val returnToOverviewUrl = CommonHelper.addFragmentIdentifierToUrl(routes.MoneyOverviewController.onPageLoad.url, AssetsMoneyOwnID)

  lazy val regDetails = CommonBuilder.buildRegistrationDetails copy (
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails), ihtReference = Some("AbC123"))

  lazy val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)

  def setUpTests(applicationDetails: ApplicationDetails) = {
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      regDetails = regDetails,
      appDetails = Some(applicationDetails),
      getAppDetails = true,
      saveAppDetails = true,
      storeAppDetailsInCache = true)
  }

  def moneyDeceasedOwnController = new TestController {
    val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def moneyDeceasedOwnControllerNotAuthorised = new TestController {
    val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }



  "MoneyDeceasedOwnController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = moneyDeceasedOwnControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = moneyDeceasedOwnControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      setUpTests(applicationDetails)
      val result = moneyDeceasedOwnController.onPageLoad(createFakeRequest())
      status(result) must be(OK)
    }

    "save application and go to money overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = List())
      val formFill = moneyFormOwn.fill(CommonBuilder.buildShareableBasicElementExtended.copy(value = Some(100),
                                                    shareValue = None, isOwned = Some(true), isOwnedShare = None))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)
      setUpTests(applicationDetails)

      val result = moneyDeceasedOwnController.onSubmit()(request)
      status(result) must be (SEE_OTHER)
      redirectLocation(result) must be (Some(returnToOverviewUrl))
    }

    "wipe out the money value if user selects No, save application and go to money overview page on submit" in {

      val moneyOwed = CommonBuilder.buildShareableBasicElementExtended.copy(value = Some(100),
        shareValue = None, isOwned = Some(false), isOwnedShare = None)

      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder.buildAllAssets.copy(
        money = Some(moneyOwed))))

      val formFill = moneyFormOwn.fill(moneyOwed)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)
      setUpTests(applicationDetails)

      val result = moneyDeceasedOwnController.onSubmit()(request)
      status(result) must be (SEE_OTHER)
      redirectLocation(result) must be (Some(returnToOverviewUrl))

      val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      val expectedAppDetails = applicationDetails.copy(allAssets = applicationDetails.allAssets.map(_.copy(
        money = Some(CommonBuilder.buildShareableBasicElementExtended.copy(value = None, isOwned = Some(false))))))

      capturedValue mustBe expectedAppDetails
    }

    "display validation message when form is submitted with no values entered" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      implicit val request = createFakeRequest()

      setUpTests(applicationDetails)

      val result = moneyDeceasedOwnController.onSubmit()(request)
      status(result) must be (BAD_REQUEST)
      contentAsString(result) must include (messagesApi("error.problem"))
    }

    "redirect to overview when form is submitted with answer yes and a value entered" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      implicit val request = createFakeRequest().withFormUrlEncodedBody(("isOwned", "true"), ("value", "233"))

      setUpTests(applicationDetails)

      val result = moneyDeceasedOwnController.onSubmit()(request)
      status(result) must be (SEE_OTHER)
      redirectLocation(result) must be (Some(returnToOverviewUrl))
    }

    "respond with bad request when incorrect value are entered on the page" in {
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("value", "utytyyterrrrrrrrrrrrrr"))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)

      val result = moneyDeceasedOwnController.onSubmit (fakePostRequest)
      status(result) mustBe (BAD_REQUEST)
    }

    "respond with bad request and correct error message when no answer is selected" in {
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("", ""))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)

      val result = moneyDeceasedOwnController.onSubmit (fakePostRequest)
      status(result) mustBe (BAD_REQUEST)
      contentAsString(result) must include(messagesApi("error.assets.money.deceasedOwned.select",
                                              CommonBuilder.buildDeceasedDetails.name))
    }


    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      moneyDeceasedOwnController.onPageLoad(createFakeRequest()))

  }
}
