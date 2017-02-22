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

package iht.controllers.application.assets.household

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import iht.utils.CommonHelper
import play.api.i18n.{Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._
import play.api.test.Helpers.{contentAsString, _}

class HouseholdDeceasedOwnControllerTest extends ApplicationControllerTest {

  val mockCachingConnector = mock[CachingConnector]
  var mockIhtConnector = mock[IhtConnector]

  lazy val regDetails = CommonBuilder.buildRegistrationDetails copy (
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails), ihtReference = Some("AbC123"))

  def setUpTests(applicationDetails: ApplicationDetails) = {
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      regDetails = regDetails,
      appDetails = Some(applicationDetails),
      getAppDetails = true,
      saveAppDetails = true,
      storeAppDetailsInCache = true)
  }

  def householdDeceasedOwnController = new HouseholdDeceasedOwnController {
    val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def householdDeceasedOwnControllerNotAuthorised = new HouseholdDeceasedOwnController {
    val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  before {
    mockIhtConnector = mock[IhtConnector]
  }

  "HouseholdDeceasedOwnController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = householdDeceasedOwnControllerNotAuthorised.onPageLoad(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = householdDeceasedOwnControllerNotAuthorised.onSubmit(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      setUpTests(applicationDetails)
      val result = householdDeceasedOwnController.onPageLoad(createFakeRequest())
      status(result) should be(OK)
    }

    "save application and go to household overview page on submit" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.copy(propertyList = List())
      val formFill = householdFormOwn.fill(CommonBuilder.buildShareableBasicElementExtended.copy(
                                        value = Some(100), shareValue = None, isOwned = Some(true), isOwnedShare = None))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)
      setUpTests(applicationDetails)

      val result = householdDeceasedOwnController.onSubmit()(request)
      status(result) should be (SEE_OTHER)
      redirectLocation(result) should be (Some(routes.HouseholdOverviewController.onPageLoad.url))
    }

    "wipe out the household value if user selects No, save application and go to household overview page on submit" in {

      val houseHold = CommonBuilder.buildShareableBasicElementExtended.copy(
        value = Some(100), shareValue = None, isOwned = Some(false), isOwnedShare = None)

      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = List(),
        allAssets = Some(CommonBuilder.buildAllAssets.copy(
                          household = Some(houseHold))))

      val formFill = householdFormOwn.fill(houseHold)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)
      setUpTests(applicationDetails)

      val result = householdDeceasedOwnController.onSubmit()(request)
      status(result) should be (SEE_OTHER)
      redirectLocation(result) should be (Some(routes.HouseholdOverviewController.onPageLoad.url))

      val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      val expectedAppDetails = applicationDetails.copy(allAssets = applicationDetails.allAssets.map(_.copy(
                                   household = Some(CommonBuilder.buildShareableBasicElementExtended.copy(
                                    value = None, shareValue = None, isOwned = Some(false), isOwnedShare = None)))))

      capturedValue shouldBe expectedAppDetails
    }

    "display validation message when form is submitted with no values entered" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      implicit val request = createFakeRequest()

      setUpTests(applicationDetails)

      val result = householdDeceasedOwnController.onSubmit()(request)
      status(result) should be (BAD_REQUEST)
      contentAsString(result) should include (messagesApi("error.problem"))
    }

    "redirect to overview when form is submitted with answer yes and a value entered" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      implicit val request = createFakeRequest().withFormUrlEncodedBody(("isOwned", "true"), ("value", "233"))

      setUpTests(applicationDetails)

      val result = householdDeceasedOwnController.onSubmit()(request)
      status(result) should be (SEE_OTHER)
      redirectLocation(result) should be (Some(routes.HouseholdOverviewController.onPageLoad().url))
    }

    "respond with bad request when incorrect value are entered on the page" in {
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("value", "utytyyterrrrrrrrrrrrrr"))

      createMockToGetExistingRegDetailsFromCache(mockCachingConnector)

      val result = householdDeceasedOwnController.onSubmit (fakePostRequest)
      status(result) shouldBe (BAD_REQUEST)
    }
  }
 
}
