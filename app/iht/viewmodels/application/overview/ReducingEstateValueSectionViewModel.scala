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

import iht.config.AppConfig
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.utils.RegistrationDetailsHelperFixture
import play.api.i18n.Messages

case class ReducingEstateValueSectionViewModel(debtRow: Option[OverviewRow],
                                      exemptionRow: OverviewRow,
                                      totalRow: OverviewRowWithoutLink)

object ReducingEstateValueSectionViewModel {

  private def isExemptionsCompletedWithNoValueDependentOnMaritalStatus(appDetails: ApplicationDetails, registrationDetails: RegistrationDetails)
                                                                      (implicit appConfig: AppConfig)= {
    if (!registrationDetails.deceasedDetails.flatMap(_.maritalStatus).contains(appConfig.statusMarried)) {
      appDetails.isExemptionsCompletedWithoutPartnerExemptionWithNoValue
    } else {
      appDetails.isExemptionsCompletedWithNoValue
    }
  }

  private def getExemptionsDisplayValue(applicationDetails: ApplicationDetails) = applicationDetails.allExemptions match {
    case None => NoValueEntered
    case Some(allExemptions) if allExemptions.isExemptionsSectionCompletedWithNoValue =>
      AllAnsweredNo("page.iht.application.estateOverview.exemptions.noExemptionsValue")
    case Some(allExemptions) if !applicationDetails.isValueEnteredForExemptions => NoValueEntered
    case _ => CurrentValue(applicationDetails.totalExemptionsValue)
  }

  private def getDebtsDisplayValue(applicationDetails: ApplicationDetails) = applicationDetails.allLiabilities match {
    case None => NoValueEntered
    case Some(allLiabilities) if allLiabilities.areAllDebtsSectionsAnsweredNo  => AllAnsweredNo("site.noDebts")
    case Some(allLiabilities) if !allLiabilities.doesAnyDebtSectionHaveAValue => NoValueEntered
    case _ => CurrentValue(applicationDetails.totalLiabilitiesValue)
  }

  def getScreenReaderQualifyingText(isComplete: RowCompletionStatus, moreDetailText: String, valueText: String, noValueText: String) =
    isComplete match {
      case NotStarted => noValueText
      case PartiallyComplete => moreDetailText
      case _ => valueText
    }

  def apply(applicationDetails: ApplicationDetails, registrationDetails: RegistrationDetails)
           (implicit messages: Messages, appConfig: AppConfig): ReducingEstateValueSectionViewModel = {
    val displayValue = if (isExemptionsCompletedWithNoValueDependentOnMaritalStatus(applicationDetails, registrationDetails)) {
      messages("page.iht.application.estateOverview.exemptions.noExemptionsValue")
      } else {
        DisplayValueAsNegative(getExemptionsDisplayValue(applicationDetails))(messages)
      }
    val exemptionCompletionStatus = if (new RegistrationDetailsHelperFixture().isExemptionsCompleted(registrationDetails, applicationDetails)) {
        Complete
      } else if (applicationDetails.noExemptionsHaveBeenAnswered) {
        NotStarted
      } else {
        PartiallyComplete
      }
    val areDebtsIncluded: Boolean = exemptionCompletionStatus != NotStarted && (applicationDetails.totalExemptionsValue > BigDecimal(0))

    val exemptionsScreenreaderText = getScreenReaderQualifyingText(
      RowCompletionStatus(applicationDetails.areAllAssetsCompleted),
      messages("page.iht.application.overview.exemptions.screenReader.moreDetails.link"),
      messages("page.iht.application.overview.exemptions.screenReader.value.link"),
      messages("page.iht.application.overview.exemptions.screenReader.noValue.link")
    )
    val debtsScreenreaderText = getScreenReaderQualifyingText(
      RowCompletionStatus(applicationDetails.areAllAssetsCompleted),
      messages("page.iht.application.overview.debts.screenReader.moreDetails.link"),
      messages("page.iht.application.overview.debts.screenReader.value.link"),
      messages("page.iht.application.overview.debts.screenReader.noValue.link")
    )
    val theDebtRow = if (areDebtsIncluded) {
            Some(OverviewRow(appConfig.EstateDebtsID, messages("iht.estateReport.debts.owedFromEstate"),
              DisplayValueAsNegative(getDebtsDisplayValue(applicationDetails))(messages),
              RowCompletionStatus(applicationDetails.areAllDebtsCompleted),
              iht.controllers.application.debts.routes.DebtsOverviewController.onPageLoad(), debtsScreenreaderText))
    } else {
      None
    }
    val totalValue = applicationDetails.totalExemptionsValue +
      (if (areDebtsIncluded) applicationDetails.totalLiabilitiesValue else BigDecimal(0))

    ReducingEstateValueSectionViewModel(
      debtRow = theDebtRow,
      exemptionRow = OverviewRow(appConfig.EstateExemptionsID, messages("iht.estateReport.exemptions.title"),
        displayValue, exemptionCompletionStatus,
        iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad(),
        exemptionsScreenreaderText),
      totalRow = OverviewRowWithoutLink("reducing-estate-totals", messages("page.iht.application.exemptions.total"),
         DisplayValueAsNegative(CurrentValue(totalValue))(messages), qualifyingText = "", headingLevel = "h3",
        headingClass = "visually-hidden")
    )
  }
}
