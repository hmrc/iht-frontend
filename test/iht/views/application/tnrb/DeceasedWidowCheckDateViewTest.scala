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

package iht.views.application.tnrb

import iht.forms.TnrbForms._
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils.CommonHelper
import iht.utils.tnrb.TnrbHelper
import iht.views.ViewTestHelper
import play.api.i18n.Messages.Implicits._
import iht.views.html.application.tnrb.deceased_widow_check_date

class DeceasedWidowCheckDateViewTest extends ViewTestHelper {

  val ihtReference = Some("ABC1A1A1A")
  val deceasedDetails = CommonBuilder.buildDeceasedDetails
  val regDetails = CommonBuilder.buildRegistrationDetails.copy(ihtReference = ihtReference,
    deceasedDetails = Some(deceasedDetails.copy(maritalStatus = Some(TestHelper.MaritalStatusMarried))),
    deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath))

  val tnrbModel = CommonBuilder.buildTnrbEligibility
  val widowCheckModel = CommonBuilder.buildWidowedCheck


  lazy val pageTitle = messagesApi("page.iht.application.tnrbEligibilty.overview.partner.dod.question",
                              TnrbHelper.spouseOrCivilPartnerLabel(tnrbModel, widowCheckModel,
                                   messagesApi("page.iht.application.tnrbEligibilty.partner.additional.label.the.deceased",
                                               CommonHelper.getDeceasedNameOrDefaultString(regDetails))))


  lazy val browserTitle = messagesApi("iht.estateReport.tnrb.increasingIHTThreshold")
  lazy val guidanceParagraphs = Set(messagesApi("iht.dateExample2"))

  lazy val returnLinkId = "cancel-button"
  lazy val returnLinkText = messagesApi("page.iht.application.tnrb.returnToIncreasingThreshold")
  lazy val returnLinkTargetUrl = iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad()

  "DeceasedWidowCheckDateView " must {

    "have no message keys in html" in {
      implicit val request = createFakeRequest()

      val view = deceased_widow_check_date(deceasedWidowCheckQuestionForm,
        widowCheckModel, tnrbModel, regDetails,
        returnLinkTargetUrl, returnLinkText).toString
      noMessageKeysShouldBePresent(view)
    }

    "have the correct title" in {
      implicit val request = createFakeRequest()

      val view = deceased_widow_check_date(deceasedWidowCheckQuestionForm,
        widowCheckModel, tnrbModel, regDetails,
        returnLinkTargetUrl, returnLinkText).toString

      titleShouldBeCorrect(view, pageTitle)
    }

    "have the correct browser title" in {
      implicit val request = createFakeRequest()

      val view = deceased_widow_check_date(deceasedWidowCheckQuestionForm,
        widowCheckModel, tnrbModel, regDetails,
        returnLinkTargetUrl, returnLinkText).toString

      browserTitleShouldBeCorrect(view, browserTitle)
    }

    "show the correct guidance paragraphs" in {
      implicit val request = createFakeRequest()

      val view = deceased_widow_check_date(deceasedWidowCheckQuestionForm,
        widowCheckModel, tnrbModel, regDetails,
        returnLinkTargetUrl, returnLinkText).toString

      for (paragraph <- guidanceParagraphs) messagesShouldBePresent(view, paragraph)
    }

    "show the Save and continue button" in {
      implicit val request = createFakeRequest()

      val view = deceased_widow_check_date(deceasedWidowCheckQuestionForm,
        widowCheckModel, tnrbModel, regDetails,
        returnLinkTargetUrl, returnLinkText).toString

      val saveAndContinueButton = asDocument(view).getElementById("save-continue")
      saveAndContinueButton.text() shouldBe messagesApi("iht.saveAndContinue")
    }

    "show the correct return link with text" in {
      implicit val request = createFakeRequest()

      val view = deceased_widow_check_date(deceasedWidowCheckQuestionForm,
        widowCheckModel, tnrbModel, regDetails,
        returnLinkTargetUrl, returnLinkText).toString

      val returnLink = asDocument(view).getElementById(returnLinkId)
      returnLink.attr("href") shouldBe returnLinkTargetUrl.url
      returnLink.text() shouldBe returnLinkText
    }
  }

}
