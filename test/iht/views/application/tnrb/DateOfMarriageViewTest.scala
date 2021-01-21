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

package iht.views.application.tnrb

import iht.forms.TnrbForms._
import iht.testhelpers.CommonBuilder
import iht.utils.CommonHelper
import iht.utils.tnrb.TnrbHelper
import iht.views.ViewTestHelper
import iht.views.html.application.tnrb.date_of_marriage


class DateOfMarriageViewTest extends ViewTestHelper with TnrbHelper {
  val tnrbModel = CommonBuilder.buildTnrbEligibility
  val widowCheckModel = CommonBuilder.buildWidowedCheck

  lazy val pageTitle = messagesApi("iht.estateReport.tnrb.dateOfMarriage",
    marriageOrCivilPartnerShipLabel(widowCheckModel))

  lazy val guidanceParagraphs = Set(messagesApi("iht.estateReport.tnrb.dateOfMarriage.hint",
    marriageOrCivilPartnerShipLabel(widowCheckModel), deceasedName, predeceasedName))

  lazy val returnLinkId = "cancel-button"
  lazy val returnLinkText = messagesApi("page.iht.application.tnrb.returnToIncreasingThreshold")
  lazy val returnLinkTargetUrl = iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad()

  lazy val deceasedName = "Xyz zzm"
  lazy val predeceasedName = "Pll Zbb"

  "DateOfMarriageView " must {

    "have no message keys in html" in {
      implicit val request = createFakeRequest()
      val view = date_of_marriage(dateOfMarriageForm, widowCheckModel, deceasedName, predeceasedName, returnLinkTargetUrl).toString
      noMessageKeysShouldBePresent(view)
    }

    "have the correct title" in {
      implicit val request = createFakeRequest()

      val view = date_of_marriage(dateOfMarriageForm, widowCheckModel, deceasedName, predeceasedName, returnLinkTargetUrl).toString

      titleShouldBeCorrect(view, pageTitle)
    }

    "have the correct browser title" in {
      implicit val request = createFakeRequest()

      val view = date_of_marriage(dateOfMarriageForm, widowCheckModel, deceasedName, predeceasedName, returnLinkTargetUrl).toString

      browserTitleShouldBeCorrect(view, pageTitle)
    }

    "show the correct guidance paragraphs" in {
      implicit val request = createFakeRequest()

      val view = date_of_marriage(dateOfMarriageForm, widowCheckModel, deceasedName, predeceasedName, returnLinkTargetUrl).toString

      for (paragraph <- guidanceParagraphs) messagesShouldBePresent(view, paragraph)
    }

    "show the Save and continue button" in {
      implicit val request = createFakeRequest()

      val view = date_of_marriage(dateOfMarriageForm, widowCheckModel, deceasedName, predeceasedName, returnLinkTargetUrl).toString

      val saveAndContinueButton = asDocument(view).getElementById("save-continue")
      saveAndContinueButton.text() mustBe messagesApi("iht.saveAndContinue")
    }

    "show the correct return link with text" in {
      implicit val request = createFakeRequest()

      val view = date_of_marriage(dateOfMarriageForm, widowCheckModel, deceasedName, predeceasedName, CommonHelper.addFragmentIdentifier(returnLinkTargetUrl, Some(appConfig.TnrbSpouseDateOfMarriageID))).toString

      val returnLink = asDocument(view).getElementById(returnLinkId)
      returnLink.attr("href") mustBe returnLinkTargetUrl.url + "#" + appConfig.TnrbSpouseDateOfMarriageID
      returnLink.text() mustBe returnLinkText
    }
  }
}
