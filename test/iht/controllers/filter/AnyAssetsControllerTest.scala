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

package iht.controllers.filter

import iht.config.AppConfig
import iht.constants.Constants
import iht.controllers.application.ApplicationControllerTest
import iht.forms.FilterForms.anyAssetsForm
import iht.views.HtmlSpec
import iht.views.html.filter.any_assets
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class AnyAssetsControllerTest extends ApplicationControllerTest with HtmlSpec {

  implicit val fakedMessagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  protected abstract class TestController extends FrontendController(mockControllerComponents) with AnyAssetsController {
    override implicit val appConfig: AppConfig = mockAppConfig
    override val anyAssetsView: any_assets = app.injector.instanceOf[any_assets]
  }

  def controller: TestController = new TestController {
    override def messagesApi: MessagesApi = fakedMessagesApi
  }

  "Any Assets Controller" must {

    "show the Any Assets page when accessed by an unauthorised person" in {
      val result = controller.onPageLoadWithoutJointAssets()(createFakeRequest(isAuthorised = false))
      status(result) must be(OK)

      val doc = asDocument(contentAsString(result))
      val titleElement = doc.getElementsByTag("h1").first
      titleElement.text() must be(messages("page.iht.filter.anyAssets.title"))
    }

    "show an error if no radio button is selected" in {
      val request = createFakeRequestWithBody(isAuthorised = false, data = anyAssetsForm.data.toSeq).withMethod("POST")
      val result = controller.onSubmitWithoutJointAssets()(request)

      status(result) must be(BAD_REQUEST)

      val doc = asDocument(contentAsString(result))
      assertRenderedById(doc, "errors")
    }

    "redirect to the Use Service page if 'yes' is selected" in {
      val form = anyAssetsForm.fill(Some(Constants.anyAssetsYes))
      val request = createFakeRequestWithBody(isAuthorised = false, data = form.data.toSeq).withMethod("POST")
      val result = controller.onSubmitWithoutJointAssets()(request)

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(iht.controllers.filter.routes.UseServiceController.onPageLoadUnder.url))
    }

    "redirect to the No Assets page if 'no' is selected" in {
      val form = anyAssetsForm.fill(Some(Constants.anyAssetsNo))
      val request = createFakeRequestWithBody(isAuthorised = false, data = form.data.toSeq).withMethod("POST")
      val result = controller.onSubmitWithoutJointAssets()(request)

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(iht.controllers.filter.routes.NoAssetsController.onPageLoadWithoutJointAssets.url))
    }
  }

}
