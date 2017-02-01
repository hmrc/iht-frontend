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
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class MoneyOverviewViewTest extends ViewTestHelper with ShareableElementOverviewViewBehaviour {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  override def pageTitle = Messages("iht.estateReport.assets.money.upperCaseInitial")
  override def browserTitle = Messages("iht.estateReport.assets.money.upperCaseInitial")
  override def guidanceParagraphs = Set(Messages("page.iht.application.assets.money.overview.description.p1", deceasedName),
                                        Messages("page.iht.application.assets.money.overview.description.p2", deceasedName),
                                        Messages("page.iht.application.assets.money.overview.description.p3", deceasedName))
  override def ownHeadingElementId = "deceased-own-heading"
  override def jointlyOwnedHeadingElementId = "deceased-shared-heading"
  override def urlToOwnPage = MoneyDeceasedOwnController.onPageLoad().url
  override def urlToJointlyOwnedPage = MoneyJointlyOwnedController.onPageLoad().url
  override def ownHeaderText = Messages("iht.estateReport.assets.moneyOwned", deceasedName)
  override def jointlyOwnedHeaderText = Messages("iht.estateReport.assets.money.jointlyOwned")
  override def ownQuestionRowId = "deceased-own-question"
  override def ownQuestionText = Messages("iht.estateReport.assets.money.ownName.question", deceasedName)
  override def ownValueRowId = "deceased-own-value"
  override def ownValueText = Messages("iht.estateReport.assets.money.valueOfMoneyOwnedInOwnName")
  override def jointlyOwnedQuestionRowId = "deceased-shared-question"
  override def jointlyOwnedQuestionText = Messages("page.iht.application.assets.money.jointly.owned.question", deceasedName)
  override def jointlyOwnedValueRowId = "deceased-shared-value"
  override def jointlyOwnedValueText = Messages("page.iht.application.assets.money.jointly.owned.input.value.label")

  "Money overview view" must {
    behave like overviewView()
  }

  "Money overview view" when {
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

  override def fixture(data: Option[ShareableBasicEstateElement]) = new {
    implicit val request = createFakeRequest()
    val view = money_overview(data, regDetails).toString
    val doc: Document = asDocument(view)
  }
}
