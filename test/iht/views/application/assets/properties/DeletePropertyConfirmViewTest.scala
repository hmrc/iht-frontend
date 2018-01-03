/*
 * Copyright 2018 HM Revenue & Customs
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

package iht.views.application.assets.properties

import iht.testhelpers.CommonBuilder
import iht.utils.CommonHelper
import iht.views.GenericNonSubmittablePageBehaviour
import iht.views.html.application.asset.properties.delete_property_confirm
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.i18n.Messages.Implicits._
import iht.testhelpers.TestHelper

class DeletePropertyConfirmViewTest extends GenericNonSubmittablePageBehaviour {
  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()

  override def guidanceParagraphs = Set(
    messagesApi("page.iht.application.assets.main-section.properties.delete.warning")
  )

  override def pageTitle = messagesApi("page.iht.application.propertyDetails.deleteProperty.title")

  override def browserTitle = messagesApi("page.iht.application.propertyDetails.deleteProperty.title")

  def exitComponent = None

  def view = delete_property_confirm(CommonBuilder.property).toString

  "Delete property confirmation page Question View" must {
    behave like nonSubmittablePage()

    "show submit button with correct target and text" in {
      doc.getElementsByTag("form").attr("action") shouldBe iht.controllers.application.assets.properties.routes.DeletePropertyController.onSubmit("1").url
      val submitButton = doc.getElementById("delete-confirm")
      submitButton.text() shouldBe messagesApi("site.button.confirmDelete")
    }

    "show cancel link with correct target and text" in {
      val submitButton = doc.getElementById("cancel-button")
      submitButton.attr("href") shouldBe CommonHelper.addFragmentIdentifierToUrl(iht.controllers.application.assets.properties.routes.PropertiesOverviewController.onPageLoad().url, TestHelper.AssetsPropertiesDeleteID + "1")
      submitButton.text() shouldBe messagesApi("site.link.cancel")
    }

    "show the address" in {
      val addressDiv = doc.getElementById("address")
      addressDiv.text shouldBe formatAddressForDisplay(CommonBuilder.DefaultUkAddress)
    }
  }
}
