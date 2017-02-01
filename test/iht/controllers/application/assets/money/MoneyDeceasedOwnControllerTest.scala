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

package iht.controllers.application.assets.money

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import iht.utils.CommonHelper
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._

/**
 * Created by jennygj on 17/06/16.
 */
class MoneyDeceasedOwnControllerTest extends ApplicationControllerTest {

  val mockCachingConnector = mock[CachingConnector]
  var mockIhtConnector = mock[IhtConnector]

  lazy val regDetails = CommonBuilder.buildRegistrationDetails copy (
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails), ihtReference = Some("AbC123"))

  lazy val deceasedName = CommonHelper.getDeceasedNameOrDefaultString(regDetails)

  def setUpTests(applicationDetails: ApplicationDetails) = {
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      regDetails = regDetails,
      appDetails = Some(applicationDetails),
      getAppDetails = true,
      saveAppDetails = true,
      storeAppDetailsInCache = true)
  }

  def moneyDeceasedOwnController = new MoneyDeceasedOwnController {
    val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def moneyDeceasedOwnControllerNotAuthorised = new MoneyDeceasedOwnController {
    val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  before {
    mockIhtConnector = mock[IhtConnector]
  }

  "MoneyDeceasedOwnController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = moneyDeceasedOwnControllerNotAuthorised.onPageLoad(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = moneyDeceasedOwnControllerNotAuthorised.onSubmit(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      setUpTests(applicationDetails)
      val result = moneyDeceasedOwnController.onPageLoad(createFakeRequest())
      status(result) should be(OK)
    }

    "save application and go to money overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = List())
      val formFill = moneyFormOwn.fill(CommonBuilder.buildShareableBasicElementExtended.copy(value = Some(100),
                                                    shareValue = None, isOwned = Some(true), isOwnedShare = None))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)
      setUpTests(applicationDetails)

      val result = moneyDeceasedOwnController.onSubmit()(request)
      status(result) should be (SEE_OTHER)
      redirectLocation(result) should be (Some(routes.MoneyOverviewController.onPageLoad.url))
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
      status(result) should be (SEE_OTHER)
      redirectLocation(result) should be (Some(routes.MoneyOverviewController.onPageLoad.url))

      val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      val expectedAppDetails = applicationDetails.copy(allAssets = applicationDetails.allAssets.map(_.copy(
        money = Some(CommonBuilder.buildShareableBasicElementExtended.copy(value = None, isOwned = Some(false))))))

      capturedValue shouldBe expectedAppDetails
    }

    "display validation message when incomplete form is submitted" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = List())
      val formFill = moneyFormOwn.fill(CommonBuilder.buildShareableBasicElementExtended)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)

      setUpTests(applicationDetails)

      val result = moneyDeceasedOwnController.onSubmit()(request)
      status(result) should be (BAD_REQUEST)
      contentAsString(result) should include (Messages("error.problem"))
    }

    "respond with bad request when incorrect value are entered on the page" in {
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("value", "utytyyterrrrrrrrrrrrrr"))

      createMockToGetExistingRegDetailsFromCache(mockCachingConnector)

      val result = moneyDeceasedOwnController.onSubmit (fakePostRequest)
      status(result) shouldBe (BAD_REQUEST)
    }

    "display the correct title on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      setUpTests(applicationDetails)

      val result = moneyDeceasedOwnController.onPageLoad()(createFakeRequest())
      status(result) should be (OK)
      contentAsString(result) should include (Messages("iht.estateReport.assets.moneyOwned", deceasedName))
    }
  }
}
