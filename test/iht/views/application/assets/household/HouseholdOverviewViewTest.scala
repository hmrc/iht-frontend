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

package iht.views.application.assets.household

import iht.controllers.application.assets.household.routes._
import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.application.ShareableElementOverviewViewBehaviour
import iht.views.html.application.asset.household.household_overview
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

class HouseholdOverviewViewTest extends ViewTestHelper with ShareableElementOverviewViewBehaviour {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  override def deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  override def pageTitle = Messages("iht.estateReport.assets.householdAndPersonalItems.title")
  override def browserTitle = Messages("iht.estateReport.assets.householdAndPersonalItems.title")
  override def guidanceParagraphs = Set(Messages("page.iht.application.assets.household.overview.guidance"))
  override def ownHeadingElementId = "deceased-own-heading"
  override def jointlyOwnedHeadingElementId = "deceased-shared-heading"
  override def urlToOwnPage = HouseholdDeceasedOwnController.onPageLoad().url
  override def urlToJointlyOwnedPage = HouseholdJointlyOwnedController.onPageLoad().url
  override def ownHeaderText = Messages("iht.estateReport.assets.householdAndPersonalItemsOwnedByDeceased.title",
                                        deceasedName)
  override def jointlyOwnedHeaderText = Messages("iht.estateReport.assets.householdAndPersonalItemsJointlyOwned.title",
                                                  deceasedName)
  override def ownQuestionRowId = "deceased-own-question"
  override def ownQuestionText = Messages("iht.estateReport.assets.household.ownName.question", deceasedName)
  override def ownValueRowId = "deceased-own-value"
  override def ownValueText = Messages("iht.estateReport.assets.household.deceasedOwnedValue")
  override def jointlyOwnedQuestionRowId = "deceased-shared-question"
  override def jointlyOwnedQuestionText = Messages("iht.estateReport.assets.household.joint.question", deceasedName)
  override def jointlyOwnedValueRowId = "deceased-share-value"
  override def jointlyOwnedValueText = Messages("page.iht.application.assets.household.overview.joint.value")

  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()
  override def viewWithQuestionsAnsweredNo: String = household_overview(dataWithQuestionsAnsweredNo, regDetails).toString
  override def viewWithQuestionsAnsweredYes: String = household_overview(dataWithQuestionsAnsweredYes, regDetails).toString
  override def viewWithQuestionsUnanswered: String = household_overview(None, regDetails).toString
  override def viewWithValues: String = household_overview(dataWithValues, regDetails).toString

  "Household overview view" must {
    behave like overviewView()
  }

  "Household overview view" when {
    "no questions have been answered" must {

      behave like overviewViewWithQuestionsUnanswered()
    }

    "the questions have been answered as No" must {

      behave like overviewViewWithQuestionsAnsweredNo()
    }

    "the questions have been answered as Yes with no value supplied" must {

      behave like overviewViewWithQuestionsAnsweredYes()
    }

    "the questions have been answered and values given" must {

      behave like overviewViewWithValues()
    }
  }

}
