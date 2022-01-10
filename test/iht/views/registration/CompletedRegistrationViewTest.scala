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

package iht.views.registration

import iht.utils._
import iht.views.ViewTestHelper
import iht.views.html.registration.completed_registration

class CompletedRegistrationViewTest extends ViewTestHelper{

  val ihtRef = "A1A1A1"
  lazy val completedRegistrationView: completed_registration = app.injector.instanceOf[completed_registration]

  "CompletedRegistrationView" must {

    "have no message keys in html" in {
      implicit val request = createFakeRequest()
      val view = completedRegistrationView(ihtRef).toString
      noMessageKeysShouldBePresent(view)
    }

    "contain the correct title and browser title" in {
      implicit val request = createFakeRequest()
      val view = completedRegistrationView(ihtRef).toString

      titleShouldBeCorrect(view, messagesApi("iht.registration.complete"))
      browserTitleShouldBeCorrect(view, messagesApi("iht.registration.complete"))
    }

    "contain the correct guidance" in {
      implicit val request = createFakeRequest()
      val view = completedRegistrationView(ihtRef).toString

      messagesShouldBePresent(view, messagesApi("page.iht.registration.completedRegistration.ref.title"))
      messagesShouldBePresent(view, messagesApi("page.iht.registration.completedRegistration.ref.text"))
      messagesShouldBePresent(view, messagesApi("iht.nextSteps"))
      messagesShouldBePresent(view, messagesApi("page.iht.registration.completedRegistration.p1"))
      messagesShouldBePresent(view, messagesApi("page.iht.registration.completedRegistration.p2"))
    }

    "contain correct formatted reference number" in {
      implicit val request = createFakeRequest()
      val view = completedRegistrationView(ihtRef).toString

      messagesShouldBePresent(view, formattedIHTReference(ihtRef))
    }

   "contain button with correct text and target as Estate report page" in {

      implicit val request = createFakeRequest()
      val view = completedRegistrationView(ihtRef).toString
      val doc = asDocument(view)

      val button = doc.getElementById("go-to-inheritance-tax-report")
      button.text mustBe messagesApi("page.iht.registration.completedRegistration.button")
      button.attr("href") mustBe iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad.url

    }

    "contain text-link with correct text and target as Save and Exit page" in {

      implicit val request = createFakeRequest()
      val view = completedRegistrationView(ihtRef).toString
      val doc = asDocument(view)

      val textlink = doc.getElementById("go-to-save-and-exit")
      textlink.text mustBe messagesApi("page.iht.registration.completedRegistration.link")
      textlink.attr("href") mustBe iht.controllers.routes.SessionTimeoutController.onSaveAndExitPageLoad.url

    }

  }
}
