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

import iht.testhelpers.CommonBuilder
import iht.utils.DeceasedInfoHelper
import iht.views.ViewTestHelper
import iht.views.html.application.declaration.probate_application_form_details
import play.api.i18n.Messages.Implicits._

class ProbateApplicationFormDetailsViewTest extends ViewTestHelper {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val probateDetails = CommonBuilder.buildProbateDetails
  val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)

  def probateApplicationFormDetailsView() = {
    implicit val request = createFakeRequest()

    val view = probate_application_form_details(Some(probateDetails), regDetails).toString
    asDocument(view)
  }

  "ProbateApplicationFormDetails Page" must {

    "have no message keys in html" in {
      val view = probateApplicationFormDetailsView().toString
      noMessageKeysShouldBePresent(view)
    }

    "show correct title and browserTitle" in {
      val view = probateApplicationFormDetailsView().toString
      titleShouldBeCorrect(view, messagesApi("page.iht.application.probate.title"))
      browserTitleShouldBeCorrect(view, messagesApi("page.iht.application.probate.browserTitle"))
    }

    "show the correct guidance" in {
      val view = probateApplicationFormDetailsView().toString
      messagesShouldBePresent(view, messagesApi("page.iht.application.probate.guidance.p1", deceasedName))
      messagesShouldBePresent(view, messagesApi("page.iht.application.probate.guidance.indent"))
    }

    "show the continue to Inheritance Tax estate reports link" in {
      val view = probateApplicationFormDetailsView()
      val continue = view.getElementById("continue-to-estate-reports")
      continue.attr("href") shouldBe iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad.url
      continue.text() shouldBe messagesApi("site.button.continue.iht.app.page")
    }
  }

}
