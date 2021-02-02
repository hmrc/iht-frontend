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

package iht.views.registration.applicant

import iht.forms.registration.ApplicantForms.probateLocationForm
import iht.models.ApplicantDetails
import iht.testhelpers.CommonBuilder
import iht.views.html.registration.applicant.probate_location
import iht.views.registration.RegistrationPageBehaviour
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.config.AppConfig
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat.Appendable

import scala.collection.immutable.ListMap

class ProbateLocationViewTest extends RegistrationPageBehaviour[ApplicantDetails] {

  override def pageTitle = messagesApi("page.iht.registration.applicant.probateLocation.title")
  override def browserTitle = messagesApi("page.iht.registration.applicant.probateLocation.browserTitle")

  override def form:Form[ApplicantDetails] = probateLocationForm
  override def formToView:Form[ApplicantDetails] => Appendable = form => probate_location(form, CommonBuilder.DefaultCall1)

  "Probate Location View" must {

    behave like registrationPageWithErrorSummaryBox()

    "have radio button" which {
      "has a fieldset with the Id 'country'" in {
        doc.getElementsByTag("fieldset").first.id mustBe "country"
      }

      "includes england or wales" in {
        radioButtonShouldBeCorrect(doc, "iht.countries.englandOrWales", "country-england_or_wales")
      }

      "includes scotland" in {
        radioButtonShouldBeCorrect(doc, "iht.countries.scotland", "country-scotland")
      }

      "includes northern ireland" in {
        radioButtonShouldBeCorrect(doc, "iht.countries.northernIreland", "country-northern_ireland")
      }
    }


  }
}
