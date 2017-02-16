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

package iht.views.registration.applicant

import iht.forms.registration.ApplicantForms.{applicantAddressAbroadForm, applicantAddressUkForm}
import iht.views.html.registration.applicant.applicant_address
import iht.views.registration.RegistrationPageBehaviour
import play.api.mvc.Call
import play.api.i18n.MessagesApi
import play.api.i18n.Messages.Implicits._

class ApplicantAddressViewTest extends RegistrationPageBehaviour {
  
  override def pageTitle = messagesApi("page.iht.registration.applicantAddress.title")
  override def browserTitle = messagesApi("page.iht.registration.applicantAddress.title")

  override def fixture() = new {
    implicit val request = createFakeRequest()
    val view = applicant_address(applicantAddressUkForm, false, Call("", ""), Call("", "")).toString
    val doc = asDocument(view)
  }

  def fixtureAbroad() = new {
    implicit val request = createFakeRequest()
    val view = applicant_address(applicantAddressAbroadForm, true, Call("", ""), Call("", "")).toString
    val doc = asDocument(view)
  }

  "Applicant Address View" must {

    behave like registrationPage()

    "show the correct guidance" in {
      val f = fixture()
      messagesShouldBePresent(f.view, messagesApi("page.iht.registration.applicantAddress.hint"))
    }

    "have a line 1 field" in {
      val f = fixture()
      assertRenderedById(f.doc, "ukAddressLine1")
    }

    "have the correct label for line 1" in {
      val f = fixture()
      labelShouldBe(f.doc, "ukAddressLine1-container", "iht.address.line1")
    }

    "have a line 2 field" in {
      val f = fixture()
      assertRenderedById(f.doc, "ukAddressLine2")
    }

    "have the correct label for line 2" in {
      val f = fixture()
      labelShouldBe(f.doc, "ukAddressLine2-container", "iht.address.line2")
    }

    "have a line 3 field" in {
      val f = fixture()
      assertRenderedById(f.doc, "ukAddressLine3")
    }

    "have the correct label for line 3" in {
      val f = fixture()
      labelShouldBe(f.doc, "ukAddressLine3-container", "iht.address.line3")
    }

    "have a line 4 field" in {
      val f = fixture()
      assertRenderedById(f.doc, "ukAddressLine4")
    }

    "have the correct label for line 4" in {
      val f = fixture()
      labelShouldBe(f.doc, "ukAddressLine4-container", "iht.address.line4")
    }
  }

  "Applicant Address View" when {
    "showing in UK mode" must {

      "have a fieldset with the Id 'details'" in {
        val view = applicant_address(applicantAddressUkForm, isInternational = false,
          Call("", ""),
          Call("", ""))(createFakeRequest(), applicationMessages).toString

        asDocument(view).getElementsByTag("fieldset").first.id shouldBe "details"
      }

      "have a post code field" in {
        val f = fixture()
        assertRenderedById(f.doc, "postCode")
      }

      "have the correct label for post code" in {
        val f = fixture()
        labelShouldBe(f.doc, "postCode-container", "iht.postcode")
      }

      "not have a country code field" in {
        val f = fixture()
        assertNotRenderedById(f.doc, "countryCode")
      }
    }

    "showing in international mode" must {

      "have a fieldset with the Id 'details'" in {
        val f = fixtureAbroad()
        f.doc.getElementsByTag("fieldset").first.id shouldBe "details"
      }

      "have a country code field" in {
        val f = fixtureAbroad()
        assertRenderedById(f.doc, "countryCode")
      }

      "not have a post code field" in {
        val f = fixtureAbroad()
        assertNotRenderedById(f.doc, "postCode")
      }
    }
  }
}
