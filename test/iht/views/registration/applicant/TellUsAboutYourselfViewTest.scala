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

import iht.controllers.ControllerHelper.Mode
import iht.forms.registration.ApplicantForms.applicantTellUsAboutYourselfForm
import iht.models.ApplicantDetails
import iht.testhelpers.CommonBuilder
import iht.views.html.registration.applicant.applicant_tell_us_about_yourself
import iht.views.registration.{PersonalDetailsViewBehaviour, YesNoQuestionViewBehaviour}
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat.Appendable

class TellUsAboutYourselfViewTest extends YesNoQuestionViewBehaviour[ApplicantDetails] with PersonalDetailsViewBehaviour[ApplicantDetails]{

  override def guidanceParagraphs = Set.empty

  override def pageTitle = Messages("iht.registration.applicant.tellUsAboutYourself")

  override def browserTitle = Messages("iht.registration.applicant.tellUsAboutYourself")

  override def form: Form[ApplicantDetails] = applicantTellUsAboutYourselfForm

  override def formToView: Form[ApplicantDetails] => Appendable =
    form => applicant_tell_us_about_yourself(form, Mode.Standard, CommonBuilder.DefaultCall1)

  "Tell Us About Yourself View" must {
    behave like yesNoQuestion

    behave like phoneNumber(
        label = "page.iht.registration.applicantTellUsAboutYourself.value.label",
        hint = "page.iht.registration.applicantTellUsAboutYourself.value.sublabel"
    )
  }
}
