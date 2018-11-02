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

package iht.views

import iht.views.html.iht_main_template
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi

class IhtMainTemplateTest extends ViewTestHelper {

  implicit  override val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  "RegistrationMainTemplate" must {

    "contain the correct text for the sign out link" in {
      implicit val request = createFakeRequest()
      val signOutUrl = "localhost"
      iht_main_template(title = "", signOutText = "", signOutUrl = Some(Call("GET", signOutUrl)), headerTitle = None)(HtmlFormat.empty)
        .toString should include (signOutUrl)
    }

    "contain the correct text for need help accordion component" in {
      implicit val request = createFakeRequest()
      val signOutUrl = "localhost"
      val view = iht_main_template(title = "", signOutText = "", signOutUrl = Some(Call("GET", signOutUrl)), headerTitle = None)(HtmlFormat.empty)
        .toString
      view should include (messagesApi("site.progressiveDisclosure"))
    }
  }
}
