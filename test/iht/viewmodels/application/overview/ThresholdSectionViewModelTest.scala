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
import iht.models.application.tnrb.WidowCheck
import iht.testhelpers.CommonBuilder
import iht.testhelpers.TestHelper._
import iht.{FakeIhtApp, TestUtils}
import org.joda.time.LocalDate
import org.scalatest.BeforeAndAfter
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.mvc.MessagesControllerComponents

class ThresholdSectionViewModelTest extends FakeIhtApp with MockitoSugar with TestUtils with BeforeAndAfter with I18nSupport {
  val mockControllerComponents: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val lang: Lang = Lang.defaultLang
  override val messagesApi: MessagesApi = mockControllerComponents.messagesApi
  implicit val messages: Messages = messagesApi.preferred(Seq(lang)).messages

  val dodWithInClaimDate = LocalDate.now().minusYears(1)
  val deceasedDateOfDeath = CommonBuilder.buildDeceasedDateOfDeath.copy(dodWithInClaimDate)

  val regDetailsSingle = CommonBuilder.buildRegistrationDetails1.copy(deceasedDateOfDeath = Some(deceasedDateOfDeath))
  val regDetailsMarried = CommonBuilder.buildRegistrationDetails4.copy(deceasedDateOfDeath = Some(deceasedDateOfDeath))
  val regDetailsWidowed = CommonBuilder.buildRegistrationDetails5.copy(deceasedDateOfDeath = Some(deceasedDateOfDeath))
  val applicationDetails = CommonBuilder.buildApplicationDetails
  val appDetailsTnrbUnlocked = applicationDetails copy(widowCheck = Some(WidowCheck(Some(true), Some(new LocalDate()))))
  val appDetailsTnrbNotAvailable = applicationDetails copy(widowCheck = Some(WidowCheck(Some(false), None)))
  val appDetailsTnrbComplete = appDetailsTnrbUnlocked copy(increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility))

  "Threshold Section view model" must {

   "have an id of 'threshold' for the increase threshold row" in {
      val viewModel = ThresholdSectionViewModel(regDetailsSingle, applicationDetails)

      viewModel.thresholdRow.id mustBe EstateIncreasingID
    }

    "have the correct caption for the threshold row" in {
      val viewModel = ThresholdSectionViewModel(regDetailsSingle, applicationDetails)

      viewModel.thresholdRow.label mustBe messagesApi("iht.estateReport.ihtThreshold")
    }

    "have the correct value for the threshold row when it has not been increased" in {
      val viewModel = ThresholdSectionViewModel(regDetailsSingle, applicationDetails)

      viewModel.thresholdRow.value mustBe "£325,000.00"
    }

    "have the correct value for the threshold row when it has been increased" in {
      val viewModel = ThresholdSectionViewModel(regDetailsSingle, appDetailsTnrbComplete)

      viewModel.thresholdRow.value mustBe "£650,000.00"
    }

    "show the threshold as a normal row when it has not been increased" in {
      val viewModel = ThresholdSectionViewModel(regDetailsSingle, applicationDetails)

      viewModel.thresholdRow.renderAsTotalRow mustBe false
    }

    "show the threshold as a total row when it has been increased" in {
      val appDetails = applicationDetails copy(increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility),
        widowCheck = Some(CommonBuilder.buildWidowedCheck))
      val viewModel = ThresholdSectionViewModel(regDetailsSingle, appDetails)

      viewModel.thresholdRow.renderAsTotalRow mustBe true
    }

    "not show the Increasing the Threshold link if the deceased was Single" in {
      val viewModel = ThresholdSectionViewModel(regDetailsSingle, applicationDetails)

      viewModel.showIncreaseThresholdLink mustBe false
    }

    "show the Increasing the Threshold link if the deceased was Married and the transfer section has not been accessed" in {
      val dodWithInClaimDate = LocalDate.now().minusYears(1)
      val deceasedDateOfDeath = CommonBuilder.buildDeceasedDateOfDeath.copy(dodWithInClaimDate)

      val viewModel = ThresholdSectionViewModel(regDetailsMarried.copy(
        deceasedDateOfDeath = Some(deceasedDateOfDeath)), applicationDetails)

      viewModel.showIncreaseThresholdLink mustBe true
    }

    "not show the Increasing the Threshold link if the deceased was Married and the transfer section has been accessed" in {
      val dodWithInClaimDate = LocalDate.now().minusYears(1)
      val deceasedDateOfDeath = CommonBuilder.buildDeceasedDateOfDeath.copy(dodWithInClaimDate)

      val viewModel = ThresholdSectionViewModel(regDetailsMarried.copy(
        deceasedDateOfDeath = Some(deceasedDateOfDeath)), appDetailsTnrbUnlocked)
      viewModel.showIncreaseThresholdLink mustBe false
    }

    "not show the Increasing the Threshold row if the deceased was Single" in {
      val viewModel = ThresholdSectionViewModel(regDetailsSingle, applicationDetails)

      viewModel.increasingThresholdRow.isEmpty mustBe true
    }

    "not show the Increasing the Threshold link if the claim date is after 2 years of  " +
      "deceased date of death, deceased was Married and the transfer section has not been accessed" in {

      val dodNotInRangeOfClaimDate = LocalDate.now().minusYears(3)
      val deceasedDateOfDeath = CommonBuilder.buildDeceasedDateOfDeath.copy(dodNotInRangeOfClaimDate)

      val viewModel = ThresholdSectionViewModel(regDetailsMarried.copy(
        deceasedDateOfDeath = Some(deceasedDateOfDeath)), appDetailsTnrbUnlocked)
      viewModel.showIncreaseThresholdLink mustBe false
    }

    "not show the Increasing the Threshold row if the deceased was Married and the transfer section has not been accessed" in {
      val viewModel = ThresholdSectionViewModel(regDetailsMarried, applicationDetails)

      viewModel.increasingThresholdRow.isEmpty mustBe true
    }

    "show the Increasing the Threshold row if the section has been accessed" in {
      val viewModel = ThresholdSectionViewModel(regDetailsMarried, appDetailsTnrbUnlocked)
      viewModel.increasingThresholdRow.isDefined mustBe true
    }

    "have the id 'increasing-threshold' for the Increasing the Threshold row" in {
      val viewModel = ThresholdSectionViewModel(regDetailsMarried, appDetailsTnrbUnlocked)
      viewModel.increasingThresholdRow.get.id mustBe EstateIncreasingID
    }

    "have the correct caption for the Increasing the Threshold row" in {
      val viewModel = ThresholdSectionViewModel(regDetailsMarried, appDetailsTnrbUnlocked)
      viewModel.increasingThresholdRow.get.label mustBe messagesApi("iht.estateReport.tnrb.increasingThreshold")
    }

    "not show a value in the Increasing the Threshold row if the section is incomplete" in {
      val viewModel = ThresholdSectionViewModel(regDetailsMarried, appDetailsTnrbUnlocked)
      viewModel.increasingThresholdRow.get.value mustBe ""
    }

    "show link text of Give more details in the Increasing the Threshold row if the section is incomplete" in {
      val viewModel = ThresholdSectionViewModel(regDetailsMarried, appDetailsTnrbUnlocked)
      viewModel.increasingThresholdRow.get.linkText mustBe messagesApi("iht.giveMoreDetails")
    }

    "show 'Increased' as the value in the Increasing the Threshold row if the section is complete" in {
      val viewModel = ThresholdSectionViewModel(regDetailsSingle, appDetailsTnrbComplete)
      viewModel.increasingThresholdRow.get.value mustBe messagesApi("page.iht.application.estateOverview.increaseThreshold.increased")
    }

    "show link text of View or change in the Increasing the Threshold row if the section is complete" in {
      val viewModel = ThresholdSectionViewModel(regDetailsMarried, appDetailsTnrbComplete)
      viewModel.increasingThresholdRow.get.linkText mustBe messagesApi("iht.viewOrChange")
    }

    "show 'Not available' as the value in the Increasing the Threshold row if the Widow Check was answered No'" in {
      val viewModel = ThresholdSectionViewModel(regDetailsMarried, appDetailsTnrbNotAvailable)
      viewModel.increasingThresholdRow.get.value mustBe messagesApi("page.iht.application.estateOverview.increaseThreshold.notAvailable")
    }

    "show link text of View or change in the Increasing the Threshold row if the Widow Check was answered No'" in {
      val viewModel = ThresholdSectionViewModel(regDetailsMarried, appDetailsTnrbNotAvailable)
      viewModel.increasingThresholdRow.get.linkText mustBe messagesApi("iht.viewOrChange")
    }

    "have target link for Widowed check question page in the Increasing the Threshold row if predeceased date has not been saved" +
      "and marital status is married'" in {
      val viewModel = ThresholdSectionViewModel(regDetailsMarried, appDetailsTnrbNotAvailable.copy(widowCheck = Some(WidowCheck(Some(true), None))))
      viewModel.increasingThresholdRow.get.linkUrl.toString mustBe
        iht.controllers.application.tnrb.routes.DeceasedWidowCheckQuestionController.onPageLoad().url
    }

    "have target link for Widowed date of death page in the Increasing the Threshold row if predeceased date has not been saved" +
      "and marital status is Widowed'" in {
      val viewModel = ThresholdSectionViewModel(regDetailsWidowed, appDetailsTnrbNotAvailable.copy(widowCheck = Some(WidowCheck(Some(true), None))))
      viewModel.increasingThresholdRow.get.linkUrl.toString mustBe
        iht.controllers.application.tnrb.routes.DeceasedWidowCheckDateController.onPageLoad().url
    }

    "have target link for Tnrb overview page in the Increasing the Threshold row if predeceased date of death has been saved'" in {
      val viewModel = ThresholdSectionViewModel(regDetailsMarried,
                        appDetailsTnrbNotAvailable.copy(widowCheck = Some(WidowCheck(Some(true), Some(new LocalDate(1998, 12, 12))))))
      viewModel.increasingThresholdRow.get.linkUrl.toString mustBe
        iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad().url
    }
  }
}
