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

package iht.views.ihtHelpers

import iht.FakeIhtApp
import iht.views.HtmlSpec
import iht.views.html.ihtHelpers.input_radio_group
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by jennygj on 28/10/16.
 */
class InputRadioGroupTest extends UnitSpec with FakeIhtApp with HtmlSpec {

  def view = {
    val radio = Seq(("a", "b"), ("c", "d"))
    val form = Form("testing" -> optional(text))
    val field = form("testing")
    input_radio_group(field, radio, '_ariaHintID -> "testID2")
  }

  def doc = asDocument(view)

  "input radio group" must {
    "display the correct data-target attribute based on field mappings" in {
      doc.getElementById("testing-a-label").text shouldBe "b"
      doc.getElementById("testing-c-label").text shouldBe "d"
    }
  }
}
