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

import iht.models.application.debts.AllLiabilities
import iht.models.application.assets.{AllAssets, Properties}
import iht.testhelpers.CommonBuilder
import iht.{FakeIhtApp, TestUtils}
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.play.test.UnitSpec

class OtherDetailsSectionViewModelTest
  extends UnitSpec with FakeIhtApp with MockitoSugar with TestUtils with BeforeAndAfter with I18nSupport {

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  val applicationDetailsGuidanceSeen = CommonBuilder.buildApplicationDetails copy (hasSeenExemptionGuidance=Some(true))
  val applicationDetailsGuidanceNotSeen = CommonBuilder.buildApplicationDetails copy (hasSeenExemptionGuidance=Some(false))

  val ihtRef = "ABC123"

  "Other Details Section View Model" must {

    //region Debts overview row tests

    "have an id of 'debts' for the debts row" in {
      val viewModel = OtherDetailsSectionViewModel(applicationDetailsGuidanceNotSeen, ihtRef)
      viewModel.debtRow.id shouldBe "debts"
    }

    "have the correct caption for the debts row" in {
      val viewModel = OtherDetailsSectionViewModel(applicationDetailsGuidanceNotSeen, ihtRef)
      viewModel.debtRow.label shouldBe Messages("iht.estateReport.debts.owedFromEstate")
    }

    "have a blank value for debts when there are no debts" in {
      val viewModel = OtherDetailsSectionViewModel(applicationDetailsGuidanceNotSeen, ihtRef)
      viewModel.debtRow.value shouldBe ""
    }

    "have a blank value for debts when there are debts but no values have been given" in {
      val appDetails = applicationDetailsGuidanceNotSeen copy (allLiabilities = Some(buildEveryLiabilityWithNoValues))
      val viewModel = OtherDetailsSectionViewModel(appDetails, ihtRef)
      viewModel.debtRow.value shouldBe ""
    }

    "have the correct value with a pound sign for debts where there are some debts" in {
      val appDetails = applicationDetailsGuidanceNotSeen copy (allLiabilities = Some(buildEveryLiability))
      val viewModel = OtherDetailsSectionViewModel(appDetails, ihtRef)

      viewModel.debtRow.value shouldBe "£500.00"
    }

    "have the correct text when all answers to the debts questions are 'No'" in {
      val appDetails = applicationDetailsGuidanceNotSeen copy (allLiabilities = Some(CommonBuilder.buildAllLiabilitiesAnsweredNo))
      val viewModel = OtherDetailsSectionViewModel(appDetails, ihtRef)

      viewModel.debtRow.value shouldBe Messages("site.noDebts")
    }

    "show View or Change when all debts are completed" in {
      val appDetails = applicationDetailsGuidanceNotSeen copy (allLiabilities = Some(buildEveryLiability),
        allAssets = Some(AllAssets(properties = Some(Properties(Some(false))))))

      val viewModel = OtherDetailsSectionViewModel(appDetails, ihtRef)

      viewModel.debtRow.linkText shouldBe Messages("iht.viewOrChange")
    }

    "show Start when no debts questions have been answered" in {
      val viewModel = OtherDetailsSectionViewModel(applicationDetailsGuidanceNotSeen, ihtRef)
      viewModel.debtRow.linkText shouldBe Messages("iht.start")
    }

    "show Give more details when some debts questions have been answered" in {
      val appDetails = applicationDetailsGuidanceNotSeen copy (allLiabilities = Some(buildSomeLiabilities))

      val viewModel = OtherDetailsSectionViewModel(appDetails, ihtRef)

      viewModel.debtRow.linkText shouldBe Messages("iht.giveMoreDetails")
    }

    "have the correct URL for the debts link" in {
      val viewModel = OtherDetailsSectionViewModel(applicationDetailsGuidanceNotSeen, ihtRef)
      viewModel.debtRow.linkUrl shouldBe iht.controllers.application.debts.routes.DebtsOverviewController.onPageLoad()
    }

    //endregion

    "not show the claim exemption link when exemptions are unlocked" in {
      val viewModel = OtherDetailsSectionViewModel(applicationDetailsGuidanceSeen, ihtRef)
      viewModel.showClaimExemptionLink shouldBe false
    }

    "show the claim exemption link when exemptions are not unlocked" in {
      val viewModel = OtherDetailsSectionViewModel(applicationDetailsGuidanceNotSeen, ihtRef)
      viewModel.showClaimExemptionLink shouldBe true
    }

    // TODO: Move these elsewhere if and when common builder is refactored
    lazy val buildEveryLiability = AllLiabilities(
      funeralExpenses=Some(CommonBuilder.buildBasicEstateElementLiabilityWithValue),
      trust = Some(CommonBuilder.buildBasicEstateElementLiabilityWithValue),
      debtsOutsideUk = Some(CommonBuilder.buildBasicEstateElementLiabilityWithValue),
      jointlyOwned = Some(CommonBuilder.buildBasicEstateElementLiabilityWithValue),
      other = Some(CommonBuilder.buildBasicEstateElementLiabilityWithValue),
      mortgages = None
    )

    lazy val buildEveryLiabilityWithNoValues = AllLiabilities(
      funeralExpenses = Some(CommonBuilder.buildBasicEstateElementLiabilityWithNoValue),
      trust = Some(CommonBuilder.buildBasicEstateElementLiabilityWithNoValue),
      debtsOutsideUk = Some(CommonBuilder.buildBasicEstateElementLiabilityWithNoValue),
      jointlyOwned = Some(CommonBuilder.buildBasicEstateElementLiabilityWithNoValue),
      other = Some(CommonBuilder.buildBasicEstateElementLiabilityWithNoValue),
      mortgages = None
    )

    lazy val buildSomeLiabilities = AllLiabilities(
      funeralExpenses=Some(CommonBuilder.buildBasicEstateElementLiabilityWithValue)
    )
  }
}
