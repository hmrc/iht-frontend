/*
 * Copyright 2022 HM Revenue & Customs
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
import iht.testhelpers.TestHelper._
import iht.views.html.application.exemption.charity.charity_delete_confirm
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

class CharityDeleteConfirmViewTest extends GenericNonSubmittablePageBehaviour {
  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()

  override def guidanceParagraphs = Set.empty

  override def pageTitle = messagesApi("page.iht.application.exemptions.charityDelete.sectionTitle")

  override def browserTitle = messagesApi("page.iht.application.exemptions.charityDelete.browserTitle")

  override val exitId: String = "return-button"

  def exitComponent = Some(
    ExitComponent(
      iht.controllers.application.exemptions.charity.routes.CharitiesOverviewController.onPageLoad(),
      messagesApi("iht.estateReport.exemptions.charities.returnToAssetsLeftToCharities"),
      ExemptionsCharitiesDeleteID + "1"
    )
  )

  val nameOfCharity = CommonBuilder.charity.name.fold("")(identity)
  lazy val charityDeleteConfirmView: charity_delete_confirm = app.injector.instanceOf[charity_delete_confirm]

  def view = charityDeleteConfirmView(CommonBuilder.charity, CommonBuilder.DefaultCall1).toString

  "Delete qualifying body confirmation page Question View" must {
    behave like nonSubmittablePage()

    "show submit button with correct target and text" in {
      doc.getElementsByTag("form").attr("action") mustBe CommonBuilder.DefaultCall1.url
      val submitButton = doc.getElementById("confirm-delete")
      submitButton.text() mustBe messagesApi("site.button.confirmDelete")
    }

    "show the name of the charity" in {
      val nameParagraph = doc.getElementById("charity-name")
      nameParagraph.text mustBe nameOfCharity
    }
  }
}
