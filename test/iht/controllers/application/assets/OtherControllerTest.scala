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

package iht.controllers.application.assets

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import play.api.test.Helpers._

class OtherControllerTest extends ApplicationControllerTest{

  val mockCachingConnector = mock[CachingConnector]
  var mockIhtConnector = mock[IhtConnector]

  def otherController = new OtherController {
    override val authConnector = createFakeAuthConnector(isAuthorised=true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def otherControllerNotAuthorised = new OtherController {
    override val authConnector = createFakeAuthConnector(isAuthorised=false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  before {
    mockIhtConnector = mock[IhtConnector]
  }

  "OtherController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {

      val result = otherControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {

      val result = otherControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
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

      val result = otherController.onPageLoad (createFakeRequest())
      status(result) shouldBe (OK)
    }

    "save application and go to Asset Overview page on submit where yes and value chosen" in {

      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets.copy(other = Some(CommonBuilder.buildBasicElement.copy(value = Some(20), isOwned=Some(true))))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val otherValue = CommonBuilder.buildBasicElement.copy(value=Some(20), isOwned=Some(true))

      val filledOtherForm = otherForm.fill(otherValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledOtherForm.data.toSeq: _*)

      val result = otherController.onSubmit (request)
      status(result) shouldBe (SEE_OTHER)
    }

    "save application and go to Asset Overview page on submit where no chosen" in {

      val otherAsset = CommonBuilder.buildBasicElement.copy(value = Some(BigDecimal(2000)), isOwned=Some(false))

      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets.copy(other = Some(otherAsset))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val filledOtherForm = otherForm.fill(otherAsset)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledOtherForm.data.toSeq: _*)

      val result = otherController.onSubmit (request)
      status(result) shouldBe (SEE_OTHER)

      val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      val expectedAppDetails = applicationDetails.copy(allAssets = applicationDetails.allAssets.map(_.copy(
        other = Some(CommonBuilder.buildBasicElement.copy(value = None, isOwned = Some(false))))))

      capturedValue shouldBe expectedAppDetails
    }

    "respond with bad request when incorrect value are entered on the page" in {

      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("value", "utytyyterrrrrrrrrrrrrr"))

      createMockToGetExistingRegDetailsFromCache(mockCachingConnector)

      val result = otherController.onSubmit (fakePostRequest)
      status(result) shouldBe (BAD_REQUEST)
    }

    "save application and go to Asset Overview page on submit where no assets previously saved" in {

      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = None)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val otherValue = CommonBuilder.buildBasicElement.copy(value=Some(20), isOwned=Some(true))

      val filledOtherForm = otherForm.fill(otherValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledOtherForm.data.toSeq: _*)

      val result = otherController.onSubmit (request)
      status(result) shouldBe (SEE_OTHER)
    }
  }
}
