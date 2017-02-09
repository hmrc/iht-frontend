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

package iht.views.registration

import play.api.data.FormError
import play.api.i18n.Messages
import play.api.mvc.Call
import org.jsoup.nodes.Document

trait PersonalDetailsViewBehaviour[A] extends RegistrationPageBehaviour[A] {

  /**
    * Assumes that the Call for the continue button has been set up as CommonBuilder.DefaultCall1.
    */
  def personalDetails() = {
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
  }

  def phoneNumber(label: String, hint: String) = {
    "have a phone number field" in {
      assertRenderedById(doc, "phoneNo")
    }

    "have the correct label for phone number" in {
      labelShouldBe(doc, "phoneNo-container", label)
    }

    "have a form hint for phone number" in {
      messagesShouldBePresent(view, Messages(hint))
    }
  }

   def personalDetailsInEditMode(view: => Document, cancelUrl: => Call) = {
		personalDetails()

		"have a continue button with correct text" in {
		  val continueLink = view.getElementById("continue-button")
		  continueLink.attr("value") shouldBe Messages("iht.continue")

		}

		"have a cancel link with correct text" in {
		  val cancelLink = view.getElementById("cancel-button")
		  cancelLink.attr("href") shouldBe cancelUrl.url
		  cancelLink.text() shouldBe Messages("site.link.cancel")
		}
	  }
}
