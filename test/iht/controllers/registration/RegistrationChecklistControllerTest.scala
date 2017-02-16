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

package iht.controllers.registration

import iht.config.FrontendAuthConnector
import iht.connector.{CachingConnector, IhtConnector}
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._

class RegistrationChecklistControllerTest extends RegistrationControllerTest {

  before {
    mockCachingConnector = mock[CachingConnector]
  }

  val mockIhtConnector = mock[IhtConnector]

  def registrationChecklistController = new RegistrationChecklistController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  "RegistrationChecklistController" must {

    "return OK on page load" in {
      val result = registrationChecklistController.onPageLoad()(createFakeRequest())
      status(result) should be (OK)
    }

    "display a title on the page" in {
      val result = registrationChecklistController.onPageLoad()(createFakeRequest())
      status(result) should be (OK)
      contentAsString(result) should include (messagesApi("page.iht.registration.checklist.title"))
    }

    "display the introduction paragraph on the page" in {
      val result = registrationChecklistController.onPageLoad()(createFakeRequest())
      status(result) should be (OK)
      contentAsString(result) should include (messagesApi("page.iht.registration.checklist.label1"))
      contentAsString(result) should include (messagesApi("page.iht.registration.checklist.label2"))
      contentAsString(result) should include (messagesApi("page.iht.registration.checklist.label3"))
    }

    "display at least one click and reveal link on the page" in {
      val result = registrationChecklistController.onPageLoad()(createFakeRequest())
      status(result) should be (OK)
      contentAsString(result) should include (messagesApi("page.iht.registration.checklist.revealText"))
    }

    "display start registration button on page" in {
      val result = registrationChecklistController.onPageLoad()(createFakeRequest())
      status(result) should be (OK)
      contentAsString(result) should include (messagesApi("page.iht.registration.checklist.startRegistrationButton"))
    }

    "display a return link on page" in {
      val result = registrationChecklistController.onPageLoad()(createFakeRequest())
      status(result) should be (OK)
      contentAsString(result) should include(messagesApi("page.iht.registration.checklist.leaveLink"))
    }

    "set up instance for caching connector" in {
      RegistrationChecklistController.cachingConnector shouldBe CachingConnector
    }

    "set up instance for iht connector" in {
      RegistrationChecklistController.ihtConnector shouldBe IhtConnector
    }

    "set up instance for auth connector" in {
      RegistrationChecklistController.authConnector shouldBe FrontendAuthConnector
    }
  }
}
