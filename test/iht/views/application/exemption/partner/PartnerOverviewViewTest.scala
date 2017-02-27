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

import iht.models.application.exemptions.{AllExemptions, PartnerExemption}
import iht.testhelpers.CommonBuilder
import iht.utils.CommonHelper._
import iht.views.html.application.exemption.partner.partner_overview
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}
import org.joda.time.LocalDate
import play.api.i18n.Messages.Implicits._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

trait PartnerOverviewViewBehaviour extends GenericNonSubmittablePageBehaviour {
  val regDetails = CommonBuilder.buildRegistrationDetails1

  val deceasedName = regDetails.deceasedDetails.map(_.name).fold("")(identity)

  val applicationDetails =
    CommonBuilder.buildApplicationDetails copy (allExemptions = Some(
    AllExemptions(
    partner =
      Some(PartnerExemption(
        isAssetForDeceasedPartner = Some(true),
        isPartnerHomeInUK = Some(true),
        firstName = Some(CommonBuilder.DefaultFirstName),
        lastName = Some(CommonBuilder.DefaultLastName),
        dateOfBirth = Some(CommonBuilder.DefaultDateOfBirth),
        nino = Some(CommonBuilder.DefaultNino),
        totalAssets = Some(CommonBuilder.DefaultTotalAssets)))
    )))

  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()

  override def guidanceParagraphs = Set.empty

  override def pageTitle = messagesApi("iht.estateReport.exemptions.partner.assetsLeftToSpouse.title")

  override def browserTitle = messagesApi("page.iht.application.exemptions.partner.overview.browserTitle")

  override def exitComponent = Some(
    ExitComponent(
      iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad(),
      messagesApi("page.iht.application.return.to.exemptionsOf", deceasedName)
    )
  )

  val propertyAttributesTableId = "partner-overview-table"

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
  override def view =
    partner_overview(applicationDetails, regDetails).toString()

  "partner overview view" must {
    behave like nonSubmittablePage()

//    behave like propertyAttributeWithValueAndChange(0,
//      messagesApi("iht.estateReport.partner.partnerName"),
//      CommonBuilder.partner.map(_.name).fold("")(identity),
//      messagesApi("iht.change")
//    )
//
//    behave like propertyAttributeWithValueAndChange(1,
//      messagesApi("page.iht.application.exemptions.overview.partner.detailsOverview.value.title"),
//      CommonBuilder.currencyValue(getOrException(CommonBuilder.partner.map(_.totalValue))),
//      messagesApi("iht.change")
//    )
  }
}

class PartnerOverviewViewWithNoValuesTest extends PartnerOverviewViewBehaviour {
  override def view =
    partner_overview(applicationDetails, regDetails).toString()

  "Partner overview view where no values entered" must {
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
