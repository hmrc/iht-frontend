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

import iht.forms.registration.DeceasedForms.deceasedAddressDetailsOutsideUKForm
import iht.models.DeceasedDetails
import iht.views.html.registration.deceased.deceased_address_details_outside_uk
import iht.views.registration.RegistrationPageBehaviour
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.HtmlFormat.Appendable

class DeceasedAddressDetailsOutsideUKViewTest extends RegistrationPageBehaviour[DeceasedDetails] {

  override def pageTitle = Messages("iht.registration.deceased.lastContactAddress")
  override def browserTitle = Messages("iht.registration.contactAddress")

  override def form:Form[DeceasedDetails] = deceasedAddressDetailsOutsideUKForm
  override def formToView:Form[DeceasedDetails] => Appendable = form => deceased_address_details_outside_uk(form, Call("", ""), Call("", ""))

  "Deceased Address Details (outside UK) View" must {

    behave like registrationPage()

    "have a fieldset with the Id 'details'" in {
      doc.getElementsByTag("fieldset").first.id shouldBe "details"
    }

    "have a line 1 field" in {
      assertRenderedById(doc, "ukAddress.addressLine1")
    }

    "have the correct label for line 1" in {
      labelShouldBe(doc, "ukAddress.addressLine1-container", "iht.address.line1")
    }

    "have a line 2 field" in {
      assertRenderedById(doc, "ukAddress.ukAddressLine2")
    }

    "have the correct label for line 2" in {
      labelShouldBe(doc, "ukAddress.ukAddressLine2-container", "iht.address.line2")
    }

    "have a line 3 field" in {
      assertRenderedById(doc, "ukAddress.addressLine3")
    }

    "have the correct label for line 3" in {
      labelShouldBe(doc, "ukAddress.addressLine3-container", "iht.address.line3")
    }

    "have a line 4 field" in {
      assertRenderedById(doc, "ukAddress.addressLine4")
    }

    "have the correct label for line 4" in {
      labelShouldBe(doc, "ukAddress.addressLine4-container", "iht.address.line4")
    }

    "have a country code field" in {
      assertRenderedById(doc, "ukAddress.countryCode")
    }

    "have a link to change to a UK address" in {
      val link = doc.getElementById("return-button")
      link.text shouldBe Messages("iht.registration.changeAddressToUK")
    }
  }
}
