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

import iht.controllers.ControllerHelper.Mode
import iht.forms.registration.CoExecutorForms
import iht.models.CoExecutor
import iht.testhelpers.CommonBuilder
import iht.views.html.registration.executor.coexecutor_personal_details
import iht.views.registration.{PersonalDetailsViewBehaviour, YesNoQuestionViewBehaviour}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class CoexecutorPersonalDetailsViewTest extends YesNoQuestionViewBehaviour[CoExecutor] with PersonalDetailsViewBehaviour[CoExecutor] with CoExecutorForms {

  override def guidanceParagraphs = Set.empty

  override def pageTitle = messagesApi("page.iht.registration.co-executor-personal-details.title")

  override def browserTitle = messagesApi("page.iht.registration.co-executor-personal-details.browserTitle")

  val fakedFormNino = "SR000009C"

  override def form: Form[CoExecutor] = {
    implicit val request = createFakeRequest()
    implicit val hc = new HeaderCarrier()
    coExecutorPersonalDetailsForm(loginNino = fakedFormNino)
  }

  override def formToView: Form[CoExecutor] => Appendable =
    form => coexecutor_personal_details(form,
      Mode.Standard,
      CommonBuilder.DefaultCall1)(createFakeRequest(), messages, formPartialRetriever, appConfig)


  def editModeViewAsDocument(): Document = {
    implicit val request = createFakeRequest()
    implicit val hc = new HeaderCarrier()
    val view = coexecutor_personal_details(coExecutorPersonalDetailsForm(loginNino = fakedFormNino), Mode.Edit,
                                                CommonBuilder.DefaultCall1, Some(CommonBuilder.DefaultCall2)).toString
    asDocument(view)
  }

  "Co Exec Personal Details View" must {

    behave like personalDetails

    behave like phoneNumber(
      label = "iht.registration.checklist.phoneNo.upperCaseInitial",
      hint = "site.phoneNo.hint"
    )

    behave like yesNoQuestion
  }

  "Co Exec Personal Details View in Edit mode" must {
      behave like personalDetailsInEditMode(editModeViewAsDocument, CommonBuilder.DefaultCall2)
  }
}
