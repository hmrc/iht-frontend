/*
 * Copyright 2018 HM Revenue & Customs
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
import iht.models.application.gifts.AllGifts
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils.{CommonHelper, DateHelper}
import iht.utils.CommonHelper._
import iht.views.application.{CancelComponent, SubmittableApplicationPageBehaviour}
import iht.views.html.application.gift.given_away
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.twirl.api.HtmlFormat.Appendable
import iht.constants.IhtProperties._
import org.joda.time.LocalDate
import play.api.i18n.Lang

class GivenAwayViewTest extends SubmittableApplicationPageBehaviour[AllGifts] {

  def registrationDetails = CommonBuilder.buildRegistrationDetails.copy(ihtReference = Some("ABC1234567890"),
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(
      maritalStatus = Some(TestHelper.MaritalStatusMarried))),
    deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath))

  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def pageTitle = messagesApi("iht.estateReport.gifts.givenAwayBy", deceasedName)

  override def browserTitle = messagesApi("iht.estateReport.gifts.givenAway.title")

  def getDateBeforeSevenYears(date: LocalDate) = {
    date.minusYears(IhtProperties.giftsYears.toInt).plusDays(1)
  }

  override def guidance = guidance(
    Set(
      messagesApi("page.iht.application.gifts.lastYears.givenAway.p1",
        deceasedName,
        getDateBeforeSevenYears(
          getOrException(registrationDetails.deceasedDateOfDeath).dateOfDeath).toString(IhtProperties.dateFormatForDisplay),
        getOrException(registrationDetails.deceasedDateOfDeath).dateOfDeath.toString(IhtProperties.dateFormatForDisplay)),
      messagesApi("page.iht.application.gifts.lastYears.givenAway.p2", deceasedName)
    )
  )

  override def formTarget = Some(iht.controllers.application.gifts.routes.GivenAwayController.onSubmit())

  override def cancelComponent = Some(
    CancelComponent(
      iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(
        CommonHelper.getOrException(registrationDetails.ihtReference)),
      messagesApi("iht.estateReport.returnToEstateOverview")
    )
  )

  override def form: Form[AllGifts] = giftsGivenAwayForm

  override def formToView: Form[AllGifts] => Appendable =
    form =>
      given_away(form, registrationDetails, CommonBuilder.buildGiftsList)


  "GivenAway View" must {

    behave like applicationPageWithErrorSummaryBox()

    "show return to gifts given away link when user is in edit mode" in {
      implicit val request = createFakeRequest()
      val fakeRequest = createFakeRequest(isAuthorised = false)
      val allGifts = CommonBuilder.buildAllGifts.copy(isGivenAway = Some(true))
      val filledForm = giftsGivenAwayForm.fill(allGifts)
      val view = given_away(filledForm, registrationDetails, CommonBuilder.buildGiftsList)
      val doc = asDocument(view)

      val link = doc.getElementById("return-button")
      link.text shouldBe messagesApi("page.iht.application.gifts.return.to.givenAwayBy",
        getOrException(registrationDetails.deceasedDetails).name)
      link.attr("href") shouldBe
        iht.controllers.application.gifts.routes.GiftsOverviewController.onPageLoad().url + "#" + GiftsGivenAwayQuestionID
    }

    "show all previous years as bullet points in the correct order" in {
      implicit val request = createFakeRequest()
      implicit val lang = Lang("en")

      val allGifts = CommonBuilder.buildAllGifts.copy(isGivenAway = Some(true))
      val filledForm = giftsGivenAwayForm.fill(allGifts)
      val view = given_away(filledForm, registrationDetails, CommonBuilder.buildGiftsList)
      val doc = asDocument(view)

      doc.getElementById("tax-year-1").text shouldBe "6 April 2014 to 12 December 2014"
      doc.getElementById("tax-year-2").text shouldBe "6 April 2013 to 5 April 2013"
      doc.getElementById("tax-year-3").text shouldBe "6 April 2012 to 5 April 2012"
    }
  }
}
