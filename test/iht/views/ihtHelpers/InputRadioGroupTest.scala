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
import iht.views.html.ihtHelpers.standard.input_radio_group
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by jennygj on 28/10/16.
 */
class InputRadioGroupTest extends UnitSpec with FakeIhtApp with HtmlSpec {

  val items = Seq(("a", "b"), ("c", "d"))

  def view(radio: Seq[(String,String)], args: (Symbol, Any)*) = {
    val form = Form("testing" -> optional(text))
    val field = form("testing")
    input_radio_group(field, radio, args: _*)
  }

  "input radio group" must {
    "display the correct data-target attribute based on field mappings" in {
      val doc = asDocument(view(items))
      doc.getElementById("testing-a-label").text shouldBe "b"
      doc.getElementById("testing-c-label").text shouldBe "d"
    }

    "display the correct data-target attribute based on field mappings where space in label" in {
      val itemsWithSpaces = Seq(("a", "b"), ("c", "d"))
      val doc = asDocument(view(itemsWithSpaces))
      doc.getElementById("testing-a-label").text shouldBe "b"
      doc.getElementById("testing-c-label").text shouldBe "d"
    }

    "display the legend when specified" in {
      val expectedLegend = "test-legend"
      val doc = asDocument(view(items, '_legend -> expectedLegend))
      doc.getElementById("testing-container").getElementsByTag("legend").first.text shouldBe expectedLegend
    }

    "display the hint text when specified" in {
      val expectedHint = "test-hint"
      val doc = asDocument(view(items, '_hintText -> expectedHint))
      doc.getElementsByTag("div").first.children.first.text shouldBe expectedHint
    }
  }
}
