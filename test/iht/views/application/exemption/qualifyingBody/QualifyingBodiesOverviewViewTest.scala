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

package iht.views.application.exemption.qualifyingBody

import iht.testhelpers.CommonBuilder
import iht.views.html.application.exemption.qualifyingBody.qualifying_bodies_overview
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}
import play.api.i18n.Messages.Implicits._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import iht.testhelpers.TestHelper._

import scala.concurrent.ExecutionContext.Implicits.global

trait QualifyingBodiesOverviewViewBehaviour extends GenericNonSubmittablePageBehaviour {
  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()

  def registrationDetails = CommonBuilder.buildRegistrationDetails1

  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def guidanceParagraphs = Set(
    messagesApi(""),
    messagesApi("")
  )

  override def pageTitle = messagesApi("iht.estateReport.exemptions.qualifyingBodies.assetsLeftToQualifyingBodies.title", deceasedName)

  override def browserTitle = messagesApi("page.iht.application.exemptions.qualifyingBodyOverview.browserTitle")

  override def exitComponent = Some(
    ExitComponent(
      iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad(),
      messagesApi("site.link.return.exemptions"),
      ExemptionsOtherID
    )
  )
}

class QualifyingBodiesOverviewViewTest extends QualifyingBodiesOverviewViewBehaviour {
  val qualifyingBodyName1 = CommonBuilder.qualifyingBody.map(_.name).fold("")(identity)
  val qualifyingBodyValue1 = CommonBuilder.currencyValue(CommonBuilder.qualifyingBody.map(_.totalValue).fold(BigDecimal(0))(identity))
  val qualifyingBodyName2 = CommonBuilder.qualifyingBody2.map(_.name).fold("")(identity)
  val qualifyingBodyValue2 = CommonBuilder.currencyValue(CommonBuilder.qualifyingBody2.map(_.totalValue).fold(BigDecimal(0))(identity))

  val qualifyingBodyTableId = "qualifying_bodies_table"

  override def view =
    qualifying_bodies_overview(
      Seq(CommonBuilder.qualifyingBody, CommonBuilder.qualifyingBody2),
      registrationDetails,
      isAssetLeftToQualifyingBody = true
    ).toString()

  def qualifyingBodyWithDeleteAndModify(rowNo: Int, expectedName: String, expectedValue: String) = {
    s"show qualifyingBody number ${rowNo + 1} name" in {
      tableCell(doc, qualifyingBodyTableId, 0, rowNo).ownText shouldBe expectedName
    }

    s"show qualifyingBody number ${rowNo + 1} value" in {
      tableCell(doc, qualifyingBodyTableId, 1, rowNo).text shouldBe expectedValue
    }

    s"show qualifyingBody number ${rowNo + 1} delete link" in {
      val div = tableCell(doc, qualifyingBodyTableId, 2, rowNo)
      val anchor = div.getElementsByTag("a").first
      getVisibleText(anchor) shouldBe messagesApi("iht.delete")
    }

    s"show qualifyingBody number ${rowNo + 1} change link" in {
      val div = tableCell(doc, qualifyingBodyTableId, 3, rowNo)
      val anchor = div.getElementsByTag("a").first
      getVisibleText(anchor) shouldBe messagesApi("iht.change")
    }

  }

  "Qualifying bodies overview view" must {
    behave like nonSubmittablePage()

    behave like link(ExemptionsOtherAddID,
      iht.controllers.application.exemptions.qualifyingBody.routes.QualifyingBodyDetailsOverviewController.onPageLoad().url,
      messagesApi("iht.estateReport.assets.qualifyingBodyAddAnother"))

    "show ownership question" in {
      elementShouldHaveText(doc, ExemptionsOtherAssetsID + "-question", messagesApi("page.iht.application.exemptions.qualifyingBodyOverview.question", deceasedName))
    }

    "show ownership question value" in {
      elementShouldHaveText(doc, ExemptionsOtherAssetsID + "-question-value", messagesApi("iht.yes"))
    }

    behave like link(ExemptionsOtherAssetsID,
      iht.controllers.application.exemptions.qualifyingBody.routes.AssetsLeftToQualifyingBodyQuestionController.onPageLoad().url,
      messagesApi("iht.change"))

    behave like qualifyingBodyWithDeleteAndModify(0, qualifyingBodyName1, qualifyingBodyValue1)

    behave like qualifyingBodyWithDeleteAndModify(1, qualifyingBodyName2, qualifyingBodyValue2)
  }
}

class QualifyingBodiesOverviewViewWithNoBodiesTest extends QualifyingBodiesOverviewViewBehaviour {
  override def view =
    qualifying_bodies_overview(
      Seq.empty,
      registrationDetails,
      isAssetLeftToQualifyingBody = true
    ).toString()

  "Qualifying bodies overview view with no qualifying bodies" must {
    behave like link(ExemptionsOtherAddID,
      iht.controllers.application.exemptions.qualifyingBody.routes.QualifyingBodyDetailsOverviewController.onPageLoad().url,
      messagesApi("iht.estateReport.assets.qualifyingBodyAdd"))
  }
}
