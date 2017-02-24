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

package iht.views.application.exemption.charity

import iht.testhelpers.CommonBuilder
import iht.views.html.application.exemption.charity.charity_delete_confirm
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}
import play.api.i18n.Messages.Implicits._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import scala.concurrent.ExecutionContext.Implicits.global

class CharityDeleteConfirmViewTest extends GenericNonSubmittablePageBehaviour {
  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()

  override def guidanceParagraphs = Set.empty

  override def pageTitle = messagesApi("iht.estateReport.exemptions.charities.confirmDeleteCharity")

  override def browserTitle = messagesApi("page.iht.application.exemptions.charityDelete.browserTitle")

  def exitComponent = Some(
    ExitComponent(
      iht.controllers.application.exemptions.charity.routes.CharitiesOverviewController.onPageLoad(),
      messagesApi("iht.estateReport.exemptions.charities.returnToAssetsLeftToCharities")
    )
  )

  val nameOfCharity = CommonBuilder.charity.map(_.name).fold("")(identity)

  def view = charity_delete_confirm(nameOfCharity, CommonBuilder.DefaultCall1).toString

  "Delete qualifying body confirmation page Question View" must {
    behave like nonSubmittablePage()

    "show submit button with correct target and text" in {
      doc.getElementsByTag("form").attr("action") shouldBe CommonBuilder.DefaultCall1.url
      val submitButton = doc.getElementById("confirm-delete")
      submitButton.text() shouldBe messagesApi("site.button.confirmDelete")
    }

    "show the name of the qualifying body" in {
      val nameParagraph = doc.getElementById("qualifying-body-name")
      nameParagraph.text shouldBe nameOfCharity
    }
  }
}
