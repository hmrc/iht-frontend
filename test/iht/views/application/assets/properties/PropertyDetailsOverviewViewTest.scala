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

package iht.views.application.assets.properties

import iht.testhelpers.CommonBuilder
import iht.views.html.application.asset.properties.property_details_overview
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.config.AppConfig
import iht.testhelpers.TestHelper

class PropertyDetailsOverviewViewTest extends GenericNonSubmittablePageBehaviour {
  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()

  def registrationDetails = CommonBuilder.buildRegistrationDetails1

  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def guidanceParagraphs = Set(
    messagesApi("page.iht.application.assets.property.detailsOverview.mortgage.text")
  )

  override def pageTitle = messagesApi("iht.estateReport.assets.propertyAdd")

  override def browserTitle = messagesApi("iht.estateReport.assets.propertyAdd")

  override val exitId: String = "return-button"

  override def exitComponent = Some(
    ExitComponent(
      iht.controllers.application.assets.properties.routes.PropertiesOverviewController.onPageLoad(),
      messagesApi("page.iht.application.assets.property.detailsOverview.returnLink"),
      TestHelper.AssetsPropertiesChangeID + "1"
    )
  )

  override def view =
    property_details_overview(deceasedName, Some(CommonBuilder.property)).toString()

  val propertyAttributesTableId = "property-details-table"

  def propertyAttributeWithValueAndChange(rowNo: Int, expectedAttributeName: => String, expectedAttributeValue: => String) = {
    s"show attribute number ${rowNo + 1}" in {
      tableCell(doc, propertyAttributesTableId, 0, rowNo).text mustBe expectedAttributeName
    }

    s"show attribute number ${rowNo + 1} value" in {
      tableCell(doc, propertyAttributesTableId, 1, rowNo).text mustBe expectedAttributeValue
    }

    s"show attribute number ${rowNo + 1} change link" in {
      val changeDiv = tableCell(doc, propertyAttributesTableId, 2, rowNo)
      val anchor = changeDiv.getElementsByTag("a").first
      getVisibleText(anchor) mustBe messagesApi("iht.change")
    }
  }

  "Property details overview view" must {
    behave like nonSubmittablePage()

    behave like propertyAttributeWithValueAndChange(0,
      messagesApi("iht.estateReport.assets.property.whatIsAddress.question"),
      formatAddressForDisplay(CommonBuilder.DefaultUkAddress))

    behave like propertyAttributeWithValueAndChange(1,
      messagesApi("iht.estateReport.assets.properties.whatKind.question"),
      messagesApi("page.iht.application.assets.propertyType.deceasedHome.label"))

    behave like propertyAttributeWithValueAndChange(2,
      messagesApi("iht.estateReport.assets.howOwnedByDeceased", deceasedName),
      messagesApi("page.iht.application.assets.typeOfOwnership.deceasedOnly.label", deceasedName))

    behave like propertyAttributeWithValueAndChange(3,
      messagesApi("iht.estateReport.assets.properties.freeholdOrLeasehold"),
      messagesApi("page.iht.application.assets.tenure.freehold.label"))

    behave like propertyAttributeWithValueAndChange(4,
      messagesApi("iht.estateReport.assets.properties.value.question", deceasedName),
      messagesApi("£12,345.00"))
  }
}
