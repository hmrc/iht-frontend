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

import iht.testhelpers.CommonBuilder
import iht.utils.CommonHelper
import iht.utils.OverviewHelper.Section
import iht.views.ViewTestHelper
import play.api.i18n.Messages.Implicits._
import iht.views.html.application.exemption.partner.partner_overview

//TODO Need to add few more tests to write the tests for correct values


class CharityDetailsOverviewViewTestPartnerOverviewViewTest extends ViewTestHelper {

  lazy val ihtRef = "ABC123"
  lazy val regDetails = CommonBuilder.buildRegistrationDetails1.copy(ihtReference = Some(ihtRef))
  lazy val appDetails = CommonBuilder.buildApplicationDetails
  lazy val exemptionsOverviewPageUrl = iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad()

  def partnerOverviewView(sectionsToDisplay: Seq[Section] = Nil) = {
    implicit val request = createFakeRequest()
    
    val view = partner_overview(appDetails, regDetails).toString()
    asDocument(view)
  }

  "PartnerOverview view" must {

    "have correct title and browser title " in {
      val view = partnerOverviewView().toString

      titleShouldBeCorrect(view, messagesApi("iht.estateReport.exemptions.partner.assetsLeftToSpouse.title"))
      browserTitleShouldBeCorrect(view, messagesApi("page.iht.application.exemptions.partner.overview.browserTitle"))
    }

    "have correct questions" in {
      val view = partnerOverviewView()
      messagesShouldBePresent(view.toString, messagesApi("iht.estateReport.exemptions.spouse.assetLeftToSpouse.question",
                                                      CommonHelper.getDeceasedNameOrDefaultString(regDetails)))
      messagesShouldBePresent(view.toString, messagesApi("iht.estateReport.exemptions.partner.homeInUK.question"))
      messagesShouldBePresent(view.toString, messagesApi("page.iht.application.exemptions.overview.partner.name.title"))
      messagesShouldBePresent(view.toString, messagesApi("page.iht.application.exemptions.overview.partner.dob.title"))
      messagesShouldBePresent(view.toString, messagesApi("page.iht.application.exemptions.overview.partner.nino.title"))
      messagesShouldBePresent(view.toString, messagesApi("page.iht.application.exemptions.overview.partner.totalAssets.title"))
    }

    "have the return link with correct text" in {
      val view = partnerOverviewView()

      val returnLink = view.getElementById("return-button")
      returnLink.attr("href") shouldBe exemptionsOverviewPageUrl.url
      returnLink.text() shouldBe messagesApi("page.iht.application.return.to.exemptionsOf",
                                          CommonHelper.getOrException(regDetails.deceasedDetails.map(_.name)))
    }

  }

}
