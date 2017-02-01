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

import iht.forms.registration.ApplicantForms.applyingForProbateForm
import iht.forms.registration.DeceasedForms.deceasedAddressQuestionForm
import iht.models.ApplicantDetails
import iht.views.html.registration.applicant.applying_for_probate
import iht.views.html.registration.deceased.deceased_address_question
import iht.views.registration.YesNoQuestionViewBehaviour
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.HtmlFormat.Appendable

class ApplyingForProbateViewTest extends YesNoQuestionViewBehaviour[ApplicantDetails] {

  override def guidanceParagraphs = Set(Messages("page.iht.registration.applicant.applyingForProbate.p1"),
    Messages("page.iht.registration.applicant.applyingForProbate.p2"))

  override def pageTitle = Messages("iht.registration.applicant.applyingForProbate")
  override def browserTitle = Messages("page.iht.registration.applicant.applyingForProbate.browserTitle")

  override def fixture() = new {
    implicit val request = createFakeRequest()
    val form: Form[ApplicantDetails] = applyingForProbateForm
    val func: Form[ApplicantDetails] => Appendable = form => applying_for_probate(form, Call("", ""))
    val view = func(form).toString
    val doc = asDocument(view)
  }

  "Deceased Address Question View" must {
    behave like yesNoQuestion
  }
}
