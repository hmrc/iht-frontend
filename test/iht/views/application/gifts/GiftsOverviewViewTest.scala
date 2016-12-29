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

package iht.views.application.gifts

import iht.testhelpers.CommonBuilder
import iht.utils.CommonHelper
import iht.utils.OverviewHelper.Section
import iht.views.ViewTestHelper
import iht.views.html.application.gift.gifts_overview
import play.api.i18n.Messages

//TODO Need to add tests cases to check all the Gifts question.Can be done once we start working on new Acceptance Test framework


class GiftsOverviewViewTest extends ViewTestHelper {

  lazy val ihtRef = "ABC123"
  lazy val regDetails = CommonBuilder.buildRegistrationDetails1.copy(ihtReference = Some(ihtRef))
  lazy val whatAGiftPageUrl = iht.controllers.application.gifts.guidance.routes.WhatIsAGiftController.onPageLoad()
  lazy val estateOverviewPageUrl = iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(ihtRef)

  def giftsOverviewView(sectionsToDisplay: Seq[Section] = Nil) = {
    implicit val request = createFakeRequest()
    
    val view = gifts_overview(regDetails,
                              sectionsToDisplay,
                              Some(estateOverviewPageUrl),
                              "iht.estateReport.returnToEstateOverview").toString()
    asDocument(view)
  }

  "GiftsOverview view" must {

    "have correct title and browser title " in {
      val view = giftsOverviewView().toString

      titleShouldBeCorrect(view, Messages("iht.estateReport.gifts.givenAwayBy",
                                          CommonHelper.getOrException(regDetails.deceasedDetails.map(_.name))))
      browserTitleShouldBeCorrect(view, Messages("iht.estateReport.gifts.givenAway.title"))
    }

    "have correct guidance paragraphs" in {
      val view = giftsOverviewView()
      messagesShouldBePresent(view.toString, Messages("page.iht.application.gifts.overview.guidance1",
                                                      CommonHelper.getDeceasedNameOrDefaultString(regDetails),
                                                      CommonHelper.getDeceasedNameOrDefaultString(regDetails)))
      assertNotContainsText(view, Messages("iht.estateReport.saved.estate"))
    }

    "have the 'What a gift' link with correct text" in {
      val view = giftsOverviewView()

      val returnLink = view.getElementById("whatIsAGift")
      returnLink.attr("href") shouldBe whatAGiftPageUrl.url
      returnLink.text() shouldBe Messages("page.iht.application.gifts.guidance.whatsAGift.title")
    }

    "have the return link with correct text" in {
      val view = giftsOverviewView()

      val returnLink = view.getElementById("return-button")
      returnLink.attr("href") shouldBe estateOverviewPageUrl.url
      returnLink.text() shouldBe Messages("iht.estateReport.returnToEstateOverview")
    }

  }

}
