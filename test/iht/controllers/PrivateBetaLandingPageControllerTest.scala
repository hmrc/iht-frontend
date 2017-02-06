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

package iht.controllers

import iht.config.FrontendAuthConnector
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import play.api.test.Helpers._

class PrivateBetaLandingPageControllerTest extends ApplicationControllerTest {

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def privateBetaLandingPageController = new PrivateBetaLandingPageController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }


  "private beta landing page" must {
    "use passcode to load landing page with passcode" in {
      val result = privateBetaLandingPageController.passcode(Some("sometoken"))(createFakeRequest(isAuthorised = true))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some("/inheritance-tax/landing/sometoken?p=sometoken"))
    }
    "use no passcode to load landing page" in {
      val result = privateBetaLandingPageController.passcode(None)(createFakeRequest(isAuthorised = true))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some("/inheritance-tax/landing") )
    }

    "go  to landing page" in {
      val result = privateBetaLandingPageController.showLandingPageWithPasscode("1234", Some("1234"))(createFakeRequest(isAuthorised = true))
      status(result) should be(OK)
    }

    "go to landing page without passcode" in {
      val result = privateBetaLandingPageController.showLandingPage(createFakeRequest(isAuthorised = true))
      status(result) should be(OK)
    }

    "redirect to landing page without passcode" in {
      val result = privateBetaLandingPageController.showLandingPage(createFakeRequest(isAuthorised = true))
      status(result) should be(OK)
    }

    "set up instance for caching connector" in {
      PrivateBetaLandingPageController.cachingConnector shouldBe CachingConnector
    }

    "set up instance for iht connector" in {
      PrivateBetaLandingPageController.ihtConnector shouldBe IhtConnector
    }

    "set up instance for auth connector" in {
      PrivateBetaLandingPageController.authConnector shouldBe FrontendAuthConnector
    }

    "start redirects correctly" in {
     val result = privateBetaLandingPageController.start(Some("aa"))(createFakeRequest(isAuthorised = true))
     status(result) should be(SEE_OTHER)
     redirectLocation(result) should be(Some(iht.controllers.filter.routes.FilterController.onPageLoad().url))
    }
  }
}
