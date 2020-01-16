/*
 * Copyright 2020 HM Revenue & Customs
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

import iht.config.AppConfig
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.utils.tnrb.TnrbHelperFixture
import iht.utils.{CommonHelper, DateHelper}
import org.joda.time.LocalDate
import play.api.i18n.Messages

case class ThresholdSectionViewModel(thresholdRow: OverviewRowWithoutLink,
                                     increasingThresholdRow: Option[OverviewRow],
                                     showIncreaseThresholdLink: Boolean,
                                     thresholdIncreased: Boolean)

object ThresholdSectionViewModel {

 def getScreenReaderQualifyingText(isComplete: RowCompletionStatus, moreDetailText: String, valueText: String) =
    isComplete match {
      case NotStarted => moreDetailText
      case PartiallyComplete => moreDetailText
      case _ => valueText
    }


  def apply(registrationDetails: RegistrationDetails, applicationDetails: ApplicationDetails)
           (implicit messages: Messages, appConfig: AppConfig): ThresholdSectionViewModel = {

    val thresholdIncreased = applicationDetails.isSuccessfulTnrbCase
    val thresholdIncreaseSectionAccessed = applicationDetails.isWidowCheckQuestionAnswered

    val thresholdValueMessage = if (thresholdIncreased) {
      messages("site.tnrb.value.display")
    } else {
      messages("site.threshold.value.display")
    }

    val showIncreaseThresholdLink = {

      val claimDateInRange = DateHelper.isDateWithInRange( registrationDetails.deceasedDateOfDeath.map(_.dateOfDeath)
        .fold[LocalDate](new LocalDate)(identity))

      CommonHelper.getOrException(registrationDetails.deceasedDetails).maritalStatus match {
        case Some(appConfig.statusSingle) => false
        case _ => !thresholdIncreaseSectionAccessed && claimDateInRange
      }
    }

    val increasingThresholdRow = thresholdIncreaseSectionAccessed match {
      case true => Some(buildIncreaseThresholdRow(registrationDetails, applicationDetails, thresholdIncreased, thresholdIncreaseSectionAccessed))
      case _ => None
    }

    ThresholdSectionViewModel(
      thresholdRow = OverviewRowWithoutLink(
        id = appConfig.EstateIncreasingID,
        label = messages("iht.estateReport.ihtThreshold"),
        value = thresholdValueMessage,
        qualifyingText = "",
        renderAsTotalRow = thresholdIncreased,
      headingLevel = "h3"),
      increasingThresholdRow = increasingThresholdRow,
      showIncreaseThresholdLink = showIncreaseThresholdLink,
      thresholdIncreased = thresholdIncreased
    )
  }

  def buildIncreaseThresholdRow(registrationDetails: RegistrationDetails,
                                applicationDetails: ApplicationDetails,
                                thresholdIncreased: Boolean,
                                thresholdIncreaseSectionAccessed: Boolean)(implicit messages: Messages, appConfig: AppConfig): OverviewRow = {

    val thresholdIncreaseNotAvailable = applicationDetails.widowCheck match {
      case Some(widowCheck) if widowCheck.widowed.isDefined && !CommonHelper.getOrException(widowCheck.widowed) => true
      case _ => false
    }

    val thresholdScreenreaderText = getScreenReaderQualifyingText(
      RowCompletionStatus(applicationDetails.areAllAssetsCompleted),
      messages("page.iht.application.overview.threshold.screenReader.moreDetails.link"),
      messages("page.iht.application.overview.threshold.screenReader.value.link")
    )

    lazy val thresholdRowValue = if (thresholdIncreased) {
      messages("page.iht.application.estateOverview.increaseThreshold.increased")
    } else if (thresholdIncreaseNotAvailable) {
      messages("page.iht.application.estateOverview.increaseThreshold.notAvailable")
    } else {
      ""
    }

    OverviewRow(
      id = appConfig.EstateIncreasingID,
      label = messages("iht.estateReport.tnrb.increasingThreshold"),
      value = thresholdRowValue,
      completionStatus = if (thresholdIncreased || thresholdIncreaseNotAvailable) Complete else PartiallyComplete,
      linkUrl = new TnrbHelperFixture().getEntryPointForTnrb(registrationDetails, applicationDetails),
      qualifyingText = thresholdScreenreaderText
    )
  }
}
