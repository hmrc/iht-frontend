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

package iht.controllers.application.gifts

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.models.application.gifts.AllGifts
import iht.utils.CommonHelper._
import iht.utils.OverviewHelper._
import iht.utils._
import javax.inject.Inject
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class GiftsOverviewControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                            val cachingConnector: CachingConnector,
                                            val authConnector: AuthConnector,
                                            val formPartialRetriever: FormPartialRetriever,
                                            implicit val appConfig: AppConfig,
                                            val cc: MessagesControllerComponents) extends FrontendController(cc) with GiftsOverviewController

trait GiftsOverviewController extends EstateController with ExemptionsGuidanceHelper {


  private def givenAwayYesNoItems(allGifts: AllGifts, rd: RegistrationDetails) = {
    Seq[QuestionAnswer](
      QuestionAnswer(allGifts.isGivenAway, routes.GivenAwayController.onPageLoad(),
        _.allGifts.flatMap(_.isGivenAway).fold(true)(_ => true),
        "page.iht.application.gifts.overview.givenAway.question1.yes.screenReader.link.value",
        "page.iht.application.gifts.overview.givenAway.question1.no.screenReader.link.value",
        "page.iht.application.gifts.overview.givenAway.question1.none.screenReader.link.value")
    )
  }

  private def withReservationYesNoItems(allGifts: AllGifts, rd: RegistrationDetails)(implicit messages:Messages) =  {
    val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(rd, wrapName = true)
    Seq[QuestionAnswer](
      QuestionAnswer(allGifts.isReservation, routes.WithReservationOfBenefitController.onPageLoad(),
        _.allGifts.flatMap(_.isReservation).fold(false)(_ => true),
        messages("page.iht.application.gifts.overview.reservation.question1.yes.screenReader.link.value", deceasedName),
        messages("page.iht.application.gifts.overview.reservation.question1.no.screenReader.link.value", deceasedName),
        "page.iht.application.gifts.overview.reservation.question1.none.screenReader.link.value")
    )
  }

  private def sevenYearsYesNoItems(allGifts: AllGifts, rd: RegistrationDetails)(implicit messages:Messages) = {
    val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(rd, wrapName = true)
    Seq[QuestionAnswer](
      QuestionAnswer(allGifts.isGivenInLast7Years, routes.SevenYearsGivenInLast7YearsController.onPageLoad(),
        _.allGifts.flatMap(_.isGivenInLast7Years).fold(false)(_ => true),
        messages("page.iht.application.gifts.lastYears.question.yes.screenReader.link.value", deceasedName),
        messages("page.iht.application.gifts.lastYears.question.no.screenReader.link.value", deceasedName),
        "page.iht.application.gifts.lastYears.question.none.screenReader.link.value"),
      QuestionAnswer(allGifts.isToTrust, iht.controllers.application.gifts.routes.SevenYearsToTrustController.onPageLoad(),
        _.allGifts.flatMap(_.isGivenInLast7Years).fold(false)(_ => !allGifts.isGivenInLast7Years.get),
        messages("page.iht.application.gifts.trust.question.yes.screenReader.link.value", deceasedName),
        messages("page.iht.application.gifts.trust.question.no.screenReader.link.value", deceasedName),
        messages("page.iht.application.gifts.trust.question.none.screenReader.link.value", deceasedName))
    )
  }

  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>

      implicit request => {
        withRegistrationDetails { regDetails =>
          val applicationDetailsFuture: Future[Option[ApplicationDetails]] = ihtConnector
            .getApplication(getNino(userNino), getOrExceptionNoIHTRef(regDetails.ihtReference),
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
  }

  private def createIsGivenAwayQuestion(regDetails: RegistrationDetails,
                                ad: ApplicationDetails,
                                allGifts: AllGifts, deceasedName: String, messages: Messages) = createSectionFromYesNoQuestions(
    id = "givenAway",
    title = None,
    linkUrl = routes.GivenAwayController.onPageLoad(),
    sectionLevelLinkAccessibilityText = "page.iht.application.gifts.overview.givenAway.giveAnswer.screenReader.link.value",
    questionAnswersPlusChangeLinks = givenAwayYesNoItems(allGifts, regDetails),
    questionTitlesMessagesFileItems = Seq(messages("page.iht.application.gifts.lastYears.givenAway.question", deceasedName)),
    ad,
    regDetails,
    questionLinkIds = Seq(appConfig.GiftsGivenAwayQuestionID)
  )

  private def createReservationQuestion(regDetails: RegistrationDetails,
                                ad: ApplicationDetails,
                                allGifts: AllGifts, deceasedName: String, messages: Messages) = createSectionFromYesNoQuestions(
    id = "reservation",
    title = Some(messages("iht.estateReport.gifts.withReservation.title", deceasedName)),
    linkUrl = routes.WithReservationOfBenefitController.onPageLoad(),
    sectionLevelLinkAccessibilityText = messages("page.iht.application.gifts.overview.reservation.giveAnswer.screenReader.link.value", deceasedName),
    questionAnswersPlusChangeLinks = withReservationYesNoItems(allGifts, regDetails)(messages),
    questionTitlesMessagesFileItems = Seq(messages("iht.estateReport.gifts.reservation.question", deceasedName)),
    ad,
    regDetails,
    sectionLinkId = appConfig.GiftsReservationBenefitSectionID,
    questionLinkIds = Seq(appConfig.GiftsReservationBenefitQuestionID)
  )

  private def createSevenYearsQuestion(regDetails: RegistrationDetails,
                               ad: ApplicationDetails,
                               allGifts: AllGifts, deceasedName: String, messages: Messages) = createSectionFromYesNoQuestions(
    id = "sevenYear",
    title = Some("iht.estateReport.gifts.givenAwayIn7YearsBeforeDeath"),
    linkUrl = routes.SevenYearsGivenInLast7YearsController.onPageLoad(),
    sectionLevelLinkAccessibilityText = "page.iht.application.gifts.overview.sevenYears.giveAnswer.screenReader.link.value",
    questionAnswersPlusChangeLinks = sevenYearsYesNoItems(allGifts, regDetails)(messages),
    questionTitlesMessagesFileItems = Seq(messages("page.iht.application.gifts.lastYears.question", deceasedName),
      messages("page.iht.application.gifts.trust.question", deceasedName)),
    ad,
    regDetails,
    sectionLinkId = appConfig.GiftsSevenYearsSectionID,
    questionLinkIds = Seq(appConfig.GiftsSevenYearsQuestionID, appConfig.GiftsSevenYearsQuestionID2)
  )

  private def createValueGivenAwayQuestion(regDetails: RegistrationDetails,
                                   ad: ApplicationDetails,
                                   allGifts: AllGifts, deceasedName: String) = createSectionFromValueQuestions(
    id = "value",
    title = Some("iht.estateReport.gifts.valueOfGiftsGivenAway"),
    linkUrl = routes.SevenYearsGiftsValuesController.onPageLoad(),
    sectionLevelLinkAccessibilityText = "page.iht.application.gifts.overview.value.giveAnswer.screenReader.link.value",
    questionLevelLinkAccessibilityTextValue = "page.iht.application.gifts.overview.value.amount.screenReader.link.value",
    questionAnswerExprValue = if (ad.isValueEnteredForPastYearsGifts) {
      ad.totalPastYearsGiftsOption
    } else {
      None
    },
    questionTitlesMessagesFilePrefix = "page.iht.application.gifts.overview.value",
    _.isValueEnteredForPastYearsGifts,
    ad,
    sectionLinkId = appConfig.GiftsValueOfGiftsSectionID,
    questionLinkId = appConfig.GiftsValueOfGiftsQuestionID
  )

  private def createSeqOfQuestions(regDetails: RegistrationDetails,
                                   ad: ApplicationDetails,
                                   allGifts: AllGifts)(implicit messages: Messages) = {

    val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails, wrapName = true)

    lazy val sectionIsGivenAway = createIsGivenAwayQuestion(regDetails, ad, allGifts, deceasedName, messages)
    lazy val sectionReservation = createReservationQuestion(regDetails, ad, allGifts, deceasedName, messages)
    lazy val sectionSevenYears = createSevenYearsQuestion(regDetails, ad, allGifts, deceasedName, messages)
    lazy val sectionValueGivenAway = createValueGivenAwayQuestion(regDetails, ad, allGifts, deceasedName)

    allGifts.isGivenAway match {
      case Some(false) => Seq(sectionIsGivenAway, sectionReservation, sectionSevenYears)
      case _ =>
        Seq(sectionIsGivenAway, sectionReservation, sectionSevenYears, sectionValueGivenAway)
    }
  }

}
