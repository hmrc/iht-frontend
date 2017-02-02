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

import iht.forms.registration.DeceasedForms._
import iht.models.DeceasedDetails
import iht.views.html.registration.deceased.about_deceased
import iht.views.registration.RegistrationPageBehaviour
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.HtmlFormat.Appendable

class AboutDeceasedViewTest extends RegistrationPageBehaviour[DeceasedDetails] {

  override def pageTitle = Messages("iht.registration.deceasedDetails.title")
  override def browserTitle = Messages("iht.registration.deceasedDetails.title")

  override def form:Form[DeceasedDetails] = aboutDeceasedForm(new LocalDate())
  override def formToView:Form[DeceasedDetails] => Appendable = form => about_deceased(form, Call("", ""))

  "About Deceased View" must {

    behave like registrationPage()

    "have the correct label for first name" in {
      labelShouldBe(doc, "firstName-container", "iht.firstName")
    }

    "have hint text for first name" in {
      labelHelpTextShouldBe(doc, "firstName-container", "iht.firstName.hint")
    }

    "have a first name field" in {
      assertRenderedById(doc, "firstName")
    }

    "have the correct label for last name" in {
      labelShouldBe(doc, "lastName-container", "iht.lastName")
    }

    "have a last name field" in {
      assertRenderedById(doc, "lastName")
    }

    "have a fieldset with the Id 'date-of-birth'" in {
      assertRenderedById(doc, "date-of-birth")
    }

    "have a 'day' input box" in {
      assertRenderedById(doc, "dateOfBirth.day")
    }

    "have a 'month' input box" in {
      assertRenderedById(doc, "dateOfBirth.month")
    }

    "have a 'year' input box" in {
      assertRenderedById(doc, "dateOfBirth.year")
    }

    "have a form hint for date of birth" in {
      messagesShouldBePresent(view, Messages("iht.dateExample"))
    }

    "have the correct label for nino" in {
      labelShouldBe(doc, "nino-container", "iht.nationalInsuranceNo")
    }

    "have a nino field" in {
      assertRenderedById(doc, "nino")
    }

    "have a fieldset with the Id 'relationship-status'" in {
      assertRenderedById(doc, "relationship-status")
    }

    "have all the correct marital status on the page'" in {
      messagesShouldBePresent(view, Messages("page.iht.registration.deceasedDetails.maritalStatus.civilPartnership.label"))
      messagesShouldBePresent(view, Messages("page.iht.registration.deceasedDetails.maritalStatus.civilPartner.label"))
      messagesShouldBePresent(view, Messages("page.iht.registration.deceasedDetails.maritalStatus.widowed.label"))
      messagesShouldBePresent(view, Messages("page.iht.registration.deceasedDetails.maritalStatus.single.label"))
    }
  }
}
