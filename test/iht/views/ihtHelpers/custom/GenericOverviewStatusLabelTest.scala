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

package iht.views.ihtHelpers.custom

import iht.FakeIhtApp
import iht.views.HtmlSpec
import iht.views.html.ihtHelpers.custom.generic_overview_status_label
import play.api.i18n.Lang
import play.api.mvc.MessagesControllerComponents

class GenericOverviewStatusLabelTest extends FakeIhtApp with HtmlSpec {

  override implicit val lang = Lang.defaultLang
  implicit val messages = app.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(Seq(lang)).messages

  "GenericOverviewStatusLabel helper" must {
   "return 'NOT STARTED' label when item has not been started" in {

      val result = generic_overview_status_label(isComplete = None, "iht.notStarted", "iht.complete", "iht.inComplete")
      val doc = asDocument(result)

      assertContainsText(doc, messagesApi("iht.notStarted"))
      assertNotContainsText(doc, messagesApi("iht.inComplete"))
      assertNotContainsText(doc, messagesApi("iht.complete"))
    }

    "return 'INCOMPLETE' label when item has been started but not completed" in {

      val result = generic_overview_status_label(isComplete = Some(false), "iht.notStarted", "iht.complete", "iht.inComplete")
      val doc = asDocument(result)

      assertContainsText(doc, messagesApi("iht.inComplete"))
      assertNotContainsText(doc, messagesApi("iht.notStarted"))
    }

    "return 'COMPLETE' label when item has completed" in {

      val result = generic_overview_status_label(isComplete = Some(true), "iht.notStarted", "iht.complete", "iht.inComplete")
      val doc = asDocument(result)

      assertContainsText(doc, messagesApi("iht.complete"))
      assertNotContainsText(doc, messagesApi("iht.inComplete"))
      assertNotContainsText(doc, messagesApi("iht.notStarted"))
    }

  }
}
