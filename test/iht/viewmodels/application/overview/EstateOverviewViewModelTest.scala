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


import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import org.joda.time.LocalDate
import play.api.test.FakeRequest

class EstateOverviewViewModelTest extends ViewTestHelper {

  val ihtReference = Some("ABC")
  val registrationDetails = CommonBuilder.buildRegistrationDetails1.copy(ihtReference = ihtReference)
  val applicationDetailsGuidanceSeen = CommonBuilder.buildApplicationDetails copy (hasSeenExemptionGuidance=Some(true))
  val applicationDetailsGuidanceNotSeen = CommonBuilder.buildApplicationDetails copy (hasSeenExemptionGuidance=Some(false))

  val fakeDeadline = new LocalDate()
  implicit val request = FakeRequest()

  "EstateOverviewViewModel" must {

    "have the correct deceased name when created" in {
      val viewModel = EstateOverviewViewModel(registrationDetails, applicationDetailsGuidanceNotSeen, fakeDeadline)

      viewModel.deceasedName mustBe registrationDetails.deceasedDetails.get.name
    }

    "instruct the Assets and Gifts section to behave as an Increasing Estate Value section if exemption guidance has been seen" in {
      val viewModel = EstateOverviewViewModel(registrationDetails, applicationDetailsGuidanceSeen, fakeDeadline)
      viewModel.assetsAndGiftsSection.behaveAsIncreasingTheEstateSection mustBe true
    }

    "instruct the Assets and Gifts section to behave as a normal section if exemption guidance has not been seen and no " +
      "exemptions questions have been answered" in {
      val viewModel = EstateOverviewViewModel(registrationDetails, applicationDetailsGuidanceNotSeen, fakeDeadline)
      viewModel.assetsAndGiftsSection.behaveAsIncreasingTheEstateSection mustBe false
    }

    "contain an Other Details section if no exemption values have been added" in {
      val viewModel = EstateOverviewViewModel(registrationDetails, applicationDetailsGuidanceNotSeen, fakeDeadline)

      viewModel.otherDetailsSection.isDefined mustBe true
    }

    "contain an Other Details section if exemption guidance has been seen but no exemption values have been added" in {
      val viewModel = EstateOverviewViewModel(registrationDetails, applicationDetailsGuidanceSeen, fakeDeadline)

      viewModel.otherDetailsSection.isDefined mustBe true
    }

    "instruct the Other Details section not to show the claim exemptions link when exemptions guidance has been seen" in {
      val viewModel = EstateOverviewViewModel(registrationDetails, applicationDetailsGuidanceSeen, fakeDeadline)
      viewModel.otherDetailsSection.get.showClaimExemptionLink mustBe false
    }

    "contain an Other Details section if exemptions questions have been answered but no numeric values have been entered" in {
      val appDetails = applicationDetailsGuidanceSeen copy (allExemptions = Some(CommonBuilder.buildAllExemptions))
      val viewModel = EstateOverviewViewModel(registrationDetails, appDetails, fakeDeadline)

      viewModel.otherDetailsSection.isDefined mustBe true
    }

    "not contain an Other Details section if an exemption value has been added" in {
      val appDetails = CommonBuilder.buildSomeExemptions(applicationDetailsGuidanceNotSeen)
      val viewModel = EstateOverviewViewModel(registrationDetails, appDetails, fakeDeadline)

      viewModel.otherDetailsSection.isDefined mustBe false
    }

    "contain a Reducing the Estate section if exemptions guidance has been seen" in {
      val viewModel = EstateOverviewViewModel(registrationDetails, applicationDetailsGuidanceSeen, fakeDeadline)
      viewModel.reducingEstateValueSection.isDefined mustBe true
    }

    "not contain a Reducing the Estate section if exemptions have not been entered and guidance has not been seen" in {
      val viewModel = EstateOverviewViewModel(registrationDetails, applicationDetailsGuidanceNotSeen, fakeDeadline)

      viewModel.reducingEstateValueSection.isDefined mustBe false
    }

    "not contain a total row if exemptions guidance has not been seen and no exemptions questions have been answered" in {
      val viewModel = EstateOverviewViewModel(registrationDetails, applicationDetailsGuidanceNotSeen, fakeDeadline)

      viewModel.grandTotalRow.isDefined mustBe false
    }

    "contain a total row entitled 'Value of assets and gifts' if exemptions guidance has been seen but no value added" in {
      val viewModel = EstateOverviewViewModel(registrationDetails, applicationDetailsGuidanceSeen, fakeDeadline)

      viewModel.grandTotalRow.isDefined mustBe true
      viewModel.grandTotalRow.get.label mustBe messagesApi("page.iht.application.estateOverview.valueOfAssetsAndGifts")
    }

    "contain a total row entitled 'Value of assets and gifts' if exemptions questions have been answered but no value entered" in {
      val appDetails = CommonBuilder.buildExemptionsWithNoValues(applicationDetailsGuidanceNotSeen)
      val viewModel = EstateOverviewViewModel(registrationDetails, appDetails, fakeDeadline)

      viewModel.grandTotalRow.isDefined mustBe true
      viewModel.grandTotalRow.get.label mustBe messagesApi("page.iht.application.estateOverview.valueOfAssetsAndGifts")    }

    "contain a total row entitled 'Total value of the estate' if an exemption value has been added" in {
      val appDetails = CommonBuilder.buildSomeExemptions(applicationDetailsGuidanceNotSeen)
      val viewModel = EstateOverviewViewModel(registrationDetails, appDetails, fakeDeadline)

      viewModel.grandTotalRow.isDefined mustBe true
      viewModel.grandTotalRow.get.label mustBe messagesApi("page.iht.application.estateOverview.totalValueOfTheEstate")
    }

    "contain ihtReference when created" in {
      val viewModel = EstateOverviewViewModel(registrationDetails, applicationDetailsGuidanceNotSeen, fakeDeadline)

      viewModel.ihtReference mustBe registrationDetails.ihtReference.getOrElse("")
    }

    "have the correct value with a pound sign when there are some assets, gifts and debts but no exemptions" in {
      val appDetails = CommonBuilder.buildSomeGifts(CommonBuilder.buildApplicationDetailsWithAllAssets) copy (
        allLiabilities = Some(CommonBuilder.buildEveryLiability),
        hasSeenExemptionGuidance=Some(true)
        )

      val viewModel = EstateOverviewViewModel(registrationDetails, appDetails, fakeDeadline)
      viewModel.grandTotalRow.get.value mustBe "£57,345.00"
    }

    "have the correct value with a pound sign when there are some assets, gifts, debts and exemptions" in {
      val appDetails = CommonBuilder.buildSomeExemptions(
        CommonBuilder.buildSomeGifts(
          CommonBuilder.buildApplicationDetailsWithAllAssets)) copy (
        hasSeenExemptionGuidance=Some(true),
        allLiabilities = Some(CommonBuilder.buildEveryLiability))

      val viewModel = EstateOverviewViewModel(registrationDetails, appDetails, fakeDeadline)
      // Assets + gifts is £57,345.00.  Debts are £500 and exemptions are £44.45.
      viewModel.grandTotalRow.get.value mustBe "£56,800.55"
    }

    "have a value of £0.00 when there are no assets or gifts" in {
      val viewModel = EstateOverviewViewModel(registrationDetails, applicationDetailsGuidanceSeen, fakeDeadline)

      viewModel.grandTotalRow.get.value mustBe "£0.00"
    }

    "have a value of £0.00 when the total of assets and gifts is less than the total of debts and exemptions" in {
      val appDetails = CommonBuilder.buildSomeExemptions(applicationDetailsGuidanceNotSeen) copy (
        hasSeenExemptionGuidance=Some(true),
        allLiabilities = Some(CommonBuilder.buildEveryLiability))

      val viewModel = EstateOverviewViewModel(registrationDetails, appDetails, fakeDeadline)
      viewModel.grandTotalRow.get.value mustBe "£0.00"
    }

    "display the correct submission deadline" in {
      val viewModel = EstateOverviewViewModel(registrationDetails, applicationDetailsGuidanceNotSeen, new LocalDate(2015, 4, 3))
      viewModel.submissionDeadline mustBe "3 April 2015"
    }

    "create declaration section with the given Registration and Application details" in {
      val viewModel = EstateOverviewViewModel(registrationDetails, applicationDetailsGuidanceNotSeen, new LocalDate(2015, 4, 3))
      viewModel.declarationSection mustBe DeclarationSectionViewModel(ihtReference.getOrElse(""),InComplete)
    }
  }
}
