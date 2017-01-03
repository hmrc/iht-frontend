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

package iht.forms

import iht.constants.IhtProperties
import iht.forms.mappings.DateMapping
import iht.forms.validators.{MandatoryCurrency, OptionalCurrency}
import iht.models._
import iht.models.application.assets._
import iht.models.application.basicElements.{BasicEstateElement, ShareableBasicEstateElement}
import iht.models.application.debts._
import iht.models.application.exemptions._
import iht.models.application.gifts._
import iht.utils.IhtFormValidator
import iht.utils.IhtFormValidator._
import play.api.data.Form
import play.api.data.Forms._

object ApplicationForms {
  val addressMapping = mapping(
    "ukAddressLine1" -> of(ihtAddress("address.ukAddressLine2", "address.ukAddressLine3",
      "address.ukAddressLine4", "address.postCode", "address.countryCode",
      "error.address.give", "error.address.giveInLine1And2",
      "error.address.giveUsing35CharsOrLess", "error.address.givePostcode",
      "error.address.givePostcodeUsingNumbersAndLetters", "error.country.select")),
    "ukAddressLine2" -> text,
    "ukAddressLine3" -> optional(text),
    "ukAddressLine4" -> optional(text),
    "postCode" -> text,
    "countryCode"->default(text, "GB")
  )(UkAddress.apply)(UkAddress.unapply)

  def basicEstateElementMapping(selectErrorKey:String) =  mapping(
    "value" -> OptionalCurrency(),
    "isOwned" -> yesNoQuestion(selectErrorKey)
  )(BasicEstateElement.apply)(BasicEstateElement.unapply)

  def basicEstateElementLiabilitiesMapping(selectErrorKey:String) =  mapping(
    "isOwned" -> yesNoQuestion(selectErrorKey),
    "value" -> OptionalCurrency()
  )(BasicEstateElementLiabilities.apply)(BasicEstateElementLiabilities.unapply)


  def shareableBasicEstateElementMapping(fieldName:String) =  mapping(
    "value" -> optionalCurrencyWithoutFieldName,
    "shareValue" -> optionalCurrencyWithoutFieldName,
    "isOwned" -> optional(boolean),
    "isOwnedShare" -> optional(boolean)
  )(ShareableBasicEstateElement.apply)(ShareableBasicEstateElement.unapply)

  def shareableBasicEstateElementFormOwn(selectErrorKey:String): Form[ShareableBasicEstateElement] = Form (
    mapping(
      "value" -> OptionalCurrency(),
      "isOwned" -> yesNoQuestion(selectErrorKey)
    )(
      (value, isOwned) => ShareableBasicEstateElement(value, None, isOwned, None)
    )(
      (element) => Some((element.value, element.isOwned))
    )
  )

  def shareableBasicEstateElementFormJoint(selectErrorKey:String): Form[ShareableBasicEstateElement] = Form (
    mapping(
      "shareValue" -> OptionalCurrency(),
      "isOwnedShare" -> yesNoQuestion(selectErrorKey)
    )(
      (shareValue, isOwnedShare) => ShareableBasicEstateElement(None, shareValue, None, isOwnedShare)
    )(
      (element) => Some((element.shareValue, element.isOwnedShare))
    )
  )

  // Assets forms.
  val propertyTenureForm = Form(mapping(
    "tenure" -> of(ihtRadio("error.assets.property.tenure.select")))
    ((tenure) => Property(None,None,None,None,Some(tenure),None))
    ((property: Property) => property.tenure)
  )

  val propertyAddressForm = Form(mapping(
    "address" -> addressMapping
  )(
      (address) => Property(None, Some(address), None, None, None, None)
    )(
      (property: Property) => property.address
    )
  )

  val propertyTypeForm = Form(mapping(
    "propertyType" -> of(ihtRadio("error.assets.property.type.select")))
    ( (propertyType) => Property(None, None, Some(propertyType), None, None, None) )
    ( (property: Property) => property.propertyType)
  )

  val typeOfOwnershipForm = Form(mapping(
    "typeOfOwnership" -> of(ihtRadio("error.assets.property.ownership.select")))
    ((typeOfOwnership) => Property(None, None, None, Some(typeOfOwnership), None, None))
    ((property: Property) => property.typeOfOwnership)
  )

  val moneyFormOwn = shareableBasicEstateElementFormOwn("error.assets.money.deceasedOwned.select")

  val moneyJointlyOwnedForm = shareableBasicEstateElementFormJoint("error.assets.money.jointlyOwned.select")

  val householdFormOwn = shareableBasicEstateElementFormOwn("error.assets.household.deceasedOwned.select")

  val householdJointlyOwnedForm = shareableBasicEstateElementFormJoint("error.assets.household.jointlyOwned.select")

  val vehiclesFormOwn = shareableBasicEstateElementFormOwn("error.assets.vehicles.deceasedOwned.select")

  val vehiclesJointlyOwnedForm = shareableBasicEstateElementFormJoint("error.assets.vehicles.jointlyOwned.select")

  val privatePensionForm = Form (
    mapping(
      "isChanged" -> optional(boolean),
      "value" -> OptionalCurrency(),
      "isOwned" -> optional(boolean)
    )(PrivatePension.apply)(PrivatePension.unapply))


  val pensionsOwnedQuestionForm = Form(
    mapping(
      "isOwned" -> yesNoQuestion("error.assets.privatePensions.deceasedOwned.select")
    )(
      (isOwned) => PrivatePension(None,None,isOwned)
    )(
      (privatePension: PrivatePension) => Some(privatePension.isOwned)
    )
  )

  val pensionsChangedQuestionForm = Form(
    mapping(
      "isChanged" -> yesNoQuestion("error.assets.privatePensions.changed.select")
    )(
      (isChanged) => PrivatePension(isChanged,None,None)
    )(
      (privatePension: PrivatePension) => Some(privatePension.isChanged)
    )
  )

  val pensionsValueForm = Form(
    mapping(
      "value" -> OptionalCurrency()
    )(
      (value) => PrivatePension(None,value,None)
    )(
      (privatePension: PrivatePension) => Some(privatePension.value)
    )
  )

  def stockAndShareListedForm = Form (
    mapping(
      "valueListed" -> OptionalCurrency(),
      "isListed" -> yesNoQuestion("error.assets.stocksAndShares.listed.select")
    )(
      (valueListed, isListed) => StockAndShare(None, valueListed, None, None, isListed)
    )(
      (element) => Some((element.valueListed, element.isListed))
    )
  )

  def stockAndShareNotListedForm = Form (
    mapping(
      "valueNotListed" -> OptionalCurrency(),
      "isNotListed" -> yesNoQuestion("error.assets.stocksAndShares.notListed.select")
    )(
      (valueNotListed, isNotListed) => StockAndShare(valueNotListed, None, None, isNotListed, None)
    )(
      (element) => Some((element.valueNotListed, element.isNotListed))
    )
  )

  val insurancePolicyMapping =  mapping(
    "isAnnuitiesBought" -> optional(boolean),
    "isInsurancePremiumsPayedForSomeoneElse" -> optional(boolean),
    "value" -> OptionalCurrency(),
    "shareValue" -> OptionalCurrency(),
    "policyInDeceasedName" -> optional(boolean),
    "isJointlyOwned" -> optional(boolean),
    "isInTrust" -> optional(boolean),
    "coveredByExemption" -> optional(boolean),
    "sevenYearsBefore" -> optional(boolean),
    "moreThanMaxValue" -> optional(boolean)
  )(InsurancePolicy.apply)(InsurancePolicy.unapply)

  val insurancePolicyForm = Form (insurancePolicyMapping)

  val businessInterestForm= Form (basicEstateElementMapping("error.assets.businessInterest.select"))

  val nominatedForm= Form (basicEstateElementMapping("error.assets.nominated.select"))

  val trustsOwnedQuestionForm = Form(
    mapping(
      "isOwned" -> yesNoQuestion("error.assets.heldInTrust.deceasedOwned.select")
    )(
        (isOwned) => HeldInTrust(None,None,isOwned)
      )(
        (heldInTrust: HeldInTrust) => Some(heldInTrust.isOwned)
      )
  )

  val trustsMoreThanOneQuestionForm = Form(
    mapping(
      "isMoreThanOne" -> yesNoQuestion("error.assets.heldInTrust.moreThanOne.select")
    )(
      (isMoreThanOne) => HeldInTrust(isMoreThanOne,None,None)
    )(
      (heldInTrust: HeldInTrust) => Some(heldInTrust.isMoreThanOne)
    )
  )

  val trustsValueForm = Form(
    mapping(
      "value" -> OptionalCurrency()
    )(
      (value) => HeldInTrust(None,value,None)
    )(
      (heldInTrust: HeldInTrust) => Some(heldInTrust.value)
    )
  )

  val foreignForm = Form (basicEstateElementMapping("error.assets.foreign.select"))

  val moneyOwedForm = Form (basicEstateElementMapping("error.assets.moneyOwedToDeceased.select"))

  val otherForm = Form (basicEstateElementMapping("error.assets.other.select"))

  val propertiesForm = Form(
    mapping(
      "isOwned" -> yesNoQuestion("error.assets.property.owned.select")
    )(Properties.apply)(Properties.unapply)
  )

  val propertyValueForm = Form(
    mapping(
      "value" -> MandatoryCurrency()
    )(
        (value)=> Property(None, None, None, None, None, value)
      )(
        (property: Property) => Some(property.value)
      ))

  // Gifts forms.
  val giftsGivenAwayForm = Form(
    mapping("isGivenAway" -> yesNoQuestion("error.giftsGivenAway.select")
    )(
        (isGivenAway) => AllGifts(isGivenAway, None, None, None , None)
      )
      (
          (allGifts: AllGifts) => Some(allGifts.isGivenAway)
        )
  )

  val giftWithReservationFromBenefitForm = Form(
    mapping("reservation.isReservation" -> yesNoQuestion("error.giftWithReservationFromBenefit.select")
    )(
        (giftWithReservation) => AllGifts(None, giftWithReservation, None, None ,None)
      )
      (
          (allGifts: AllGifts) => Some(allGifts.isReservation)
        )
  )

  val giftSevenYearsToTrustForm = Form(
    mapping("trust.isToTrust" -> yesNoQuestion("error.giftSevenYearsToTrust.select")
    )(
        (isToTrust) => AllGifts(None, None, isToTrust, None ,None)
      )
      (
          (allGifts: AllGifts) => Some(allGifts.isToTrust)
        )
  )

  val giftSevenYearsGivenInLast7YearsForm = Form(
    mapping("givenInPast.isGivenInLast7Years" -> yesNoQuestion("error.giftSevenYearsGivenInLast7Years.select")
    )(
        (isGivenInLast7Years) => AllGifts(None, None, None, isGivenInLast7Years ,None)
      )
      (
          (allGifts: AllGifts) => Some(allGifts.isGivenInLast7Years)
        )
  )

  val previousYearsGiftsForm = Form(mapping(
    "yearId" -> of(IhtFormValidator.validateGiftsDetails("value", "exemptions")),
    "value" -> OptionalCurrency(),
    "exemptions" -> OptionalCurrency(),
    "startDate" -> optional(text),
    "endDate" -> optional(text)
  )(PreviousYearsGifts.apply)(PreviousYearsGifts.unapply))

  val sevenYearsGiftsValuesForm = Form(mapping(
    "action" -> optional(text)
  )
    (
        (action) => AllGifts(None, None, None, None, action)
      )
    (
        (allGifts: AllGifts) => Some(allGifts.action)
      )
  )

  // Debts forms.
  val  mortgagesForm = Form(
    mapping(
      "id" -> text,
      "value" -> OptionalCurrency(),
      "isOwned" -> yesNoQuestion("error.debts.mortgage.select")
    )(Mortgage.apply)(Mortgage.unapply)
  )

  val funeralExpensesForm = Form(basicEstateElementLiabilitiesMapping("error.debts.funeralExpenses.select"))

  val debtsTrustForm = Form(basicEstateElementLiabilitiesMapping("error.debts.trusts.select"))

  val debtsOutsideUkForm = Form(basicEstateElementLiabilitiesMapping("error.debts.debtsOutsideUk.select"))

  val jointlyOwnedDebts = Form(basicEstateElementLiabilitiesMapping("error.debts.jointlyOwned.select"))

  val anyOtherDebtsForm = Form(basicEstateElementLiabilitiesMapping("error.debts.anyOther.select"))


  // Exemptions forms.
  val assetsLeftToSpouseQuestionForm = Form(mapping(
    "isAssetForDeceasedPartner" -> yesNoQuestion("error.isAssetForDeceasedPartner.select")
  )(
    (isAssetForDeceasedPartner) => PartnerExemption(isAssetForDeceasedPartner, None, None, None, None, None, None)
  )
  (
    (partnerExemption: PartnerExemption) => Some(partnerExemption.isAssetForDeceasedPartner)
  )
  )

  val partnerPermanentHomeQuestionForm = Form(mapping(
    "isPartnerHomeInUK" -> yesNoQuestion("error.isPartnerHomeInUK.select")
  )(
    (isPartnerHomeInUK) => PartnerExemption(None, isPartnerHomeInUK, None, None, None, None, None)
  )
  (
    (partnerExemption: PartnerExemption) => Some(partnerExemption.isPartnerHomeInUK)
  )
  )

  val partnerNinoForm = Form(mapping(
    "nino" -> nino
  )(
    (nino) => PartnerExemption(None, None, None, None, None, Some(nino), None)
  )
  (
    (partnerExemption: PartnerExemption) => partnerExemption.nino
  )
  )

  val partnerValueForm = Form(mapping(
    "totalAssets" -> MandatoryCurrency()
  )
  (
    (totalAssets) => PartnerExemption(None, None, None, None, None, None, totalAssets)
  )
  (
    (partnerExemption: PartnerExemption) => Some(partnerExemption.totalAssets)
  )
  )


  val charityNumberForm = Form(mapping(
    "charityNumber" -> ihtNonEmptyText("error.charityNumber.give")
      .verifying("error.charityNumber.enterUsingOnly6Or7Numbers",
        f=>f.length <= IhtProperties.validationMaxCharityNumberLength)
      .verifying("error.charityNumber.enterUsingOnly6Or7Numbers",
        f=>f.length >= IhtProperties.validationMinCharityNumberLength || f.length==0)
  )(
      (charityNumber) => Charity(None, None, Some(charityNumber), None)
    )
    (
        (charity: Charity) => charity.number
      )
  )

  val spouseDateOfBirthForm = Form(mapping(
    "dateOfBirth" -> DateMapping.dateOfBirth
  )(
      (dateOfBirth) => PartnerExemption(None, None, None, None, Some(dateOfBirth), None, None)
    )
    (
        (partnerExemption: PartnerExemption) => partnerExemption.dateOfBirth
      )
  )

  val partnerExemptionNameForm = Form(mapping(
  "firstName" -> ihtNonEmptyText( "error.firstName.give")
    .verifying("error.firstName.giveUsingXCharsOrLess",
      _.trim.length < IhtProperties.validationMaxLengthFirstName),
  "lastName" -> ihtNonEmptyText( "error.lastName.give")
    .verifying( "error.lastName.giveUsingXCharsOrLess",
      _.trim.length < IhtProperties.validationMaxLengthLastName))
    (
      (firstName, lastName) => PartnerExemption(None, None, Some(firstName), Some(lastName), None, None, None)
    )
    (
      (partnerExemption: PartnerExemption) => Some(Tuple2(
        partnerExemption.firstName.getOrElse(""),
        partnerExemption.lastName.getOrElse("")))
    )
  )

  val assetsLeftToCharityQuestionForm = Form(mapping(
    "isAssetForCharity" -> yesNoQuestion("error.isAssetForCharity.select")
  )(
      (isAssetForCharity) => BasicExemptionElement(isAssetForCharity)
    )
    (
        (basicExemption: BasicExemptionElement) => Some(basicExemption.isSelected)
      )
  )

  val assetsLeftToCharityValueForm: Form[Charity] = Form(mapping(
    "totalValue" -> MandatoryCurrency()
  )(
    totalValue => Charity(None, None, None, totalValue)
  )
  (
    (charity: Charity) => Option(charity.totalValue)
  )
  )

  val charityNameForm = Form(mapping(
    "name" -> ihtNonEmptyText("error.charityName.enterName")
      .verifying("error.charityName.giveUsing35CharactersOrLess", f=>f.length < 36))
  (
    (name) => Charity(None, Some(name), None, None)
  )
  (
    (charity: Charity) => charity.name
  )
  )

  val qualifyingBodyValueForm: Form[QualifyingBody] = Form(mapping(
    "totalValue" -> MandatoryCurrency()
  )(
    totalValue => QualifyingBody(None, None, totalValue)
  )(
    (qualifyingBody: QualifyingBody) => Option(qualifyingBody.totalValue)
  )
  )

  val qualifyingBodyNameForm: Form[QualifyingBody] = Form(mapping(
    "name" -> ihtNonEmptyText("error.qualifyingBodyName.enterName")
      .verifying("error.qualifyingBodyName.giveUsing35CharactersOrLess", f=>f.length < 36))
  (
    name => QualifyingBody(None, Some(name), None)
  )(
    (qualifyingBody: QualifyingBody) => qualifyingBody.name
  ))

  val assetsLeftToQualifyingBodyQuestionForm = Form(mapping(
    "isAssetForQualifyingBody" -> yesNoQuestion("error.isAssetForQualifyingBody.select")
  )(
      (isAssetForQualifyingBody) => BasicExemptionElement(isAssetForQualifyingBody)
    )
    (
        (basicExemption: BasicExemptionElement) => Some(basicExemption.isSelected)
    )
  )

  // Declaration form.
  val declarationForm = Form(
    single(
      "isDeclared" -> of(IhtFormValidator.validateDeclaration)
    )
  )
}
