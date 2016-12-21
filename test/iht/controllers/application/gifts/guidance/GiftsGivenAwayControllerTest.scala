/*
 * Copyright 2016 HM Revenue & Customs
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

package iht.controllers.application.gifts.guidance

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.MockObjectBuilder._
import iht.testhelpers.{CommonBuilder, TestHelper}
import org.mockito.Matchers._
import play.api.i18n.Messages
import play.api.test.Helpers._

/**
 * Created by james on 22/01/16.
 */
class GiftsGivenAwayControllerTest extends ApplicationControllerTest {

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def giftsGivenAwayController = new GiftsGivenAwayController {
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def giftsGivenAwayControllerNotAuthorised = new GiftsGivenAwayController {
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  private def setUpMocks() {
    val applicationDetails = CommonBuilder.buildApplicationDetails

    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetails),
      getAppDetails = true,
      saveAppDetails = true,
      storeAppDetailsInCache = true,
      getAppDetailsTempFromCache = true)

    createMockToGetSingleValueFromCache(mockCachingConnector,
      singleValueFormKey = same(TestHelper.lastQuestionUrl),
      singleValueReturn = Some("true"))
  }

  "GiftsGivenAwayController" must {

    "redirect to ida login page on page load if user is not logged in" in {
      setUpMocks()

      val result = giftsGivenAwayControllerNotAuthorised.onPageLoad()(createFakeRequest())

      status(result) should be (SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to ida login page on submit if user is not logged in" in {
      setUpMocks()

      val result = giftsGivenAwayControllerNotAuthorised.onSubmit()(createFakeRequest())

      status(result) should be (SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      setUpMocks()

      val result = giftsGivenAwayController.onPageLoad()(createFakeRequest())

      status(result) should be (OK)
    }

    "display content on page" in {
      setUpMocks()

      val result = giftsGivenAwayController.onPageLoad()(createFakeRequest())

      status(result) should be (OK)
      contentAsString(result) should include (Messages("page.iht.application.gifts.guidance.giftsGivenAway.description3"))
    }


    "redirect to gifts more than 3000 question on submit" in {
      setUpMocks()

      val result = giftsGivenAwayController.onSubmit()(createFakeRequest())

      status(result) should be (SEE_OTHER)
      redirectLocation(result) should be (Some(iht.controllers.application.gifts.routes.GivenAwayController.onPageLoad().url))
    }


    "display link for Gifts Given Away and Estate Overview as part of side bar links on the page" in {
      setUpMocks()

      val result = giftsGivenAwayController.onPageLoad()(createFakeRequest())

      contentAsString(result) should include (Messages("site.link.go.to.giftsGivenAwaySection"))
      contentAsString(result) should include (Messages("site.link.go.to.estateOverview"))
    }
  }
}
