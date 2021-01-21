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

package iht.views.registration.executor

import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.html.registration.executor.delete_coexecutor_confirm
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import iht.config.AppConfig
import iht.utils._
import play.api.test.Helpers.contentAsString

class DeleteCoExecutorConfirmViewTest extends ViewTestHelper{

  lazy val coExecutor = CommonBuilder.buildCoExecutor

  lazy val coExecutorNonUK = CommonBuilder.buildCoExecutor copy (
    isAddressInUk = Some(false),
    ukAddress = Some(CommonBuilder.DefaultUkAddress copy(countryCode = "AF"))
    )

  def deleteCoExecutorUKAddressConfirmView() = {
    implicit val request = createFakeRequest()
    val view = delete_coexecutor_confirm(coExecutor).toString
     asDocument(view)
  }

  def deleteCoExecutorNonUKAddressConfirmView() = {
    implicit val request = createFakeRequest()
    val view = delete_coexecutor_confirm(coExecutorNonUK).toString
    asDocument(view)
  }

  "DeleteCoExecutorConfirmView" must {

    "have no message keys in html" in {
      val view = deleteCoExecutorUKAddressConfirmView().toString
      noMessageKeysShouldBePresent(view)
    }

    "have the correct title and browser title" in {
      val view = deleteCoExecutorUKAddressConfirmView().toString

      titleShouldBeCorrect(view,
        messagesApi("page.iht.registration.deleteExecutor.title",
                  messagesApi("page.iht.registration.executor-overview.entity-name")))

     browserTitleShouldBeCorrect(view, messagesApi("page.iht.registration.deleteExecutor.browserTitle"))
    }

    "show the CoExecutor name" in {
      val view = deleteCoExecutorUKAddressConfirmView().toString
      messagesShouldBePresent(view, coExecutor.name)
    }

    "show the CoExecutor address line 1" in {
      val view = deleteCoExecutorUKAddressConfirmView().toString
      messagesShouldBePresent(view, coExecutor.ukAddress.map(_.ukAddressLine1).fold("")(identity))
    }

    "show the CoExecutor address line 2" in {
      val view = deleteCoExecutorUKAddressConfirmView().toString
      messagesShouldBePresent(view, coExecutor.ukAddress.map(_.ukAddressLine2).fold("")(identity))
    }

    "show the CoExecutor address line 3" in {
      val view = deleteCoExecutorUKAddressConfirmView().toString
      messagesShouldBePresent(view, coExecutor.ukAddress.flatMap(_.ukAddressLine3).fold("")(identity))
    }

    "show the CoExecutor address line 4" in {
      val view = deleteCoExecutorUKAddressConfirmView().toString
      messagesShouldBePresent(view, coExecutor.ukAddress.flatMap(_.ukAddressLine4).fold("")(identity))
    }

    "show the CoExecutor postcode" in {
      val view = deleteCoExecutorUKAddressConfirmView().toString
      messagesShouldBePresent(view, coExecutor.ukAddress.map(_.postCode).fold("")(identity))
    }

    "not show the CoExecutor country for UK" in {
      val view = deleteCoExecutorUKAddressConfirmView().toString
      messagesShouldNotBePresent(view, coExecutor.ukAddress.map(_.countryCode).fold("")(identity))
    }

    "show the CoExecutor country for non UK" in {
      val view = deleteCoExecutorNonUKAddressConfirmView().toString
      messagesShouldBePresent(view, countryName(coExecutorNonUK.ukAddress.map(_.countryCode).fold("")(identity)))
    }

    "show Confirm delete button" in {
      val view = deleteCoExecutorUKAddressConfirmView().toString

      val button = asDocument(view).getElementById("confirm-delete")
      button.attr("value") mustBe messagesApi("site.button.confirmDelete")
    }

    "show the cancel link with correct text" in {
      val view = deleteCoExecutorUKAddressConfirmView().toString

      val link = asDocument(view).getElementById("cancel")
      link.text() mustBe messagesApi("site.link.cancel")
      link.attr("href") mustBe
        iht.controllers.registration.executor.routes.ExecutorOverviewController.onPageLoad().url
    }

  }
}
