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

package iht.utils

import iht.constants.Constants._
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.utils.CommonHelper._
import org.joda.time.LocalDate
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.i18n.{Lang, Messages}
import play.api.mvc.Call
import uk.gov.hmrc.play.language.LanguageUtils.Dates

import scala.collection.immutable.ListMap

object OverviewHelper {
  val messagesFileChangeAnswer = "iht.change"
  val messagesFileYesValue = "iht.yes"
  val messagesFileNoValue = "iht.no"
  val messagesFileGiveAnswer = "site.link.giveAnswer"
  val messagesFileGiveValues = "site.link.giveValues"
  val messagesFileChangeValues = "iht.estateReport.changeValues"
  val messagesFileChange = "iht.change"
  val messagesFileGiveMoreDetails = "iht.giveMoreDetails"
  val messageNotStarted = "iht.notStarted"
  val messageInComplete = "iht.inComplete"
  val messageComplete = "iht.complete"

  case class QuestionAnswer(answer: Option[Boolean],
                            url: Call,
                            shouldDisplay: ApplicationDetails => Boolean,
                            linkAccessibilityTextYes: String,
                            linkAccessibilityTextNo: String,
                            linkAccessibilityTextNone: String)

  case class Link(linkText: String, linkTextAccessibility: String, linkUrl: Call)

  case class Section(id: String, title: Option[String], link: Link, details: Seq[Question], sectionLinkId: String = "")

  case class Question(id: String, title: String, link: Link, value: String, status: String = "", linkId: String = "")

  private def totalAssetsValueOption(ad:ApplicationDetails): Option[BigDecimal] = {
    if (ad.allAssets.map(_.areAllAssetsSectionsAnsweredNo).fold(false)(identity)) {
      None
    } else {
      val allAssetsValue = ad.allAssets.map(_.totalValueWithoutPropertiesOption)
      val allPropertyValue = CommonHelper.aggregateOfSeqOfOptionDecimal(ad.propertyList.map(_.value))
      CommonHelper.aggregateOfSeqOfOptionDecimal(Seq(allAssetsValue.flatten, allPropertyValue))
    }
  }

  private def overviewDisplayValues(implicit lang: Lang): ListMap[String, ApplicationDetails => String] = ListMap(
    AppSectionProperties -> { (ad) =>
      if (ad.propertyList.filter(_.value.isDefined).isEmpty) {
        ""
      }
      else {
        "£" + numberWithCommas(ad.propertyList.map(_.value.getOrElse(BigDecimal(0))).sum)
      }
    },
    AppSectionMoney -> { ad => ad.allAssets.flatMap(_.money).flatMap(_.totalValue).fold("")("£" + numberWithCommas(_)) },
    AppSectionHousehold -> { ad => ad.allAssets.flatMap(_.household).flatMap(_.totalValue).fold("")("£" + numberWithCommas(_)) },
    AppSectionVehicles -> { ad => ad.allAssets.flatMap(_.vehicles).flatMap(_.totalValue).fold("")("£" + numberWithCommas(_)) },
    AppSectionPrivatePension -> { ad => ad.allAssets.flatMap(_.privatePension).flatMap(_.totalValue).fold("")("£" + numberWithCommas(_)) },
    AppSectionStockAndShare -> { ad => ad.allAssets.flatMap(_.stockAndShare).flatMap(_.totalValue).fold("")("£" + numberWithCommas(_)) },
    AppSectionInsurancePolicy -> { ad => ad.allAssets.flatMap(_.insurancePolicy).flatMap(_.totalValue).fold("")("£" + numberWithCommas(_)) },
    AppSectionBusinessInterest -> { ad => ad.allAssets.flatMap(_.businessInterest).flatMap(_.value).fold("")("£" + numberWithCommas(_)) },
    AppSectionNominated -> { ad => ad.allAssets.flatMap(_.nominated).flatMap(_.value).fold("")("£" + numberWithCommas(_)) },
    AppSectionHeldInTrust -> { ad => ad.allAssets.flatMap(_.heldInTrust).flatMap(_.value).fold("")("£" + numberWithCommas(_)) },
    AppSectionForeign -> { ad => ad.allAssets.flatMap(_.foreign).flatMap(_.value).fold("")("£" + numberWithCommas(_)) },
    AppSectionMoneyOwed -> { ad => ad.allAssets.flatMap(_.moneyOwed).flatMap(_.value).fold("")("£" + numberWithCommas(_)) },
    AppSectionOther -> { ad => ad.allAssets.flatMap(_.other).flatMap(_.value).fold("")("£" + numberWithCommas(_)) },
    AppSectionMortgages -> { ad =>
      ad.allLiabilities.flatMap(_.mortgages).flatMap(_ =>
        if (ad.propertyList.isEmpty) {
          None
        }
        else {
          ad.allLiabilities.map(_.mortgageValue)
        })
        .fold("")("£" + numberWithCommas(_))
    },
    AppSectionFuneralExpenses -> {
      _.allLiabilities.flatMap(_.funeralExpenses).flatMap(_.totalValue).fold("")("£" + numberWithCommas(_))
    },

    AppSectionDebtsOwedFromTrust -> {
      _.allLiabilities.flatMap(_.trust).flatMap(_.totalValue).fold("")("£" + numberWithCommas(_))
    },
    AppSectionDebtsOwedToAnyoneOutsideUK -> {
      _.allLiabilities.flatMap(_.debtsOutsideUk).flatMap(_.totalValue).fold("")("£" + numberWithCommas(_))
    },
    AppSectionDebtsOwedOnJointAssets -> {
      _.allLiabilities.flatMap(_.jointlyOwned).flatMap(_.totalValue).fold("")("£" + numberWithCommas(_))
    },
    AppSectionDebtsOther -> {
      _.allLiabilities.flatMap(_.other).flatMap(_.totalValue).fold("")("£" + numberWithCommas(_))
    },

    AppSectionExemptionsPartnerIsAssetForDeceasedPartner -> { ad =>
      getMessageKeyValueOrBlank(
        getBooleanDisplayValue(ad.allExemptions.flatMap(_.partner).flatMap(_.isAssetForDeceasedPartner)))
    },
    AppSectionExemptionsPartnerIsPartnerHomeInUK -> { ad =>
      getMessageKeyValueOrBlank(
        getBooleanDisplayValue(ad.allExemptions.flatMap(_.partner).flatMap(_.isPartnerHomeInUK)))
    },
    AppSectionExemptionsPartnerName -> { ad => ad.allExemptions.flatMap(_.partner).flatMap(_.name) },
    AppSectionExemptionsPartnerDateOfBirth -> { ad => getDateDisplayValue(ad.allExemptions.flatMap(_.partner).flatMap(_.dateOfBirth)) },
    AppSectionExemptionsPartnerNino -> { ad => ad.allExemptions.flatMap(_.partner).flatMap(_.nino).fold("")(identity) },
    AppSectionExemptionsPartnerTotalAssets -> { ad => ad.allExemptions.flatMap(_.partner).flatMap(_.totalAssets).fold("")("£" + numberWithCommas(_)) },
    AppSectionExemptionsCharityValue -> { (ad) =>
      if (ad.charities.filter(_.totalValue.isDefined).isEmpty) {
        ""
      } else {
        "£" + numberWithCommas(ad.charities.map(_.totalValue.getOrElse(BigDecimal(0))).sum)
      }
    },
    AppSectionExemptionsQualifyingBodyValue -> { (ad) =>
      if (ad.qualifyingBodies.filter(_.totalValue.isDefined).isEmpty) {
        ""
      } else {
        "£" + numberWithCommas(ad.qualifyingBodies.map(_.totalValue.getOrElse(BigDecimal(0))).sum)
      }
    },
    AppSectionEstateAssets -> { ad =>
      totalAssetsValueOption(ad).fold("")(assetsValue =>
        "£" + numberWithCommas(assetsValue))
    },
    AppSectionEstateGifts -> { ad =>
      ad.totalPastYearsGiftsOption.fold("")(giftsValue =>
        "£" + numberWithCommas(giftsValue))
    },
    AppSectionEstateDebts -> { ad =>
      ad.totalLiabilitiesValueOption.fold("")(debtValue =>
        (if (ad.totalExemptionsValue > BigDecimal(0)) "-£" else "£") + numberWithCommas(debtValue))
    }
  )

  def displayValue(appDetails: ApplicationDetails,
                   section: String,
                   isComplete: Option[Boolean],
                   noneMessage: Option[String] = Some("site.noneInEstate"))(implicit lang: Lang) = {

    overviewDisplayValues.find(_._1 == section).map(_._2).map(expr => expr(appDetails))
      .fold(throw new RuntimeException("Attempt to display value for unknown section:" + section)) { displayValueFound =>
        (displayValueFound.isEmpty, isComplete) match {
          case (true, Some(true)) =>
            noneMessage.map(Messages(_)).fold("")(identity)
          case _ =>
            displayValueFound
        }
      }
  }

  def mapYesNoNone(value: String, yesValue: String, noValue: String, noneValue: String) =
    value match {
      case `messagesFileYesValue` => yesValue
      case `messagesFileNoValue` => noValue
      case _ => noneValue
    }

  def mapYesNoNoneDisplayValue(value: String, yesValue: String, noValue: String, noneValue: String) = {
    lazy val yesDisplayValue = Messages(messagesFileYesValue)
    lazy val noDisplayValue = Messages(messagesFileNoValue)
    value match {
      case `yesDisplayValue` => yesValue
      case `noDisplayValue` => noValue
      case _ => noneValue
    }
  }

  def getBooleanDisplayValue(optBoolean: Option[Boolean]): String =
    optBoolean.fold("")(booleanValue => getDisplayValueForBoolean(booleanValue))

  def getDisplayValueForBoolean(inputValue: Boolean): String =
    if (inputValue) messagesFileYesValue else messagesFileNoValue

  def getDateDisplayValue(optDate: Option[LocalDate])(implicit lang: Lang): String =
    optDate.fold("")(Dates.formatDate(_))

  def getBigDecimalDisplayValue(optBigDecimal: Option[BigDecimal]) =
    optBigDecimal.fold("")("£" + CommonHelper.numberWithCommas(_))

  private def createSeqFromYesNoQuestions(id: String,
                                          questionAnswersPlusChangeLinks: Seq[QuestionAnswer],
                                          questionTitlesMessagesFileItems: Seq[String],
                                          ad: ApplicationDetails, rd: RegistrationDetails, questionLinkIds: Seq[String]): Seq[Question] = {
    questionAnswersPlusChangeLinks.flatMap(answerPlusLink => {
      val index = questionAnswersPlusChangeLinks.indexOf(answerPlusLink)
      if (answerPlusLink.shouldDisplay(ad)) {
        val questionDisplayValue = getBooleanDisplayValue(answerPlusLink.answer)
        val indexPlusOne = index + 1
        Seq(Question(
          id = s"$id-question-$indexPlusOne",
          title = questionTitlesMessagesFileItems(index),
          link = Link(
            if (questionDisplayValue.length == 0) messagesFileGiveAnswer else messagesFileChangeAnswer,
            mapYesNoNone(questionDisplayValue,
              answerPlusLink.linkAccessibilityTextYes,
              answerPlusLink.linkAccessibilityTextNo,
              answerPlusLink.linkAccessibilityTextNone),
            answerPlusLink.url),
          value = questionDisplayValue,
          status = if (questionDisplayValue.length == 0) messageNotStarted else messageComplete,
          linkId = if(questionLinkIds.length > index) {questionLinkIds(index)} else {""}
        )
        )
      } else {
        Nil
      }
    })
  }

  /**
    * Create a section from the sequence of yes/no items passed in: the subscript number of each item is used to
    * generate the title message key and the id for each question.
    */
  def createSectionFromYesNoQuestions(id: String,
                                      title: Option[String],
                                      linkUrl: Call,
                                      sectionLevelLinkAccessibilityText: String,
                                      questionAnswersPlusChangeLinks: Seq[QuestionAnswer],
                                      questionTitlesMessagesFileItems: Seq[String],
                                      ad: ApplicationDetails,
                                      rd: RegistrationDetails,
                                      sectionLinkId: String = "",
                                      questionLinkIds: Seq[String]): Section = {
    Section(
      id = id,
      title = title,
      link = Link(getEmptyStringOrElse(questionAnswersPlusChangeLinks.head.answer, messagesFileGiveAnswer),
        sectionLevelLinkAccessibilityText, linkUrl),
      details = createSeqFromYesNoQuestions(id, questionAnswersPlusChangeLinks, questionTitlesMessagesFileItems, ad, rd, questionLinkIds),
      sectionLinkId = sectionLinkId
    )
  }

  /**
    * Create a section from the boolean and big decimal expressions passed in: the messages file prefix is used to
    * generate the title message key for each question.
    */
  def createSectionFromYesNoValueQuestions(id: String,
                                           title: Option[String],
                                           linkUrl: Call,
                                           sectionLevelLinkAccessibilityText: String,
                                           questionLevelLinkAccessibilityTextYes: String,
                                           questionLevelLinkAccessibilityTextNo: String,
                                           questionLevelLinkAccessibilityTextValue: String,
                                           questionAnswerExprYesNo: Option[Boolean],
                                           questionAnswerExprValue: Option[BigDecimal],
                                           questionTitleYesNoMessage: String,
                                           questionTitleValueMessage: String,
                                           sectionLinkId: String = "",
                                           questionLinkID: String = "",
                                           answerLinkID: String = ""): Section =
    Section(id = id,
      title = title,
      link = Link(getEmptyStringOrElse(questionAnswerExprYesNo, messagesFileGiveAnswer),
        sectionLevelLinkAccessibilityText,
        linkUrl),
      details = questionAnswerExprYesNo.fold[Seq[Question]](Nil)(bool => {
        val displayValue = getBooleanDisplayValue(questionAnswerExprYesNo)
        val accessibilityValue = mapYesNoNone(displayValue,
          questionLevelLinkAccessibilityTextYes,
          questionLevelLinkAccessibilityTextNo, "")
        val booleanElement = Seq(Question(
          id = id + "-yes-no-question",
          title = questionTitleYesNoMessage,
          link = Link(messagesFileChangeAnswer, accessibilityValue, linkUrl),
          value = displayValue,
          linkId = questionLinkID
        ))

        val valueElement =
          if (bool) {
            Seq(Question(
              id = id + "-value",
              title = questionTitleValueMessage,
              link = if(questionAnswerExprValue.isDefined) Link(messagesFileChange, questionLevelLinkAccessibilityTextValue, linkUrl) else Link(messagesFileGiveAnswer, questionLevelLinkAccessibilityTextValue, linkUrl),
              value = getBigDecimalDisplayValue(questionAnswerExprValue),
              linkId = answerLinkID
            ))
          } else {
            Nil
          }
        booleanElement ++ valueElement
      }),
      sectionLinkId = sectionLinkId
    )

  def createSectionFromValueQuestions(id: String,
                                      title: Option[String],
                                      linkUrl: Call,
                                      sectionLevelLinkAccessibilityText: String,
                                      questionLevelLinkAccessibilityTextValue: String,
                                      questionAnswerExprValue: Option[BigDecimal],
                                      questionTitlesMessagesFilePrefix: String,
                                      shouldDisplay: ApplicationDetails => Boolean,
                                      ad: ApplicationDetails,
                                      sectionLinkId: String = "",
                                      questionLinkId: String = ""): Section =
    Section(id = id,
      title = title,
      link = Link(getEmptyStringOrElse(questionAnswerExprValue, messagesFileGiveValues),
        sectionLevelLinkAccessibilityText,
        linkUrl),
      details = if (shouldDisplay(ad)) {
        Seq(Question(
          id = id + "-value",
          title = Messages(s"$questionTitlesMessagesFilePrefix.question1"),
          link = Link(messagesFileChangeValues, questionLevelLinkAccessibilityTextValue, linkUrl),
          value = getBigDecimalDisplayValue(questionAnswerExprValue),
          status = if (getBigDecimalDisplayValue(questionAnswerExprValue) == "") {
            messageNotStarted
          } else {
            messageComplete
          },
          questionLinkId))
      } else {
        Nil
      },
      sectionLinkId = sectionLinkId
    )
}
