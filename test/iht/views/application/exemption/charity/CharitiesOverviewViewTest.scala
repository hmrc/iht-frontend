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

import iht.testhelpers.CommonBuilder
import iht.utils.CommonHelper
import iht.utils.OverviewHelper.Section
import iht.views.ViewTestHelper
import play.api.i18n.Messages.Implicits._
import iht.views.html.application.exemption.charity.charities_overview

//TODO Need to add few more tests to write the tests for correct values

class CharitiesOverviewViewTest extends ViewTestHelper {

  lazy val ihtRef = "ABC123"
  lazy val regDetails = CommonBuilder.buildRegistrationDetails1.copy(ihtReference = Some(ihtRef))
  lazy val appDetails = CommonBuilder.buildApplicationDetails
  lazy val exemptionsOverviewPageUrl = iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad()
  lazy val  charityDetailsPageUrl= iht.controllers.application.exemptions.charity.routes.CharityDetailsOverviewController.onPageLoad()

  def charitiesOverviewView() = {
    implicit val request = createFakeRequest()
    
    val view = charities_overview(Nil, regDetails, true).toString()
    asDocument(view)
  }

  "CharitiesOverview view" must {

    "have correct title and browser title " in {
      val view = charitiesOverviewView().toString

      titleShouldBeCorrect(view, messagesApi("iht.estateReport.exemptions.charities.assetsLeftToCharities.title"))
      browserTitleShouldBeCorrect(view, messagesApi("iht.estateReport.exemptions.charities.assetsLeftToCharities.title"))
    }

    "have correct questions" in {
      val view = charitiesOverviewView()
      messagesShouldBePresent(view.toString, messagesApi("iht.estateReport.exemptions.charities.assetLeftToCharity.question",
                                                      CommonHelper.getDeceasedNameOrDefaultString(regDetails)))
    }

    "have Add a charity link with correct target" in {
      val view = charitiesOverviewView()

      val returnLink = view.getElementById("add-charity")
      returnLink.attr("href") shouldBe charityDetailsPageUrl.url
      returnLink.text() shouldBe messagesApi("page.iht.application.exemptions.assetLeftToCharity.addCharity")
    }

    "show no charities added message when the is no charity present" in {
      val view = charitiesOverviewView()

      messagesShouldBePresent(view.toString, messagesApi("page.iht.application.exemptions.charityOverview.noCharities.text"))
    }

    "have the return link with correct text" in {
      val view = charitiesOverviewView()

      val returnLink = view.getElementById("return-button")
      returnLink.attr("href") shouldBe exemptionsOverviewPageUrl.url
      returnLink.text() shouldBe messagesApi("page.iht.application.return.to.exemptionsOf",
                                          CommonHelper.getOrException(regDetails.deceasedDetails.map(_.name)))
    }

  }

}
