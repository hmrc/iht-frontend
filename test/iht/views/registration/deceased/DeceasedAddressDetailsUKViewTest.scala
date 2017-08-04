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
import iht.forms.registration.DeceasedForms.deceasedAddressDetailsUKForm
import iht.models.DeceasedDetails
import iht.views.html.registration.deceased.deceased_address_details_uk
import iht.views.registration.RegistrationPageBehaviour
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Call
import play.twirl.api.HtmlFormat.Appendable
import iht.testhelpers.CommonBuilder

class DeceasedAddressDetailsUKViewTest extends RegistrationPageBehaviour[DeceasedDetails] {

  lazy val regSummaryPage = routes.RegistrationSummaryController.onPageLoad
  lazy val editSubmitLocation = CommonBuilder.DefaultCall1
  lazy val addressOutsideUK= CommonBuilder.DefaultCall2
  lazy val name = CommonBuilder.firstNameGenerator

  override def pageTitle = messagesApi("iht.registration.deceased.lastContactAddress", name)
  override def browserTitle = messagesApi("iht.registration.contactAddress")

  override def form:Form[DeceasedDetails] = deceasedAddressDetailsUKForm
  override def formToView:Form[DeceasedDetails] => Appendable = form => deceased_address_details_uk(form, name,
                                                                            CommonBuilder.DefaultCall1, addressOutsideUK)

  def editModeView = {
    implicit val request = createFakeRequest()
    val view = deceased_address_details_uk(
                      deceasedAddressDetailsUKForm, name, editSubmitLocation, addressOutsideUK, Some(regSummaryPage)).toString
    asDocument(view)
  }

  "Deceased Address Details (UK) View" must {

    behave like registrationPageWithErrorSummaryBox()

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

    "have a post code field" in {
      assertRenderedById(doc, "ukAddress.postCode")
    }

    "have the correct label for post code" in {
      labelShouldBe(doc, "ukAddress.postCode-container", "iht.postcode")
    }

    "not have a Cancel button" in {
      assertNotRenderedById(doc, "cancel-button")
    }

    "have a link to change to an address abroad" in {
      val link = doc.getElementById("return-button")
      link.attr("href") shouldBe (addressOutsideUK.url)
      link.text shouldBe messagesApi("iht.registration.changeAddressToAbroad")
    }
  }

  "Deceased Address Details (UK) View in Edit mode" must {
    behave like registrationPageInEditModeWithErrorSummaryBox(editModeView, regSummaryPage)

    "have a link to change to an address abroad" in {
      val link = editModeView.getElementById("return-button")
      link.attr("href") shouldBe (addressOutsideUK.url)
      link.text shouldBe messagesApi("iht.registration.changeAddressToAbroad")
    }
  }
}
