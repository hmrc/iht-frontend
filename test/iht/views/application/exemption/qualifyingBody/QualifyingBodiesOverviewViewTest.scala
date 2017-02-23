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

package iht.views.application.exemption.qualifyingBody

import iht.testhelpers.CommonBuilder
import iht.views.html.application.exemption.qualifyingBody.qualifying_bodies_overview
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}
import play.api.i18n.Messages.Implicits._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

import scala.concurrent.ExecutionContext.Implicits.global

class QualifyingBodiesOverviewViewTest extends GenericNonSubmittablePageBehaviour {
  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()

  def registrationDetails = CommonBuilder.buildRegistrationDetails1

  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def guidanceParagraphs = Set(
    messagesApi("page.iht.application.exemptions.qualifyingBodyOverview.lede"),
    messagesApi("iht.estateReport.exemptions.qualifyingBodies.howFindOutQualifies")
  )

  override def pageTitle = messagesApi("iht.estateReport.exemptions.qualifyingBodies.assetsLeftToQualifyingBodies.title", deceasedName)

  override def browserTitle = messagesApi("page.iht.application.exemptions.qualifyingBodyOverview.browserTitle")

  override def exitComponent = Some(
    ExitComponent(
      iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad(),
      messagesApi("site.link.return.exemptions")
    )
  )

  override def view =
    qualifying_bodies_overview(
      Seq(CommonBuilder.qualifyingBody, CommonBuilder.qualifyingBody2),
      registrationDetails,
      isAssetLeftToQualifyingBody = true
    ).toString()

  val qualifyingBodyTableId = "qualifying_bodies_table"

  def qualifyingBodyWithDeleteAndModify(rowNo: Int, expectedName: String, expectedValue: String) = {
    s"show qualifyingBody number ${rowNo + 1} name" in {
      tableCell(doc, qualifyingBodyTableId, 0, rowNo).ownText shouldBe expectedName
    }

    s"show qualifyingBody number ${rowNo + 1} value" in {
      tableCell(doc, qualifyingBodyTableId, 1, rowNo).text shouldBe expectedValue
    }

    s"show qualifyingBody number ${rowNo + 1} change link" in {
      val div = tableCell(doc, qualifyingBodyTableId, 2, rowNo)
      val anchor = div.getElementsByTag("a").first
      getVisibleText(anchor) shouldBe messagesApi("iht.change")
    }

    s"show qualifyingBody number ${rowNo + 1} delete link" in {
      val div = tableCell(doc, qualifyingBodyTableId, 3, rowNo)
      val anchor = div.getElementsByTag("a").first
      getVisibleText(anchor) shouldBe messagesApi("iht.delete")
    }


  }

  val qualifyingBodyName1 = CommonBuilder.qualifyingBody.map(_.name).fold("")(identity)
  val qualifyingBodyValue1 = CommonBuilder.currencyValue(CommonBuilder.qualifyingBody.map(_.totalValue).fold(BigDecimal(0))(identity))
  val qualifyingBodyName2 = CommonBuilder.qualifyingBody2.map(_.name).fold("")(identity)
  val qualifyingBodyValue2 = CommonBuilder.currencyValue(CommonBuilder.qualifyingBody2.map(_.totalValue).fold(BigDecimal(0))(identity))

  "Qualifying bodies overview view" must {
    behave like nonSubmittablePage()

    behave like link("add-qualifyingBody",
      iht.controllers.application.exemptions.qualifyingBody.routes.QualifyingBodyDetailsOverviewController.onPageLoad().url,
      messagesApi("iht.estateReport.assets.qualifyingBodyAddAnother"))

    "show ownership question" in {
      elementShouldHaveText(doc, "qualifying-bodies-question", messagesApi("page.iht.application.exemptions.qualifyingBodyOverview.question", deceasedName))
    }

    "show ownership question value" in {
      elementShouldHaveText(doc, "qualifying-bodies-value", messagesApi("iht.yes"))
    }

    behave like link("qualifying-bodies-link",
      iht.controllers.application.exemptions.qualifyingBody.routes.AssetsLeftToQualifyingBodyQuestionController.onPageLoad().url,
      messagesApi("iht.change"))

    behave like qualifyingBodyWithDeleteAndModify(0, qualifyingBodyName1, qualifyingBodyValue1)

    behave like qualifyingBodyWithDeleteAndModify(1, qualifyingBodyName2, qualifyingBodyValue2)


//
//    "show you haven't added message when there are no properties" in {
//      val view = properties_overview(List(),
//        Some(Properties(isOwned = Some(true))),
//        registrationDetails).toString()
//      val doc = asDocument(view)
//      doc.getElementById("properties-empty-table-row").text shouldBe
//        messagesApi("page.iht.application.assets.deceased-permanent-home.table.emptyRow.text")
//
//    }
  }
}
