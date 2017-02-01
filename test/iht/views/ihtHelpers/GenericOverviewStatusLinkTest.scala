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
import iht.views.html.ihtHelpers.generic_overview_status_link
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.play.test.UnitSpec

class GenericOverviewStatusLinkTest extends UnitSpec with FakeIhtApp with HtmlSpec {
  "GenericOverviewStatusLink helper" must {
    "return 'Give answer' label when item has not been started" in {

      val result = generic_overview_status_link(isComplete = None)
      val doc = asDocument(result)

      assertContainsText(doc, Messages("site.link.giveAnswer"))
      assertNotContainsText(doc, Messages("iht.giveMoreDetails"))
      assertNotContainsText(doc, Messages("iht.change"))
    }

    "return 'Give more details' label when item has been started but not completed" in {

      val result = generic_overview_status_link(isComplete = Some(false))
      val doc = asDocument(result)

      assertContainsText(doc, Messages("iht.giveMoreDetails"))
      assertNotContainsText(doc, Messages("site.link.giveAnswer"))
      assertNotContainsText(doc, Messages("iht.change"))
    }

    "return 'View or change' label when item has completed" in {

      val result = generic_overview_status_link(isComplete = Some(true))
      val doc = asDocument(result)

      assertContainsText(doc, Messages("iht.change"))
      assertNotContainsText(doc, Messages("iht.giveMoreDetails"))
      assertNotContainsText(doc, Messages("site.link.giveAnswer"))
    }

  }
}
