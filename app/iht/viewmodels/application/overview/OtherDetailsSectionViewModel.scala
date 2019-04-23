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

package iht.viewmodels.application.overview

import iht.config.AppConfig
import iht.models.application.ApplicationDetails
import play.api.i18n.Messages

case class OtherDetailsSectionViewModel(debtRow: OverviewRow,
                                        showClaimExemptionLink: Boolean,
                                        ihtReference: String)

object OtherDetailsSectionViewModel {

  def apply(applicationDetails: ApplicationDetails, ihtReference: String)(implicit messages: Messages, appConfig: AppConfig): OtherDetailsSectionViewModel = {

    val debtsScreenreaderText = getScreenReaderQualifyingText(
      RowCompletionStatus(applicationDetails.areAllAssetsCompleted),
      messages("page.iht.application.overview.debts.screenReader.moreDetails.link"),
      messages("page.iht.application.overview.debts.screenReader.value.link"),
      messages("page.iht.application.overview.debts.screenReader.noValue.link")
    )

    OtherDetailsSectionViewModel(
      debtRow = OverviewRow(
        id = appConfig.EstateDebtsID,
        label = messages("iht.estateReport.debts.owedFromEstate"),
        value = DisplayValueAsNegative(getDebtsDisplayValue(applicationDetails), areThereNoExemptions = true)(messages),
        completionStatus = RowCompletionStatus(applicationDetails.areAllDebtsCompleted),
        linkUrl = iht.controllers.application.debts.routes.DebtsOverviewController.onPageLoad(),
        qualifyingText = debtsScreenreaderText)(messages),
      showClaimExemptionLink = !applicationDetails.hasSeenExemptionGuidance.getOrElse(false),
      ihtReference = ihtReference)
  }

  def getDebtsDisplayValue(applicationDetails: ApplicationDetails) = applicationDetails.allLiabilities match {
    case None => NoValueEntered
    case Some(allLiabilities) if allLiabilities.areAllDebtsSectionsAnsweredNo && allLiabilities.isEmpty => AllAnsweredNo("site.noDebts")
    case Some(allLiabilities) if !allLiabilities.doesAnyDebtSectionHaveAValue => NoValueEntered
    case _ => CurrentValue(applicationDetails.totalLiabilitiesValue)
  }

  def getScreenReaderQualifyingText(isComplete: RowCompletionStatus, moreDetailText: String, valueText: String, noValueText: String) =
    isComplete match {
      case NotStarted => noValueText
      case PartiallyComplete => moreDetailText
      case _ => valueText
    }

}
