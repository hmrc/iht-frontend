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

import iht.constants.IhtProperties
import iht.forms.ApplicationForms._
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils.CommonHelper._
import iht.views.HtmlSpec
import iht.views.html.application.gift.given_away
import iht.{FakeIhtApp, TestUtils}
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by vineet on 15/11/16.
  */
class GivenAwayViewTest extends UnitSpec with FakeIhtApp with MockitoSugar with TestUtils with HtmlSpec with BeforeAndAfter{

  val ihtReference = Some("ABC1234567890")
  val regDetails = CommonBuilder.buildRegistrationDetails.copy(ihtReference = ihtReference,
                                  deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(
                                    maritalStatus = Some(TestHelper.MaritalStatusMarried))),
    deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath))

  val allGifts = CommonBuilder.buildAllGifts.copy(isGivenAway = Some(true))
  val fakeRequest = createFakeRequest(isAuthorised = false)

  "GivenAway View" must {

    "contain the title and save and continue button " in {
      val view = given_away(giftsGivenAwayForm, regDetails)(fakeRequest)

      val doc = asDocument(contentAsString(view))
      val title = doc.getElementsByTag("h1").first

      title.text should include(Messages("iht.estateReport.gifts.givenAwayBy",
        getOrException(regDetails.deceasedDetails).name))

      val saveAndContinueLink = doc.getElementById("save-continue")
      saveAndContinueLink.text shouldBe Messages("iht.saveAndContinue")
    }

    "show the correct question and guidance" in {
      implicit val request = createFakeRequest()
      val view = given_away(giftsGivenAwayForm, regDetails)
      view.toString should include(Messages("page.iht.application.gifts.lastYears.givenAway.question",
                                              getDeceasedNameOrDefaultString(regDetails)))

      view.toString should include(Messages("page.iht.application.gifts.lastYears.givenAway.p1",
        getDeceasedNameOrDefaultString(regDetails),
        getDateBeforeSevenYears(getOrException(regDetails.deceasedDateOfDeath).dateOfDeath).toString(IhtProperties.dateFormatForDisplay),
        getOrException(regDetails.deceasedDateOfDeath).dateOfDeath.toString(IhtProperties.dateFormatForDisplay)))

      view.toString should include(Messages("page.iht.application.gifts.lastYears.givenAway.p2",
        getDeceasedNameOrDefaultString(regDetails)))
    }

    "show return to estate overview link when user land on the page first time" in {
      val view = given_away(giftsGivenAwayForm, regDetails)(fakeRequest)

      val doc = asDocument(contentAsString(view))

      val link = doc.getElementById("return-button")
      link.text shouldBe Messages("iht.estateReport.returnToEstateOverview")
      link.attr("href") shouldBe
        iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(ihtReference.getOrElse("")).url

    }

    "show return to gifts given away link when user is in edit mode" in {

      val filledForm = giftsGivenAwayForm.fill(allGifts)
      val view = given_away(filledForm, regDetails)(fakeRequest)
      val doc = asDocument(contentAsString(view))

      val link = doc.getElementById("return-button")
      link.text shouldBe Messages("page.iht.application.gifts.return.to.givenAwayBy",
        getOrException(regDetails.deceasedDetails).name)
      link.attr("href") shouldBe
        iht.controllers.application.gifts.routes.GiftsOverviewController.onPageLoad.url

    }
  }
}
