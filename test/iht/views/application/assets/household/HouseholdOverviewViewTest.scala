/*
 * Copyright 2022 HM Revenue & Customs
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

package iht.views.application.assets.household

import iht.controllers.application.assets.household.routes._
import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.application.ShareableElementOverviewViewBehaviour
import iht.views.html.application.asset.household.household_overview
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest


class HouseholdOverviewViewTest extends ViewTestHelper with ShareableElementOverviewViewBehaviour {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  override def deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  override def pageTitle = messagesApi("iht.estateReport.assets.householdAndPersonalItems.title")
  override def browserTitle = messagesApi("iht.estateReport.assets.householdAndPersonalItems.title")
  override def guidanceParagraphs = Set(messagesApi("page.iht.application.assets.household.overview.guidance"))
  override def ownHeadingElementId = "deceased-own-household"
  override def jointlyOwnedHeadingElementId = "deceased-shared-household"
  override def urlToOwnPage = HouseholdDeceasedOwnController.onPageLoad.url
  override def urlToJointlyOwnedPage = HouseholdJointlyOwnedController.onPageLoad.url
  override def ownHeaderText = messagesApi("iht.estateReport.assets.householdAndPersonalItemsOwnedByDeceased.title",
                                        deceasedName)
  override def jointlyOwnedHeaderText = messagesApi("iht.estateReport.assets.householdAndPersonalItemsJointlyOwned.title",
                                                  deceasedName)
  override def ownQuestionRowId = "deceased-own-household-block"
  override def ownQuestionText = messagesApi("iht.estateReport.assets.household.ownName.question", deceasedName)
  override def ownValueRowId = "deceased-own-value-block"
  override def ownValueText = messagesApi("iht.estateReport.assets.household.deceasedOwnedValue", deceasedName)
  override def jointlyOwnedQuestionRowId = "deceased-shared-household-block"
  override def jointlyOwnedQuestionText = messagesApi("iht.estateReport.assets.household.joint.question", deceasedName)
  override def jointlyOwnedValueRowId = "deceased-shared-value-block"
  override def jointlyOwnedValueText = messagesApi("page.iht.application.assets.household.overview.joint.value", deceasedName)

  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()
  lazy val householdOverviewView: household_overview = app.injector.instanceOf[household_overview]

  override def viewWithQuestionsAnsweredNo: String = householdOverviewView(dataWithQuestionsAnsweredNo, regDetails).toString
  override def viewWithQuestionsAnsweredYes: String = householdOverviewView(dataWithQuestionsAnsweredYes, regDetails).toString
  override def viewWithQuestionsUnanswered: String = householdOverviewView(None, regDetails).toString
  override def viewWithValues: String = householdOverviewView(dataWithValues, regDetails).toString
  override def linkHash = appConfig.AppSectionHouseholdID

  "Household overview view" must {
    behave like overviewPage()
  }

}
