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
import iht.views.html.ihtHelpers.estate_overview_row
import play.api.mvc.Call
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.play.test.UnitSpec

class EstateOverviewRowTest extends UnitSpec with FakeIhtApp with HtmlSpec{
  "estate overview row helper" must {
    "generate appropriate view as per input" in {
      val id = "debts"
      val caption = "This is a caption"
      val value = "No debts owed"
      val linkText = Html("This is a link")

      val result = estate_overview_row(id, caption, value, linkText, Call("",""))
      val resultAsString = contentAsString(result)

      resultAsString should include ("id=\"debts-caption\"")
      resultAsString should include ("id=\"debts-value\"")
      resultAsString should include ("id=\"debts-link-text\"")

      resultAsString should include (caption)
      resultAsString should include (value)
      resultAsString should include (linkText.toString)
    }
  }
}
