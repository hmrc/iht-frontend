/*
 * Copyright 2019 HM Revenue & Customs
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

import iht.testhelpers.MockFormPartialRetriever
import play.api.i18n.MessagesApi
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

class RegistrationChecklistControllerTest extends RegistrationControllerTest {

  implicit val fakedMessagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  def registrationChecklistController = new RegistrationChecklistController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
    override def messagesApi: MessagesApi = fakedMessagesApi
  }

  "RegistrationChecklistController" must {

    "return OK on page load" in {
      val result = registrationChecklistController.onPageLoad()(createFakeRequest())
      status(result) must be (OK)
    }

    "display a title on the page" in {
      val result = registrationChecklistController.onPageLoad()(createFakeRequest())
      status(result) must be (OK)
      contentAsString(result) must include (fakedMessagesApi("page.iht.registration.checklist.title"))
    }

    "display the introduction paragraph on the page" in {
      val result = registrationChecklistController.onPageLoad()(createFakeRequest())
      status(result) must be (OK)
      contentAsString(result) must include (fakedMessagesApi("page.iht.registration.checklist.label1"))
      contentAsString(result) must include (fakedMessagesApi("page.iht.registration.checklist.label2"))
    }

    "display at least one click and reveal link on the page" in {
      val result = registrationChecklistController.onPageLoad()(createFakeRequest())
      status(result) must be (OK)
      contentAsString(result) must include (fakedMessagesApi("page.iht.registration.checklist.revealText"))
    }

    "display start registration button on page" in {
      val result = registrationChecklistController.onPageLoad()(createFakeRequest())
      status(result) must be (OK)
      contentAsString(result) must include (fakedMessagesApi("page.iht.registration.checklist.continueButton"))
    }

    "display a return link on page" in {
      val result = registrationChecklistController.onPageLoad()(createFakeRequest())
      status(result) must be (OK)
      contentAsString(result) must include(fakedMessagesApi("page.iht.registration.checklist.leaveLink"))
    }
  }
}
