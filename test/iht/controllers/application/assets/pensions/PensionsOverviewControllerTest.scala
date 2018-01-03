/*
 * Copyright 2018 HM Revenue & Customs
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
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder}
import iht.testhelpers.MockObjectBuilder._
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

/**
 * Created by jennygj on 30/06/16.
 */
class PensionsOverviewControllerTest extends ApplicationControllerTest {

    val mockCachingConnector = mock[CachingConnector]
    val mockIhtConnector = mock[IhtConnector]

    def pensionsOverviewController = new PensionsOverviewController {
      override val authConnector = createFakeAuthConnector()
      override val cachingConnector = mockCachingConnector
      override val ihtConnector = mockIhtConnector
      override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
    }

    def pensionsOverviewControllerNotAuthorised = new PensionsOverviewController {
      override val authConnector = createFakeAuthConnector(false)
      override val cachingConnector = mockCachingConnector
      override val ihtConnector = mockIhtConnector
      override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
    }
  "PensionsOverviewController" must {
    "redirect to login page on PageLoad if the user is not logged in" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = pensionsOverviewControllerNotAuthorised.onPageLoad(createFakeRequest(false))
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

      val result = pensionsOverviewController.onPageLoad(createFakeRequest())
      status(result) shouldBe (OK)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      pensionsOverviewController.onPageLoad(createFakeRequest()))

  }

}
