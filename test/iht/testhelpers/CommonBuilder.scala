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

package iht.testhelpers

import scala.util.Random
import iht.constants.IhtProperties
import iht.models.application.{ApplicationDetails, IhtApplication, ProbateDetails}
import iht.models.application.basicElements.{BasicEstateElement, ShareableBasicEstateElement}
import iht.models.application.debts._
import iht.models.application.assets._
import iht.models.application.gifts._
import iht.models.application.exemptions._
import iht.models.application.tnrb._
import iht.models.{ReturnDetails, _}
import iht.utils.{CommonHelper, KickOutReason, ApplicationStatus => AppStatus}
import models.des.iht_return.{Declaration, IHTReturn}
import models.des.{Deceased, Event, EventRegistration}
import org.joda.time.{DateTime, LocalDate}
import org.mockito.invocation.InvocationOnMock
import play.api.mvc.Call
import uk.gov.hmrc.domain.{Nino, TaxIds}
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{Accounts, ConfidenceLevel, CredentialStrength}
import uk.gov.hmrc.play.frontend.auth.{AuthContext, LoggedInUser, Principal}


object CommonBuilder {
  val rng = new Random

  def randomElement[T](elements: List[T]) = elements(rng.nextInt(elements.length))

  def firstNameGenerator = {
    val firstNames = List("Mary", "James", "Patricia", "John", "Jennifer", "Robert")
    randomElement(firstNames)
  }

  def surnameGenerator = {
    val surnames = List("Smith", "Jones", "Williams", "Taylor", "Brown", "Davies")
    randomElement(surnames)
  }

  val mockitoUnitAnswer = new org.mockito.stubbing.Answer[Unit] {
    def answer(i: InvocationOnMock): Unit = {
    }
  }

  val DefaultCall1 = Call("GET", "Call1")
  val DefaultCall2 = Call("GET", "Call2")

  val DefaultId = "1"
  val DefaultDeceasedDOD = new LocalDate(2011, 12, 12)
  val DefaultFirstName = firstNameGenerator
  val DefaultMiddleName = ""
  val DefaultLastName = surnameGenerator
  val DefaultName = s"$DefaultFirstName $DefaultLastName"
  val DefaultNino = NinoBuilder.defaultNino
  val DefaultUtr = None
  val DefaultDateOfBirth = new LocalDate(1998, 12, 12)
  val DefaultDateOfBirthDateTime = new DateTime(DateTime.parse("2000-08-16T07:22:05Z"))
  val DefaultPostCode = "AA1 1AA"
  val DefaultUkAddress = new UkAddress("addr1", "addr2", Some("addr3"), Some("addr4"), DefaultPostCode)
  val DefaultUkAddress2 = new UkAddress("addr21", "addr22", Some("addr23"), Some("addr24"), "BB1 1BB")
  val DefaultPhoneNo = "02079460093"
  val DefaultContactDetails = new iht.models.ContactDetails(DefaultPhoneNo, Some("a@example.com"))

  val DefaultCountry = TestHelper.ApplicantCountryEnglandOrWales
  val DefaultRole = TestHelper.RoleLeadExecutor
  val DefaultCoExecutorRole = TestHelper.RoleExecutor
  val DefaultDomicile = TestHelper.DomicileEnglandOrWales
  val DefaultMaritalStatus = TestHelper.MaritalStatusMarried
  val DefaultIHTReference = Some("ABC1234567890")
  val DefaultAcknowledgmentReference = CommonHelper.generateAcknowledgeReference
  val DefaultIsAddressInUK = Some(true)

  //Default value for TnrbEligibilty Model
  val DefaultIsPartnerLivingInUk = Some(true)
  val DefaultIsGiftMadeBeforeDeath = Some(false)
  val DefaultIsStateClaimAnyBusiness = Some(false)
  val DefaultIsPartnerGiftWithResToOther = Some(false)
  val DefaultIsPartnerBenFromTrust = Some(false)
  val DefaultIsEstateBelowIhtThresholdApplied = Some(true)
  val DefaultIsJointAssetPassed = Some(true)
  val DefaultDateOfMarriage = Some(new LocalDate(1992, 12, 11))
  val DefaultPartnerDOD = Some(new LocalDate(1993, 12, 11))

  //Default value for WidowedCheck Model
  val DefaultWidowed = Some(true)
  val DefaultDateOfPreDeceased = new LocalDate(1987, 12, 12)

  //Default values for Application Model
  val DefaultIsAssetForDeceasedPartner = Some(true)
  val DefaultIsPartnerHomeInUK = Some(true)
  val DefaultTotalAssets = BigDecimal(120)

  //Default valuse for IhtApplication
  val DefaultIhtRefNo = "A1912232"
  val DefaultDOD = new LocalDate(2014, 10, 5)
  val DefaultEntryType = "Free Estate"
  val DefaultRegistrationDate = new LocalDate(2014, 10, 5)
  val DefaultCurrentStatus = "Awaiting Return"

  //Default values for Probate Details
  val DefaultGrossEstateforIHTPurposes = BigDecimal(123456.78)
  val DefaultGrossEstateforProbatePurposes = BigDecimal(123456.78)
  val DefaultTotalDeductionsForProbatePurposes = BigDecimal(123456.78)
  val DefaultNetEstateForProbatePurposes = BigDecimal(123456.78)
  val DefaultValueOfEstateOutsideOfTheUK = BigDecimal(123456.78)
  val DefaultValueOfTaxPaid = BigDecimal(0)
  val DefaultProbateReference = "12345678A01-123"

  // Default values for Return details
  val DefaultReturnId = "1234567890"
  val DefaultReturnVersionNumber = "0123"
  val DefaultReturnDate = "2015-05-01"
  val DefaultSubmitterRole = "Lead Executor"

  // Default values for IhtReturn Declaration object
  val DefaultReasonForBeingBelowLimit = Some("Transferred Nil Rate Band")
  val DefaultDeclarationAccepted = Some(true)
  val DefaultCoExecutorsAccepted = Some(true)
  val DefaultDeclarationDate = Some(new LocalDate(2014, 10, 5))

  // Creates the DeceasedDateOfDeath with default values
  val buildDeceasedDateOfDeath = new DeceasedDateOfDeath(
    dateOfDeath = DefaultDeceasedDOD)

  // Creates the ApplicantDetails with default values
  val buildApplicantDetails = ApplicantDetails(
    firstName = Some(DefaultFirstName),
    middleName = Some(DefaultMiddleName),
    lastName = Some(DefaultLastName),
    nino = Some(DefaultNino),
    utr = DefaultUtr,
    dateOfBirth = Some(DefaultDateOfBirth),
    ukAddress = Some(DefaultUkAddress),
    country = Some(DefaultCountry),
    phoneNo = Some(DefaultPhoneNo),
    role = Some(DefaultRole),
    doesLiveInUK = Some(true),
    isApplyingForProbate = Some(true)
  )

  // Creates the DeceasedDetails with default values
  val buildDeceasedDetails = DeceasedDetails(
    firstName = Some(DefaultFirstName),
    middleName = Some(DefaultMiddleName),
    lastName = Some(DefaultLastName),
    nino = Some(DefaultNino),
    ukAddress = Some(DefaultUkAddress),
    utr = DefaultUtr,
    dateOfBirth = Some(DefaultDateOfBirth),
    domicile = Some(DefaultDomicile),
    maritalStatus = Some(DefaultMaritalStatus),
    isAddressInUK = DefaultIsAddressInUK)

  // Creates the CoExecutor with default values
  val buildCoExecutor = CoExecutor(
    id = Some(DefaultId),
    firstName = DefaultFirstName,
    middleName = None,
    lastName = DefaultLastName,
    dateOfBirth = DefaultDateOfBirth,
    nino = DefaultNino,
    utr = DefaultUtr,
    ukAddress = Some(DefaultUkAddress),
    contactDetails = DefaultContactDetails,
    role = Some(DefaultCoExecutorRole),
    isAddressInUk = Some(true))

  val DefaultCoExecutor1 = CommonBuilder.buildCoExecutor copy(id = Some("1"), firstName = firstNameGenerator, lastName = surnameGenerator)
  val DefaultCoExecutor2 = CommonBuilder.buildCoExecutor copy(id = Some("2"), firstName = firstNameGenerator, lastName = surnameGenerator)
  val DefaultCoExecutor3 = CommonBuilder.buildCoExecutor copy(id = Some("3"), firstName = firstNameGenerator, lastName = surnameGenerator)

  def buildCoExecutorWithId(identifier: Option[String] = Some(DefaultId)) = CoExecutor(
    id = identifier,
    firstName = DefaultFirstName,
    middleName = None,
    lastName = DefaultLastName,
    dateOfBirth = DefaultDateOfBirth,
    nino = DefaultNino,
    utr = DefaultUtr,
    ukAddress = Some(DefaultUkAddress),
    contactDetails = DefaultContactDetails,
    role = Some(DefaultCoExecutorRole),
    isAddressInUk = Some(true))

  def buildCoExecutorPersonalDetails(identifier: Option[String] = Some(DefaultId)) = CoExecutor(
    id = identifier,
    firstName = DefaultFirstName,
    middleName = None,
    lastName = DefaultLastName,
    dateOfBirth = DefaultDateOfBirth,
    nino = DefaultNino,
    utr = None,
    ukAddress = None,
    contactDetails = ContactDetails(DefaultPhoneNo),
    role = None,
    isAddressInUk = Some(true)
  )

  // Creates Return Details with default values
  val buildReturnDetails = ReturnDetails(
    returnDate = Some(DefaultReturnDate),
    returnId = Some(DefaultReturnId),
    returnVersionNumber = Some(DefaultReturnVersionNumber),
    submitterRole = DefaultSubmitterRole
  )

  // Creates the RegistrationDetails with default values
  val buildRegistrationDetails = RegistrationDetails(
    deceasedDateOfDeath = None,
    applicantDetails = None,
    deceasedDetails = None,
    coExecutors = Seq(),
    ihtReference = DefaultIHTReference,
    acknowledgmentReference = DefaultAcknowledgmentReference,
    returns = Seq(),
    areOthersApplyingForProbate = None
  )

  val buildRegistrationDetailsWithOthersApplyingForProbate = buildRegistrationDetails copy
                                                                  (areOthersApplyingForProbate = Some(true))


  val buildRegistrationDetailsWithCoExecutors = buildRegistrationDetails copy(areOthersApplyingForProbate = Some(true),
                                                        coExecutors = Seq(buildCoExecutor))

  val buildRegistrationDetailsWithDeceasedDetails =
    buildRegistrationDetails copy(deceasedDetails = Some(buildDeceasedDetails),
                                  deceasedDateOfDeath = Some(DeceasedDateOfDeath(DefaultDeceasedDOD)))


  //Create WidowCheck Model with default values
  val buildWidowedCheck = WidowCheck(
    widowed = DefaultWidowed,
    dateOfPreDeceased = Some(DefaultDateOfPreDeceased)
  )

  //Create TnrbEligibiltyModel with default value
  val buildTnrbEligibility = TnrbEligibiltyModel(
    isPartnerLivingInUk = DefaultIsPartnerLivingInUk,
    isGiftMadeBeforeDeath = DefaultIsGiftMadeBeforeDeath,
    isStateClaimAnyBusiness = DefaultIsStateClaimAnyBusiness,
    isPartnerGiftWithResToOther = DefaultIsPartnerGiftWithResToOther,
    isPartnerBenFromTrust = DefaultIsPartnerBenFromTrust,
    isEstateBelowIhtThresholdApplied = DefaultIsEstateBelowIhtThresholdApplied,
    isJointAssetPassed = DefaultIsJointAssetPassed,
    firstName = Some(DefaultFirstName),
    lastName = Some(DefaultLastName),
    dateOfMarriage = DefaultDateOfMarriage,
    dateOfPreDeceased = None
  )

  //Creates PartnerExemption with default values
  val buildPartnerExemption = PartnerExemption(
    isAssetForDeceasedPartner = DefaultIsAssetForDeceasedPartner,
    isPartnerHomeInUK = DefaultIsPartnerHomeInUK,
    firstName = Some(DefaultFirstName),
    lastName = Some(DefaultLastName),
    dateOfBirth = Some(DefaultDateOfBirth),
    nino = Some(DefaultNino),
    totalAssets = Some(DefaultTotalAssets)
  )

  //Creates AllExemptions with Default values
  val buildAllExemptions = AllExemptions(
    partner = None,
    charity = None,
    qualifyingBody = None
  )

  //Creates Liabilities with default values
  val buildAllLiabilities = AllLiabilities(
    funeralExpenses = None,
    trust = None,
    debtsOutsideUk = None,
    jointlyOwned = None,
    other = None,
    mortgages = None
  )

  val buildMortgage = Mortgage(
    id = "1",
    value = Some(1000),
    isOwned = Some(true)
  )

  val buildShareableBasicElement = ShareableBasicEstateElement(
    value = None,
    shareValue = None
  )

  val buildShareableBasicElementExtended = ShareableBasicEstateElement(
    value = None,
    shareValue = None,
    isOwned = None,
    isOwnedShare = None)

  val buildBasicElement = BasicEstateElement(
    value = None,
    isOwned = None
  )

  val buildAllLiabilitiesWithAllSectionsFilled = {

    val mortgage1 = Mortgage(id = "1", value = Some(BigDecimal(5000)),isOwned = Some(true))
    val mortgage2 = Mortgage(id = "2", value = Some(BigDecimal(2000)), isOwned = Some(true))
    val mortgageList = List(mortgage1, mortgage2)

    AllLiabilities(
      funeralExpenses = Some(BasicEstateElementLiabilities(value = Some(BigDecimal(4200)), isOwned = Some(true))),
      trust = Some(BasicEstateElementLiabilities(value = Some(BigDecimal(1200)), isOwned = Some(true))),
      debtsOutsideUk = Some(BasicEstateElementLiabilities(value = Some(BigDecimal(3000)), isOwned = Some(true))),
      jointlyOwned = Some(BasicEstateElementLiabilities(value = Some(BigDecimal(1000)), isOwned = Some(true))),
      other = Some(BasicEstateElementLiabilities(value = Some(BigDecimal(1000)), isOwned = Some(true))),
      mortgages = Some(MortgageEstateElement(Some(true), mortgageList))
    )
  }

  val buildAllAssets = AllAssets(
    action = None,
    money = None,
    household = None,
    vehicles = None,
    privatePension = None,
    stockAndShare = None,
    insurancePolicy = None,
    businessInterest = None,
    nominated = None,
    heldInTrust = None,
    foreign = None,
    moneyOwed = None,
    other = None,
    properties = None
  )

  val buildBasicExemptionElement = BasicExemptionElement(
    isSelected = None
  )

  val buildStockAndShare = StockAndShare(
    valueNotListed = None,
    valueListed = None,
    value = None,
    isNotListed = None,
    isListed = None
  )

  val buildInsurancePolicy = InsurancePolicy(
    isAnnuitiesBought = None,
    isInsurancePremiumsPayedForSomeoneElse = None,
    value = None,
    shareValue = None,
    policyInDeceasedName = None,
    isJointlyOwned = None,
    isInTrust = None,
    coveredByExemption = None,
    sevenYearsBefore = None,
    moreThanMaxValue = None

  )

  val buildPrivatePension = PrivatePension(
    isChanged = None,
    value = None
  )

  val buildPrivatePensionExtended = PrivatePension(
    isChanged = None,
    value = None,
    isOwned = None
  )

  val buildAssetsHeldInTrust = HeldInTrust(
    isMoreThanOne = None,
    value = None
  )

  val buildAllAssetsWithAllSectionsFilled = {

    def createShareableBasicEstateElement(value: BigDecimal, shareValue: BigDecimal) =
      buildShareableBasicElementExtended.copy(value = Some(value), shareValue = Some(shareValue), isOwned = Some(true), isOwnedShare = Some(true))

    AllAssets(
      money = Some(createShareableBasicEstateElement(BigDecimal(100), BigDecimal(100))),
      household = Some(createShareableBasicEstateElement(BigDecimal(100), BigDecimal(100))),
      vehicles = Some(createShareableBasicEstateElement(BigDecimal(100), BigDecimal(100))),
      privatePension = Some(buildPrivatePensionExtended.copy(isChanged = Some(true),
        value = Some(BigDecimal(100)), isOwned = Some(true))),
      stockAndShare = Some(buildStockAndShare.copy(
        valueNotListed = Some(BigDecimal(100)),
        valueListed = Some(BigDecimal(100)),
        value = Some(BigDecimal(100)),
        isNotListed = Some(true),
        isListed = Some(true))),
      insurancePolicy = Some(buildInsurancePolicy.copy(policyInDeceasedName = Some(false),
        isJointlyOwned = Some(false), isInsurancePremiumsPayedForSomeoneElse = Some(false))),
      businessInterest = Some(buildBasicElement.copy(value = Some(BigDecimal(100)), isOwned = Some(true))),
      nominated = Some(buildBasicElement.copy(value = Some(BigDecimal(100)), isOwned = Some(true))),
      heldInTrust = Some(buildAssetsHeldInTrust.copy(isOwned = Some(true), isMoreThanOne = Some(true), value = Some(BigDecimal(100)))),
      foreign = Some(buildBasicElement.copy(value = Some(BigDecimal(100)), isOwned = Some(true))),
      moneyOwed = Some(buildBasicElement.copy(value = Some(BigDecimal(100)), isOwned = Some(true))),
      other = Some(buildBasicElement.copy(value = Some(BigDecimal(100)), isOwned = Some(true))),
      properties = Some(Properties(isOwned = Some(true)))
    )
  }


  val buildAllGifts = AllGifts(None, None, None, None, None)

  val buildPreviousYearsGifts = PreviousYearsGifts(None, Some(0.0), Some(0.0), None, None)

  val buildAllGiftsWithValues = buildAllGifts.copy(isGivenAway = Some(true),isReservation = Some(false),
                                isToTrust = Some(false), isGivenInLast7Years = Some(true))

  val buildGiftsList = Seq(
    PreviousYearsGifts(Some("1"), Some(1000.00), Some(0), Some("6 April 2014"), Some("12 December 2014")),
    PreviousYearsGifts(Some("2"), Some(1000.00), Some(0), Some("6 April 2013"), Some("5 April 2013")),
    PreviousYearsGifts(Some("3"), Some(1000.00), Some(0), Some("6 April 2012"), Some("5 April 2012"))
  )

  //Creates the ApplicationDetails with default values
  val buildApplicationDetails2 = ApplicationDetails(allAssets = None,
    propertyList = Nil,
    allLiabilities = None,
    allExemptions = None,
    allGifts = None,
    charities = Seq(),
    qualifyingBodies = Seq(),
    widowCheck = None,
    increaseIhtThreshold = None,
    status = TestHelper.AppStatusInProgress,
    kickoutReason = None
  )
  val buildApplicationDetails = ApplicationDetails(allAssets = None,
    propertyList = Nil,
    allLiabilities = None,
    allExemptions = None,
    allGifts = None,
    giftsList = None,
    charities = Seq(),
    qualifyingBodies = Seq(),
    widowCheck = None,
    increaseIhtThreshold = None,
    status = TestHelper.AppStatusInProgress,
    kickoutReason = None,
    ihtRef = None,
    reasonForBeingBelowLimit = None)

  def buildApplicationDetailsUnderLowerThreshold(ihtRef: String) = CommonBuilder.buildApplicationDetails copy (ihtRef = Some(ihtRef),
    allAssets = Some(AllAssets(
      money = Some(ShareableBasicEstateElement(
        value = Some(BigDecimal(1001)), shareValue = Some(BigDecimal(0)))))))

  def buildApplicationDetailsOverLowerThreshold(ihtRef: String) = CommonBuilder.buildApplicationDetails copy (ihtRef = Some(ihtRef),
    allAssets = Some(AllAssets(
      money = Some(ShareableBasicEstateElement(
        value = Some(IhtProperties.exemptionsThresholdValue + BigDecimal(1)), shareValue = Some(BigDecimal(0)))))))

  def buildApplicationDetailsOverLowerThresholdAndGuidanceSeenFlagSet(ihtRef: String) = buildApplicationDetailsOverLowerThreshold(ihtRef) copy(ihtRef = Some(ihtRef),
    hasSeenExemptionGuidance=Some(true))

  def  buildApplicationDetailsOverLowerThresholdAndGuidanceSeenFlagNotSet(ihtRef: String) = buildApplicationDetailsOverLowerThreshold(ihtRef) copy(ihtRef = Some(ihtRef),
    hasSeenExemptionGuidance=Some(false))

  val buildBasicEstateElementLiabilities = BasicEstateElementLiabilities(
    isOwned = Some(false),
    value = None
  )

  val buildBasicEstateElementLiabilityWithValue = BasicEstateElementLiabilities(
    isOwned = Some(true),
    value = Some(100)
  )

  val buildBasicEstateElementLiabilityWithNoValue = BasicEstateElementLiabilities(
    isOwned = Some(true),
    value = None
  )

  val buildMortgageEstateElement = MortgageEstateElement(
    isOwned = Some(true),
    mortgageList = List(buildMortgage)
  )

  val charity = Charity(
    id = Some(DefaultId),
    name = Some("A Charity"),
    number = Some("1234567"),
    totalValue = Some(44.45)
  )

  val qualifyingBody = QualifyingBody(
    id = Some("1"),
    name = Some("Qualifying Body"),
    totalValue = Some(12345)
  )

  val buildProperty = Property(
    id = None,
    address = None,
    propertyType = None,
    typeOfOwnership = None,
    tenure = None,
    value = None
  )

  val buildProperties = Properties(
    isOwned = Some(false)
  )

  val buildPropertyList =
        List(buildProperty.copy(Some("1"), Some(DefaultUkAddress), TestHelper.PropertyTypeDeceasedHome,
                                TestHelper.TypesOfOwnershipDeceasedOnly, TestHelper.TenureFreehold, Some(12345)),
             buildProperty.copy(Some("2"), Some(DefaultUkAddress), TestHelper.PropertyTypeDeceasedHome,
                                TestHelper.TypesOfOwnershipDeceasedOnly, TestHelper.TenureFreehold, Some(12345)))

  val buildCharity = Charity(
    id = None,
    name = None,
    number = None,
    totalValue = None
  )

  val buildQualifyingBody = QualifyingBody(
    id = None,
    name = None,
    totalValue = None
  )

  val property = Property(
    id = Some("1"),
    address = Some(DefaultUkAddress),
    propertyType = TestHelper.PropertyTypeDeceasedHome,
    typeOfOwnership = TestHelper.TypesOfOwnershipDeceasedOnly,
    tenure = TestHelper.TenureFreehold,
    value = Some(12345)
  )

  val property2 = Property(
    id = Some("2"),
    address = Some(DefaultUkAddress2),
    propertyType = TestHelper.PropertyTypeOtherResidentialBuilding,
    typeOfOwnership = TestHelper.TypesOfOwnershipJoint,
    tenure = TestHelper.TenureLeasehold,
    value = Some(489)
  )

  lazy val buildLeadExecutor = models.des.LeadExecutor(CommonBuilder.buildApplicantDetails.firstName.fold("")(identity),
    CommonBuilder.buildApplicantDetails.lastName.fold("")(identity),
    CommonBuilder.buildApplicantDetails.nino,
    CommonBuilder.buildApplicantDetails.utr,
    "1998-12-12",
    models.des.Address(
      CommonBuilder.buildApplicantDetails.ukAddress.map(_.ukAddressLine1).fold("")(identity),
      CommonBuilder.buildApplicantDetails.ukAddress.map(_.ukAddressLine2).fold("")(identity),
      CommonBuilder.buildApplicantDetails.ukAddress.flatMap(_.ukAddressLine3),
      CommonBuilder.buildApplicantDetails.ukAddress.flatMap(_.ukAddressLine4),
      CommonBuilder.buildApplicantDetails.ukAddress.map(_.postCode).fold(DefaultUkAddress.postCode)(identity), "GB"),
    Some(TestHelper.ApplicantCountryEnglandOrWales),
    Some(models.des.ContactDetails(CommonBuilder.buildApplicantDetails.phoneNo,
      None, None, None, None)))

  lazy val buildDeceased = Deceased(None,
    DefaultFirstName,
    Some(DefaultMiddleName),
    DefaultLastName,
    "1998-12-12",
    None,
    Some(DefaultNino),
    CommonBuilder.buildDeceasedDetails.utr,
    models.des.Address(
      CommonBuilder.buildDeceasedDetails.ukAddress.fold("addr1")(_.ukAddressLine1),
      CommonBuilder.buildDeceasedDetails.ukAddress.fold("addr2")(_.ukAddressLine2),
      CommonBuilder.buildDeceasedDetails.ukAddress.flatMap(_.ukAddressLine3),
      CommonBuilder.buildDeceasedDetails.ukAddress.flatMap(_.ukAddressLine4),
      CommonBuilder.buildDeceasedDetails.ukAddress.fold("addr2")(_.postCode), "GB"),
    "2011-12-12",
    "England or Wales", None, None, TestHelper.MaritalStatusSingle)

  lazy val buildRegistrationDetails1 = {
    RegistrationDetails(
      deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath),
      applicantDetails = Some(CommonBuilder.buildApplicantDetails copy (country = Some(TestHelper.ApplicantCountryEnglandOrWales))),
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails copy(domicile = Some(TestHelper.DomicileEnglandOrWales),
        maritalStatus = Some(TestHelper.MaritalStatusSingle))),
      coExecutors = Seq(
        DefaultCoExecutor1,
        DefaultCoExecutor2,
        DefaultCoExecutor3
      ),
      ihtReference = Some("ABC"),
      acknowledgmentReference = DefaultAcknowledgmentReference
    )
  }

  lazy val buildRegistrationDetails2 = {
    RegistrationDetails(
      deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath),
      applicantDetails = Some(CommonBuilder.buildApplicantDetails copy (country = Some(TestHelper.ApplicantCountryEnglandOrWales))),
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails copy(domicile = Some(TestHelper.DomicileEnglandOrWales),
        maritalStatus = Some(TestHelper.MaritalStatusSingle))),
      coExecutors = Seq(),
      ihtReference = Some("ABC"),
      acknowledgmentReference = DefaultAcknowledgmentReference
    )
  }

  lazy val buildRegistrationDetails3 = {
    RegistrationDetails(
      deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath),
      applicantDetails = Some(CommonBuilder.buildApplicantDetails
        copy(country = Some(TestHelper.ApplicantCountryEnglandOrWales),
        nino = Some(NinoBuilder.addSpacesToNino(CommonBuilder.DefaultNino)))),
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails
        copy(domicile = Some(TestHelper.DomicileEnglandOrWales),
        maritalStatus = Some(TestHelper.MaritalStatusSingle))),
      coExecutors = Seq(),
      ihtReference = Some("ABC"),
      acknowledgmentReference = DefaultAcknowledgmentReference
    )
  }

  lazy val buildRegistrationDetails4 = {
    RegistrationDetails(
      deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath),
      applicantDetails = Some(CommonBuilder.buildApplicantDetails),
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails
        copy (maritalStatus = Some(TestHelper.MaritalStatusMarried))),
      coExecutors = Seq(),
      ihtReference = Some("ABC"),
      acknowledgmentReference = DefaultAcknowledgmentReference
    )
  }

  lazy val buildRegistrationDetails5 = {
    RegistrationDetails(
      deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath),
      applicantDetails = Some(CommonBuilder.buildApplicantDetails),
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails
        copy (maritalStatus = Some(TestHelper.MaritalStatusWidowed))),
      coExecutors = Seq(),
      ihtReference = Some("ABC"),
      acknowledgmentReference = DefaultAcknowledgmentReference
    )
  }

  lazy val buildRegistrationDetails6 = {
    RegistrationDetails(
      deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath),
      applicantDetails = Some(CommonBuilder.buildApplicantDetails),
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails
        copy (maritalStatus = Some(TestHelper.MaritalStatusSingle))),
      coExecutors = Seq(),
      ihtReference = Some("ABC"),
      acknowledgmentReference = DefaultAcknowledgmentReference
    )
  }

  lazy val buildRegistrationDetails7 = {
    RegistrationDetails(
      deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath),
      applicantDetails = Some(CommonBuilder.buildApplicantDetails),
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails
        copy (maritalStatus = Some(TestHelper.MaritalStatusDivorced))),
      coExecutors = Seq(),
      ihtReference = Some("ABC"),
      acknowledgmentReference = DefaultAcknowledgmentReference
    )
  }

  lazy val buildEventRegistration1 = buildEventRegistration(true)
  lazy val buildEventRegistration2 = buildEventRegistration(false)

  lazy val buildEventRegistration3 = buildEventRegistration(false) copy (
    leadExecutor = Some(buildLeadExecutor copy (nino = Some(CommonBuilder.DefaultNino))))

  lazy val buildEventRegistration4 = buildEventRegistration(false) copy (
    deceased = Some(buildDeceased copy (maritalStatus = TestHelper.MaritalStatusMarried)))

  lazy val buildEventRegistration5 = buildEventRegistration(false) copy (
    deceased = Some(buildDeceased copy (maritalStatus = TestHelper.MaritalStatusWidowed)))

  lazy val buildEventRegistration6 = buildEventRegistration(false) copy (
    deceased = Some(buildDeceased copy (maritalStatus = TestHelper.MaritalStatusSingle)))

  lazy val buildEventRegistration7 = buildEventRegistration(false) copy (
    deceased = Some(buildDeceased copy (maritalStatus = TestHelper.MaritalStatusDivorced)))

  private def buildEventRegistration(includeCoExecutors: Boolean = true) = {
    val erCoExec1 = models.des.CoExecutor(Some(DefaultCoExecutor1.firstName),
      Some(DefaultCoExecutor1.lastName), Some(CommonBuilder.buildCoExecutor.nino),
      CommonBuilder.buildCoExecutor.utr,
      Some("1998-12-12"),
      Some(models.des.Address(CommonBuilder.buildCoExecutor.ukAddress.fold("addr1")(_.ukAddressLine1),
        CommonBuilder.buildCoExecutor.ukAddress.fold("addr2")(_.ukAddressLine2),
        CommonBuilder.buildCoExecutor.ukAddress.flatMap(_.ukAddressLine3),
        CommonBuilder.buildCoExecutor.ukAddress.flatMap(_.ukAddressLine4),
        CommonBuilder.buildCoExecutor.ukAddress.fold("addr2")(_.postCode), "GB")),
      Some(models.des.ContactDetails(Some(CommonBuilder.buildCoExecutor.contactDetails.phoneNo),
        None, None,
        CommonBuilder.buildCoExecutor.contactDetails.email, None)))
    val erCoExec2 = erCoExec1 copy(firstName = Some(DefaultCoExecutor2.firstName),
      lastName = Some(DefaultCoExecutor2.lastName))
    val erCoExec3 = erCoExec1 copy(firstName = Some(DefaultCoExecutor3.firstName),
      lastName = Some(DefaultCoExecutor3.lastName))

    EventRegistration(Some(CommonHelper.generateAcknowledgeReference),
      Some(Event("death", "Free Estate")),
      Some(buildLeadExecutor),
      if (includeCoExecutors) Some(Seq(erCoExec1, erCoExec2, erCoExec3)) else None,
      Some(buildDeceased))
  }

  val buildApplicationDetailsWithAssetsGiftsAndDebts = buildApplicationDetails.copy(
    allAssets = Some(buildAllAssetsWithAllSectionsFilled),
    allGifts = Some(buildAllGiftsWithValues),
    giftsList = Some(buildGiftsList),
    propertyList = buildPropertyList,
    allLiabilities = Some(buildAllLiabilitiesWithAllSectionsFilled),
    ihtRef = DefaultIHTReference)

  /*
   * Create IhtApplication with default values
   */

  lazy val buildIhtApplication = IhtApplication(
    ihtRefNo = DefaultIhtRefNo,
    firstName = DefaultFirstName,
    lastName = DefaultLastName,
    dateOfBirth = DefaultDateOfBirth,
    dateOfDeath = DefaultDOD,
    nino = DefaultNino,
    entryType = DefaultEntryType,
    role = DefaultRole,
    registrationDate = DefaultRegistrationDate,
    currentStatus = DefaultCurrentStatus,
    acknowledgmentReference = DefaultAcknowledgmentReference
  )

  // Creates the Person with default values
  val buildPerson = Person(
    firstName = Some(DefaultFirstName),
    middleName = Some(DefaultMiddleName),
    lastName = Some(DefaultLastName),
    initials = None,
    title = None,
    honours = None,
    sex = None,
    dateOfBirth = Some(DefaultDateOfBirthDateTime),
    nino = Some(Nino(DefaultNino))
  )

  // Creates the CidPerson with default values
  val buildCidPerson = CidPerson(
    name = Some(CidNames(current = Some(CidName(Some(DefaultFirstName), Some(DefaultLastName))), previous = Some(Nil))),
    ids = TaxIds(Nino(DefaultNino)),
    dateOfBirth = Some("05031969")
  )

  //Create ProbateDetails with default value
  val buildProbateDetails = ProbateDetails(
    grossEstateforIHTPurposes = DefaultGrossEstateforIHTPurposes,
    grossEstateforProbatePurposes = DefaultGrossEstateforProbatePurposes,
    totalDeductionsForProbatePurposes = DefaultTotalDeductionsForProbatePurposes,
    netEstateForProbatePurposes = DefaultNetEstateForProbatePurposes,
    valueOfEstateOutsideOfTheUK = DefaultValueOfEstateOutsideOfTheUK,
    valueOfTaxPaid = DefaultValueOfTaxPaid,
    probateReference = DefaultProbateReference
  )


  val buildDeclarationDetails = Declaration(
    reasonForBeingBelowLimit = DefaultReasonForBeingBelowLimit,
    declarationAccepted = DefaultDeclarationAccepted,
    coExecutorsAccepted = DefaultCoExecutorsAccepted,
    declarationDate = DefaultDeclarationDate)

  val buildIHTReturn = IHTReturn(
    acknowledgmentReference = None,
    submitter = None,
    deceased = None,
    freeEstate = None,
    gifts = None,
    trusts = None,
    declaration = Some(buildDeclarationDetails))

  val someExemptions = AllExemptions(partner = None, charity = Some(BasicExemptionElement(Some(true))), qualifyingBody = None)

  def buildSomeExemptions(ad: ApplicationDetails) = {
    val someExemptions = AllExemptions(partner = None, charity = Some(BasicExemptionElement(Some(true))), qualifyingBody = None)
    ad copy(allExemptions = Some(someExemptions), charities = Seq(charity))
  }

  def buildExemptionsWithNoValues(ad: ApplicationDetails) = {
    val exemptions = AllExemptions(
      partner =
        Some(PartnerExemption(
          isAssetForDeceasedPartner = Some(false),
          isPartnerHomeInUK = None,
          firstName = None,
          lastName = None,
          dateOfBirth = None,
          nino = None,
        totalAssets = None)),
      charity = Some(BasicExemptionElement(Some(false))),
      qualifyingBody = Some(BasicExemptionElement(Some(false))))
    ad copy (allExemptions = Some(exemptions))
  }

  def buildExemptionsWithPartnerZeroValue(ad: ApplicationDetails) = {
    val exemptions = AllExemptions(
      partner =
        Some(buildPartnerExemption copy (totalAssets = Some(BigDecimal(0)))),
      charity = None,
      qualifyingBody = None)
    ad copy (allExemptions = Some(exemptions))
  }

  val kickoutAllAssets = CommonBuilder
    .buildAllAssets copy(
    money = Some(CommonBuilder.buildShareableBasicElement.copy(value = Some(400000),
      shareValue = Some(50))),
    household = Some(CommonBuilder.buildShareableBasicElement.copy(value = Some(200000),
      shareValue = Some(60))),
    vehicles = Some(CommonBuilder.buildShareableBasicElement.copy(value = Some(240000),
      shareValue = Some(70))),
    privatePension = Some(CommonBuilder.buildPrivatePension.copy(value = Some(500),
      isChanged = Some(false))),
    stockAndShare = Some(CommonBuilder.buildStockAndShare.copy(
      valueNotListed = Some(25000), valueListed = Some(26000))),
    insurancePolicy = Some(CommonBuilder.buildInsurancePolicy.copy(value = Some(9000),
      shareValue = Some(8000),
      isAnnuitiesBought = Some(false), isInsurancePremiumsPayedForSomeoneElse = Some(false),
      policyInDeceasedName = Some(false),
      isJointlyOwned = Some(false),
      isInTrust = Some(false),
      coveredByExemption = Some(false),
      sevenYearsBefore = Some(false)
    )),
    businessInterest = Some(CommonBuilder.buildBasicElement.copy(value = Some(250))),
    nominated = Some(CommonBuilder.buildBasicElement.copy(value = Some(200))),
    heldInTrust = Some(CommonBuilder.buildAssetsHeldInTrust.copy(value = Some(25),
      isMoreThanOne = Some(false))),
    foreign = Some(CommonBuilder.buildBasicElement.copy(value = Some(20))),
    moneyOwed = Some(CommonBuilder.buildBasicElement.copy(value = Some(4))),
    other = Some(CommonBuilder.buildBasicElement.copy(value = Some(2)))
    )

  val kickoutApplicationDetails = buildApplicationDetails.copy(
    status = AppStatus.KickOut,
    allAssets = Some(kickoutAllAssets)
  )

  def kickoutUpdateGift(ad: ApplicationDetails,
                        isRes: Boolean, isToTrust: Boolean, isLast7: Boolean,
                        yr1: BigDecimal,
                        yr2: BigDecimal,
                        yr3: BigDecimal,
                        yr4: BigDecimal,
                        yr5: BigDecimal,
                        yr6: BigDecimal,
                        yr7: BigDecimal): ApplicationDetails = {

    ad copy(

      allGifts = Some(CommonBuilder
        .buildAllGifts copy(
        isReservation = Some(isRes),
        isToTrust = Some(isToTrust),
        isGivenInLast7Years = Some(isLast7)
        )
      ),
      giftsList = Some(Seq(
        PreviousYearsGifts(Some("1"), Some(yr1), Some(0), Some("6 April 2014"), Some("12 December 2014")),
        PreviousYearsGifts(Some("2"), Some(yr2), Some(0), Some("6 April 2013"), Some("5 April 2014")),
        PreviousYearsGifts(Some("3"), Some(yr3), Some(0), Some("6 April 2012"), Some("5 April 2013")),
        PreviousYearsGifts(Some("4"), Some(yr4), Some(0), Some("6 April 2011"), Some("5 April 2012")),
        PreviousYearsGifts(Some("5"), Some(yr5), Some(0), Some("6 April 2010"), Some("5 April 2011")),
        PreviousYearsGifts(Some("6"), Some(yr6), Some(0), Some("6 April 2009"), Some("5 April 2010")),
        PreviousYearsGifts(Some("7"), Some(yr7), Some(0), Some("6 April 2008"), Some("5 April 2009"))
      ))
      )

  }

  def buildSomeGifts(ad: ApplicationDetails) =
    ad copy(
      allGifts = Some(CommonBuilder
        .buildAllGifts copy(
        isGivenAway = Some(true) ,
        isReservation = Some(false),
        isToTrust = Some(false),
        isGivenInLast7Years = Some(true)
        )
      ),
      giftsList = Some(Seq(
        PreviousYearsGifts(Some("1"), Some(1000.00), Some(0), Some("6 April 2014"), Some("12 December 2014")),
        PreviousYearsGifts(Some("2"), Some(1000.00), Some(0), Some("6 April 2013"), Some("5 April 2013")),
        PreviousYearsGifts(Some("3"), Some(1000.00), Some(0), Some("6 April 2012"), Some("5 April 2012"))
      ))
      )

  def buildApplicationDetailsForKickoutPlusValuesInMoneySection(kickoutReason: String,
                                                                value: BigDecimal,
                                                                shareValue: BigDecimal): Option[ApplicationDetails] =
    buildApplicationDetailsForKickout(kickoutReason).map(ad => {
      ad copy (allAssets = ad.allAssets.map(xx => xx.copy(money =
        Some(ShareableBasicEstateElement(value = Some(BigDecimal(1000001)), shareValue = Some(BigDecimal(0)))))))
    })

  def buildApplicationDetailsForKickout(kickoutReason: String): Option[ApplicationDetails] = {
    kickoutReason match {
      case KickOutReason.TrustsMoreThanOne =>
        Some(kickoutUpdateGift(kickoutApplicationDetails copy (
          allAssets = Some(kickoutAllAssets copy (
            heldInTrust = Some(CommonBuilder.buildAssetsHeldInTrust.copy(value = Some(25),
              isMoreThanOne = Some(true)))
            ))
          ), isRes = false, isToTrust = false, isLast7 = false, 0, 0, 0, 0, 0, 0, 0))
      case KickOutReason.ForeignAssetsValueMoreThanMax =>
        Some(kickoutUpdateGift(kickoutApplicationDetails copy (
          allAssets = Some(kickoutAllAssets copy (
            foreign = Some(CommonBuilder.buildBasicElement.copy(value = Some(100001)))
            ))
          ), isRes = false, isToTrust = false, isLast7 = false, 0, 0, 0, 0, 0, 0, 0))
      case KickOutReason.TrustValueMoreThanMax =>
        Some(kickoutUpdateGift(kickoutApplicationDetails copy (
          allAssets = Some(kickoutAllAssets copy(
            money = Some(CommonBuilder.buildShareableBasicElement.copy(value = Some(0))),
            heldInTrust = Some(CommonBuilder.buildAssetsHeldInTrust.copy(value = Some(150001),
              isMoreThanOne = Some(false)))
            ))
          ), isRes = false, isToTrust = false, isLast7 = false, 0, 0, 0, 0, 0, 0, 0))
      case KickOutReason.AnnuitiesOnInsurance =>
        Some(kickoutUpdateGift(kickoutApplicationDetails copy (
          allAssets = Some(kickoutAllAssets copy (
            insurancePolicy = Some(CommonBuilder.buildInsurancePolicy.copy(value = Some(9000),
              isAnnuitiesBought = Some(true), isInsurancePremiumsPayedForSomeoneElse = Some(true),
              policyInDeceasedName = Some(false),
              isJointlyOwned = Some(false),
              isInTrust = Some(false),
              coveredByExemption = Some(false),
              sevenYearsBefore = Some(false)
            ))
            ))
          ), isRes = false, isToTrust = false, isLast7 = false, 0, 0, 0, 0, 0, 0, 0))
      case KickOutReason.PensionDisposedLastTwoYears =>
        Some(kickoutUpdateGift(kickoutApplicationDetails copy (
          allAssets = Some(kickoutAllAssets copy (
            privatePension = Some(CommonBuilder.buildPrivatePension.copy(value = Some(500),
              isChanged = Some(true)))
            ))
          ), isRes = false, isToTrust = false, isLast7 = false, 0, 0, 0, 0, 0, 0, 0))
      case KickOutReason.InTrustLessThanSevenYears =>
        Some(kickoutUpdateGift(kickoutApplicationDetails copy (
          allAssets = Some(kickoutAllAssets copy (
            insurancePolicy = Some(CommonBuilder.buildInsurancePolicy.copy(value = Some(9000),
              isAnnuitiesBought = Some(false), isInsurancePremiumsPayedForSomeoneElse = Some(true),
              policyInDeceasedName = Some(false),
              isJointlyOwned = Some(false),
              isInTrust = Some(true),
              coveredByExemption = Some(false),
              sevenYearsBefore = Some(false)
            ))
            ))
          ), isRes = false, isToTrust = false, isLast7 = false, 0, 0, 0, 0, 0, 0, 0))
      case KickOutReason.InsuranceMoreThanMax =>
        Some(kickoutUpdateGift(kickoutApplicationDetails copy (
          allAssets = Some(kickoutAllAssets copy (
            insurancePolicy = Some(CommonBuilder.buildInsurancePolicy.copy(value = Some(9000),
              isAnnuitiesBought = Some(false), isInsurancePremiumsPayedForSomeoneElse = Some(true),
              policyInDeceasedName = Some(false),
              isJointlyOwned = Some(false),
              isInTrust = Some(true),
              coveredByExemption = Some(false),
              sevenYearsBefore = Some(true),
              moreThanMaxValue = Some(true)
            ))
            ))
          ), isRes = false, isToTrust = false, isLast7 = false, 0, 0, 0, 0, 0, 0, 0))
      case KickOutReason.AssetsTotalValueMoreThanMax =>
        Some(kickoutUpdateGift(kickoutApplicationDetails, isRes = false, isToTrust = false, isLast7 = false,
          50000, 25000, 20000, 2500, 2000, 250, 250))
      case KickOutReason.GiftsWithReservationOfBenefit =>
        Some(kickoutUpdateGift(kickoutApplicationDetails, isRes = true, isToTrust = false, isLast7 = false, 0, 0, 0, 0, 0, 0, 0))
      case KickOutReason.GiftsGivenInPast =>
        Some(kickoutUpdateGift(kickoutApplicationDetails, isRes = false, isToTrust = true, isLast7 = false, 0, 0, 0, 0, 0, 0, 0))
      case KickOutReason.GiftsToTrust =>
        Some(kickoutUpdateGift(kickoutApplicationDetails, isRes = false, isToTrust = false, isLast7 = true, 0, 0, 0, 0, 0, 0, 0))
      case KickOutReason.GiftsMaxValue =>
        Some(kickoutUpdateGift(kickoutApplicationDetails copy (
          allAssets = Some(kickoutAllAssets copy (
            money = Some(CommonBuilder.buildShareableBasicElement.copy(value = Some(0),
              shareValue = Some(0)))
            ))
          ), isRes = false, isToTrust = false, isLast7 = false, 50000, 25000, 20000, 2500, 2000, 400250, 2))
      case KickOutReason.PartnerHomeInUK =>
        Some(kickoutUpdateGift(kickoutApplicationDetails copy (
          allExemptions = Some(buildAllExemptions copy (partner = Some(PartnerExemption(
            isAssetForDeceasedPartner = None,
            isPartnerHomeInUK = Some(false),
            firstName = None,
            lastName = None,
            dateOfBirth = None,
            nino = None,
            totalAssets = None
          ))))
          ), isRes = false, isToTrust = false, isLast7 = false, 0, 0, 0, 0, 0, 0, 0))
      case _ => None
    }
  }

  def buildAuthContext(): AuthContext = {
    val loggedInUser = new LoggedInUser(firstNameGenerator, None, None, None, CredentialStrength.Strong, ConfidenceLevel.L300 ,"")
    new AuthContext(loggedInUser, Principal(None, Accounts()), None, None, None, None)
  }

  def buildApplicationDetailsWithAllAssets = {
    val allAssets = buildAllAssets.copy(
      money = Some(buildShareableBasicElementExtended.copy(
        Some(BigDecimal(1000)), Some(BigDecimal(2000)), Some(true), Some(true))),
      household = Some(buildShareableBasicElementExtended.copy(
        Some(BigDecimal(1000)), Some(BigDecimal(2000)), Some(true), Some(true))),
      vehicles = Some(buildShareableBasicElementExtended.copy(
        Some(BigDecimal(1000)), Some(BigDecimal(2000)), Some(true), Some(true))),
      privatePension = Some(buildPrivatePensionExtended.copy(isOwned = Some(true),
        value = Some(BigDecimal(10000)), isChanged = Some(true))),
      stockAndShare = Some(buildStockAndShare.copy(valueNotListed = Some(BigDecimal(10000)),
        valueListed = Some(BigDecimal(10000)), isNotListed = Some(true), isListed = Some(true))),
      insurancePolicy = Some(buildInsurancePolicy.copy(Some(true),
        Some(true), Some(BigDecimal(1000)), Some(BigDecimal(2000)),
        Some(true), Some(true), Some(true), Some(true), Some(true), Some(true))),
      businessInterest = Some(buildBasicElement.copy(isOwned = Some(false))),
      nominated = Some(buildBasicElement.copy(isOwned = Some(false))),
      heldInTrust = Some(buildAssetsHeldInTrust.copy(isOwned = Some(false))),
      foreign = Some(buildBasicElement.copy(isOwned = Some(false))),
      moneyOwed = Some(buildBasicElement.copy(isOwned = Some(false))),
      other = Some(buildBasicElement.copy(isOwned = Some(false))),
      properties = Some(Properties(isOwned = Some(true)))
    )

    buildApplicationDetails.copy(allAssets = Some(allAssets), propertyList = List(property))
  }

  lazy val buildEveryLiability = AllLiabilities(
    funeralExpenses = Some(CommonBuilder.buildBasicEstateElementLiabilityWithValue),
    trust = Some(CommonBuilder.buildBasicEstateElementLiabilityWithValue),
    debtsOutsideUk = Some(CommonBuilder.buildBasicEstateElementLiabilityWithValue),
    jointlyOwned = Some(CommonBuilder.buildBasicEstateElementLiabilityWithValue),
    other = Some(CommonBuilder.buildBasicEstateElementLiabilityWithValue),
    mortgages = None
  )

  lazy val buildSomeLiabilities = AllLiabilities(
    funeralExpenses = Some(CommonBuilder.buildBasicEstateElementLiabilityWithValue)
  )

  lazy val buildAllLiabilitiesAnsweredNo = AllLiabilities(
    funeralExpenses = Some(BasicEstateElementLiabilities(isOwned = Some(false), value = None)),
    trust = Some(BasicEstateElementLiabilities(isOwned = Some(false), value = None)),
    debtsOutsideUk = Some(BasicEstateElementLiabilities(isOwned = Some(false), value = None)),
    jointlyOwned = Some(BasicEstateElementLiabilities(isOwned = Some(false), value = None)),
    other = Some(BasicEstateElementLiabilities(isOwned = Some(false), value = None)))

}

object AssetsWithAllSectionsSetToNoBuilder {

  val buildShareableBasicEstateElement = ShareableBasicEstateElement(isOwned = Some(false), value = None,
    isOwnedShare = Some(false), shareValue = None)
  val buildPrivatePension = PrivatePension(
    isChanged = None,
    value = None,
    isOwned = Some(false)
  )
  val buildStockAndShare = StockAndShare(
    valueNotListed = None,
    valueListed = None,
    value = None,
    isNotListed = Some(false),
    isListed = Some(false)
  )

  val buildInsurancePolicy = InsurancePolicy(
    isAnnuitiesBought = None,
    isInsurancePremiumsPayedForSomeoneElse = Some(false),
    value = None,
    shareValue = None,
    policyInDeceasedName = Some(false),
    isJointlyOwned = Some(false),
    isInTrust = None,
    coveredByExemption = None,
    sevenYearsBefore = None,
    moreThanMaxValue = None
  )

  val buildBasicElement = BasicEstateElement(
    isOwned = Some(false),
    value = None
  )

  val buildAssetsHeldInTrust = HeldInTrust(
    isOwned = Some(false),
    isMoreThanOne = None,
    value = None
  )

  val buildProperties = Properties(
    isOwned = Some(false)
  )

  val buildAllAssets = AllAssets(
    action = None,
    money = Some(buildShareableBasicEstateElement),
    household = Some(buildShareableBasicEstateElement),
    vehicles = Some(buildShareableBasicEstateElement),
    privatePension = Some(buildPrivatePension),
    stockAndShare = Some(buildStockAndShare),
    insurancePolicy = Some(buildInsurancePolicy),
    businessInterest = Some(buildBasicElement),
    nominated = Some(buildBasicElement),
    heldInTrust = Some(buildAssetsHeldInTrust),
    foreign = Some(buildBasicElement),
    moneyOwed = Some(buildBasicElement),
    other = Some(BasicEstateElement(
      isOwned = Some(true),
      value = Some(BigDecimal(1000))
    )),
    properties = Some(buildProperties)
  )

  val buildMortgageEstateElement = MortgageEstateElement(
    isOwned = Some(false)
  )

  val buildBasicEstateElementLiabilities = BasicEstateElementLiabilities(
    isOwned = Some(false),
    value = None
  )

  val buildAllLiabilities = AllLiabilities(
    funeralExpenses = Some(buildBasicEstateElementLiabilities),
    trust = Some(buildBasicEstateElementLiabilities),
    debtsOutsideUk = Some(buildBasicEstateElementLiabilities),
    jointlyOwned = Some(buildBasicEstateElementLiabilities),
    other = Some(buildBasicEstateElementLiabilities),
    mortgages = Some(buildMortgageEstateElement)
  )

  val buildAllGifts = AllGifts(isGivenAway = Some(false),
    isReservation = Some(false),
    isToTrust = Some(false),
    isGivenInLast7Years = Some(false),
    action = None)


  val buildApplicationDetails = ApplicationDetails(allAssets = Some(buildAllAssets),
    propertyList = Nil,
    allLiabilities = Some(buildAllLiabilities),
    allExemptions = None,
    allGifts = Some(buildAllGifts),
    charities = Seq(),
    qualifyingBodies = Seq(),
    widowCheck = None,
    increaseIhtThreshold = None,
    status = TestHelper.AppStatusInProgress,
    kickoutReason = None,
    ihtRef = Some("AbC123")
  )

}
