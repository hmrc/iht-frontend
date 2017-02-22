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

package iht.viewmodels.application.overview

import iht.constants.IhtProperties
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.utils.CommonHelper
import iht.utils.tnrb.TnrbHelper
import org.joda.time.LocalDate
import play.api.Logger
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.inject._
import play.api.Play
import play.api.i18n.MessagesApi
import iht.utils.MessagesApiInjection

case class ThresholdSectionViewModel(thresholdRow: OverviewRowWithoutLink,
                                     increasingThresholdRow: Option[OverviewRow],
                                     showIncreaseThresholdLink: Boolean,
                                     thresholdIncreased: Boolean)

object ThresholdSectionViewModel extends MessagesApiInjection {

 def getScreenReaderQualifyingText(isComplete: RowCompletionStatus, moreDetailText: String, valueText: String) =
    isComplete match {
      case NotStarted => moreDetailText
      case PartiallyComplete => moreDetailText
      case _ => valueText
    }


  def apply(registrationDetails: RegistrationDetails, applicationDetails: ApplicationDetails): ThresholdSectionViewModel = {

    val thresholdIncreased = applicationDetails.isSuccessfulTnrbCase
    val thresholdIncreaseSectionAccessed = applicationDetails.isWidowCheckQuestionAnswered

    val thresholdValueMessage = thresholdIncreased match {
      case true => messageApi("site.tnrb.value.display")
      case _ => messageApi("site.threshold.value.display")
    }

    val showIncreaseThresholdLink = {

      val claimDateInRange = CommonHelper.isDateWithInRange( registrationDetails.deceasedDateOfDeath.map(_.dateOfDeath)
        .fold[LocalDate](new LocalDate)(identity))

      CommonHelper.getOrException(registrationDetails.deceasedDetails).maritalStatus match {
        case Some(IhtProperties.statusSingle) => false
        case _ => !thresholdIncreaseSectionAccessed && claimDateInRange
      }
    }

    val increasingThresholdRow = thresholdIncreaseSectionAccessed match {
      case true => Some(buildIncreaseThresholdRow(registrationDetails, applicationDetails, thresholdIncreased, thresholdIncreaseSectionAccessed))
      case _ => None
    }

    ThresholdSectionViewModel(
      thresholdRow = OverviewRowWithoutLink(
        id = "threshold",
        label = messageApi("iht.estateReport.ihtThreshold"),
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
                                thresholdIncreaseSectionAccessed: Boolean): OverviewRow = {

    val thresholdIncreaseNotAvailable = applicationDetails.widowCheck match {
      case Some(widowCheck) if widowCheck.widowed.isDefined && !CommonHelper.getOrException(widowCheck.widowed) => true
      case _ => false
    }

    val thresholdScreenreaderText = getScreenReaderQualifyingText(
      RowCompletionStatus(applicationDetails.areAllAssetsCompleted),
      messageApi("page.iht.application.overview.threshold.screenReader.moreDetails.link"),
      messageApi("page.iht.application.overview.threshold.screenReader.value.link")
    )

    val thresholdValueMessage = thresholdIncreased match {
      case true => messageApi("site.tnrb.value.display")
      case _ => messageApi("site.threshold.value.display")
    }

    lazy val thresholdRowValue = if (thresholdIncreased) {
      messageApi("page.iht.application.estateOverview.increaseThreshold.increased")
    } else if (thresholdIncreaseNotAvailable) {
      messageApi("page.iht.application.estateOverview.increaseThreshold.notAvailable")
    } else {
      ""
    }

    OverviewRow(
      id = "increasing-threshold",
      label = messageApi("iht.estateReport.tnrb.increasingThreshold"),
      value = thresholdRowValue,
      completionStatus = if (thresholdIncreased || thresholdIncreaseNotAvailable) Complete else PartiallyComplete,
      linkUrl = TnrbHelper.getEntryPointForTnrb(registrationDetails, applicationDetails),
      qualifyingText = thresholdScreenreaderText
    )
  }
}
