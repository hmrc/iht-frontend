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

import iht.forms.registration.ApplicantForms.executorOfEstateForm
import iht.models.ApplicantDetails
import iht.testhelpers.CommonBuilder
import iht.views.html.registration.applicant.executor_of_estate
import iht.views.registration.YesNoQuestionViewBehaviour
import play.api.data.Form
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.config.AppConfig
import play.twirl.api.HtmlFormat.Appendable

class ExecutorOfEstateViewTest extends YesNoQuestionViewBehaviour[ApplicantDetails] {

  lazy val name = CommonBuilder.firstNameGenerator

  override def guidanceParagraphs = Set(messagesApi("page.iht.registration.applicant.executorOfEstate.p1"))

  override def pageTitle = messagesApi("page.iht.registration.applicant.executorOfEstate", name)

  override def browserTitle = messagesApi("page.iht.registration.applicant.executorOfEstate.browserTitle")

  override def form: Form[ApplicantDetails] = executorOfEstateForm
  lazy val executorOfEstateView: executor_of_estate = app.injector.instanceOf[executor_of_estate]

  override def formToView: Form[ApplicantDetails] => Appendable =
    form => executorOfEstateView(form, name, CommonBuilder.DefaultCall1)

  "Applying For Probate View" must {
    behave like yesNoQuestion
  }
}
