/*
 * Copyright 2021 HM Revenue & Customs
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

package iht.viewmodels.application.overview

import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.utils.CommonHelper
import org.joda.time.{LocalDate, Months}
import iht.config.AppConfig
import iht.utils.CustomLanguageUtils.Dates
import play.api.i18n.Messages
import play.api.mvc.Call

sealed abstract class RowCompletionStatus
case object NotStarted extends RowCompletionStatus
case object PartiallyComplete extends RowCompletionStatus
case object Complete extends RowCompletionStatus

object RowCompletionStatus {
  def apply(isComplete: Option[Boolean]): RowCompletionStatus = isComplete match {
    case None         => NotStarted
    case Some(false)  => PartiallyComplete
    case _            => Complete
  }
}

sealed abstract class EstateOverviewValue
case object NoValueEntered extends EstateOverviewValue
case class CurrentValue(value: BigDecimal) extends EstateOverviewValue
case class AllAnsweredNo(messageKey: String) extends EstateOverviewValue

object DisplayValue {
  def apply(value: EstateOverviewValue)(implicit messages: Messages): String = value match {
    case NoValueEntered => ""
    case CurrentValue(amount) => "£" + CommonHelper.numberWithCommas(amount)
    case AllAnsweredNo(key) => messages(key)
  }
}

object DisplayValueAsNegative {
  def apply(value: EstateOverviewValue, areThereNoExemptions: Boolean = false)(implicit messages: Messages): String =
    (value, areThereNoExemptions) match {
      case (NoValueEntered, _) => ""
      case (CurrentValue(amount), _) if amount <= BigDecimal(0) => "£" + CommonHelper.numberWithCommas(amount)
      case (CurrentValue(amount), true) => "£" + CommonHelper.numberWithCommas(amount)
      case (CurrentValue(amount), _) => "-£" + CommonHelper.numberWithCommas(amount)
      case (AllAnsweredNo(key), _) => messages(key)
    }
}

case class OverviewRow(id: String,
                       label: String,
                       value: String,
                       completionStatus: RowCompletionStatus,
                       linkUrl: Call,
                       qualifyingText: String) {

  def linkText(implicit messages: Messages): String = this.completionStatus match {
    case NotStarted => messages("iht.start")
    case PartiallyComplete => messages("iht.giveMoreDetails")
    case _ => messages("iht.viewOrChange")
  }
}

case class OverviewRowWithoutLink(id: String,
                                  label: String,
                                  value: String,
                                  qualifyingText: String,
                                  renderAsTotalRow: Boolean = true,
                                  headingLevel: String = "h2",
                                  headingClass: String = "")

case class EstateOverviewViewModel (ihtReference: String,
                                    deceasedName: String,
                                    submissionDeadline: String,
                                    assetsAndGiftsSection: AssetsAndGiftsSectionViewModel,
                                    reducingEstateValueSection: Option[ReducingEstateValueSectionViewModel],
                                    otherDetailsSection: Option[OtherDetailsSectionViewModel],
                                    thresholdSection: ThresholdSectionViewModel,
                                    grandTotalRow: Option[OverviewRowWithoutLink],
                                    declarationSection: DeclarationSectionViewModel,
                                    increasingThresholdRow: Option[OverviewRow],
                                    submissionMonthsLeft: Int)

object EstateOverviewViewModel {

  def apply(registrationDetails: RegistrationDetails,
            applicationDetails: ApplicationDetails,
            deadlineDate: LocalDate)(implicit messages: Messages, appConfig: AppConfig): EstateOverviewViewModel = {

    val isExemptionsGreaterThanZero = applicationDetails.totalExemptionsValue > BigDecimal(0)

    val otherDetailsSection = if(isExemptionsGreaterThanZero) {
        None
      } else {
        Some(OtherDetailsSectionViewModel(applicationDetails, registrationDetails.ihtReference.getOrElse(""))(messages, appConfig))
      }

    val reducingEstateValueSection =
      (applicationDetails.hasSeenExemptionGuidance, isExemptionsGreaterThanZero) match {
        case (Some(hasSeen), aboveZero) if hasSeen || aboveZero =>
          Some(ReducingEstateValueSectionViewModel(applicationDetails, registrationDetails)(messages, appConfig))
        case _ => None
      }

    EstateOverviewViewModel(
      ihtReference = CommonHelper.getOrException(registrationDetails.ihtReference),
      deceasedName = registrationDetails.deceasedDetails.fold("")(_.name),
      submissionDeadline = Dates.formatDate(deadlineDate)(messages).toString,
      assetsAndGiftsSection = AssetsAndGiftsSectionViewModel(applicationDetails,
        behaveAsIncreasingTheEstateSection = applicationDetails.hasSeenExemptionGuidance.getOrElse(false))(messages, appConfig),
      reducingEstateValueSection = reducingEstateValueSection,
      otherDetailsSection = otherDetailsSection,
      thresholdSection = ThresholdSectionViewModel(registrationDetails, applicationDetails)(messages, appConfig),
      grandTotalRow = buildTotalRow(applicationDetails),
      declarationSection = DeclarationSectionViewModel(registrationDetails, applicationDetails),
      increasingThresholdRow = ThresholdSectionViewModel(registrationDetails, applicationDetails)(messages, appConfig).increasingThresholdRow,
      submissionMonthsLeft = getMonthsLeft(new LocalDate(), new LocalDate(deadlineDate))
      )
  }

  private def getMonthsLeft(currentDate: LocalDate, deadlineDate: LocalDate) = {
    Months.monthsBetween(new LocalDate(), new LocalDate(deadlineDate)).getMonths + 1
  }

  private def buildTotalRow(applicationDetails: ApplicationDetails)(implicit messages: Messages) = {
    (applicationDetails.hasSeenExemptionGuidance, applicationDetails.isValueEnteredForExemptions) match {
      case (Some(hasSeen), isEntered) if hasSeen || isEntered => Some(OverviewRowWithoutLink(
        id = "grand-total-section",
        label = createTotalRowLabel(applicationDetails),
        value = createTotalRowValue(applicationDetails),
        qualifyingText = ""))
      case _ => None
    }
  }

  def createTotalRowLabel(applicationDetails: ApplicationDetails)(implicit messages: Messages): String = applicationDetails.totalExemptionsValueOption match {
    case Some(x) if x > 0 => Messages("page.iht.application.estateOverview.totalValueOfTheEstate")
    case _ => Messages("page.iht.application.estateOverview.valueOfAssetsAndGifts")
  }

  def createTotalRowValue(applicationDetails: ApplicationDetails)(implicit messages: Messages): String = applicationDetails.totalExemptionsValueOption match {
    case Some(x) if x > 0 => DisplayValue(CurrentValue(applicationDetails.totalNetValue.max(0)))
    case _ => DisplayValue(CurrentValue(applicationDetails.totalValue))
  }

}
