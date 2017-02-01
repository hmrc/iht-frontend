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

import iht.forms.registration.ApplicantForms.probateLocationForm
import iht.models.ApplicantDetails
import iht.views.html.registration.applicant.probate_location
import iht.views.registration.RegistrationPageBehaviour
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Call

import scala.collection.immutable.ListMap

class ProbateLocationViewTest extends RegistrationPageBehaviour[ApplicantDetails] {

  override def pageTitle = Messages("page.iht.registration.applicant.probateLocation.title")
  override def browserTitle = Messages("page.iht.registration.applicant.probateLocation.browserTitle")

  override def fixture() = new {
    implicit val request = createFakeRequest()
    val view = probate_location(probateLocationForm, ListMap[String, String](), Call("", "")).toString
    val doc = asDocument(view)
    val form:Form[ApplicantDetails] = null
    val func:Form[ApplicantDetails] => play.twirl.api.HtmlFormat.Appendable = null
  }

  "Probate Location View" must {

    behave like registrationPage()

    "have a fieldset with the Id 'country'" in {
      val f = fixture()
      f.doc.getElementsByTag("fieldset").first.id shouldBe "country"
    }
  }
}
