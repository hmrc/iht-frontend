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
import iht.testhelpers.CommonBuilder
import iht.views.html.registration.deceased.about_deceased
import iht.views.registration.RegistrationPageBehaviour
import org.joda.time.LocalDate
import play.api.i18n.Messages.Implicits._
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable
import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.http.HeaderCarrier

class AboutDeceasedViewTest extends RegistrationPageBehaviour[DeceasedDetails] {

  override def pageTitle = messagesApi("iht.registration.deceasedDetails.title")
  override def browserTitle = messagesApi("iht.registration.deceasedDetails.title")

  implicit val hc = mock[HeaderCarrier]

  override def form:Form[DeceasedDetails] = aboutDeceasedForm(new LocalDate())
  override def formToView:Form[DeceasedDetails] => Appendable = form => about_deceased(form, CommonBuilder.DefaultCall1)


  def editModeViewAsDocument = {
    implicit val request = createFakeRequest()
    val view = about_deceased(form, CommonBuilder.DefaultCall1, Some(CommonBuilder.DefaultCall2)).toString
    asDocument(view)
  }

  "About Deceased View" must {

    behave like registrationPageWithErrorSummaryBox()

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
      messagesShouldBePresent(view, messagesApi("iht.dateExample"))
    }

    "have the correct label for nino" in {
      labelShouldBe(doc, "nino-container", "iht.nationalInsuranceNo")
    }

    "have hint text for nino" in {
      labelHelpTextShouldBe(doc, "nino-container", "iht.ninoExample")
    }

    "have a nino field" in {
      assertRenderedById(doc, "nino")
    }

    "have radio button" which {
      "has a fieldset with the Id 'relationship-status'" in {
        assertRenderedById(doc, "relationship-status")
      }

      "includes Married or in a civil partnership" in {
        radioButtonShouldBeCorrect(doc, "page.iht.registration.deceasedDetails.maritalStatus.civilPartnership.label", "maritalStatus-married_or_in_civil_partnership")
      }

      "includes Divorced or a former civil partner" in {
        radioButtonShouldBeCorrect(doc, "page.iht.registration.deceasedDetails.maritalStatus.civilPartner.label", "maritalStatus-divorced_or_former_civil_partner")
      }

      "includes Widowed or a surviving civil partner" in {
        radioButtonShouldBeCorrect(doc, "page.iht.registration.deceasedDetails.maritalStatus.widowed.label", "maritalStatus-widowed_or_a_surviving_civil_partner")
      }

      "includes Never married or in a civil partnership" in {
        radioButtonShouldBeCorrect(doc, "page.iht.registration.deceasedDetails.maritalStatus.single.label", "maritalStatus-single")
      }
    }

    "have a continue and cancel link in edit mode" in {
      val doc = editModeViewAsDocument

      val continueLink = doc.getElementById("continue-button")
      continueLink.attr("value") shouldBe messagesApi("iht.continue")

      val cancelLink = doc.getElementById("cancel-button")
      cancelLink.attr("href") shouldBe CommonBuilder.DefaultCall2.url
      cancelLink.text() shouldBe messagesApi("site.link.cancel")
    }
  }
}
