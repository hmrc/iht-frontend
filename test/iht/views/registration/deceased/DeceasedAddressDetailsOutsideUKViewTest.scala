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
import iht.views.html.registration.deceased.deceased_address_details_outside_uk
import iht.views.registration.RegistrationPageBehaviour
import org.jsoup.select.Elements
import play.api.i18n.{Lang, Messages}
import play.api.mvc.Call

import scala.concurrent.Future

class DeceasedAddressDetailsOutsideUKViewTest extends RegistrationPageBehaviour {

  override def pageTitle = Messages("iht.registration.deceased.lastContactAddress")
  override def browserTitle = Messages("iht.registration.contactAddress")

  override def fixture() = new {
    implicit val request = createFakeRequest()
    val view = deceased_address_details_outside_uk(deceasedAddressDetailsOutsideUKForm, Call("", ""), Call("", "")).toString
    val doc = asDocument(view)
  }

  "Deceased Address Details (outside UK) View" must {

    behave like registrationPage()

    "have a fieldset with the Id 'details'" in {
      val f = fixture()
      f.doc.getElementsByTag("fieldset").first.id shouldBe "details"
    }

    "have a line 1 field" in {
      val f = fixture()
      assertRenderedById(f.doc, "ukAddress.addressLine1")
    }

    "have the correct label for line 1" in {
      val f = fixture()
      labelShouldBe(f.doc, "ukAddress.addressLine1-container", "iht.address.line1")
    }

    "have a line 2 field" in {
      val f = fixture()
      assertRenderedById(f.doc, "ukAddress.ukAddressLine2")
    }

    "have the correct label for line 2" in {
      val f = fixture()
      labelShouldBe(f.doc, "ukAddress.ukAddressLine2-container", "iht.address.line2")
    }

    "have a line 3 field" in {
      val f = fixture()
      assertRenderedById(f.doc, "ukAddress.addressLine3")
    }

    "have the correct label for line 3" in {
      val f = fixture()
      labelShouldBe(f.doc, "ukAddress.addressLine3-container", "iht.address.line3")
    }

    "have a line 4 field" in {
      val f = fixture()
      assertRenderedById(f.doc, "ukAddress.addressLine4")
    }

    "have the correct label for line 4" in {
      val f = fixture()
      labelShouldBe(f.doc, "ukAddress.addressLine4-container", "iht.address.line4")
    }

    "have a country code field" in {
      val f = fixture()
      assertRenderedById(f.doc, "ukAddress.countryCode")
    }

    "have a link to change to a UK address" in {
      val f = fixture()
      val link = f.doc.getElementById("return-button")
      link.text shouldBe Messages("iht.registration.changeAddressToUK")
    }
  }
}
