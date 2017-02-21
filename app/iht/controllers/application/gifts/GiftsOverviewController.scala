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

package iht.controllers.application.gifts

import iht.controllers.application.EstateController
import iht.controllers.{ControllerHelper, IhtConnectors}
import iht.metrics.Metrics
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.models.application.gifts.AllGifts
import iht.utils.CommonHelper
import iht.utils.CommonHelper._
import iht.utils.ExemptionsGuidanceHelper._
import iht.utils.OverviewHelper._
import play.api.i18n.Messages
import iht.views.html._
import iht.constants.Constants._

import scala.concurrent.Future

object GiftsOverviewController extends GiftsOverviewController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait GiftsOverviewController extends EstateController {
  private def givenAwayYesNoItems(allGifts: AllGifts, rd: RegistrationDetails) = {
    Seq[QuestionAnswer](
      QuestionAnswer(allGifts.isGivenAway, routes.GivenAwayController.onPageLoad(),
        _.allGifts.flatMap(_.isGivenAway).fold(true)(_ => true),
        "page.iht.application.gifts.overview.givenAway.question1.yes.screenReader.link.value",
        "page.iht.application.gifts.overview.givenAway.question1.no.screenReader.link.value",
        "page.iht.application.gifts.overview.givenAway.question1.none.screenReader.link.value")
    )
  }

  private def withReservationYesNoItems(allGifts: AllGifts, rd: RegistrationDetails) = {
    Seq[QuestionAnswer](
      QuestionAnswer(allGifts.isReservation, routes.WithReservationOfBenefitController.onPageLoad(),
        _.allGifts.flatMap(_.isReservation).fold(false)(_ => true),
        "page.iht.application.gifts.overview.reservation.question1.yes.screenReader.link.value",
        "page.iht.application.gifts.overview.reservation.question1.no.screenReader.link.value",
        "page.iht.application.gifts.overview.reservation.question1.none.screenReader.link.value")
    )
  }

  private def sevenYearsYesNoItems(allGifts: AllGifts, rd: RegistrationDetails) = {
    Seq[QuestionAnswer](
      QuestionAnswer(allGifts.isGivenInLast7Years, routes.SevenYearsGivenInLast7YearsController.onPageLoad(),
        _.allGifts.flatMap(_.isGivenInLast7Years).fold(false)(_ => true),
        "page.iht.application.gifts.overview.sevenYears.question1.yes.screenReader.link.value",
        "page.iht.application.gifts.overview.sevenYears.question1.no.screenReader.link.value",
        "page.iht.application.gifts.overview.sevenYears.question1.none.screenReader.link.value"),
      QuestionAnswer(allGifts.isToTrust, iht.controllers.application.gifts.routes.SevenYearsToTrustController.onPageLoad(),
        _.allGifts.flatMap(_.isGivenInLast7Years).fold(false)(_ => !allGifts.isGivenInLast7Years.get),
        "page.iht.application.gifts.overview.sevenYears.question2.yes.screenReader.link.value",
        "page.iht.application.gifts.overview.sevenYears.question2.no.screenReader.link.value",
        "page.iht.application.gifts.overview.sevenYears.question2.none.screenReader.link.value")
    )
  }

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      cachingConnector.storeSingleValue(ControllerHelper.lastQuestionUrl,
        routes.GiftsOverviewController.onPageLoad().toString())

      val regDetails: RegistrationDetails = cachingConnector.getExistingRegistrationDetails

      val applicationDetailsFuture: Future[Option[ApplicationDetails]] = ihtConnector
        .getApplication(getNino(user), getOrExceptionNoIHTRef(regDetails.ihtReference),
          regDetails.acknowledgmentReference)

      applicationDetailsFuture.flatMap { optionApplicationDetails =>
        val ad = getOrExceptionNoApplication(optionApplicationDetails)

        ad.allGifts.flatMap(_.isGivenAway)
          .fold(Future.successful(Redirect(routes.GivenAwayController.onPageLoad()))) { _ =>
            guidanceRedirect(routes.GiftsOverviewController.onPageLoad(), ad, cachingConnector).map {
              case Some(call) => Redirect(call)
              case None =>
                val optionAllGifts: Option[AllGifts] = ad.allGifts
                val allGifts: AllGifts = optionAllGifts.fold[AllGifts](new AllGifts(None, None, None, None, None))(identity)

                lazy val ihtRef = CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference)
                lazy val seqToDisplay = createSeqOfQuestions(regDetails, ad, allGifts)

                Ok(iht.views.html.application.gift.gifts_overview(regDetails,
                  seqToDisplay,
                  Some(iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(ihtRef)),
                  "iht.estateReport.returnToEstateOverview"))
            }
          }
      }
    }
  }

  private def createSeqOfQuestions(regDetails: RegistrationDetails,
                                   ad: ApplicationDetails,
                                   allGifts: AllGifts) = {
    val deceasedName = CommonHelper.getDeceasedNameOrDefaultString(regDetails, true)
    lazy val sectionIsGivenAway = createSectionFromYesNoQuestions(
      id = "givenAway",
      title = None,
      linkUrl = routes.GivenAwayController.onPageLoad(),
      sectionLevelLinkAccessibilityText = "page.iht.application.gifts.overview.givenAway.giveAnswer.screenReader.link.value",
      questionAnswersPlusChangeLinks = givenAwayYesNoItems(allGifts, regDetails),
      questionTitlesMessagesFileItems = Seq(Messages("page.iht.application.gifts.overview.givenAway.question1", deceasedName)),
      ad,
      regDetails,
      questionLinkIds = Seq(GiftsGivenAwayQuestionID)
    )

    lazy val sectionReservation = createSectionFromYesNoQuestions(
      id = "reservation",
      title = Some("iht.estateReport.gifts.withReservation.title"),
      linkUrl = routes.WithReservationOfBenefitController.onPageLoad(),
      sectionLevelLinkAccessibilityText = "page.iht.application.gifts.overview.reservation.giveAnswer.screenReader.link.value",
      questionAnswersPlusChangeLinks = withReservationYesNoItems(allGifts, regDetails),
      questionTitlesMessagesFileItems = Seq(Messages("iht.estateReport.gifts.reservation.question", deceasedName)),
      ad,
      regDetails,
      sectionLinkId = GiftsReservationBenefitSectionID,
      questionLinkIds = Seq(GiftsReservationBenefitQuestionID)
    )

    lazy val sectionSevenYears = createSectionFromYesNoQuestions(
      id = "sevenYear",
      title = Some("iht.estateReport.gifts.givenAwayIn7YearsBeforeDeath"),
      linkUrl = routes.SevenYearsGivenInLast7YearsController.onPageLoad(),
      sectionLevelLinkAccessibilityText = "page.iht.application.gifts.overview.sevenYears.giveAnswer.screenReader.link.value",
      questionAnswersPlusChangeLinks = sevenYearsYesNoItems(allGifts, regDetails),
      questionTitlesMessagesFileItems = Seq(Messages("page.iht.application.gifts.overview.sevenYears.question1", deceasedName),
                                            Messages("page.iht.application.gifts.overview.sevenYears.question2", deceasedName)),
      ad,
      regDetails,
      sectionLinkId = GiftsSevenYearsSectionID,
      questionLinkIds = Seq(GiftsSevenYearsQuestionID, GiftsSevenYearsQuestionID2)
    )

    lazy val sectionValueGivenAway = createSectionFromValueQuestions(
      id = "value",
      title = Some("iht.estateReport.gifts.valueOfGiftsGivenAway"),
      linkUrl = routes.SevenYearsGiftsValuesController.onPageLoad(),
      sectionLevelLinkAccessibilityText = "page.iht.application.gifts.overview.value.giveAnswer.screenReader.link.value",
      questionLevelLinkAccessibilityTextValue = "page.iht.application.gifts.overview.value.amount.screenReader.link.value",
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

}
