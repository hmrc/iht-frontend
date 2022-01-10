/*
 * Copyright 2022 HM Revenue & Customs
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

import iht.views.html.registration.registration_error_manual_correspondence_indicator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.i18n.MessagesApi
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class ManualCorrespondenceIndicatorControllerTest extends RegistrationControllerTest {

  val controller = new ManualCorrespondenceIndicatorController(
    app.injector.instanceOf[registration_error_manual_correspondence_indicator],
    mockControllerComponents,
    mockAppConfig,
    mockAuthConnector,
    mockCachingConnector
  )

  implicit val req: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest(authRetrieveNino = false)
  implicit val messages: MessagesApi = app.injector.instanceOf[MessagesApi]
  "ManualCorrespondenceIndicatorController" must {
    "load the page for authorized users" in {
      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any())).thenReturn(Future.successful(()))
      val result: Future[Result] = controller.onPageLoad(implicitly)
      status(result) mustBe OK
      contentAsString(result) must include(messagesApi("page.iht.registration.applicantDetails.mci.title"))
    }
  }
}
