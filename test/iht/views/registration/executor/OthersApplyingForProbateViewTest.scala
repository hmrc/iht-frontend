/*
 * Copyright 2019 HM Revenue & Customs
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

import iht.forms.registration.CoExecutorForms.othersApplyingForProbateForm
import iht.testhelpers.CommonBuilder
import iht.views.html.registration.executor.others_applying_for_probate
import iht.views.registration.YesNoQuestionViewBehaviour
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.twirl.api.HtmlFormat.Appendable

trait OthersApplyingForProbateViewFixture extends YesNoQuestionViewBehaviour[Option[Boolean]] {
  override def guidanceParagraphs = Set(messagesApi("page.iht.registration.others-applying-for-probate.description"))

  override def pageTitle = messagesApi("page.iht.registration.others-applying-for-probate.sectionTitle")

  override def browserTitle = messagesApi("page.iht.registration.others-applying-for-probate.browserTitle")

  override def form: Form[Option[Boolean]] = othersApplyingForProbateForm
}

class OthersApplyingForProbateViewTest extends OthersApplyingForProbateViewFixture {
  override def formToView: Form[Option[Boolean]] => Appendable =
    form => others_applying_for_probate(form, CommonBuilder.DefaultCall1)

  "Others Applying for Probate View" must {
    behave like yesNoQuestion
  }
}

class OthersApplyingForProbateViewTestInEditMode extends OthersApplyingForProbateViewFixture {
  override def formToView: Form[Option[Boolean]] => Appendable =
    form => others_applying_for_probate(form,
      CommonBuilder.DefaultCall1, Some(CommonBuilder.DefaultCall2))

  "Others Applying for Probate View in Edit Mode" must {
    behave like yesNoQuestionWithCancelLink
  }
}
