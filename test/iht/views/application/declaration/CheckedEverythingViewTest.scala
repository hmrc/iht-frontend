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

package iht.views.application.declaration

import iht.forms.ApplicationForms._
import iht.models.RegistrationDetails
import iht.testhelpers.CommonBuilder
import iht.utils.CommonHelper._
import iht.views.ViewTestHelper
import iht.views.html.application.declaration.checked_everything_question

class CheckedEverythingViewTest extends ViewTestHelper {

  lazy val checkedEverythingQuestionView: checked_everything_question = app.injector.instanceOf[checked_everything_question]

  def page(regDetails: RegistrationDetails):String = {
    implicit val request = createFakeRequest()
    checkedEverythingQuestionView(checkedEverythingQuestionForm, regDetails).toString
  }

  "Checked everything page" must {
    "have no message keys in html" in {
      val view = page(CommonBuilder.buildRegistrationDetails1)
      noMessageKeysShouldBePresent(view)
    }

    "show correct title and browserTitle" in {
      val rd = CommonBuilder.buildRegistrationDetails1
      val deceasedName = getOrException(rd.deceasedDetails.map(_.name))
      asDocument(page(rd)).title() must not include deceasedName
      browserTitleShouldBeCorrect(page(CommonBuilder.buildRegistrationDetails1),
        messagesApi("iht.estateReport.declaration.checkedEverything.browserTitle"))
    }

    "show correct paragraph 1" in {
      messagesShouldBePresent(page(CommonBuilder.buildRegistrationDetails1), messagesApi("iht.estateReport.declaration.checkedEverything.p1"))
    }

    "show correct paragraph 2" in {
      messagesShouldBePresent(page(CommonBuilder.buildRegistrationDetails1), messagesApi("iht.estateReport.declaration.checkedEverything.p2"))
    }
  }
}
