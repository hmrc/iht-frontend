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
import iht.views.html.registration.deceased.about_deceased
import iht.views.registration.RegistrationPageBehaviour
import org.joda.time.LocalDate
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Call

class AboutDeceasedViewTest extends RegistrationPageBehaviour {

  override def pageTitle = messagesApi("iht.registration.deceasedDetails.title")
  override def browserTitle = messagesApi("iht.registration.deceasedDetails.title")

  override def fixture() = new {
    implicit val request = createFakeRequest()
    val view = about_deceased(aboutDeceasedForm(new LocalDate()), Call("", "")).toString
    val doc = asDocument(view)
  }

  "About Deceased View" must {

    behave like registrationPage()

    "have the correct label for first name" in {
      val f = fixture()
      labelShouldBe(f.doc, "firstName-container", "iht.firstName")
    }

    "have hint text for first name" in {
      val f = fixture()
      labelHelpTextShouldBe(f.doc, "firstName-container", "iht.firstName.hint")
    }

    "have a first name field" in {
      val f = fixture()
      assertRenderedById(f.doc, "firstName")
    }

    "have the correct label for last name" in {
      val f = fixture()
      labelShouldBe(f.doc, "lastName-container", "iht.lastName")
    }

    "have a last name field" in {
      val f = fixture()
      assertRenderedById(f.doc, "lastName")
    }

    "have a fieldset with the Id 'date-of-birth'" in {
      val f = fixture()
      assertRenderedById(f.doc, "date-of-birth")
    }

    "have a 'day' input box" in {
      val f = fixture()
      assertRenderedById(f.doc, "dateOfBirth.day")
    }

    "have a 'month' input box" in {
      val f = fixture()
      assertRenderedById(f.doc, "dateOfBirth.month")
    }

    "have a 'year' input box" in {
      val f = fixture()
      assertRenderedById(f.doc, "dateOfBirth.year")
    }

    "have a form hint for date of birth" in {
      val f = fixture()
      messagesShouldBePresent(f.view, messagesApi("iht.dateExample"))
    }

    "have the correct label for nino" in {
      val f = fixture()
      labelShouldBe(f.doc, "nino-container", "iht.nationalInsuranceNo")
    }

    "have a nino field" in {
      val f = fixture()
      assertRenderedById(f.doc, "nino")
    }

    "have a fieldset with the Id 'relationship-status'" in {
      val f = fixture()
      assertRenderedById(f.doc, "relationship-status")
    }

    "have all the correct marital status on the page'" in {
      val f = fixture()
      messagesShouldBePresent(f.view, messagesApi("page.iht.registration.deceasedDetails.maritalStatus.civilPartnership.label"))
      messagesShouldBePresent(f.view, messagesApi("page.iht.registration.deceasedDetails.maritalStatus.civilPartner.label"))
      messagesShouldBePresent(f.view, messagesApi("page.iht.registration.deceasedDetails.maritalStatus.widowed.label"))
      messagesShouldBePresent(f.view, messagesApi("page.iht.registration.deceasedDetails.maritalStatus.single.label"))
    }
  }
}
