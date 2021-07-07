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

package iht.views.application.assets.money

import iht.controllers.application.assets.money.routes._
import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.application.ShareableElementOverviewViewBehaviour
import iht.views.html.application.asset.money.money_overview
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

class MoneyOverviewViewTest extends ViewTestHelper with ShareableElementOverviewViewBehaviour {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  override def pageTitle = messagesApi("iht.estateReport.assets.money.upperCaseInitial")
  override def browserTitle = messagesApi("iht.estateReport.assets.money.upperCaseInitial")
  override def guidanceParagraphs = Set(messagesApi("page.iht.application.assets.money.overview.description.p1", deceasedName),
                                        messagesApi("page.iht.application.assets.money.overview.description.p2", deceasedName),
                                        messagesApi("page.iht.application.assets.money.overview.description.p3", deceasedName))
  override def ownHeadingElementId = "deceased-own-money"
  override def jointlyOwnedHeadingElementId = "deceased-shared-money"
  override def urlToOwnPage = MoneyDeceasedOwnController.onPageLoad().url
  override def urlToJointlyOwnedPage = MoneyJointlyOwnedController.onPageLoad().url
  override def ownHeaderText = messagesApi("iht.estateReport.assets.moneyOwned", deceasedName)
  override def jointlyOwnedHeaderText = messagesApi("iht.estateReport.assets.money.jointlyOwned")
  override def ownQuestionRowId = "deceased-own-money-block"
  override def ownQuestionText = messagesApi("iht.estateReport.assets.money.ownName.question", deceasedName)
  override def ownValueRowId = "deceased-own-value-block"
  override def ownValueText = messagesApi("iht.estateReport.assets.money.valueOfMoneyOwnedInOwnName", deceasedName)
  override def jointlyOwnedQuestionRowId = "deceased-shared-money-block"
  override def jointlyOwnedQuestionText = messagesApi("page.iht.application.assets.money.jointly.owned.question", deceasedName)
  override def jointlyOwnedValueRowId = "deceased-shared-value-block"
  override def jointlyOwnedValueText = messagesApi("page.iht.application.assets.money.jointly.owned.input.value.label", deceasedName)
  override def linkHash = appConfig.AppSectionMoneyID
  lazy val moneyOverviewView: money_overview = app.injector.instanceOf[money_overview]

  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()
  override def viewWithQuestionsAnsweredNo: String = moneyOverviewView(dataWithQuestionsAnsweredNo, regDetails).toString
  override def viewWithQuestionsAnsweredYes: String = moneyOverviewView(dataWithQuestionsAnsweredYes, regDetails).toString
  override def viewWithQuestionsUnanswered: String = moneyOverviewView(None, regDetails).toString
  override def viewWithValues: String = moneyOverviewView(dataWithValues, regDetails).toString

  "Money overview view" must {
    behave like overviewPage()
  }

}
