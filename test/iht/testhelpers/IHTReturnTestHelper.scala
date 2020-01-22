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

package iht.testhelpers

import iht.models.des.ihtReturn._
import org.joda.time.{LocalDate, LocalDateTime}

object IHTReturnTestHelper {

  val dateOfDeath = new LocalDate(2000,6,28)

  val IHTReturnDateFormat = "YYYY-MM-dd"
  // Dummy values for mandatory fields I can't find source for:-
  val IHTReturnDummyDateOfMarriage = "1670-12-01" // Surviving spouse
  val IHTReturnDummyDateOfBirth = toDate("1670-12-01") // Deceased spouse
  val IHTReturnDummyDateOfDeath = "1670-12-01" // Surviving spouse
  val IHTReturnDummyLiabilityOwner = ""
  val IHTReturnDummyDomicile = "England or Wales"

  val DefaultAddress = models.des.Address(
    addressLine1= "addr1", addressLine2= "addr2",
    addressLine3= None, addressLine4= None,
    postalCode= CommonBuilder.DefaultPostCode, countryCode= "GB"
  )

  def dateTimeToDesString(dt: LocalDateTime) = {
    val s = dt.toString().substring(0,19)
    s
  }

  def dateToDesString( ld: LocalDate ) = {
    ld.toString(IHTReturnDateFormat)
  }

  def toDate(date:String) = {
    val year = date.substring(0,4)
    val month = date.substring(5,7)
    val day = date.substring(8,10)
    new LocalDate(year.toInt, month.toInt, day.toInt)
  }

  def buildIHTReturnCorrespondingToApplicationDetailsAllFields(declarationDate:LocalDate,
                                                               acknowledgmentReference: String) = {


    val declaration = Declaration(
      reasonForBeingBelowLimit= Some("Excepted Estate"),
      declarationAccepted= Some(true),
      coExecutorsAccepted= Some(true),
      declarationDate= Some(declarationDate))

    val freeEstate = FreeEstate(
      estateAssets= Some(buildAssets),
      interestInOtherEstate= None,
      estateLiabilities= Some(buildLiabilities),
      estateExemptions= Some(buildExemptions)
    )

    IHTReturn(Some(acknowledgmentReference),
      submitter=Some(Submitter(submitterRole=Some("Lead Executor"))),
      deceased=Some(buildTNRB),
      freeEstate=Some(freeEstate),
      gifts=Some(buildGifts),
      trusts=Some(buildTrusts),
      declaration=Some(declaration))
  }

  def buildGifts = {
    Set(Seq(
      makeGiftWithOutExemption(1000, toDate("2005-04-05")),
      makeGiftWithExemption(2000, 200, toDate("2006-04-05")),
      makeGiftWithOutExemption(3000, toDate("2007-04-05")),
      makeGiftWithOutExemption(4000, toDate("2008-04-05")),
      makeGiftWithOutExemption(5000, toDate("2009-04-05")),
      makeGiftWithOutExemption(6000, toDate("2010-04-05")),
      makeGiftWithOutExemption(7000, toDate("2011-04-05"))
    ))
  }

  def buildTrusts = {
    Set(
      makeTrust(17)
    )
  }

  private def makeTrust(value:BigDecimal) = {
    Trust(
      trustName=Some("Deceased Trust"),
      trustAssets=
        Some(
          Set(
            Asset(
              // General asset
              assetCode= Some("9097"),
              assetDescription= Some("Rolled up trust assets"),
              assetID= Some("null"),
              assetTotalValue= Some(value),
              howheld= Some("Standard"),
              devolutions= None,
              liabilities= None
            )))
    )
  }

  def makeGiftWithOutExemption(value:BigDecimal, date:LocalDate) = {
    Gift(
      assetCode=Some("9095"),
      assetDescription=Some("Rolled up gifts"),
      assetID=Some("null"),
      valuePrevOwned = Some(value),
      percentageSharePrevOwned = Some(BigDecimal(100)),
      valueRetained = Some(BigDecimal(0)),
      percentageRetained = Some(BigDecimal(0)),
      lossToEstate = Some(value),
      dateOfGift = Some(date),
      assetTotalValue = Some(value),
      howheld = Some("Standard")
    )
  }

  def makeGiftWithExemption(value: BigDecimal, exemptions: BigDecimal, date: LocalDate) = {
    val totalGiftValue = value - exemptions
    Gift(
      assetCode=Some("9095"),
      assetDescription=Some(s"Rolled up gifts minus exemption of £$exemptions"),
      assetID=Some("null"),
      valuePrevOwned = Some(value),
      percentageSharePrevOwned = Some(BigDecimal(100)),
      valueRetained = Some(BigDecimal(0)),
      percentageRetained = Some(BigDecimal(0)),
      lossToEstate = Some(totalGiftValue),
      dateOfGift = Some(date),
      assetTotalValue = Some(totalGiftValue),
      howheld = Some("Standard")
    )
  }

  private def makeGiftToTrustOrganisation(value:BigDecimal, date:LocalDate) = {
    Gift(
      assetCode=Some("9095"),
      assetDescription=Some("Rolled up gifts given away to Trusts or Organisation"),
      assetID=Some("null"),
      valuePrevOwned = Some(value),
      percentageSharePrevOwned = Some(BigDecimal(100)),
      valueRetained = Some(BigDecimal(0)),
      percentageRetained = Some(BigDecimal(0)),
      lossToEstate = Some(value),
      dateOfGift = Some(date),
      assetTotalValue = Some(value),
      howheld = Some("Standard")
    )
  }

  private def buildTNRB = {
    val survivingSpouse = SurvivingSpouse(
      // Person
      title= Some("Mrs"), firstName= Some(CommonBuilder.firstNameGenerator), middleName= None,
      lastName= Some(CommonBuilder.surnameGenerator), dateOfBirth= Some(toDate("2011-11-12")),
      gender= None, nino= Some(CommonBuilder.DefaultNino), utr= None,
      mainAddress= Some(DefaultAddress),

      // Other
      dateOfMarriage= Some(dateOfDeath.minusDays(1)), domicile= Some(IHTReturnDummyDomicile),
      otherDomicile= None)

    val spouse = Spouse(
      // Person
      title= Some("Ms"), firstName= Some(CommonBuilder.firstNameGenerator), middleName= None,
      lastName= Some(CommonBuilder.surnameGenerator), dateOfBirth= Some(IHTReturnDummyDateOfBirth),
      gender= None, nino= Some(CommonBuilder.DefaultNino), utr= None,
      mainAddress= Some(DefaultAddress),

      // Other
      dateOfMarriage= Some(toDate("2008-12-13")), dateOfDeath=Some(toDate("2010-10-12"))
    )

    val spousesEstate = SpousesEstate(
      domiciledInUk= Some(true), whollyExempt= Some(false), jointAssetsPassingToOther= Some(true),
      otherGifts= Some(false), agriculturalOrBusinessRelief= Some(true), giftsWithReservation= Some(false),
      benefitFromTrust= Some(true), unusedNilRateBand= Some(BigDecimal(100))
    )

    val tnrbForm = TNRBForm(
      spouse=Some(spouse),
      spousesEstate=Some(spousesEstate)
    )

    Deceased(survivingSpouse=Some(survivingSpouse),
      transferOfNilRateBand=Some(TransferOfNilRateBand(
        totalNilRateBandTransferred=Some(BigDecimal(100)),
        deceasedSpouses = Set(tnrbForm)))
    )
  }



  // Liabilities excluding mortgages, split into funeral expenses and other.
  private def buildLiabilities = {
    val liabilityFuneralExp = Liability(
      liabilityType=Some("Funeral Expenses"),
      liabilityAmount=Some(BigDecimal(20)),
      liabilityOwner=Some(IHTReturnDummyLiabilityOwner)
    )

    val liabilityOther = Liability(
      liabilityType=Some("Other"),
      liabilityAmount=Some(BigDecimal(90)),
      liabilityOwner=Some(IHTReturnDummyLiabilityOwner)
    )

    Set(liabilityFuneralExp, liabilityOther)
  }

  // Not sure whether to include spouse exemption (already included in surviving spouse section).
  private def buildExemptions = {
    val exemption1 = Exemption(
      exemptionType=Some("Charity"),
      percentageAmount=None,
      overrideValue=Some(BigDecimal(27))
    )
    val exemption2 = Exemption(
      exemptionType=Some("Charity"),
      percentageAmount=None,
      overrideValue=Some(BigDecimal(28))
    )
    val exemption3 = Exemption(
      exemptionType=Some("GNCP"),
      percentageAmount=None,
      overrideValue=Some(BigDecimal(30))
    )
    val exemption4 = Exemption(
      exemptionType=Some("GNCP"),
      percentageAmount=None,
      overrideValue=Some(BigDecimal(31))
    )
    val exemption5 = Exemption(
      exemptionType=Some("Spouse"),
      percentageAmount=None,
      overrideValue=Some(BigDecimal(25))
    )
    val exemption6 = Exemption(
      exemptionType=Some("Charity"),
      percentageAmount=None,
      overrideValue=Some(BigDecimal(28))
    )
    Seq(exemption1, exemption2, exemption3, exemption4, exemption5, exemption6)
  }

  private def buildAssets = {
    Set(
      buildAssetMoney,
      buildJointAssetMoney,
      buildAssetHouseholdAndPersonalItems,
      buildJointAssetHouseholdAndPersonalItems,
      buildAssetStocksAndSharesListed,
      buildAssetStocksAndSharesNotListed,
      buildAssetPrivatePensions,
      buildAssetInsurancePoliciesOwned,
      buildJointAssetInsurancePoliciesOwned,
      buildAssetBusinessInterests,
      buildAssetNominatedAssets,
      buildAssetForeignAssets,
      buildAssetMoneyOwed,
      buildAssetOther,
      buildAssetsPropertiesDeceasedsHome,
      buildAssetsPropertiesOtherResidentialBuilding,
      buildAssetsPropertiesLandNonRes
    )
  }

  def buildAssetMoney = {
    Asset(
      // General asset
      assetCode= Some("9001"),
      assetDescription= Some("Rolled up bank and building society accounts"),
      assetID= Some("null"),
      assetTotalValue= Some(BigDecimal(1)),
      howheld= Some("Standard"),
      devolutions= None,
      liabilities= None
    )
  }

  // Create Jointly owed money asset
  def buildJointAssetMoney = {
    Asset(
      // General asset
      assetCode= Some("9001"),
      assetDescription= Some("Rolled up bank and building society accounts"),
      assetID= Some("null"),
      assetTotalValue= Some(BigDecimal(2)),
      howheld= Some("Joint - Beneficial Joint Tenants"),
      devolutions= None,
      liabilities= None
    )
  }

  // Household and personal goods plus motor vehicles, caravans and boats
  def buildAssetHouseholdAndPersonalItems = {
    Asset(
      // General asset
      assetCode= Some("9004"),
      assetDescription= Some("Rolled up household and personal goods"),
      assetID= Some("null"),
      assetTotalValue= Some(BigDecimal(8)),
      howheld= Some("Standard"),
      devolutions= None,
      liabilities= None
    )
  }

  // Create joint household and personal items
  def buildJointAssetHouseholdAndPersonalItems = {
    Asset(
      // General asset
      assetCode= Some("9004"),
      assetDescription= Some("Rolled up household and personal goods"),
      assetID= Some("null"),
      assetTotalValue= Some(BigDecimal(10)),
      howheld= Some("Joint - Beneficial Joint Tenants"),
      devolutions= None,
      liabilities= None
    )
  }

  // Create joint household and personal items
  def buildJointAssetMotorVehicle = {
    Asset(
      // General asset
      assetCode= Some("9004"),
      assetDescription= Some("Rolled up household and personal goods"),
      assetID= Some("null"),
      assetTotalValue= Some(BigDecimal(6)),
      howheld= Some("Joint - Beneficial Joint Tenants"),
      devolutions= None,
      liabilities= None
    )
  }

  def buildAssetPrivatePensions = {
    Asset(
      // General asset
      assetCode= Some("9005"),
      assetDescription= Some("Rolled up pensions"),
      assetID= Some("null"),
      assetTotalValue= Some(BigDecimal(7)),
      howheld= Some("Standard"),
      devolutions= None,
      liabilities= None
    )
  }

  // Create jointly owen private pensions
  def buildJointAssetPrivatePensions = {
    Asset(
      // General asset
      assetCode= Some("9005"),
      assetDescription= Some("Rolled up pensions"),
      assetID= Some("null"),
      assetTotalValue= Some(BigDecimal(8)),
      howheld= Some("Joint - Beneficial Joint Tenants"),
      devolutions= None,
      liabilities= None
    )
  }

  def buildAssetStocksAndSharesNotListed = {
    Asset(
      // General asset
      assetCode= Some("9010"),
      assetDescription= Some("Rolled up unlisted stocks and shares"),
      assetID= Some("null"),
      assetTotalValue= Some(BigDecimal(9)),
      howheld= Some("Standard"),
      devolutions= None,
      liabilities= None
    )
  }

  def buildAssetStocksAndSharesListed = {
    Asset(
      // General asset
      assetCode= Some("9008"),
      assetDescription= Some("Rolled up quoted stocks and shares"),
      assetID= Some("null"),
      assetTotalValue= Some(BigDecimal(10)),
      howheld= Some("Standard"),
      devolutions= None,
      liabilities= None
    )
  }

  def buildAssetInsurancePoliciesOwned = {
    Asset(
      // General asset
      assetCode= Some("9006"),
      assetDescription= Some("Rolled up life assurance policies"),
      assetID= Some("null"),
      assetTotalValue= Some(BigDecimal(12)),
      howheld= Some("Standard"),
      devolutions= None,
      liabilities= None
    )
  }

  // Create jointly owed insurance policy
  def buildJointAssetInsurancePoliciesOwned = {
    Asset(
      // General asset
      assetCode= Some("9006"),
      assetDescription= Some("Rolled up life assurance policies"),
      assetID= Some("null"),
      assetTotalValue= Some(BigDecimal(13)),
      howheld= Some("Joint - Beneficial Joint Tenants"),
      devolutions= None,
      liabilities= None
    )
  }

  def buildAssetBusinessInterests = {
    Asset(
      // General asset
      assetCode= Some("9021"),
      assetDescription= Some("Rolled up business assets"),
      assetID= Some("null"),
      assetTotalValue= Some(BigDecimal(14)),
      howheld= Some("Standard"),
      devolutions= None,
      liabilities= None
    )
  }

  def buildAssetNominatedAssets = {

    Asset(
      // General asset
      assetCode= Some("9099"),
      assetDescription= Some("Rolled up nominated assets"),
      assetID= Some("null"),
      assetTotalValue= Some(BigDecimal(16)),
      howheld= Some("Nominated"),
      devolutions= None,
      liabilities= None
    )
  }

  def buildAssetForeignAssets = {
    Asset(
      // General asset
      assetCode= Some("9098"),
      assetDescription= Some("Rolled up foreign assets"),
      assetID= Some("null"),
      assetTotalValue= Some(BigDecimal(18)),
      howheld= Some("Foreign"),
      devolutions= None,
      liabilities= None
    )
  }

  def buildAssetMoneyOwed = {
    Asset(
      // General asset
      assetCode= Some("9013"),
      assetDescription= Some("Rolled up money owed to deceased"),
      assetID= Some("null"),
      assetTotalValue= Some(BigDecimal(15)),
      howheld= Some("Standard"),
      devolutions= None,
      liabilities= None
    )
  }

  def buildAssetOther = {
    Asset(
      // General asset
      assetCode= Some("9015"),
      assetDescription= Some("Rolled up other assets"),
      assetID= Some("null"),
      assetTotalValue= Some(BigDecimal(19)),
      howheld= Some("Standard"),
      devolutions= None,
      liabilities= None
    )
  }

  def buildAddr = {

  }

  def buildAssetsPropertiesDeceasedsHome = {
    val addressOrOtherLandLocation = AddressOrOtherLandLocation(
      address = Some(models.des.Address(addressLine1= "addr1", addressLine2= "addr2",
        addressLine3= None, addressLine4= None,
        postalCode= CommonBuilder.DefaultPostCode, countryCode= "GB"))
    )

    val liability1 = Liability(
      liabilityType=Some("Mortgage"),
      liabilityAmount=Some(BigDecimal(80)),
      liabilityOwner=Some(IHTReturnDummyLiabilityOwner)
    )

    Asset(
      // General asset
      assetCode= Some("0016"),
      assetDescription= Some("Deceased's residence"),
      assetID= Some("null"),
      assetTotalValue= Some(BigDecimal(100)),
      howheld= Some("Standard"),
      liabilities= Some(Set(liability1)),
      //      liabilities= None,

      // Property asset
      propertyAddress= Some(addressOrOtherLandLocation),
      tenure= Some("Freehold"), tenancyType= Some("Vacant Possession"),
      yearsLeftOnLease= Some(0),
      yearsLeftOntenancyAgreement= Some(0)
    )
  }

  def buildAssetsPropertiesOtherResidentialBuilding = {
    val addressOrOtherLandLocation = AddressOrOtherLandLocation(
      address = Some(models.des.Address(addressLine1= "addr1", addressLine2= "addr2",
        addressLine3= None, addressLine4= None,
        postalCode= CommonBuilder.DefaultPostCode, countryCode= "GB"))
    )

    val liability1 = Liability(
      liabilityType=Some("Mortgage"),
      liabilityAmount=Some(BigDecimal(150)),
      liabilityOwner=Some(IHTReturnDummyLiabilityOwner)
    )

    Asset(
      // General asset
      assetCode= Some("0017"),
      assetDescription= Some("Other residential property"),
      assetID= Some("null"),
      assetTotalValue= Some(BigDecimal(200)),
      howheld= Some("Joint - Beneficial Joint Tenants"),
      devolutions= None,
      liabilities= Some(Set(liability1)),
      //      liabilities= None,

      // Property asset
      propertyAddress= Some(addressOrOtherLandLocation),
      tenure= Some("Leasehold"), tenancyType= Some("Vacant Possession"),
      yearsLeftOnLease= Some(0),
      yearsLeftOntenancyAgreement= Some(0)
    )
  }

  def buildAssetsPropertiesLandNonRes = {
    val addressOrOtherLandLocation = AddressOrOtherLandLocation(
      address = Some(models.des.Address(addressLine1= "addr1", addressLine2= "addr2",
        addressLine3= None, addressLine4= None,
        postalCode= CommonBuilder.DefaultPostCode, countryCode= "GB"))
    )

    Asset(
      // General asset
      assetCode= Some("0018"),
      assetDescription= Some("Other land and buildings"),
      assetID= Some("null"),
      assetTotalValue= Some(BigDecimal(300)),
      howheld= Some("Joint - Tenants In Common"),
      devolutions= None,

      // Property asset
      propertyAddress= Some(addressOrOtherLandLocation),
      tenure= Some("Leasehold"), tenancyType= Some("Vacant Possession"),
      yearsLeftOnLease= Some(0),
      yearsLeftOntenancyAgreement= Some(0)
    )
  }
}
