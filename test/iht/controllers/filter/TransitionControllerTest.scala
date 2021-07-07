/*
 * Copyright 2021 HM Revenue & Customs
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
import iht.views.HtmlSpec
import iht.views.html.filter.use_paper_form
import play.api.i18n.MessagesApi
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class TransitionControllerTest extends ApplicationControllerTest with HtmlSpec {

  implicit val fakedMessagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  protected abstract class TestController extends FrontendController(mockControllerComponents) with TransitionController {
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def controller: TransitionController = new TestController {

    override def messagesApi: MessagesApi = fakedMessagesApi

    override val usePaperFormView: use_paper_form = app.injector.instanceOf[use_paper_form]
  }

  "Transition Controller" must {

    "show the Use Paper Form page when access by an unauthorised person for Scotland" in {
      val result = controller.onPageLoadScotland()(createFakeRequest(isAuthorised = false))
      status(result) must be(OK)

      val doc = asDocument(contentAsString(result))
      val titleElement = doc.getElementsByTag("h1").first
      titleElement.text() must be(fakedMessagesApi("iht.usePaperForm"))
    }

    "show the Use Paper Form page when access by an unauthorised person for Northern Ireland" in {
      val result = controller.onPageLoadNorthernIreland()(createFakeRequest(isAuthorised = false))
      status(result) must be(OK)

      val doc = asDocument(contentAsString(result))
      val titleElement = doc.getElementsByTag("h1").first
      titleElement.text() must be(fakedMessagesApi("iht.usePaperForm"))
    }

    "show the Use Paper Form page when access by an unauthorised person for another country" in {
      val result = controller.onPageLoadOtherCountry()(createFakeRequest(isAuthorised = false))
      status(result) must be(OK)

      val doc = asDocument(contentAsString(result))
      val titleElement = doc.getElementsByTag("h1").first
      titleElement.text() must be(fakedMessagesApi("iht.usePaperForm"))
    }
  }
}
