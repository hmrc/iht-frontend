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

package iht.views.ihtHelpers.standard

import iht.FakeIhtApp
import iht.views.HtmlSpec
import iht.views.html.ihtHelpers.standard.sidebar
import play.twirl.api.Html

class SideBarTest extends FakeIhtApp with HtmlSpec {
  lazy val sidebarView: sidebar = app.injector.instanceOf[sidebar]

  "sidebar" must {

    "display the correct value for sidebarLinks and sidebarClass" in {

      val result = sidebarView(Html("test"), Some("sidebar"))
      val doc = asDocument(result)

      val tag = doc.getElementsByTag("aside")
      tag.attr("class") mustBe "sidebar"
      tag.attr("lang") mustBe empty
      tag.html() mustBe "test"
    }

    "display the lang attribute when currentLang is Welsh " in {
      val result = sidebarView(Html("test"), Some("sidebar"), "cy")
      val doc = asDocument(result)

      val tag = doc.getElementsByAttribute("lang")
      tag.attr("class") mustBe "sidebar"
      tag.attr("lang") must be ("cy")
      tag.html() mustBe "test"
    }

  }
}
