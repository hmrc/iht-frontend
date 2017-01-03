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

package iht.views.application.declaration

import iht.forms.ApplicationForms._
import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import play.api.i18n.Messages


class CheckedEverythingViewTest extends ViewTestHelper {

  lazy val page:String = {
    lazy val regDetails = CommonBuilder.buildRegistrationDetails1
    implicit val request = createFakeRequest()
    iht.views.html.application.declaration.checked_everything_question(checkedEverythingQuestionForm, regDetails).toString
  }

  "Checked everything page" must {
    "show correct title and browserTitle" in {
      titleShouldBeCorrect(page, Messages("iht.estateReport.declaration.checkedEverything.question"))
      browserTitleShouldBeCorrect(page, Messages("iht.estateReport.declaration.checkedEverything.question"))
    }

    "show correct paragraph 1" in {
      messagesShouldBePresent(page, Messages("iht.estateReport.declaration.checkedEverything.p1"))
    }

    "show correct paragraph 2" in {
      messagesShouldBePresent(page, Messages("iht.estateReport.declaration.checkedEverything.p2"))
    }
  }
}
