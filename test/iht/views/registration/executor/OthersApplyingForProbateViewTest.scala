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

package iht.views.registration.executor

import iht.forms.registration.ApplicantForms.applyingForProbateForm
import iht.views.registration.RegistrationPageBehaviour
import iht.forms.registration.CoExecutorForms.othersApplyingForProbateForm
import iht.models.{ApplicantDetails, DeceasedDateOfDeath}
import iht.views.html.registration.applicant.applying_for_probate
import iht.views.html.registration.executor.others_applying_for_probate
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.HtmlFormat.Appendable

class OthersApplyingForProbateViewTest extends RegistrationPageBehaviour[Option[Boolean]] {

  override def pageTitle = Messages("page.iht.registration.others-applying-for-probate.sectionTitle")
  override def browserTitle = Messages("page.iht.registration.others-applying-for-probate.browserTitle")

  override def form:Form[Option[Boolean]] = othersApplyingForProbateForm
  override def formToView:Form[Option[Boolean]] => Appendable = form => others_applying_for_probate(form, Call("", ""))

  "Others Applying for Probate View" must {

    behave like registrationPage()

    "have a fieldset with the Id 'answer'" in {
      val view = others_applying_for_probate(othersApplyingForProbateForm, Call("", ""))(createFakeRequest()).toString
      asDocument(view).getElementsByTag("fieldset").first.id shouldBe "answer"
    }

    "show the correct guidance" in {
      messagesShouldBePresent(view,
        Messages("page.iht.registration.others-applying-for-probate.description"))
    }
  }
}
