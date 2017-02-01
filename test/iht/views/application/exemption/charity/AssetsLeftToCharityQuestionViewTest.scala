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

package iht.views.application.exemption.charity

import iht.forms.ApplicationForms._
import iht.testhelpers.CommonBuilder
import iht.utils.CommonHelper
import iht.views.ViewTestHelper
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import iht.views.html.application.exemption.charity.assets_left_to_charity_question

/**
 * Created by vineet on 29/11/16.
 */
class AssetsLeftToCharityQuestionViewTest extends ViewTestHelper{

  val regDetails = CommonBuilder.buildRegistrationDetails1

  def assetsLeftToPartnerQuestionView() = {
    implicit val request = createFakeRequest()

    val basicExemptionElement = CommonBuilder.buildBasicExemptionElement
    val assetsLeftToPartnerQuestionForm = assetsLeftToCharityQuestionForm.fill(basicExemptionElement)

    val view = assets_left_to_charity_question(assetsLeftToPartnerQuestionForm,regDetails)
    asDocument(view)
  }

  "AssetsLeftToCharityQuestion View" must {
    "have correct title and browser title " in {
      val view = assetsLeftToPartnerQuestionView().toString

      titleShouldBeCorrect(view, Messages("iht.estateReport.exemptions.charities.assetsLeftToACharity.title"))
      browserTitleShouldBeCorrect(view, Messages("page.iht.application.exemptions.assetLeftToCharity.browserTitle"))
    }

    "have 'Save and continue' button" in {
      val view = assetsLeftToPartnerQuestionView()

      val saveAndContinueButton = view.getElementById("save-continue")
      saveAndContinueButton.getElementsByAttributeValueContaining("value", Messages("iht.saveAndContinue"))
    }

    "have the return link with correct text" in {
      val returnLinkLabelMsgKey  = Messages("page.iht.application.return.to.exemptionsOf",
                                            regDetails.deceasedDetails.map(_.name).fold("")(identity))

      val returnLocation = iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad()

      val view = assetsLeftToPartnerQuestionView()

      val returnLink = view.getElementById("cancel-button")
      returnLink.attr("href") shouldBe returnLocation.url
      returnLink.text() shouldBe Messages(returnLinkLabelMsgKey)
    }

    "have the question with the right text" in {
      val view = assetsLeftToPartnerQuestionView()

      messagesShouldBePresent(view.toString, Messages("iht.estateReport.exemptions.charities.assetLeftToCharity.question",
                                                       CommonHelper.getDeceasedNameOrDefaultString(regDetails)))
    }

  }

}
