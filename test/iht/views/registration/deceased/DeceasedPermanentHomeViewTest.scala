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
import iht.views.html.registration.deceased.deceased_permanent_home
import iht.views.registration.RegistrationPageBehaviour
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.HtmlFormat.Appendable

class DeceasedPermanentHomeViewTest extends RegistrationPageBehaviour[DeceasedDetails] {
  override def pageTitle = Messages("page.iht.registration.deceasedPermanentHome.title")

  override def browserTitle = Messages("page.iht.registration.deceasedPermanentHome.browserTitle")

  override def fixture() = new {
    implicit val request = createFakeRequest()
    val form = deceasedPermanentHomeForm
    val func: Form[DeceasedDetails] => Appendable = form => deceased_permanent_home(form, Call("", ""))
    val view = func(form).toString
    val doc = asDocument(view)
  }

  "Deceased Permanent Home View" must {
    behave like registrationPageWithErrorSummaryBox()

    "have a fieldset with the Id 'country'" in {
      fixture().doc.getElementsByTag("fieldset").first.id shouldBe "country"
    }

    "have a radio button for england or wales" in {
      radioButtonShouldBeCorrect(fixture().doc, "iht.countries.englandOrWales", "domicile-england_or_wales")
    }

    "have a radio button for scotland" in {
      radioButtonShouldBeCorrect(fixture().doc, "iht.countries.scotland", "domicile-scotland")
    }

    "have a radio button for northern ireland" in {
      radioButtonShouldBeCorrect(fixture().doc, "iht.countries.northernIreland", "domicile-northern_ireland")
    }

    "have a radio button for other" in {
      radioButtonShouldBeCorrect(fixture().doc, "page.iht.registration.deceasedDetails.domicile.other.label", "domicile-other")
    }
  }
}
