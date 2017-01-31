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

package iht.views.registration.deceased

import iht.controllers.registration.routes
import iht.forms.registration.DeceasedForms.deceasedDateOfDeathForm
import iht.views.html.registration.deceased.deceased_date_of_death
import iht.views.registration.RegistrationPageBehaviour
import play.api.i18n.Messages
import play.api.mvc.Call

class DeceasedDateOfDeathViewTest extends RegistrationPageBehaviour {

  override def pageTitle = Messages("page.iht.registration.deceasedDateOfDeath.title")
  override def browserTitle = Messages("iht.dateOfDeath")

  lazy val regSummaryPage = routes.RegistrationSummaryController.onPageLoad
  lazy val editSubmitLocation= iht.controllers.registration.deceased.routes.DeceasedDateOfDeathController.onEditSubmit

  override def fixture() = new {
    implicit val request = createFakeRequest()
    val view = deceased_date_of_death(deceasedDateOfDeathForm, Call("", "")).toString
    val doc = asDocument(view)
  }

  def editModeView = {
    implicit val request = createFakeRequest()
    val view = deceased_date_of_death(deceasedDateOfDeathForm, editSubmitLocation, Some(regSummaryPage)).toString
    asDocument(view)
  }

  "Deceased Date of Death View" must {

    behave like registrationPage()

    "have a fieldset with the Id 'date-of-death'" in {
      val f = fixture()
      f.doc.getElementsByTag("fieldset").first.id shouldBe "date-of-death"
    }

    "have a 'day' input box" in {
      val f = fixture()
      assertRenderedById(f.doc, "dateOfDeath.day")
    }

    "have a 'month' input box" in {
      val f = fixture()
      assertRenderedById(f.doc, "dateOfDeath.month")
    }

    "have a 'year' input box" in {
      val f = fixture()
      assertRenderedById(f.doc, "dateOfDeath.year")
    }

    "have a form hint" in {
      val f = fixture()
      messagesShouldBePresent(f.view, Messages("page.iht.registration.deceasedDateOfDeath.dateOfDeath.hint"))
    }

    "have a continue and cancel link in edit mode" in {
      val view = editModeView

      val continueLink = view.getElementById("continue")
      continueLink.attr("value") shouldBe Messages("iht.continue")

      val cancelLink = view.getElementById("cancel-button")
      cancelLink.attr("href") shouldBe regSummaryPage.url
      cancelLink.text() shouldBe Messages("site.link.cancel")
    }
  }
}
