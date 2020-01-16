/*
 * Copyright 2020 HM Revenue & Customs
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

package iht.controllers.filter

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.{MockFormPartialRetriever, UseService}
import iht.views.HtmlSpec
import play.api.i18n.MessagesApi
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class UseServiceControllerTest extends ApplicationControllerTest with HtmlSpec with UseService {

  implicit val fakedMessagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  protected abstract class TestController extends FrontendController(mockControllerComponents) with UseServiceController {
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def controller = new TestController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever

    override def messagesApi: MessagesApi = fakedMessagesApi
  }

  "UseServiceController" must {
    "show the 'no change' page when accessed by an unauthorized person" in {
      val result = controller.onPageLoadUnder()(createFakeRequest(isAuthorised = false))
      status(result) must be(OK)

      val doc = asDocument(contentAsString(result))
      val titleElement = doc.getElementsByTag("h1").first
      titleElement.text() must be(fakedMessagesApi("iht.shouldUseOnlineService"))
    }

    "show paragraph 0 for the under 325000 estimate" in {
      val result = controller.onPageLoadUnder()(createFakeRequest(isAuthorised = false))
      status(result) must be(OK)

      val doc = asDocument(contentAsString(result))
      val paragraph0 = doc.getElementById("paragraph0")
      paragraph0.text() must be(fakedMessagesApi("page.iht.filter.useService.under325000.paragraph0"))
    }

    "show paragraph 0 for the between estimate" in {
      val result = controller.onPageLoadOver()(createFakeRequest(isAuthorised = false))
      status(result) must be(OK)

      val doc = asDocument(contentAsString(result))
      val paragraph0 = doc.getElementById("paragraph0")
      paragraph0.text() mustBe pageIHTFilterUseServiceBetween325000And1MillionParagraph0
    }

  }
}
