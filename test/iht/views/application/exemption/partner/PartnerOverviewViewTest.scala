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

package iht.views.application.exemption.partner

import iht.constants.IhtProperties
import iht.models.application.exemptions.{AllExemptions, PartnerExemption}
import iht.testhelpers.CommonBuilder
import iht.utils.CommonHelper
import iht.utils.CommonHelper._
import iht.views.html.application.exemption.partner.partner_overview
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}
import org.joda.time.LocalDate
import play.api.i18n.Messages.Implicits._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import iht.testhelpers.TestHelper._

trait PartnerOverviewViewBehaviour extends GenericNonSubmittablePageBehaviour {
  val regDetails = CommonBuilder.buildRegistrationDetails1

  val deceasedName = regDetails.deceasedDetails.map(_.name).fold("")(identity)

  val partner: PartnerExemption

  def applicationDetails =
    CommonBuilder.buildApplicationDetails copy (allExemptions = Some(
      AllExemptions(
        partner = Some(partner)
      )))

  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()

  override def guidanceParagraphs = Set.empty

  override def pageTitle = messagesApi("iht.estateReport.exemptions.partner.assetsLeftToSpouse.title")

  override def browserTitle = messagesApi("page.iht.application.exemptions.partner.overview.browserTitle")

  override def exitComponent = Some(
    ExitComponent(
      iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad(),
      messagesApi("page.iht.application.return.to.exemptionsOf", deceasedName),
      ExemptionsPartnerID
    )
  )

  val assetsLeftToSpouseAttributesTableId = "partner-overview-table"

  def assetsLeftToSpouseAttributeWithValueAndChange(rowNo: Int,
                                          expectedAttributeName: => String,
                                          expectedAttributeValue: => String,
                                          expectedLinkText: => String) = {
    s"show attribute number ${rowNo + 1} name" in {
      tableCell(doc, assetsLeftToSpouseAttributesTableId, 0, rowNo).text shouldBe expectedAttributeName
    }

    s"show attribute number ${rowNo + 1} value" in {
      tableCell(doc, assetsLeftToSpouseAttributesTableId, 1, rowNo).text shouldBe expectedAttributeValue
    }

    s"show attribute number ${rowNo + 1} change link" in {
      val changeDiv = tableCell(doc, assetsLeftToSpouseAttributesTableId, 2, rowNo)
      val anchor = changeDiv.getElementsByTag("a").first
      getVisibleText(anchor) shouldBe messagesApi(expectedLinkText)
    }
  }

  def getDateDisplayValue(optDate: Option[LocalDate]): String =
    optDate.fold("")(_.toString("d MMMM yyyy"))
}

class PartnerOverviewViewTest extends PartnerOverviewViewBehaviour {
  val partner = PartnerExemption(
    isAssetForDeceasedPartner = Some(true),
    isPartnerHomeInUK = Some(true),
    firstName = Some(CommonBuilder.DefaultFirstName),
    lastName = Some(CommonBuilder.DefaultLastName),
    dateOfBirth = Some(CommonBuilder.DefaultDateOfBirth),
    nino = Some(CommonBuilder.DefaultNino),
    totalAssets = Some(CommonBuilder.DefaultTotalAssets))

  override def view =
    partner_overview(applicationDetails, regDetails).toString()

  "Partner overview view" must {
    behave like nonSubmittablePage()

    behave like assetsLeftToSpouseAttributeWithValueAndChange(0,
      messagesApi("iht.estateReport.exemptions.spouse.assetLeftToSpouse.question", deceasedName),
      messagesApi("iht.yes"),
      messagesApi("iht.change")
    )

    behave like assetsLeftToSpouseAttributeWithValueAndChange(1,
      messagesApi("iht.estateReport.exemptions.partner.homeInUK.question"),
      messagesApi("iht.yes"),
      messagesApi("iht.change")
    )

    behave like assetsLeftToSpouseAttributeWithValueAndChange(2,
      messagesApi("page.iht.application.exemptions.partner.name.title"),
      partner.name.fold("")(identity),
      messagesApi("iht.change")
    )

    behave like assetsLeftToSpouseAttributeWithValueAndChange(3,
      messagesApi("page.iht.application.exemptions.partner.dateOfBirth.question.title"),
      getDateDisplayValue(partner.dateOfBirth),
      messagesApi("iht.change")
    )

    behave like assetsLeftToSpouseAttributeWithValueAndChange(4,
      messagesApi("page.iht.application.exemptions.partner.nino.sectionTitle"),
      CommonHelper.getOrException(partner.nino),
      messagesApi("iht.change")
    )

    behave like assetsLeftToSpouseAttributeWithValueAndChange(5,
      messagesApi("page.iht.application.exemptions.overview.partner.totalAssets.title"),
      "£" + numberWithCommas(CommonHelper.getOrException(partner.totalAssets)),
      messagesApi("iht.change")
    )
  }
}

class PartnerOverviewViewWithNoValuesTest extends PartnerOverviewViewBehaviour {
  val partner = PartnerExemption(
    isAssetForDeceasedPartner = Some(true),
    isPartnerHomeInUK = Some(true),
    firstName = None,
    lastName = None,
    dateOfBirth = None,
    nino = None,
    totalAssets = None)

  override def view =
    partner_overview(applicationDetails, regDetails).toString()

  "Partner overview view where no values entered" must {
    behave like assetsLeftToSpouseAttributeWithValueAndChange(2,
      messagesApi("page.iht.application.exemptions.partner.name.title"),
      CommonBuilder.emptyString,
      messagesApi("site.link.giveName")
    )

    behave like assetsLeftToSpouseAttributeWithValueAndChange(3,
      messagesApi("page.iht.application.exemptions.partner.dateOfBirth.question.title"),
      CommonBuilder.emptyString,
      messagesApi("site.link.giveDate")
    )

    behave like assetsLeftToSpouseAttributeWithValueAndChange(4,
      messagesApi("page.iht.application.exemptions.partner.nino.sectionTitle"),
      CommonBuilder.emptyString,
      messagesApi("site.link.giveDetails")
    )

    behave like assetsLeftToSpouseAttributeWithValueAndChange(5,
      messagesApi("page.iht.application.exemptions.overview.partner.totalAssets.title"),
      CommonBuilder.emptyString,
      messagesApi("site.link.giveValue")
    )
  }
}
