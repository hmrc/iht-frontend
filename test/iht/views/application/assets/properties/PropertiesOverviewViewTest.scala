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

package iht.views.application.assets.properties

import iht.models.application.assets.Properties
import iht.testhelpers.CommonBuilder
import iht.views.html.application.asset.properties.properties_overview
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}
import iht.testhelpers.TestHelper
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.i18n.Messages.Implicits._

class PropertiesOverviewViewTest extends GenericNonSubmittablePageBehaviour {
  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()

  def registrationDetails = CommonBuilder.buildRegistrationDetails1

  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def guidanceParagraphs = Set(
    messagesApi("page.iht.application.assets.deceased-permanent-home.description.p1", deceasedName),
    messagesApi("page.iht.application.assets.deceased-permanent-home.description.p2", deceasedName)
  )

  override def pageTitle = messagesApi("page.iht.application.assets.deceased-permanent-home.sectionTitle")

  override def browserTitle = messagesApi("iht.estateReport.assets.propertiesBuildingsAndLand")

  override def exitComponent = Some(
    ExitComponent(
      iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad(),
      messagesApi("site.link.return.assets"),
      TestHelper.AppSectionPropertiesID
    )
  )

  override def view =
    properties_overview(List(CommonBuilder.property, CommonBuilder.property2),
      Some(Properties(isOwned = Some(true))),
      registrationDetails).toString()

  val addressTableId = "properties"

  def addressWithDeleteAndModify(rowNo: Int, expectedValue: String) = {
    s"show address number ${rowNo + 1}" in {
      tableCell(doc, addressTableId, 0, rowNo).ownText mustBe expectedValue
    }

    s"show address number ${rowNo + 1} delete link" in {
      val deleteDiv = tableCell(doc, addressTableId, 3, rowNo)
      val anchor = deleteDiv.getElementsByTag("a").first
      getVisibleText(anchor) mustBe messagesApi("iht.delete")
    }

    s"show address number ${rowNo + 1} give details link" in {
      val deleteDiv = tableCell(doc, addressTableId, 4, rowNo)
      val anchor = deleteDiv.getElementsByTag("a").first
      getVisibleText(anchor) mustBe messagesApi("iht.change")
    }
  }

  "Properties overview view" must {
    behave like nonSubmittablePage()

    behave like link("add-property",
      iht.controllers.application.assets.properties.routes.PropertyDetailsOverviewController.onPageLoad().url,
      messagesApi("iht.estateReport.assets.propertyAdd"))

    "show ownership question" in {
      elementShouldHaveText(doc, "property-owned-question", messagesApi("page.iht.application.assets.properties.question.question", deceasedName))
    }

    "show ownership question value" in {
      elementShouldHaveText(doc, "property-owned-question-value", messagesApi("iht.yes"))
    }

    behave like link("property-owned",
      iht.controllers.application.assets.properties.routes.PropertiesOwnedQuestionController.onPageLoad().url,
      messagesApi("iht.change"))

    behave like addressWithDeleteAndModify(0, formatAddressForDisplay(CommonBuilder.DefaultUkAddress))

    behave like addressWithDeleteAndModify(1, formatAddressForDisplay(CommonBuilder.DefaultUkAddress2))

    "show you haven't added message when there are no properties" in {
      val view = properties_overview(List(),
        Some(Properties(isOwned = Some(true))),
        registrationDetails).toString()
      val doc = asDocument(view)
      doc.getElementById("properties-empty-table-row").text mustBe
        messagesApi("page.iht.application.assets.deceased-permanent-home.table.emptyRow.text")

    }
  }
}
