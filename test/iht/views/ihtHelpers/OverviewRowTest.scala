/*
 * Copyright 2016 HM Revenue & Customs
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
import iht.viewmodels.application.overview.{Complete, NotStarted, OverviewRow, PartiallyComplete}
import iht.views.HtmlSpec
import iht.views.html.ihtHelpers.overview_row
import play.api.mvc.Call
import uk.gov.hmrc.play.test.UnitSpec

class OverviewRowTest extends UnitSpec with FakeIhtApp with HtmlSpec {
  "overview row helper" must {
    "set the link class when the row is partially completed" in {
      val row = OverviewRow("", "", "123", PartiallyComplete, Call("", ""), "")
      val result = overview_row(row)
      val doc = asDocument(result)
      assertRenderedByCssSelector(doc, ".bold")
    }

    "set the link class when the row is partially completed with no value" in {
      val row = OverviewRow("", "", "", PartiallyComplete, Call("", ""), "")
      val result = overview_row(row)
      val doc = asDocument(result)
      assertRenderedByCssSelector(doc, ".bold")
    }

    "not set the link class when the row is not started" in {
      val row = OverviewRow("", "", "", NotStarted, Call("", ""), "")
      val result = overview_row(row)
      val doc = asDocument(result)
      assertNotRenderedByCssSelector(doc, ".bold")
    }

    "not set the link class when the row is complete" in {
      val row = OverviewRow("", "", "123", Complete, Call("", ""), "")
      val result = overview_row(row)
      val doc = asDocument(result)
      assertNotRenderedByCssSelector(doc, ".bold")
    }
  }
}
