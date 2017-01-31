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

import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.html.registration.executor.delete_coexecutor_confirm
import play.api.i18n.Messages

class DeleteCoExecutorConfirmViewTest extends ViewTestHelper{

  lazy val coExecutor = CommonBuilder.buildCoExecutor

  def deleteCoExecutorConfirmView() = {
    implicit val request = createFakeRequest()
    val view = delete_coexecutor_confirm(coExecutor).toString
     asDocument(view)
  }

  "DeleteCoExecutorConfirmView" must {

    "have the correct title and browser title" in {
      val view = deleteCoExecutorConfirmView().toString

      titleShouldBeCorrect(view,
        Messages("page.iht.registration.deleteExecutor.title",
                  Messages("page.iht.registration.executor-overview.entity-name")))

     browserTitleShouldBeCorrect(view, Messages("page.iht.registration.deleteExecutor.browserTitle"))
    }

    "show the CoExecutor name" in {
      val view = deleteCoExecutorConfirmView().toString
      messagesShouldBePresent(view, coExecutor.name)
    }

    "show Confirm delete button" in {
      val view = deleteCoExecutorConfirmView().toString

      val button = asDocument(view).getElementById("confirm-delete")
      button.attr("value") shouldBe Messages("site.button.confirmDelete")
    }

    "show the cancel link with correct text" in {
      val view = deleteCoExecutorConfirmView().toString

      val link = asDocument(view).getElementById("cancel")
      link.text() shouldBe Messages("site.link.cancel")
      link.attr("href") shouldBe
        iht.controllers.registration.executor.routes.ExecutorOverviewController.onPageLoad().url
    }

  }
}
