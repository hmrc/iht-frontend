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

package iht.views.ihtHelpers.standard

import iht.FakeIhtApp
import iht.views.HtmlSpec
import iht.views.html.ihtHelpers.standard.{sidebar, input_radio_group_with_hints}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages.Implicits._
import play.twirl.api.Html
import uk.gov.hmrc.play.test.UnitSpec

class SideBarTest extends UnitSpec with FakeIhtApp with HtmlSpec {

  "sidebar" must {

    "display the correct value for sidebarLinks and sidebarClass" in {

      val result = sidebar(Html("test"), Some("sidebar"))
      val doc = asDocument(result)

      val tag = doc.getElementsByTag("aside")
      tag.attr("class") shouldBe "sidebar"
      tag.attr("lang") shouldBe empty
      tag.html() shouldBe "test"
    }

    "display the lang attribute when currentLang is Welsh " in {
      val result = sidebar(Html("test"), Some("sidebar"), "cy")
      val doc = asDocument(result)

      val tag = doc.getElementsByAttribute("lang")
      tag.attr("class") shouldBe "sidebar"
      tag.attr("lang") should be ("cy")
      tag.html() shouldBe "test"
    }

  }
}
