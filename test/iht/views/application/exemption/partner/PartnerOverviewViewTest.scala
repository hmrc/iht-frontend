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

package iht.views.application.exemption.partner

import iht.testhelpers.CommonBuilder
import iht.utils.CommonHelper._
import iht.views.html.application.exemption.partner.partner_overview
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}
import play.api.i18n.Messages.Implicits._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

trait PartnerOverviewViewBehaviour extends GenericNonSubmittablePageBehaviour {
  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()

  override def guidanceParagraphs = Set(
    messagesApi("iht.estateReport.exemptions.partner.assetsLeftToPartnerNotCharities")
  )

  override def pageTitle = messagesApi("iht.estateReport.assets.partnerAdd")

  override def browserTitle = messagesApi("page.iht.application.exemptions.overview.partner.detailsOverview.browserTitle")

  override val exitId: String = "return-button"

  override def exitComponent = Some(
    ExitComponent(
      iht.controllers.application.exemptions.partner.routes.PartnerOverviewController.onPageLoad(),
      messagesApi("iht.estateReport.exemptions.partner.returnToAssetsLeftToPartner")
    )
  )

  val propertyAttributesTableId = "qualifying-body-details-table"

  def propertyAttributeWithValueAndChange(rowNo: Int,
                                          expectedAttributeName: => String,
                                          expectedAttributeValue: => String,
                                          expectedLinkText: => String) = {
    s"show attribute number ${rowNo + 1} name" in {
      tableCell(doc, propertyAttributesTableId, 0, rowNo).text shouldBe expectedAttributeName
    }

    s"show attribute number ${rowNo + 1} value" in {
      tableCell(doc, propertyAttributesTableId, 1, rowNo).text shouldBe expectedAttributeValue
    }

    s"show attribute number ${rowNo + 1} change link" in {
      val changeDiv = tableCell(doc, propertyAttributesTableId, 2, rowNo)
      val anchor = changeDiv.getElementsByTag("a").first
      getVisibleText(anchor) shouldBe messagesApi(expectedLinkText)
    }
  }
}

class PartnerOverviewViewTest extends PartnerOverviewViewBehaviour {
  override def view = {
    val applicationDetails = CommonBuilder.buildApplicationDetails
    val registrationDetails = CommonBuilder.buildRegistrationDetails
    partner_overview(applicationDetails, registrationDetails).toString()
  }

  "Qualifying body details overview view" must {
    behave like nonSubmittablePage()

    behave like propertyAttributeWithValueAndChange(0,
      messagesApi("iht.estateReport.partner.partnerName"),
      CommonBuilder.partner.map(_.name).fold("")(identity),
      messagesApi("iht.change")
    )

    behave like propertyAttributeWithValueAndChange(1,
      messagesApi("page.iht.application.exemptions.overview.partner.detailsOverview.value.title"),
      CommonBuilder.currencyValue(getOrException(CommonBuilder.partner.map(_.totalValue))),
      messagesApi("iht.change")
    )
  }
}

class PartnerOverviewViewWithNoValuesTest extends PartnerOverviewViewBehaviour {
  override def view = {
    val applicationDetails = CommonBuilder.buildApplicationDetails
    val registrationDetails = CommonBuilder.buildRegistrationDetails
    partner_overview(applicationDetails, registrationDetails).toString()
  }

  "Qualifying body details overview view where no values entered" must {
    behave like propertyAttributeWithValueAndChange(0,
      messagesApi("iht.estateReport.partner.partnerName"),
      CommonBuilder.emptyString,
      messagesApi("site.link.giveDetails")
    )

    behave like propertyAttributeWithValueAndChange(1,
      messagesApi("page.iht.application.exemptions.overview.partner.detailsOverview.value.title"),
      CommonBuilder.emptyString,
      messagesApi("site.link.giveValue")
    )
  }
}
