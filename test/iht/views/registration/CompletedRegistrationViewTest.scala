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

package iht.views.registration

import iht.views.ViewTestHelper
import play.api.i18n.Messages
import iht.utils._
import iht.views.html.registration.completed_registration

class CompletedRegistrationViewTest extends ViewTestHelper{

  val ihtRef = "A1A1A1"

  "CompletedRegistrationView" must {

    "contain the correct title and browser title" in {
      implicit val request = createFakeRequest()
      val view = completed_registration(ihtRef).toString

      titleShouldBeCorrect(view, Messages("iht.registration.complete"))
      browserTitleShouldBeCorrect(view, Messages("iht.registration.complete"))
    }

    "contain the correct guidance" in {
      implicit val request = createFakeRequest()
      val view = completed_registration(ihtRef).toString

      messagesShouldBePresent(view, Messages("page.iht.registration.completedRegistration.ref.title"))
      messagesShouldBePresent(view, Messages("page.iht.registration.completedRegistration.ref.text"))
      messagesShouldBePresent(view, Messages("iht.nextSteps"))
      messagesShouldBePresent(view, Messages("page.iht.registration.completedRegistration.p1"))
      messagesShouldBePresent(view, Messages("page.iht.registration.completedRegistration.p2"))
      messagesShouldBePresent(view, Messages("page.iht.registration.completedRegistration.p2.bullet1"))
      messagesShouldBePresent(view, Messages("page.iht.registration.completedRegistration.p2.bullet2"))
      messagesShouldBePresent(view, Messages("page.iht.registration.completedRegistration.p2.bullet3"))
    }

    "contain the second paragraph with bullets" in {
      implicit val request = createFakeRequest()
      val view = completed_registration(ihtRef).toString
      val doc = asDocument(view)

      val paragraphWithBullets = doc.getElementById("second-paragraph-bullets")
    }

    "contain correct formatted reference number" in {
      implicit val request = createFakeRequest()
      val view = completed_registration(ihtRef).toString

      messagesShouldBePresent(view, formattedIHTReference(ihtRef))
    }

   "contain button with correct text and target as Estate report page" in {

      implicit val request = createFakeRequest()
      val view = completed_registration(ihtRef).toString
      val doc = asDocument(view)

      val button = doc.getElementById("go-to-inheritance-tax-report")
      button.text shouldBe Messages("site.button.continue.iht.app.page")
      button.attr("href") shouldBe iht.controllers.home.routes.IhtHomeController.onPageLoad.url

    }

  }
}
