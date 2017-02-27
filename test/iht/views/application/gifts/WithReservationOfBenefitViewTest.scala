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
import iht.testhelpers.{CommonBuilder, ContentChecker, TestHelper}
import iht.utils.CommonHelper
import iht.views.ViewTestHelper
import iht.views.html.application.gift.with_reservation_of_benefit
import play.api.i18n.MessagesApi
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import iht.constants.Constants._
import iht.constants.IhtProperties._

/**
  * Created by vineet on 15/11/16.
  */
class WithReservationOfBenefitViewTest extends ViewTestHelper{

  val ihtReference = Some("ABC1234567890")
  val regDetails = CommonBuilder.buildRegistrationDetails.copy(ihtReference = ihtReference,
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(
      maritalStatus = Some(TestHelper.MaritalStatusMarried))),
    deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath))

  val allGifts = CommonBuilder.buildAllGifts.copy(isReservation = Some(false))
  val fakeRequest = createFakeRequest(isAuthorised = false)

  "WithReservationOfBenefit Page" must {

    "contain the title and save and continue button " in {
      val view = with_reservation_of_benefit(giftWithReservationFromBenefitForm, regDetails)(fakeRequest, applicationMessages)

      val doc = asDocument(contentAsString(view))
      val title = doc.getElementsByTag("h1").first

      title.text should include(messagesApi("iht.estateReport.gifts.withReservation.title"))

      val saveAndContinueLink = doc.getElementById("save-continue")
      saveAndContinueLink.text shouldBe messagesApi("iht.saveAndContinue")
    }

    "contain the correct question" in {
      val view = with_reservation_of_benefit(giftWithReservationFromBenefitForm, regDetails)(fakeRequest, applicationMessages)

      ContentChecker.stripLineBreaks(contentAsString(view)) should include(messagesApi("iht.estateReport.gifts.reservation.question",
                                            CommonHelper.getDeceasedNameOrDefaultString(regDetails)))

    }

    "show the correct text and link for the return link" in {
      val view = with_reservation_of_benefit(giftsGivenAwayForm, regDetails)(fakeRequest, applicationMessages)

      val doc = asDocument(contentAsString(view))

      val link = doc.getElementById("return-button")
      link.text shouldBe messagesApi("page.iht.application.gifts.return.to.givenAwayBy",
        CommonHelper.getOrException(regDetails.deceasedDetails).name)
      link.attr("href") shouldBe
        iht.controllers.application.gifts.routes.GiftsOverviewController.onPageLoad.url + "#" + GiftsReservationBenefitQuestionID

    }
  }
}
