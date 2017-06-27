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

package iht.controllers.application.debts

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.debts.BasicEstateElementLiabilities
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import play.api.test.Helpers._

class JointlyOwnedDebtsControllerTest extends ApplicationControllerTest{

  val mockCachingConnector = mock[CachingConnector]
  var mockIhtConnector = mock[IhtConnector]

  def jointlyOwnedDebtsController = new JointlyOwnedDebtsController(messagesApi) {
    override val authConnector = createFakeAuthConnector()
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def jointlyOwnedDebtsControllerNotAuthorised = new JointlyOwnedDebtsController {
    override val authConnector = createFakeAuthConnector(isAuthorised=false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  before {
    mockIhtConnector = mock[IhtConnector]
  }

  "JointlyOwnedDebtsControllerTest" must {

    "redirect to login page on PageLoad if the user is not logged in" in {

      val result = jointlyOwnedDebtsControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {

      val result = jointlyOwnedDebtsControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load" in {

      val applicationDetails = CommonBuilder.buildApplicationDetails
      val applicationDetailsTemp = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = jointlyOwnedDebtsController.onPageLoad (createFakeRequest())
      status(result) shouldBe OK
    }

    "save application and go to Debts Overview page on submit where yes and value chosen" in {

      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allLiabilities = Some(CommonBuilder
        .buildAllLiabilities.copy(other =  Some(BasicEstateElementLiabilities(isOwned = Some(true), value = Some(BigDecimal(33)))))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val jointlyOwnedDebtsValue = BasicEstateElementLiabilities(isOwned = Some(true), value = Some(BigDecimal(33)))

      val filledOtherDebtsForm = jointlyOwnedDebts.fill(jointlyOwnedDebtsValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledOtherDebtsForm.data.toSeq: _*)

      val result = jointlyOwnedDebtsController.onSubmit (request)
      status(result) shouldBe SEE_OTHER
    }

    "save application, wipe out the value and go to Debts Overview page on submit where no chosen" in {

      val jointlyOwned = CommonBuilder.buildBasicEstateElementLiabilities.copy(
                                                isOwned = Some(false), value = Some(BigDecimal(33)))

      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allLiabilities = Some(CommonBuilder
        .buildAllLiabilities.copy(jointlyOwned = Some(jointlyOwned))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val filledOtherDebtsForm = jointlyOwnedDebts.fill(jointlyOwned)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledOtherDebtsForm.data.toSeq: _*)

      val result = jointlyOwnedDebtsController.onSubmit (request)
      status(result) shouldBe SEE_OTHER

      val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      val expectedAppDetails = applicationDetails.copy(allLiabilities = applicationDetails.allLiabilities.map(_.copy(
        jointlyOwned = Some(CommonBuilder.buildBasicEstateElementLiabilities.copy(value = None, isOwned = Some(false))))))

      capturedValue shouldBe expectedAppDetails
    }

    "respond with bad request when incorrect value are entered on the page" in {

      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("value", "utytyyterrrrrrrrrrrrrr"))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)

      val result = jointlyOwnedDebtsController.onSubmit (fakePostRequest)
      status(result) shouldBe BAD_REQUEST
    }

    "save application and go to Debts Overview page on submit where no debts previously saved" in {

      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allLiabilities = None)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val jointlyOwnedDebtsValue = BasicEstateElementLiabilities(isOwned = Some(true), value = Some(BigDecimal(33)))

      val filledOtherDebtsForm = jointlyOwnedDebts.fill(jointlyOwnedDebtsValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledOtherDebtsForm.data.toSeq: _*)

      val result = jointlyOwnedDebtsController.onSubmit (request)
      status(result) shouldBe SEE_OTHER
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      jointlyOwnedDebtsController.onPageLoad(createFakeRequest()))
  }
}
