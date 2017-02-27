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

package iht.views.application.assets.money

import iht.controllers.application.assets.money.routes._
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.application.ShareableElementOverviewViewBehaviour
import iht.views.html.application.asset.money.money_overview
import org.jsoup.nodes.Document
import play.api.i18n.Messages.Implicits._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import iht.constants.Constants._
import iht.constants.IhtProperties._

class MoneyOverviewViewTest extends ViewTestHelper with ShareableElementOverviewViewBehaviour {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  override def pageTitle = messagesApi("iht.estateReport.assets.money.upperCaseInitial")
  override def browserTitle = messagesApi("iht.estateReport.assets.money.upperCaseInitial")
  override def guidanceParagraphs = Set(messagesApi("page.iht.application.assets.money.overview.description.p1", deceasedName),
                                        messagesApi("page.iht.application.assets.money.overview.description.p2", deceasedName),
                                        messagesApi("page.iht.application.assets.money.overview.description.p3", deceasedName))
  override def ownHeadingElementId = "deceased-own-heading"
  override def jointlyOwnedHeadingElementId = "deceased-shared-heading"
  override def urlToOwnPage = MoneyDeceasedOwnController.onPageLoad().url
  override def urlToJointlyOwnedPage = MoneyJointlyOwnedController.onPageLoad().url
  override def ownHeaderText = messagesApi("iht.estateReport.assets.moneyOwned", deceasedName)
  override def jointlyOwnedHeaderText = messagesApi("iht.estateReport.assets.money.jointlyOwned")
  override def ownQuestionRowId = "deceased-own-question"
  override def ownQuestionText = messagesApi("iht.estateReport.assets.money.ownName.question", deceasedName)
  override def ownValueRowId = "deceased-own-value"
  override def ownValueText = messagesApi("iht.estateReport.assets.money.valueOfMoneyOwnedInOwnName")
  override def jointlyOwnedQuestionRowId = "deceased-shared-question"
  override def jointlyOwnedQuestionText = messagesApi("page.iht.application.assets.money.jointly.owned.question", deceasedName)
  override def jointlyOwnedValueRowId = "deceased-shared-value"
  override def jointlyOwnedValueText = messagesApi("page.iht.application.assets.money.jointly.owned.input.value.label")
  override def linkHash = AppSectionMoneyID

  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()
  override def viewWithQuestionsAnsweredNo: String = money_overview(dataWithQuestionsAnsweredNo, regDetails).toString
  override def viewWithQuestionsAnsweredYes: String = money_overview(dataWithQuestionsAnsweredYes, regDetails).toString
  override def viewWithQuestionsUnanswered: String = money_overview(None, regDetails).toString
  override def viewWithValues: String = money_overview(dataWithValues, regDetails).toString

  "Money overview view" must {
    behave like overviewPage()
  }

}
