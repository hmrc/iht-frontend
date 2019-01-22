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

import iht.forms.registration.CoExecutorForms._
import iht.models.CoExecutor
import iht.testhelpers.CommonBuilder
import iht.views.html.registration.executor.executor_overview
import iht.views.registration.YesNoQuestionViewBehaviour
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.twirl.api.HtmlFormat.Appendable
import iht.views.html._

class ExecutorOverviewViewTest extends YesNoQuestionViewBehaviour[Option[Boolean]] {

  override def guidanceParagraphs = Set(messagesApi("page.iht.registration.executor-overview.description"),
    messagesApi("page.iht.registration.executor-overview.othersApplyingStatement.are"))

  override def pageTitle = messagesApi("iht.registration.othersApplyingForProbate")

  override def browserTitle = messagesApi("page.iht.registration.executor-overview.browserTitle")

  override def form: Form[Option[Boolean]] = executorOverviewForm

  override def formToView: Form[Option[Boolean]] => Appendable =
    form => executor_overview(form, true,
      Seq(CommonBuilder.DefaultCoExecutor1, CommonBuilder.DefaultCoExecutor2),
      CommonBuilder.DefaultCall1)(createFakeRequest(), applicationMessages, formPartialRetriever)

  def editModeViewAsDocument = {
    implicit val request = createFakeRequest()
    val view = executor_overview(form,
      true, Seq(),
      CommonBuilder.DefaultCall1,
      Some(CommonBuilder.DefaultCall2))(createFakeRequest(), applicationMessages, formPartialRetriever)
    asDocument(view)
  }

  def checkForDeleteExecutorLink(id:String, coExecutor: CoExecutor) = {
    val deleteLink = doc.getElementById("delete-executor-" + id)
    deleteLink.attr("href") mustBe
      iht.controllers.registration.executor.routes.DeleteCoExecutorController
        .onPageLoad(coExecutor.id.getOrElse("")).url
    deleteLink.text() mustBe messagesApi("iht.delete") +
      messagesApi("page.iht.registration.executor-overview.executor.delete.screenReader",
        coExecutor.name)
  }

  def checkForChangeExecutorLink(id:String, coExecutor: CoExecutor) = {
    val changeLink = doc.getElementById("change-executor-" + id)
    changeLink.attr("href") mustBe
      iht.controllers.registration.executor.routes.CoExecutorPersonalDetailsController.onPageLoad(coExecutor.id).url
    changeLink.text() mustBe messagesApi("iht.change") +
      messagesApi("page.iht.registration.executor-overview.executor.change.screenReader",
        coExecutor.name)
  }

  "Executor overview View" must {
    behave like yesNoQuestionWithLegend(
      questionLegend = messagesApi("page.iht.registration.executor-overview.yesnoQuestion")
    )

    "Display a change link for other people applying for probate" in {
      val othersApplyingLink = doc.getElementById("edit-others-applying-for-probate")
      othersApplyingLink.attr("href") mustBe iht.controllers.registration.executor.routes.OthersApplyingForProbateController.onPageLoadFromOverview().url
      othersApplyingLink.text() mustBe messagesApi("iht.change") + messagesApi("iht.registration.coExecutors.changeIfOthers")
    }

    "Display executor 1 name in table" in {
      ihtHelpers.custom.name(CommonBuilder.DefaultCoExecutor1.name.toString).toString must
        include(doc.getElementById("executorName-1").text)
    }

    "Display executor 1 delete link in table" in {
      checkForDeleteExecutorLink("1", CommonBuilder.DefaultCoExecutor1)
    }

    "Display executor 1 change link in table" in {
      checkForChangeExecutorLink("1", CommonBuilder.DefaultCoExecutor1)
    }

    "Display executor 2 name in table" in {
      doc.getElementById("executorName-2").text
      ihtHelpers.custom.name(CommonBuilder.DefaultCoExecutor2.name.toString).toString must
        include(doc.getElementById("executorName-2").text)
    }

    "Display executor 2 delete link in table" in {
      checkForDeleteExecutorLink("2", CommonBuilder.DefaultCoExecutor2)
    }

    "Display executor 2 change link in table" in {
      checkForChangeExecutorLink("2", CommonBuilder.DefaultCoExecutor2)
    }

    "have a continue and cancel link in edit mode" in {
      val doc = editModeViewAsDocument

      val continueLink = doc.getElementById("continue-button")
      continueLink.attr("value") mustBe messagesApi("iht.continue")

      val cancelLink = doc.getElementById("cancel-button")
      cancelLink.attr("href") mustBe CommonBuilder.DefaultCall2.url
      cancelLink.text() mustBe messagesApi("site.link.cancel")
    }
  }
}
