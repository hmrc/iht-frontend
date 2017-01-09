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

package iht.views.application.exemption.partner

import iht.forms.ApplicationForms._
import iht.testhelpers.CommonBuilder
import iht.utils.CommonHelper
import iht.views.ViewTestHelper
import iht.views.html.application.exemption.partner.assets_left_to_partner_question
import play.api.i18n.Messages

/**
 * Created by vineet on 29/11/16.
 */
class AssetsLeftToPartnerQuestionViewTest extends ViewTestHelper{

  val regDetails = CommonBuilder.buildRegistrationDetails1
  val returnLinkLabelMsgKey = "iht.estateReport.exemptions.partner.returnToAssetsLeftToSpouse"
  val returnLocation = iht.controllers.application.exemptions.partner.routes.PartnerOverviewController.onPageLoad()

  def assetsLeftToPartnerQuestionView() = {
    implicit val request = createFakeRequest()

    val partnerExemption = CommonBuilder.buildPartnerExemption
    val assetsLeftToPartnerQuestionForm = assetsLeftToSpouseQuestionForm.fill(partnerExemption)

    val view = assets_left_to_partner_question(assetsLeftToPartnerQuestionForm,
      regDetails,
      Messages("iht.estateReport.exemptions.partner.returnToAssetsLeftToSpouse"),
      returnLocation)

    asDocument(view)
  }

  "AssetsLeftToPartnerQuestion View" must {
    "have correct title and browser title " in {
      val view = assetsLeftToPartnerQuestionView().toString

      titleShouldBeCorrect(view, Messages("iht.estateReport.exemptions.spouse.assetLeftToSpouse.question",
                                          CommonHelper.getDeceasedNameOrDefaultString(regDetails)))
      browserTitleShouldBeCorrect(view, Messages("page.iht.application.exemptions.assetLeftToPartner.browserTitle"))
    }

    "have 'Save and continue' button" in {
      val view = assetsLeftToPartnerQuestionView()

      val saveAndContinueButton = view.getElementById("save-continue")
      saveAndContinueButton.getElementsByAttributeValueContaining("value", Messages("iht.saveAndContinue"))
    }

    "have the return link with correct text" in {
      val view = assetsLeftToPartnerQuestionView()

      val returnLink = view.getElementById("cancel-button")
      returnLink.attr("href") shouldBe returnLocation.url
      returnLink.text() shouldBe Messages(returnLinkLabelMsgKey)
    }

    "have the question with the right text" in {
      val view = assetsLeftToPartnerQuestionView()

      messagesShouldBePresent(view.toString, Messages("iht.estateReport.exemptions.spouse.assetLeftToSpouse.question",
                                                       CommonHelper.getDeceasedNameOrDefaultString(regDetails)))
    }

  }

}
