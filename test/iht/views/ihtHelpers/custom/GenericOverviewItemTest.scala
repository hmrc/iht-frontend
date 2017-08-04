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

package iht.views.ihtHelpers.custom

import iht.FakeIhtApp
import iht.views.HtmlSpec
import iht.views.html.ihtHelpers.custom._
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.test.UnitSpec


class GenericOverviewItemTest extends UnitSpec with FakeIhtApp with HtmlSpec {

  "GenericOverviewItem helper" must {
//    implicit val request = createFakeRequest()
//    implicit val messages: MessagesApi = app.injector.instanceOf[MessagesApi]
    val linkUrl = iht.controllers.application.assets.money.routes.MoneyOverviewController.onPageLoad()
    val title = "Money"
    val name = "money"
    val id = "money"
    val value = "3000"
    val valueScreenReader = "screenReaderText"

    def genericOverviewItemView() = {
      implicit val request = createFakeRequest()
      lazy val linkText = generic_overview_status_link(Some(true))
      lazy val itemStatus = generic_overview_status_label(Some(true))
      generic_overview_item(title = title, name = name, id = id, value = value,
                            valueScreenReader = valueScreenReader, link = Some(linkUrl), linkText = linkText,
        itemStatus = itemStatus)
    }

    "have the correct id" in {
      val doc = asDocument(genericOverviewItemView)

      assertRenderedById(doc, id)
    }

    "have the correct title" in {
      val doc = asDocument(genericOverviewItemView)
      assertContainsText(doc, title)
    }

    "show the correct value" in {
      val doc = asDocument(genericOverviewItemView)
      assertContainsText(doc, value)
    }

    "have the correct link with text" in {
      val doc = asDocument(genericOverviewItemView)
      val link = doc.getElementById(s"$id")
      link.attr("href") shouldBe linkUrl.url
      assertEqualsValue(doc, s"a#$id span", "Change")
    }

    "have the correct Item status" in {
      implicit val request = createFakeRequest()
     val itemStatus = generic_overview_status_label(Some(true))
     val linkText = generic_overview_status_link(Some(true))

     val view = generic_overview_item(title = title, name = name, id = id, value = value,
        valueScreenReader = valueScreenReader, link = Some(linkUrl), linkText = linkText,
        itemStatus = itemStatus)

      val doc = asDocument(view)
      assertRenderedById(doc, s"$id-status")
      assertEqualsValue(doc, s"div#$id-status span span:first-child", "Complete")
    }

  }
}
