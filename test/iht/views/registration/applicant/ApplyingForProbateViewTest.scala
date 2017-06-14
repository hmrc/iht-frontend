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
import iht.models.ApplicantDetails
import iht.testhelpers.CommonBuilder
import iht.views.html.registration.applicant.applying_for_probate
import play.api.i18n.Messages.Implicits._
import iht.views.registration.YesNoQuestionViewBehaviour
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable

class ApplyingForProbateViewTest extends YesNoQuestionViewBehaviour[ApplicantDetails] {

  lazy val name = CommonBuilder.firstNameGenerator

  override def guidanceParagraphs = Set(messagesApi("page.iht.registration.applicant.applyingForProbate.p1"),
    messagesApi("page.iht.registration.applicant.applyingForProbate.p2", name))

  override def pageTitle = messagesApi("page.iht.registration.applicant.applyingForProbate", name)

  override def browserTitle = messagesApi("page.iht.registration.applicant.applyingForProbate.browserTitle")

  override def form: Form[ApplicantDetails] = applyingForProbateForm

  override def formToView: Form[ApplicantDetails] => Appendable =
    form => applying_for_probate(form, name, CommonBuilder.DefaultCall1)

  "Applying For Probate View" must {
    behave like yesNoQuestion
  }
}
