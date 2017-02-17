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

package iht.views.application.assets.vehicles

import iht.controllers.application.assets.vehicles.routes._
import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.application.ShareableElementOverviewViewBehaviour
import iht.views.html.application.asset.vehicles.vehicles_overview
import play.api.i18n.Messages.Implicits._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

class VehiclesOverviewViewTest extends ShareableElementOverviewViewBehaviour {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  override def deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  override def pageTitle = messagesApi("iht.estateReport.assets.vehicles")
  override def browserTitle = messagesApi("iht.estateReport.assets.vehicles")
  override def guidanceParagraphs = Set(messagesApi("page.iht.application.assets.vehicles.overview.guidance",
                                                deceasedName, deceasedName))
  override def ownHeadingElementId = "deceased-own-heading"
  override def jointlyOwnedHeadingElementId = "deceased-shared-heading"
  override def urlToOwnPage = VehiclesDeceasedOwnController.onPageLoad().url
  override def urlToJointlyOwnedPage = VehiclesJointlyOwnedController.onPageLoad().url
  override def ownHeaderText = messagesApi("iht.estateReport.assets.vehiclesOwned", deceasedName)
  override def jointlyOwnedHeaderText = messagesApi("page.iht.application.assets.vehicles.overview.joint.title",
                                                 deceasedName)
  override def ownQuestionRowId = "deceased-own-question"
  override def ownQuestionText = messagesApi("iht.estateReport.assets.vehicles.ownName.question", deceasedName)
  override def ownValueRowId = "deceased-own-value"
  override def ownValueText = messagesApi("iht.estateReport.assets.household.deceasedOwnedValue")
  override def jointlyOwnedQuestionRowId = "deceased-shared-question"
  override def jointlyOwnedQuestionText = messagesApi("iht.estateReport.assets.vehicles.jointly.owned.question",
                                                    deceasedName)
  override def jointlyOwnedValueRowId = "deceased-share-value"
  override def jointlyOwnedValueText = messagesApi("iht.estateReport.assets.vehicles.valueOfJointlyOwned")

  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()
  override def viewWithQuestionsAnsweredNo: String = vehicles_overview(dataWithQuestionsAnsweredNo, regDetails).toString
  override def viewWithQuestionsAnsweredYes: String = vehicles_overview(dataWithQuestionsAnsweredYes, regDetails).toString
  override def viewWithQuestionsUnanswered: String = vehicles_overview(None, regDetails).toString
  override def viewWithValues: String = vehicles_overview(dataWithValues, regDetails).toString

  "Vehicles overview view" must {
    behave like overviewPage()
  }

}
