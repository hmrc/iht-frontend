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

package iht.views.application.gifts

import iht.forms.ApplicationForms._
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils.CommonHelper
import iht.views.ViewTestHelper
import play.api.i18n.Messages
import play.api.test.Helpers._
import iht.views.html.application.gift.seven_years_to_trust

/**
  * Created by vineet on 15/11/16.
  */
class SevenYearsToTrustViewTest extends ViewTestHelper{

  val ihtReference = Some("ABC1A1A1A")
  val regDetails = CommonBuilder.buildRegistrationDetails.copy(ihtReference = ihtReference,
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(
      maritalStatus = Some(TestHelper.MaritalStatusMarried))),
    deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath))

  val allGifts = CommonBuilder.buildAllGifts.copy(isReservation = Some(false))
  val fakeRequest = createFakeRequest(isAuthorised = false)

  "SevenYearsToTrust Page" must {

    "contain the title, browser title and save and continue button " in {
      val view = seven_years_to_trust(giftSevenYearsToTrustForm, regDetails)(fakeRequest)
      val viewAsString = contentAsString(view)
      val doc = asDocument(viewAsString)

      titleShouldBeCorrect(viewAsString, Messages("iht.estateReport.gifts.givenAwayIn7YearsBeforeDeath"))
      browserTitleShouldBeCorrect(viewAsString, Messages("iht.estateReport.gifts.givenAwayIn7YearsBeforeDeath"))

      val saveAndContinueLink = doc.getElementById("save-continue")
      saveAndContinueLink.text shouldBe Messages("iht.saveAndContinue")

    }

    "contain the correct question" in {
      val view = seven_years_to_trust(giftSevenYearsToTrustForm, regDetails)(fakeRequest)

      messagesShouldBePresent(contentAsString(view), Messages("page.iht.application.gifts.trust.question",
        CommonHelper.getDeceasedNameOrDefaultString(regDetails)))

    }

    "show the correct text and link for the return link" in {
      val view = seven_years_to_trust(giftSevenYearsToTrustForm, regDetails)(fakeRequest)
      val viewAsString = contentAsString(view)

      val doc = asDocument(viewAsString)
      val link = doc.getElementById("return-button")
      link.text shouldBe Messages("page.iht.application.gifts.return.to.givenAwayBy",
        CommonHelper.getOrException(regDetails.deceasedDetails).name)
      link.attr("href") shouldBe
        iht.controllers.application.gifts.routes.GiftsOverviewController.onPageLoad.url

    }
  }
}
