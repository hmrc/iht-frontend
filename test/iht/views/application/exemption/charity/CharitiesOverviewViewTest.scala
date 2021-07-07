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

package iht.views.application.exemption.charity

import iht.testhelpers.CommonBuilder
import iht.testhelpers.TestHelper._
import iht.utils.DeceasedInfoHelper
import iht.views.html.application.exemption.charity.charities_overview
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import scala.concurrent.ExecutionContext.Implicits.global

trait CharitiesOverviewViewBehaviour extends GenericNonSubmittablePageBehaviour {
  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()

  def registrationDetails = CommonBuilder.buildRegistrationDetails1

  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)
  lazy val charitiesOverviewView: charities_overview = app.injector.instanceOf[charities_overview]

  override def guidanceParagraphs = Set(
    messagesApi("iht.estateReport.exemptions.charities.assetLeftToCharity.question",
      DeceasedInfoHelper.getDeceasedNameOrDefaultString(registrationDetails))
  )

  override def pageTitle = messagesApi("iht.estateReport.exemptions.charities.assetsLeftToCharities.title", deceasedName)

  override def browserTitle = messagesApi("iht.estateReport.exemptions.charities.assetsLeftToCharities.title")

  override def exitComponent = Some(
    ExitComponent(
      iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad(),
      messagesApi("page.iht.application.return.to.exemptionsOf", deceasedName),
      ExemptionsCharityID
    )
  )
}

class CharitiesOverviewViewTest extends CharitiesOverviewViewBehaviour {
  val charityName1 = await(CommonBuilder.charity.map(_.name)).fold("")(identity)
  val charityValue1 = CommonBuilder.currencyValue(CommonBuilder.charity.totalValue.fold(BigDecimal(0))(identity))
  val charityName2 = await(CommonBuilder.charity2.map(_.name)).fold("")(identity)
  val charityValue2 = CommonBuilder.currencyValue(CommonBuilder.charity2.totalValue.fold(BigDecimal(0))(identity))

  val charityTableId = "charities_table"

  override def view =
    charitiesOverviewView(
      Seq(CommonBuilder.charity, CommonBuilder.charity2),
      registrationDetails,
      isAssetLeftToCharity = true
    ).toString()

  def charityWithDeleteAndModify(rowNo: Int, expectedName: String, expectedValue: String) = {
    s"show charity number ${rowNo + 1} name" in {
      tableCell(doc, charityTableId, 0, rowNo).ownText mustBe expectedName
    }

    s"show charity number ${rowNo + 1} value" in {
      tableCell(doc, charityTableId, 1, rowNo).text mustBe expectedValue
    }

    s"show charity number ${rowNo + 1} change link" in {
      val div = tableCell(doc, charityTableId, 2, rowNo)
      val anchor = div.getElementsByTag("a").first
      getVisibleText(anchor) mustBe messagesApi("iht.delete")
    }

    s"show charity number ${rowNo + 1} delete link" in {
      val div = tableCell(doc, charityTableId, 3, rowNo)
      val anchor = div.getElementsByTag("a").first
      getVisibleText(anchor) mustBe messagesApi("iht.change")
    }
  }

  "Charities overview view" must {
    behave like nonSubmittablePage()

    behave like link(ExemptionsCharitiesAddID,
      iht.controllers.application.exemptions.charity.routes.CharityDetailsOverviewController.onPageLoad().url,
      messagesApi("page.iht.application.exemptions.assetLeftToCharity.addCharity"))

    "show ownership question" in {
      elementShouldHaveText(doc, ExemptionsCharitiesAssetsID + "-question", messagesApi("iht.estateReport.exemptions.charities.assetLeftToCharity.question", deceasedName))
    }

    "show ownership question value" in {
      elementShouldHaveText(doc, ExemptionsCharitiesAssetsID + "-question-value", messagesApi("iht.yes"))
    }

    behave like link(ExemptionsCharitiesAssetsID,
      iht.controllers.application.exemptions.charity.routes.AssetsLeftToCharityQuestionController.onPageLoad().url,
      messagesApi("iht.change"))

    behave like charityWithDeleteAndModify(0, charityName1, charityValue1)

    behave like charityWithDeleteAndModify(1, charityName2, charityValue2)
  }
}

class CharitiesOverviewViewWithNoBodiesTest extends CharitiesOverviewViewBehaviour {
  override def view =
    charitiesOverviewView(
      Seq.empty,
      registrationDetails,
      isAssetLeftToCharity = true
    ).toString()

  "Charities overview view with no qualifying bodies" must {
    behave like link("add-charity",
      iht.controllers.application.exemptions.charity.routes.CharityDetailsOverviewController.onPageLoad().url,
      messagesApi("page.iht.application.exemptions.assetLeftToCharity.addCharity"))
  }
}
