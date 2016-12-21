/*
 * Copyright 2016 HM Revenue & Customs
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

package iht.views.application.assets.pensions

import iht.controllers.application.assets.pensions.routes
import iht.models.application.assets.PrivatePension
import iht.testhelpers.CommonBuilder
import iht.utils.CommonHelper
import iht.views.ViewTestHelper
import iht.views.helpers.GenericOverviewHelper._
import iht.views.html.application.asset.pensions.pensions_overview
import play.api.i18n.Messages

class PensionsOverviewViewTest extends ViewTestHelper {

  lazy val valueYes = "Yes"
  lazy val valueNo = "No"
  lazy val changeMsgKey = "iht.change"
  lazy val giveAnAnswerMsgKey= "site.link.giveAnswer"
  lazy val giveAValueMsgKey = "site.link.giveAValue"
  lazy val pensionOwnedByDeceasedQuestionId = "pensions-question"
  lazy val pensionOwnedByDeceasedQuestionMsgKey = "page.iht.application.assets.pensions.question"
  lazy val pensionOwnedByDeceasedQuestionPageUrl = routes.PensionsOwnedQuestionController.onPageLoad.url

  lazy val pensionMoreThanOneQuestionId = "pensions-more-than-one-question"
  lazy val pensionMoreThanOneQuestionMsgKey = "page.iht.application.assets.pensions.changed.title"
  lazy val pensionMoreThanOneQuestionPageUrl = routes.PensionsChangedQuestionController.onPageLoad.url

  lazy val pensionValueQuestionId = "pensions-value"
  lazy val pensionValueQuestionMsgKey = "iht.estateReport.assets.pensions.valueOfRemainingPaymentsBeingPaid"
  lazy val pensionValueQuestionPageUrl = routes.PensionsValueController.onPageLoad.url

  def pensionOverviewView(pensions:Option[PrivatePension]) = {
    implicit val request = createFakeRequest()

    lazy val regDetails = CommonBuilder.buildRegistrationDetails1
    val view = pensions_overview(pensions, regDetails).toString()
    asDocument(view)
  }

  "PensionsOverview view" must {

    "have correct title and browser title " in {
      val view = pensionOverviewView(Some(CommonBuilder.buildPrivatePensionExtended)).toString

      titleShouldBeCorrect(view, Messages("iht.estateReport.assets.privatePensions"))
      browserTitleShouldBeCorrect(view, Messages("iht.estateReport.assets.privatePensions"))
    }

    "have correct guidance paragraphs" in {
      val view = pensionOverviewView(Some(CommonBuilder.buildPrivatePensionExtended)).toString
      messagesShouldBePresent(view, Messages("page.iht.application.assets.pensions.overview.description.p1"))
      messagesShouldBePresent(view, Messages("page.iht.application.assets.pensions.overview.description.p2"))
    }

  }

  "Did the deceased have any private pensions section" must {
    "have correct question, link and value" in {
      val view = pensionOverviewView(Some(CommonBuilder.buildPrivatePensionExtended.copy(isOwned = Some(true))))

      rowShouldBeAnswered(view,
        pensionOwnedByDeceasedQuestionId,
        pensionOwnedByDeceasedQuestionMsgKey,
        valueYes,
        changeMsgKey,
        pensionOwnedByDeceasedQuestionPageUrl)
    }

  }

 "Unanswered - Private pensions change in 2 years and Pension value sections" must {

    "have the correct question, link and value for 'Private pensions change in 2 years' section" in {
      val view = pensionOverviewView(Some(CommonBuilder.buildPrivatePensionExtended.copy(isOwned = Some(true))))

      rowShouldBeUnAnswered(view,
        pensionMoreThanOneQuestionId,
        pensionMoreThanOneQuestionMsgKey,
        giveAnAnswerMsgKey,
        pensionMoreThanOneQuestionPageUrl
      )
    }

   "have the correct question, link and value for 'Pension value' section" in {
     val view = pensionOverviewView(Some(CommonBuilder.buildPrivatePensionExtended.copy(isOwned = Some(true))))

     rowShouldBeUnAnswered(view,
       pensionValueQuestionId,
       pensionValueQuestionMsgKey,
       giveAValueMsgKey,
       pensionValueQuestionPageUrl
     )
    }
  }

  "Answered with Yes - Private pensions change in 2 years and Pension value sections" must {

    "have the correct question, link and value for 'Private pensions change in 2 years' section" in {
      val view = pensionOverviewView(Some(CommonBuilder.buildPrivatePensionExtended.copy(
        isOwned = Some(true),isChanged = Some(true)))
      )

      rowShouldBeAnswered(view,
        pensionMoreThanOneQuestionId,
        pensionMoreThanOneQuestionMsgKey,
        valueYes,
        changeMsgKey,
        pensionMoreThanOneQuestionPageUrl
      )
    }

    "have the correct question, link and value for 'Pension value' section" in {
      val pensionValue = BigDecimal(1000)
      val view = pensionOverviewView(Some(CommonBuilder.buildPrivatePensionExtended.copy(
        isOwned = Some(true),value = Some(pensionValue)
      )))

      rowShouldBeAnswered(view,
        pensionValueQuestionId,
        pensionValueQuestionMsgKey,
        "£" + CommonHelper.numberWithCommas(pensionValue),
        changeMsgKey,
        pensionValueQuestionPageUrl
      )
    }
  }

  "Answered with No - Private pensions change in 2 years and Pension value sections" must {

    "have the correct question, link and value for 'Private pensions change in 2 years' section" in {
      val view = pensionOverviewView(Some(CommonBuilder.buildPrivatePensionExtended.copy(
        isOwned = Some(true),isChanged = Some(false)))
      )

      rowShouldBeAnswered(view,
        pensionMoreThanOneQuestionId,
        pensionMoreThanOneQuestionMsgKey,
        valueNo,
        changeMsgKey,
        pensionMoreThanOneQuestionPageUrl
      )
    }

    "have the correct question, link and value for 'Pension value' section" in {
      val pensionValue = BigDecimal(1000)
      val view = pensionOverviewView(Some(CommonBuilder.buildPrivatePensionExtended.copy(
        isOwned = Some(true))))

      rowShouldBeAnswered(view,
        pensionValueQuestionId,
        pensionValueQuestionMsgKey,
        "",
        giveAValueMsgKey,
        pensionValueQuestionPageUrl
      )
    }
  }

}
