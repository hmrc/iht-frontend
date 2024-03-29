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

package iht.views.ihtHelpers.standard

import iht.FakeIhtApp
import iht.views.HtmlSpec
import iht.views.html.ihtHelpers.standard.input_radio_group_with_hints
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents

class InputRadioGroupWithHintsTest extends FakeIhtApp with HtmlSpec {

  val mockControllerComponents: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  implicit val messages: Messages = mockControllerComponents.messagesApi.preferred(Seq(lang)).messages
  lazy val inputRadioGroupWithHintsView: input_radio_group_with_hints = app.injector.instanceOf[input_radio_group_with_hints]

  "input radio group with hints" must {

    "display the correct data-target attribute based on field mappings" in {
      val form = Form("testing" -> optional(text))
      val field = form("testing")
      val radio = ("test", ("testing", Some("testing"), Some(true))) // must contain a populated data-target attr
      val radio2 = ("test", ("testing", Some("testing"), Some(false))) // must contain a blank data-target attr
      val radios = Seq(radio, radio, radio2)

      val result = inputRadioGroupWithHintsView(field, radios, '_ariaHintID -> "testID2")
      val doc = asDocument(result)
      val label = doc.getElementsByTag("label").first
      val label2 = doc.getElementsByTag("label").last()

      label.attr("data-target") must be ("testID2")
      label2.attr("data-target") must be ("")
    }

  }

}
