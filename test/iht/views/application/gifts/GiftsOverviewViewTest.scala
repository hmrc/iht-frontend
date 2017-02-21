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

import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.models.application.gifts.AllGifts
import iht.testhelpers.CommonBuilder
import iht.utils.CommonHelper
import iht.utils.OverviewHelper._
import iht.views.ViewTestHelper
import iht.views.html.application.gift.gifts_overview
import play.api.i18n.Messages
import iht.constants.Constants._

class GiftsOverviewViewTest extends ViewTestHelper {

  lazy val ihtRef = "ABC123"
  lazy val regDetails = CommonBuilder.buildRegistrationDetails1.copy(ihtReference = Some(ihtRef))
  lazy val whatAGiftPageUrl = iht.controllers.application.gifts.guidance.routes.WhatIsAGiftController.onPageLoad()
  lazy val estateOverviewPageUrl = iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(ihtRef)
  lazy val giftGivenAwayPageUrl = iht.controllers.application.gifts.routes.GivenAwayController.onPageLoad()
  lazy val giftWithReservationUrl = iht.controllers.application.gifts.routes.WithReservationOfBenefitController.onPageLoad()
  lazy val giftGivenInLastSevenYearsPageUrl = iht.controllers.application.gifts.routes.SevenYearsGivenInLast7YearsController.onPageLoad()
  lazy val giftSevenYearsValuesPageUrl = iht.controllers.application.gifts.routes.SevenYearsGiftsValuesController.onPageLoad()
  lazy val giftsForTrustPageUrl = iht.controllers.application.gifts.routes.SevenYearsToTrustController.onPageLoad()

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
      assertContainsText(view, Messages("iht.estateReport.completeEverySection"))
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

    "have all question labels and the correct target links" in {
      implicit val request = createFakeRequest()
      val allGifts = CommonBuilder.buildAllGifts.copy(isGivenAway = Some(true),
                                                      isReservation = Some(false),
                                                      isToTrust = Some(false),
                                                      isGivenInLast7Years = Some(true),
                                                      action = None)

      val giftsList = CommonBuilder.buildGiftsList

      val appDetails = CommonBuilder.buildApplicationDetails.copy(allGifts = Some(allGifts), giftsList = giftsList)
      val seqOfQuestions = createSeqOfQuestions(regDetails, appDetails, allGifts)
      val deceasedName = CommonHelper.getDeceasedNameOrDefaultString(regDetails)

      val view = gifts_overview(regDetails,
                                seqOfQuestions,
                                Some(estateOverviewPageUrl),
                                "iht.estateReport.returnToEstateOverview")

      val doc = asDocument(view)

//      assertRenderedById(doc, "givenAway")
//      messagesShouldBePresent(doc.toString,
//        CommonHelper.escapePound(Messages("page.iht.application.gifts.overview.givenAway.question1", deceasedName)))
//      val givenAwayLink = doc.getElementById("gave-away-seven-years")
//      givenAwayLink.text shouldBe Messages("iht.change")
//      givenAwayLink.attr("href") shouldBe giftGivenAwayPageUrl.url
//
//      assertRenderedById(doc, "reservation")
//      messagesShouldBePresent(doc.toString, Messages("iht.estateReport.gifts.reservation.question", deceasedName))
//      val reservationLink = doc.getElementById("with-reservation-of-benefit")
//      reservationLink.text shouldBe Messages("iht.change")
//      reservationLink.attr("href") shouldBe giftWithReservationUrl.url
//
//      assertRenderedById(doc, "sevenYear")
//      messagesShouldBePresent(doc.toString, Messages("page.iht.application.gifts.overview.sevenYears.question1", deceasedName))
//      val sevenYearsLink = doc.getElementById("seven-years-anything-else")
//      sevenYearsLink.text shouldBe Messages("iht.change")
//      sevenYearsLink.attr("href") shouldBe giftGivenInLastSevenYearsPageUrl.url
println(doc)
      assertRenderedById(doc, "value")
      messagesShouldBePresent(doc.toString, Messages("page.iht.application.gifts.overview.value.question1"))
      val valueLink = doc.getElementById("value-of-gifts")
      valueLink.text shouldBe Messages("iht.estateReport.changeValues")
      valueLink.attr("href") shouldBe giftSevenYearsValuesPageUrl.url
    }

  }

  private def createSeqOfQuestions(regDetails: RegistrationDetails,
                                   ad: ApplicationDetails,
                                   allGifts: AllGifts) = {
    val deceasedName = CommonHelper.getDeceasedNameOrDefaultString(regDetails)
    lazy val sectionIsGivenAway = createSectionFromYesNoQuestions(
      id = "givenAway",
      title = None,
      linkUrl = giftGivenAwayPageUrl,
      sectionLevelLinkAccessibilityText = "",
      questionAnswersPlusChangeLinks = givenAwayYesNoItems(allGifts, regDetails),
      questionTitlesMessagesFileItems = Seq(Messages("page.iht.application.gifts.overview.givenAway.question1",
                                                      deceasedName)),ad, regDetails, questionLinkIds = Seq(GiftsGivenAwayQuestionID))

    lazy val sectionReservation = createSectionFromYesNoQuestions(
      id = "reservation",
      title = Some("iht.estateReport.gifts.withReservation.title"),
      linkUrl = giftWithReservationUrl,
      sectionLevelLinkAccessibilityText = "",
      questionAnswersPlusChangeLinks = withReservationYesNoItems(allGifts, regDetails),
      questionTitlesMessagesFileItems = Seq(Messages("iht.estateReport.gifts.reservation.question",
                                                    deceasedName)), ad, regDetails, sectionLinkId = GiftsReservationBenefitSectionID,
      questionLinkIds = Seq(GiftsReservationBenefitQuestionID))

    lazy val sectionSevenYears = createSectionFromYesNoQuestions(
      id = "sevenYear",
      title = Some("iht.estateReport.gifts.givenAwayIn7YearsBeforeDeath"),
      linkUrl = giftGivenInLastSevenYearsPageUrl,
      sectionLevelLinkAccessibilityText = "",
      questionAnswersPlusChangeLinks = sevenYearsYesNoItems(allGifts, regDetails),
      questionTitlesMessagesFileItems = Seq(Messages("page.iht.application.gifts.overview.sevenYears.question1",
                                                    deceasedName),
        Messages("page.iht.application.gifts.overview.sevenYears.question2", deceasedName)), ad, regDetails,
      sectionLinkId = GiftsSevenYearsSectionID,
      questionLinkIds = Seq(GiftsSevenYearsQuestionID, GiftsSevenYearsQuestionID2))

    lazy val sectionValueGivenAway = createSectionFromValueQuestions(
      id = "value",
      title = Some("iht.estateReport.gifts.valueOfGiftsGivenAway"),
      linkUrl = giftSevenYearsValuesPageUrl,
      sectionLevelLinkAccessibilityText = "",
      questionLevelLinkAccessibilityTextValue = "",
      questionAnswerExprValue = if (ad.isValueEnteredForPastYearsGifts) {
        ad.totalPastYearsGiftsOption
      } else { None },
      questionTitlesMessagesFilePrefix = "page.iht.application.gifts.overview.value",
      _.isValueEnteredForPastYearsGifts,
      ad,
      sectionLinkId = GiftsValueOfGiftsSectionID,
      questionLinkId = GiftsValueOfGiftsQuestionID
    )

    allGifts.isGivenAway match {
      case Some(false) => Seq(sectionIsGivenAway, sectionReservation, sectionSevenYears)
      case _ => Seq(sectionIsGivenAway, sectionReservation, sectionSevenYears, sectionValueGivenAway)
    }
  }

  private def givenAwayYesNoItems(allGifts: AllGifts, rd: RegistrationDetails) = {
    Seq[QuestionAnswer](
      QuestionAnswer(allGifts.isGivenAway, giftGivenAwayPageUrl,
        _.allGifts.flatMap(_.isGivenAway).fold(true)(_ => true), "", "", "")
    )
  }

  private def withReservationYesNoItems(allGifts: AllGifts, rd: RegistrationDetails) = {
    Seq[QuestionAnswer](
      QuestionAnswer(allGifts.isReservation, giftWithReservationUrl,
        _.allGifts.flatMap(_.isReservation).fold(false)(_ => true), "", "", "")
    )
  }

  private def sevenYearsYesNoItems(allGifts: AllGifts, rd: RegistrationDetails) = {
    Seq[QuestionAnswer](
      QuestionAnswer(allGifts.isGivenInLast7Years, giftGivenInLastSevenYearsPageUrl,
        _.allGifts.flatMap(_.isGivenInLast7Years).fold(false)(_ => true), "", "", ""),
      QuestionAnswer(allGifts.isToTrust, giftsForTrustPageUrl,
        _.allGifts.flatMap(_.isGivenInLast7Years).fold(false)(_ => !allGifts.isGivenInLast7Years.get), "", "", "")
    )
  }

}
