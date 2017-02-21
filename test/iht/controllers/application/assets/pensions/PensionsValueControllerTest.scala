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

package iht.controllers.application.assets.pensions

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import play.api.test.Helpers._

class PensionsValueControllerTest extends ApplicationControllerTest{

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def pensionsValueController = new PensionsValueController {
    override val authConnector = createFakeAuthConnector(isAuthorised=true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def pensionsValueControllerNotAuthorised = new PensionsValueController {
    override val authConnector = createFakeAuthConnector(isAuthorised=false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  "PensionsValueController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = pensionsValueControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = pensionsValueController.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = pensionsValueController.onPageLoad (createFakeRequest())
      status(result) shouldBe (OK)
    }

    "save application and go to Pensions overview page on submit after entering the value" in {
      val privatePension = CommonBuilder.buildPrivatePensionExtended.copy(value = Some(BigDecimal(1000)))
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets.copy(privatePension = Some(privatePension))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val filledPensionsValueForm = pensionsValueForm.fill(privatePension)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPensionsValueForm.data.toSeq: _*)

      val result = pensionsValueController.onSubmit (request)
      status(result) shouldBe (SEE_OTHER)
      redirectLocation(result) should be
          (Some(routes.PensionsOverviewController.onPageLoad().url))
    }

    "respond with bad request when incorrect value are entered on the page" in {
     implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("value", "utytyyterrrrrrrrrrrrrr"))

     createMockToGetExistingRegDetailsFromCache(mockCachingConnector)

      val result = pensionsValueController.onSubmit (fakePostRequest)
      status(result) shouldBe (BAD_REQUEST)
    }
  }
}
