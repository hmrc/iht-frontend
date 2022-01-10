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

package iht.views.ihtHelpers.custom

import iht.views.ViewTestHelper
import iht.views.html.ihtHelpers.custom.generic_overview_table_item

class GenericOverviewTableItemTest extends ViewTestHelper {
  lazy val genericOverviewTableItemView: generic_overview_table_item = app.injector.instanceOf[generic_overview_table_item]

  "generic overview table item" must {
    "contain correct message for date with an answer value" in {
      val view = genericOverviewTableItemView(
        id = "",
        questionText = "",
        questionScreenReaderText = "",
        questionCategory = "date",
        answerValue = "a",
        link = None,
        linkScreenReader = ""
      )
      val viewAsString = view.toString
      messagesShouldBePresent(viewAsString, messagesApi("site.changeDate"))
    }

    "contain correct message for name with an answer value" in {
      val view = genericOverviewTableItemView(
        id = "",
        questionText = "",
        questionScreenReaderText = "",
        questionCategory = "name",
        answerValue = "a",
        link = None,
        linkScreenReader = ""
      )
      val viewAsString = view.toString
      messagesShouldBePresent(viewAsString, messagesApi("site.changeName"))
    }

    "contain correct message for date with no answer value" in {
      val view = genericOverviewTableItemView(
        id = "",
        questionText = "",
        questionScreenReaderText = "",
        questionCategory = "date",
        link = None,
        linkScreenReader = ""
      )
      val viewAsString = view.toString
      messagesShouldBePresent(viewAsString, messagesApi("site.link.giveDate"))
    }

    "contain correct message for name with no answer value" in {
      val view = genericOverviewTableItemView(
        id = "",
        questionText = "",
        questionScreenReaderText = "",
        questionCategory = "name",
        link = None,
        linkScreenReader = ""
      )
      val viewAsString = view.toString
      messagesShouldBePresent(viewAsString, messagesApi("site.link.giveName"))
    }
  }
}
