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

package iht.views.registration.deceased

import iht.controllers.registration.routes
import iht.forms.registration.DeceasedForms.deceasedDateOfDeathForm
import iht.models.DeceasedDateOfDeath
import iht.testhelpers.CommonBuilder
import iht.views.html.registration.deceased.deceased_date_of_death
import iht.views.registration.RegistrationPageBehaviour
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat.Appendable

class DeceasedDateOfDeathViewTest extends RegistrationPageBehaviour[DeceasedDateOfDeath] {

  override def pageTitle = messagesApi("page.iht.registration.deceasedDateOfDeath.title")
  override def browserTitle = messagesApi("page.iht.registration.deceasedDateOfDeath.title")
  override def form:Form[DeceasedDateOfDeath] = deceasedDateOfDeathForm
  lazy val deceasedDateOfDeathView: deceased_date_of_death = app.injector.instanceOf[deceased_date_of_death]

  override def formToView:Form[DeceasedDateOfDeath] => Appendable = form => deceasedDateOfDeathView(form, CommonBuilder.DefaultCall1)

  lazy val regSummaryPage: Call = routes.RegistrationSummaryController.onPageLoad
  lazy val editSubmitLocation = iht.controllers.registration.deceased.routes.DeceasedDateOfDeathController.onEditSubmit

  def editModeView = {
    implicit val request = createFakeRequest()
    val view = deceasedDateOfDeathView(deceasedDateOfDeathForm, editSubmitLocation, Some(regSummaryPage)).toString
    asDocument(view)
   }

  "Deceased Date of Death View" must {

    behave like registrationPageWithErrorSummaryBox()

    "have a fieldset with the Id 'date-of-death'" in {
      doc.getElementsByTag("fieldset").first.id mustBe "date-of-death"
    }

    "have a 'day' input box" in {
      assertRenderedById(doc, "dateOfDeath.day")
    }

    "have a 'month' input box" in {
      assertRenderedById(doc, "dateOfDeath.month")
    }

    "have a 'year' input box" in {
      assertRenderedById(doc, "dateOfDeath.year")
    }

    "have a form hint" in {
     messagesShouldBePresent(view, messagesApi("page.iht.registration.deceasedDateOfDeath.dateOfDeath.hint"))
    }

    "not have a Cancel button" in {
      assertNotRenderedById(doc, "cancel-button")
    }
  }

  "Deceased Date of Death View in Edit mode" must {
    behave like registrationPageInEditModeWithErrorSummaryBox(editModeView, regSummaryPage)
  }
}
