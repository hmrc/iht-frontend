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

package iht.modules

import com.google.inject.AbstractModule
import iht.config.{AppConfig, DefaultAppConfig, DefaultIHTPropertyRetriever, IhtFormPartialRetriever, IhtPropertyRetriever}
import iht.connector._
import iht.controllers._
import iht.controllers.application._
import iht.controllers.application.assets._
import iht.controllers.application.assets.household._
import iht.controllers.application.assets.insurancePolicy._
import iht.controllers.application.assets.money._
import iht.controllers.application.assets.pensions._
import iht.controllers.application.assets.properties._
import iht.controllers.application.assets.stocksAndShares._
import iht.controllers.application.assets.trusts._
import iht.controllers.application.assets.vehicles._
import iht.controllers.application.debts._
import iht.controllers.application.declaration._
import iht.controllers.application.exemptions._
import iht.controllers.application.exemptions.charity._
import iht.controllers.application.exemptions.partner._
import iht.controllers.application.exemptions.qualifyingBody._
import iht.controllers.application.gifts._
import iht.controllers.application.pdf.{PDFController, PDFControllerImpl}
import iht.controllers.application.status._
import iht.controllers.application.tnrb._
import iht.controllers.estateReports.{YourEstateReportsController, YourEstateReportsControllerImpl}
import iht.controllers.filter._
import iht.controllers.registration._
import iht.controllers.registration.applicant._
import iht.controllers.registration.deceased._
import iht.controllers.registration.executor._
import iht.controllers.testonly.{TestOnlyController, TestOnlyControllerImpl}
import iht.metrics.{IhtMetrics, IhtMetricsImpl}
import iht.utils.pdf.{BaseResourceStreamResolver, DefaultFopURIResolver, DefaultResourceStreamResolver, DefaultStylesheetResourceStreamResolver, DefaultXmlFoToPDF, FopURIResolver, StylesheetResourceStreamResolver, XmlFoToPDF}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector
import uk.gov.hmrc.play.bootstrap.http.{DefaultHttpClient, HttpClient}
import uk.gov.hmrc.play.partials.FormPartialRetriever

class Module extends AbstractModule {
  def configure() = {
    bind(classOf[AppConfig]).to(classOf[DefaultAppConfig]).asEagerSingleton
    bind(classOf[FormPartialRetriever]).to(classOf[IhtFormPartialRetriever])
    bind(classOf[IhtMetrics]).to(classOf[IhtMetricsImpl])
    bind(classOf[HttpClient]).to(classOf[DefaultHttpClient])
    bind(classOf[IhtPropertyRetriever]).to(classOf[DefaultIHTPropertyRetriever])
    bind(classOf[XmlFoToPDF]).to(classOf[DefaultXmlFoToPDF])
    bind(classOf[StylesheetResourceStreamResolver]).to(classOf[DefaultStylesheetResourceStreamResolver])
    bind(classOf[FopURIResolver]).to(classOf[DefaultFopURIResolver])
    bind(classOf[BaseResourceStreamResolver]).to(classOf[DefaultResourceStreamResolver])
    bind(classOf[FeedbackSurveyController]).to(classOf[DefaultFeedbackSurveyController])

    bind(classOf[CachingConnector]).to(classOf[CachingConnectorImpl]).asEagerSingleton
    bind(classOf[AuthConnector]).to(classOf[DefaultAuthConnector])
    bind(classOf[CitizenDetailsConnector]).to(classOf[CitizenDetailsConnectorImpl]).asEagerSingleton
    bind(classOf[IdentityVerificationConnector]).to(classOf[IdentityVerificationConnectorImpl]).asEagerSingleton
    bind(classOf[IhtConnector]).to(classOf[IhtConnectorImpl]).asEagerSingleton

    bind(classOf[IVWayfinderController]).to(classOf[IVWayfinderControllerImpl]).asEagerSingleton
    bind(classOf[SessionManagementController]).to(classOf[SessionManagementControllerImpl]).asEagerSingleton
    bind(classOf[EstateOverviewController]).to(classOf[EstateOverviewControllerImpl]).asEagerSingleton
    bind(classOf[KickoutAppController]).to(classOf[KickoutAppControllerImpl]).asEagerSingleton
    bind(classOf[AssetsOverviewController]).to(classOf[AssetsOverviewControllerImpl]).asEagerSingleton
    bind(classOf[BusinessInterestsController]).to(classOf[BusinessInterestsControllerImpl]).asEagerSingleton
    bind(classOf[ForeignController]).to(classOf[ForeignControllerImpl]).asEagerSingleton
    bind(classOf[MoneyOwedController]).to(classOf[MoneyOwedControllerImpl]).asEagerSingleton
    bind(classOf[NominatedController]).to(classOf[NominatedControllerImpl]).asEagerSingleton
    bind(classOf[OtherController]).to(classOf[OtherControllerImpl]).asEagerSingleton
    bind(classOf[HouseholdDeceasedOwnController]).to(classOf[HouseholdDeceasedOwnControllerImpl]).asEagerSingleton
    bind(classOf[HouseholdJointlyOwnedController]).to(classOf[HouseholdJointlyOwnedControllerImpl]).asEagerSingleton
    bind(classOf[HouseholdOverviewController]).to(classOf[HouseholdOverviewControllerImpl]).asEagerSingleton
    bind(classOf[InsurancePolicyDetailsAnnuityController]).to(classOf[InsurancePolicyDetailsAnnuityControllerImpl]).asEagerSingleton
    bind(classOf[InsurancePolicyDetailsDeceasedOwnController]).to(classOf[InsurancePolicyDetailsDeceasedOwnControllerImpl]).asEagerSingleton
    bind(classOf[InsurancePolicyDetailsFinalGuidanceController]).to(classOf[InsurancePolicyDetailsFinalGuidanceControllerImpl]).asEagerSingleton
    bind(classOf[InsurancePolicyDetailsInTrustController]).to(classOf[InsurancePolicyDetailsInTrustControllerImpl]).asEagerSingleton
    bind(classOf[InsurancePolicyDetailsJointController]).to(classOf[InsurancePolicyDetailsJointControllerImpl]).asEagerSingleton
    bind(classOf[InsurancePolicyDetailsMoreThanMaxValueController]).to(classOf[InsurancePolicyDetailsMoreThanMaxValueControllerImpl]).asEagerSingleton
    bind(classOf[InsurancePolicyDetailsPayingOtherController]).to(classOf[InsurancePolicyDetailsPayingOtherControllerImpl]).asEagerSingleton
    bind(classOf[InsurancePolicyOverviewController]).to(classOf[InsurancePolicyOverviewControllerImpl]).asEagerSingleton
    bind(classOf[MoneyDeceasedOwnController]).to(classOf[MoneyDeceasedOwnControllerImpl]).asEagerSingleton
    bind(classOf[MoneyJointlyOwnedController]).to(classOf[MoneyJointlyOwnedControllerImpl]).asEagerSingleton
    bind(classOf[MoneyOverviewController]).to(classOf[MoneyOverviewControllerImpl]).asEagerSingleton
    bind(classOf[PensionsChangedQuestionController]).to(classOf[PensionsChangedQuestionControllerImpl]).asEagerSingleton
    bind(classOf[PensionsOverviewController]).to(classOf[PensionsOverviewControllerImpl]).asEagerSingleton
    bind(classOf[PensionsOwnedQuestionController]).to(classOf[PensionsOwnedQuestionControllerImpl]).asEagerSingleton
    bind(classOf[PensionsValueController]).to(classOf[PensionsValueControllerImpl]).asEagerSingleton
    bind(classOf[DeletePropertyController]).to(classOf[DeletePropertyControllerImpl]).asEagerSingleton
    bind(classOf[PropertiesOverviewController]).to(classOf[PropertiesOverviewControllerImpl]).asEagerSingleton
    bind(classOf[PropertiesOwnedQuestionController]).to(classOf[PropertiesOwnedQuestionControllerImpl]).asEagerSingleton
    bind(classOf[PropertyAddressController]).to(classOf[PropertyAddressControllerImpl]).asEagerSingleton
    bind(classOf[PropertyDetailsOverviewController]).to(classOf[PropertyDetailsOverviewControllerImpl]).asEagerSingleton
    bind(classOf[PropertyOwnershipController]).to(classOf[PropertyOwnershipControllerImpl]).asEagerSingleton
    bind(classOf[PropertyTenureController]).to(classOf[PropertyTenureControllerImpl]).asEagerSingleton
    bind(classOf[PropertyTypeController]).to(classOf[PropertyTypeControllerImpl]).asEagerSingleton
    bind(classOf[PropertyValueController]).to(classOf[PropertyValueControllerImpl]).asEagerSingleton
    bind(classOf[StocksAndSharesListedController]).to(classOf[StocksAndSharesListedControllerImpl]).asEagerSingleton
    bind(classOf[StocksAndSharesNotListedController]).to(classOf[StocksAndSharesNotListedControllerImpl]).asEagerSingleton
    bind(classOf[StocksAndSharesOverviewController]).to(classOf[StocksAndSharesOverviewControllerImpl]).asEagerSingleton
    bind(classOf[TrustsMoreThanOneQuestionController]).to(classOf[TrustsMoreThanOneQuestionControllerImpl]).asEagerSingleton
    bind(classOf[TrustsOverviewController]).to(classOf[TrustsOverviewControllerImpl]).asEagerSingleton
    bind(classOf[TrustsOwnedQuestionController]).to(classOf[TrustsOwnedQuestionControllerImpl]).asEagerSingleton
    bind(classOf[TrustsValueController]).to(classOf[TrustsValueControllerImpl]).asEagerSingleton
    bind(classOf[VehiclesDeceasedOwnController]).to(classOf[VehiclesDeceasedOwnControllerImpl]).asEagerSingleton
    bind(classOf[VehiclesJointlyOwnedController]).to(classOf[VehiclesJointlyOwnedControllerImpl]).asEagerSingleton
    bind(classOf[VehiclesOverviewController]).to(classOf[VehiclesOverviewControllerImpl]).asEagerSingleton

    bind(classOf[AnyOtherDebtsController]).to(classOf[AnyOtherDebtsControllerImpl]).asEagerSingleton
    bind(classOf[DebtsOverviewController]).to(classOf[DebtsOverviewControllerImpl]).asEagerSingleton
    bind(classOf[DebtsOwedFromATrustController]).to(classOf[DebtsOwedFromATrustControllerImpl]).asEagerSingleton
    bind(classOf[FuneralExpensesController]).to(classOf[FuneralExpensesControllerImpl]).asEagerSingleton
    bind(classOf[JointlyOwnedDebtsController]).to(classOf[JointlyOwnedDebtsControllerImpl]).asEagerSingleton
    bind(classOf[MortgagesOverviewController]).to(classOf[MortgagesOverviewControllerImpl]).asEagerSingleton
    bind(classOf[MortgageValueController]).to(classOf[MortgageValueControllerImpl]).asEagerSingleton
    bind(classOf[OwedOutsideUKDebtsController]).to(classOf[OwedOutsideUKDebtsControllerImpl]).asEagerSingleton
    bind(classOf[CheckedEverythingQuestionController]).to(classOf[CheckedEverythingQuestionControllerImpl]).asEagerSingleton
    bind(classOf[DeclarationController]).to(classOf[DeclarationControllerImpl]).asEagerSingleton
    bind(classOf[DeclarationReceivedController]).to(classOf[DeclarationReceivedControllerImpl]).asEagerSingleton
    bind(classOf[ProbateApplicationFormDetailsController]).to(classOf[ProbateApplicationFormDetailsControllerImpl]).asEagerSingleton
    bind(classOf[ExemptionsGuidanceController]).to(classOf[ExemptionsGuidanceControllerImpl]).asEagerSingleton
    bind(classOf[ExemptionsGuidanceIncreasingThresholdController]).to(classOf[ExemptionsGuidanceIncreasingThresholdControllerImpl]).asEagerSingleton
    bind(classOf[ExemptionsOverviewController]).to(classOf[ExemptionsOverviewControllerImpl]).asEagerSingleton
    bind(classOf[AssetsLeftToCharityQuestionController]).to(classOf[AssetsLeftToCharityQuestionControllerImpl]).asEagerSingleton
    bind(classOf[CharitiesOverviewController]).to(classOf[CharitiesOverviewControllerImpl]).asEagerSingleton
    bind(classOf[CharityDeleteConfirmController]).to(classOf[CharityDeleteConfirmControllerImpl]).asEagerSingleton
    bind(classOf[CharityDetailsOverviewController]).to(classOf[CharityDetailsOverviewControllerImpl]).asEagerSingleton
    bind(classOf[CharityNameController]).to(classOf[CharityNameControllerImpl]).asEagerSingleton
    bind(classOf[CharityNumberController]).to(classOf[CharityNumberControllerImpl]).asEagerSingleton
    bind(classOf[CharityValueController]).to(classOf[CharityValueControllerImpl]).asEagerSingleton
    bind(classOf[AssetsLeftToPartnerQuestionController]).to(classOf[AssetsLeftToPartnerQuestionControllerImpl]).asEagerSingleton
    bind(classOf[ExemptionPartnerNameController]).to(classOf[ExemptionPartnerNameControllerImpl]).asEagerSingleton
    bind(classOf[PartnerDateOfBirthController]).to(classOf[PartnerDateOfBirthControllerImpl]).asEagerSingleton
    bind(classOf[PartnerNinoController]).to(classOf[PartnerNinoControllerImpl]).asEagerSingleton
    bind(classOf[PartnerOverviewController]).to(classOf[PartnerOverviewControllerImpl]).asEagerSingleton
    bind(classOf[PartnerPermanentHomeQuestionController]).to(classOf[PartnerPermanentHomeQuestionControllerImpl]).asEagerSingleton
    bind(classOf[PartnerValueController]).to(classOf[PartnerValueControllerImpl]).asEagerSingleton
    bind(classOf[AssetsLeftToQualifyingBodyQuestionController]).to(classOf[AssetsLeftToQualifyingBodyQuestionControllerImpl]).asEagerSingleton
    bind(classOf[QualifyingBodiesOverviewController]).to(classOf[QualifyingBodiesOverviewControllerImpl]).asEagerSingleton
    bind(classOf[QualifyingBodyDeleteConfirmController]).to(classOf[QualifyingBodyDeleteConfirmControllerImpl]).asEagerSingleton
    bind(classOf[QualifyingBodyDetailsOverviewController]).to(classOf[QualifyingBodyDetailsOverviewControllerImpl]).asEagerSingleton
    bind(classOf[QualifyingBodyNameController]).to(classOf[QualifyingBodyNameControllerImpl]).asEagerSingleton
    bind(classOf[QualifyingBodyValueController]).to(classOf[QualifyingBodyValueControllerImpl]).asEagerSingleton
    bind(classOf[GiftsDetailsController]).to(classOf[GiftsDetailsControllerImpl]).asEagerSingleton
    bind(classOf[GiftsOverviewController]).to(classOf[GiftsOverviewControllerImpl]).asEagerSingleton
    bind(classOf[GivenAwayController]).to(classOf[GivenAwayControllerImpl]).asEagerSingleton
    bind(classOf[SevenYearsGiftsValuesController]).to(classOf[SevenYearsGiftsValuesControllerImpl]).asEagerSingleton
    bind(classOf[SevenYearsGivenInLast7YearsController]).to(classOf[SevenYearsGivenInLast7YearsControllerImpl]).asEagerSingleton
    bind(classOf[SevenYearsToTrustController]).to(classOf[SevenYearsToTrustControllerImpl]).asEagerSingleton
    bind(classOf[WithReservationOfBenefitController]).to(classOf[WithReservationOfBenefitControllerImpl]).asEagerSingleton
    bind(classOf[FilterController]).to(classOf[FilterControllerImpl]).asEagerSingleton
    bind(classOf[DomicileController]).to(classOf[DomicileControllerImpl]).asEagerSingleton
    bind(classOf[TransitionController]).to(classOf[TransitionControllerImpl]).asEagerSingleton
    bind(classOf[FilterJointlyOwnedController]).to(classOf[FilterJointlyOwnedControllerImpl]).asEagerSingleton
    bind(classOf[EstimateController]).to(classOf[EstimateControllerImpl]).asEagerSingleton
    bind(classOf[UseServiceController]).to(classOf[UseServiceControllerImpl]).asEagerSingleton
    bind(classOf[RegistrationChecklistController]).to(classOf[RegistrationChecklistControllerImpl]).asEagerSingleton
    bind(classOf[SessionTimeoutController]).to(classOf[SessionTimeoutControllerImpl]).asEagerSingleton
    bind(classOf[UseIHT400Controller]).to(classOf[UseIHT400ControllerImpl]).asEagerSingleton
    bind(classOf[AgentController]).to(classOf[AgentControllerImpl]).asEagerSingleton
    bind(classOf[IVUpliftFailureController]).to(classOf[IVUpliftFailureControllerImpl]).asEagerSingleton
    bind(classOf[DeadlinesController]).to(classOf[DeadlinesControllerImpl]).asEagerSingleton

    bind(classOf[BenefitFromTrustController]).to(classOf[BenefitFromTrustControllerImpl]).asEagerSingleton
    bind(classOf[DateOfMarriageController]).to(classOf[DateOfMarriageControllerImpl]).asEagerSingleton
    bind(classOf[DeceasedWidowCheckDateController]).to(classOf[DeceasedWidowCheckDateControllerImpl]).asEagerSingleton
    bind(classOf[DeceasedWidowCheckQuestionController]).to(classOf[DeceasedWidowCheckQuestionControllerImpl]).asEagerSingleton
    bind(classOf[EstateClaimController]).to(classOf[EstateClaimControllerImpl]).asEagerSingleton
    bind(classOf[EstatePassedToDeceasedOrCharityController]).to(classOf[EstatePassedToDeceasedOrCharityControllerImpl]).asEagerSingleton
    bind(classOf[GiftsMadeBeforeDeathController]).to(classOf[GiftsMadeBeforeDeathControllerImpl]).asEagerSingleton
    bind(classOf[GiftsWithReservationOfBenefitController]).to(classOf[GiftsWithReservationOfBenefitControllerImpl]).asEagerSingleton
    bind(classOf[JointlyOwnedAssetsController]).to(classOf[JointlyOwnedAssetsControllerImpl]).asEagerSingleton
    bind(classOf[PartnerNameController]).to(classOf[PartnerNameControllerImpl]).asEagerSingleton
    bind(classOf[PermanentHomeController]).to(classOf[PermanentHomeControllerImpl]).asEagerSingleton
    bind(classOf[TnrbGuidanceController]).to(classOf[TnrbGuidanceControllerImpl]).asEagerSingleton
    bind(classOf[TnrbOverviewController]).to(classOf[TnrbOverviewControllerImpl]).asEagerSingleton
    bind(classOf[TnrbSuccessController]).to(classOf[TnrbSuccessControllerImpl]).asEagerSingleton
    bind(classOf[YourEstateReportsController]).to(classOf[YourEstateReportsControllerImpl]).asEagerSingleton
    bind(classOf[CompletedRegistrationController]).to(classOf[CompletedRegistrationControllerImpl]).asEagerSingleton
    bind(classOf[DuplicateRegistrationController]).to(classOf[DuplicateRegistrationControllerImpl]).asEagerSingleton
    bind(classOf[KickoutRegController]).to(classOf[KickoutRegControllerImpl]).asEagerSingleton
    bind(classOf[RegistrationSummaryController]).to(classOf[RegistrationSummaryControllerImpl]).asEagerSingleton
    bind(classOf[ApplicantAddressController]).to(classOf[ApplicantAddressControllerImpl]).asEagerSingleton
    bind(classOf[ApplicantTellUsAboutYourselfController]).to(classOf[ApplicantTellUsAboutYourselfControllerImpl]).asEagerSingleton
    bind(classOf[ApplyingForProbateController]).to(classOf[ApplyingForProbateControllerImpl]).asEagerSingleton
    bind(classOf[ExecutorOfEstateController]).to(classOf[ExecutorOfEstateControllerImpl]).asEagerSingleton
    bind(classOf[ProbateLocationController]).to(classOf[ProbateLocationControllerImpl]).asEagerSingleton
    bind(classOf[AboutDeceasedController]).to(classOf[AboutDeceasedControllerImpl]).asEagerSingleton
    bind(classOf[DeceasedAddressDetailsOutsideUKController]).to(classOf[DeceasedAddressDetailsOutsideUKControllerImpl]).asEagerSingleton
    bind(classOf[DeceasedAddressDetailsUKController]).to(classOf[DeceasedAddressDetailsUKControllerImpl]).asEagerSingleton
    bind(classOf[DeceasedAddressQuestionController]).to(classOf[DeceasedAddressQuestionControllerImpl]).asEagerSingleton
    bind(classOf[DeceasedDateOfDeathController]).to(classOf[DeceasedDateOfDeathControllerImpl]).asEagerSingleton
    bind(classOf[DeceasedPermanentHomeController]).to(classOf[DeceasedPermanentHomeControllerImpl]).asEagerSingleton
    bind(classOf[CoExecutorPersonalDetailsController]).to(classOf[CoExecutorPersonalDetailsControllerImpl]).asEagerSingleton
    bind(classOf[DeleteCoExecutorController]).to(classOf[DeleteCoExecutorControllerImpl]).asEagerSingleton
    bind(classOf[ExecutorOverviewController]).to(classOf[ExecutorOverviewControllerImpl]).asEagerSingleton
    bind(classOf[OtherPersonsAddressController]).to(classOf[OtherPersonsAddressControllerImpl]).asEagerSingleton
    bind(classOf[OthersApplyingForProbateController]).to(classOf[OthersApplyingForProbateControllerImpl]).asEagerSingleton

    bind(classOf[ApplicationClosedController]).to(classOf[ApplicationClosedControllerImpl]).asEagerSingleton
    bind(classOf[ApplicationInReviewController]).to(classOf[ApplicationInReviewControllerImpl]).asEagerSingleton

    bind(classOf[PDFController]).to(classOf[PDFControllerImpl]).asEagerSingleton

    bind(classOf[TestOnlyController]).to(classOf[TestOnlyControllerImpl]).asEagerSingleton
  }
}

