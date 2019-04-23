/*
 * Copyright 2019 HM Revenue & Customs
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

package iht.views.application.assets.trusts


import iht.controllers.application.assets.trusts.routes
import iht.models.application.assets.HeldInTrust
import iht.testhelpers.CommonBuilder
import iht.utils.CommonHelper
import iht.views.helpers.GenericOverviewHelper
import iht.views.html.application.asset.trusts.trusts_overview
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import iht.config.AppConfig

class TrustsOverviewViewTest extends GenericOverviewHelper {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  lazy val valueYes = "Yes"
  lazy val valueNo = "No"
  lazy val changeMsgKey = "iht.change"
  lazy val giveAnAnswerMsgKey= "site.link.giveAnswer"
  lazy val giveAValueMsgKey = "site.link.giveAValue"
  lazy val deceasedBenefitFromTheTrustQuestionId = "benefited-from-trust-block"
  lazy val deceasedBenefitFromTheTrustQuestionMsg = messagesApi("iht.estateReport.assets.trusts.question",deceasedName)
  lazy val deceasedBenefitFromTheTrustQuestionPageUrl = routes.TrustsOwnedQuestionController.onPageLoad.url

  lazy val trustMoreThanOneQuestionId = "more-than-one-trust-block"
  lazy val trustMoreThanOneQuestionMsg = messagesApi("iht.estateReport.assets.trusts.moreThanOne.question", deceasedName)
  lazy val trustMoreThanOneQuestionPageUrl = routes.TrustsMoreThanOneQuestionController.onPageLoad.url

  lazy val trustValueQuestionId = "value-of-trust-block"
  lazy val trustValueQuestionMsg = messagesApi("iht.estateReport.assets.heldInTrust.valueOfTrust", deceasedName)
  lazy val trustValueQuestionPageUrl = routes.TrustsValueController.onPageLoad.url

  def trustsOverviewView(heldInTrust:Option[HeldInTrust]) = {
    implicit val request = createFakeRequest()

    val view = trusts_overview(heldInTrust, regDetails).toString()
    asDocument(view)
  }

  "TrustsOverview view" must {
    "have no message keys in html" in {
      val view = trustsOverviewView(Some(CommonBuilder.buildAssetsHeldInTrust)).toString
      noMessageKeysShouldBePresent(view)
    }

    "have correct title and browser title " in {
      val view = trustsOverviewView(Some(CommonBuilder.buildAssetsHeldInTrust)).toString

      titleShouldBeCorrect(view, messagesApi("iht.estateReport.assets.heldInTrust.title"))
      browserTitleShouldBeCorrect(view, messagesApi("iht.estateReport.assets.heldInTrust.title"))
    }

    "have correct guidance paragraphs" in {
      val view = trustsOverviewView(Some(CommonBuilder.buildAssetsHeldInTrust)).toString
      messagesShouldBePresent(view, messagesApi("iht.estateReport.assets.trusts.benefittedFromHeldInTrust",
                                            deceasedName))
      messagesShouldBePresent(view, messagesApi("iht.estateReport.assets.trusts.needInclusion",
                                              deceasedName))
      messagesShouldBePresent(view, messagesApi("iht.estateReport.assets.heldInTrust.needInclusion",
        deceasedName))
    }

    "have return link with correct text" in  {
      val view = trustsOverviewView(Some(CommonBuilder.buildAssetsHeldInTrust))

      val link = view.getElementById("return-button")
      link.text mustBe messagesApi("page.iht.application.return.to.assetsOf", deceasedName)
      link.attr("href") mustBe iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad.url + "#" + appConfig.AppSectionHeldInTrustID
    }
  }

  "Did the deceased benefit from, or have the right to benefit from, a trust during their lifetime" must {
    "have correct question, link and value" in {
      val view = trustsOverviewView(Some(CommonBuilder.buildAssetsHeldInTrust.copy(isOwned = Some(true))))

      rowShouldBeAnswered(view,
        deceasedBenefitFromTheTrustQuestionId,
        deceasedBenefitFromTheTrustQuestionMsg,
        valueYes,
        changeMsgKey,
        deceasedBenefitFromTheTrustQuestionPageUrl)
    }

  }

 "Unanswered - Trusts more than one and Trust value sections" must {
    "have the correct question, link and value for 'Trusts more than one' section" in {
      val view = trustsOverviewView(Some(CommonBuilder.buildAssetsHeldInTrust.copy(isOwned = Some(true))))

      rowShouldBeUnAnswered(view,
        trustMoreThanOneQuestionId,
        trustMoreThanOneQuestionMsg,
        giveAnAnswerMsgKey,
        trustMoreThanOneQuestionPageUrl
      )
    }

   "have the correct question, link and value for 'Trust value' section" in {
     val view = trustsOverviewView(Some(CommonBuilder.buildAssetsHeldInTrust.copy(isOwned = Some(true))))

     rowShouldBeUnAnswered(view,
       trustValueQuestionId,
       trustValueQuestionMsg,
       giveAValueMsgKey,
       trustValueQuestionPageUrl
     )
    }
  }

  "Answered with Yes - Trusts more than one and Trust value sections" must {
    "have the correct question, link and value for 'Trusts more than one' section" in {
      val view = trustsOverviewView(Some(CommonBuilder.buildAssetsHeldInTrust.copy(
        isOwned = Some(true),isMoreThanOne = Some(true)))
      )

      rowShouldBeAnswered(view,
        trustMoreThanOneQuestionId,
        trustMoreThanOneQuestionMsg,
        valueYes,
        changeMsgKey,
        trustMoreThanOneQuestionPageUrl
      )
    }

    "have the correct question, link and value for 'Trust value' section" in {
      val trustValue = BigDecimal(1000)
      val view = trustsOverviewView(Some(CommonBuilder.buildAssetsHeldInTrust.copy(
        isOwned = Some(true), value = Some(trustValue)
      )))

      rowShouldBeAnswered(view,
        trustValueQuestionId,
        trustValueQuestionMsg,
        "£" + CommonHelper.numberWithCommas(trustValue),
        changeMsgKey,
        trustValueQuestionPageUrl
      )
    }
  }

  "Answered with No - Trusts more than one and Trust value sections" must {
    "have the correct question, link and value for 'Trusts more than one' section" in {
      val view = trustsOverviewView(Some(CommonBuilder.buildAssetsHeldInTrust.copy(
        isOwned = Some(true),isMoreThanOne = Some(false)))
      )

      rowShouldBeAnswered(view,
        trustMoreThanOneQuestionId,
        trustMoreThanOneQuestionMsg,
        valueNo,
        changeMsgKey,
        trustMoreThanOneQuestionPageUrl
      )
    }

    "have the correct question, link and value for 'Trust value' section" in {
      val trustValue = BigDecimal(1000)
      val view = trustsOverviewView(Some(CommonBuilder.buildAssetsHeldInTrust.copy(
        isOwned = Some(true))))

      rowShouldBeAnswered(view,
        trustValueQuestionId,
        trustValueQuestionMsg,
        "",
        giveAValueMsgKey,
        trustValueQuestionPageUrl
      )
    }
  }

}
