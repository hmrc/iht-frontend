/*
 * Copyright 2021 HM Revenue & Customs
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

import iht.forms.registration.ApplicantForms.applyingForProbateForm
import iht.forms.registration.DeceasedForms._
import iht.models.{ApplicantDetails, DeceasedDetails}
import iht.views.html.registration.applicant.applying_for_probate
import iht.views.html.registration.deceased.deceased_permanent_home
import iht.views.registration.RegistrationPageBehaviour
import play.api.data.Form
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import iht.config.AppConfig
import play.api.mvc.Call
import play.twirl.api.HtmlFormat.Appendable

class DeceasedPermanentHomeViewTest extends RegistrationPageBehaviour[DeceasedDetails] {
  override def pageTitle = messagesApi("page.iht.registration.deceasedPermanentHome.title")

  override def browserTitle = messagesApi("page.iht.registration.deceasedPermanentHome.browserTitle")

  override def form:Form[DeceasedDetails] = deceasedPermanentHomeForm
  override def formToView:Form[DeceasedDetails] => Appendable = form => deceased_permanent_home(form, Call("", ""))

  "Deceased Permanent Home View" must {
    behave like registrationPageWithErrorSummaryBox()

    "have a fieldset with the Id 'country'" in {
      doc.getElementsByTag("fieldset").first.id mustBe "country"
    }
  }

  "radio buttons" must {
    "include england or wales" in {
      radioButtonShouldBeCorrect(doc, "iht.countries.englandOrWales", "domicile-england_or_wales")
    }

    "include scotland" in {
      radioButtonShouldBeCorrect(doc, "iht.countries.scotland", "domicile-scotland")
    }

    "include northern ireland" in {
      radioButtonShouldBeCorrect(doc, "iht.countries.northernIreland", "domicile-northern_ireland")
    }

    "include other" in {
      radioButtonShouldBeCorrect(doc, "iht.common.other", "domicile-other")
    }
  }
}
