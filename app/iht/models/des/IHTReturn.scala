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

package models.des.iht_return


import iht.constants.Constants
import iht.models.Joda._
import iht.utils.CommonHelper
import org.joda.time.LocalDate
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, Json, Reads}
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.collection.immutable.ListMap

// Can reuse the address object from Event Registration
import models.des.Address

case class OtherAddress(addressLine1: Option[String] = None, addressLine2: Option[String] = None,
                        addressLine3: Option[Option[String]] = None, addressLine4: Option[Option[String]] = None,
                        postalCode: Option[String] = None, countryCode: Option[String] = None,
                        addressType: Option[String] = None)

object OtherAddress {
  implicit val optionStringReads: Reads[Option[Option[String]]] = Reads.optionWithNull[Option[String]]

  implicit val otherAddressReads: Reads[OtherAddress] = (
    (JsPath \ "addressLine1").readNullable[String] and
      (JsPath \ "addressLine2").readNullable[String] and
      (JsPath \ "addressLine3").read(optionStringReads) and
      (JsPath \ "addressLine4").read(optionStringReads) and
      (JsPath \ "postalCode").readNullable[String] and
      (JsPath \ "countryCode").readNullable[String] and
      (JsPath \ "addressType").readNullable[String]
    )(OtherAddress.apply _)

  implicit val formats = Json.format[OtherAddress]
}

case class SpousesEstate(domiciledInUk: Option[Boolean] = None, whollyExempt: Option[Boolean] = None,
                         jointAssetsPassingToOther: Option[Boolean] = None,
                         otherGifts: Option[Boolean] = None,
                         agriculturalOrBusinessRelief: Option[Boolean] = None,
                         giftsWithReservation: Option[Boolean] = None,
                         benefitFromTrust: Option[Boolean] = None, unusedNilRateBand: Option[BigDecimal] = None)

object SpousesEstate {
  implicit val formats = Json.format[SpousesEstate]
}

case class Spouse(
                   // Person
                   title: Option[String] = None, firstName: Option[String] = None, middleName: Option[String] = None,
                   lastName: Option[String] = None, dateOfBirth: Option[LocalDate] = None,
                   gender: Option[String] = None, nino: Option[String] = None, utr: Option[String] = None,
                   mainAddress: Option[Address] = None, OtherAddresses: Option[Set[OtherAddress]] = None,

                   // Other
                   dateOfMarriage: Option[LocalDate] = None, dateOfDeath: Option[LocalDate] = None
                 )

object Spouse {
  implicit val spouseReads: Reads[Spouse] = (
    (JsPath \ "title").readNullable[String] and
      (JsPath \ "firstName").readNullable[String] and
      (JsPath \ "middleName").readNullable[String] and
      (JsPath \ "lastName").readNullable[String] and
      (JsPath \ "dateOfBirth").readNullable[LocalDate] and
      (JsPath \ "gender").readNullable[String] and
      (JsPath \ "nino").readNullable[String] and
      (JsPath \ "utr").readNullable[String] and
      (JsPath \ "mainAddress").readNullable[Address] and
      (JsPath \ "otherAddress").readNullable[Set[OtherAddress]] and
      (JsPath \ "dateOfMarriage").readNullable[LocalDate] and
      (JsPath \ "dateOfDeath").readNullable[LocalDate]
    )(Spouse.apply _)

  implicit val formats = Json.format[Spouse]
}

case class TNRBForm(spouse: Option[Spouse] = None, spousesEstate: Option[SpousesEstate] = None)

object TNRBForm {
  implicit val tNRBFormReads: Reads[TNRBForm] = (
    (JsPath \ "sposue").readNullable[Spouse] and
      (JsPath \ "spouseesEstate").readNullable[SpousesEstate]
    )(TNRBForm.apply _)
  implicit val formats = Json.format[TNRBForm]
}

case class TransferOfNilRateBand(totalNilRateBandTransferred: Option[BigDecimal] = None,
                                 deceasedSpouses: Set[TNRBForm])

object TransferOfNilRateBand {
  implicit val transferOfNilRateBandReads: Reads[TransferOfNilRateBand] = (
    (JsPath \ "totalNilRateBandTransferred").readNullable[BigDecimal] and
      (JsPath \ "deceasedSpouses").read[Set[TNRBForm]]
    )(TransferOfNilRateBand.apply _)
  implicit val formats = Json.format[TransferOfNilRateBand]
}

case class SurvivingSpouse(
                            // Person
                            title: Option[String] = None, firstName: Option[String] = None,
                            middleName: Option[String] = None,
                            lastName: Option[String] = None, dateOfBirth: Option[LocalDate] = None,
                            gender: Option[String] = None, nino: Option[String] = None, utr: Option[String] = None,
                            mainAddress: Option[Address] = None, OtherAddresses: Option[Set[OtherAddress]] = None,

                            // Other
                            dateOfMarriage: Option[LocalDate] = None, domicile: Option[String] = None,
                            otherDomicile: Option[String] = None)

object SurvivingSpouse {
  implicit val survivingSpouse: Reads[SurvivingSpouse] = (
    (JsPath \ "title").readNullable[String] and
      (JsPath \ "firstName").readNullable[String] and
      (JsPath \ "middleName").readNullable[String] and
      (JsPath \ "lastName").readNullable[String] and
      (JsPath \ "dateOfBirth").readNullable[LocalDate] and
      (JsPath \ "gender").readNullable[String] and
      (JsPath \ "nino").readNullable[String] and
      (JsPath \ "utr").readNullable[String] and
      (JsPath \ "mainAddress").readNullable[Address] and
      (JsPath \ "otherAddress").readNullable[Set[OtherAddress]] and
      (JsPath \ "dateOfMarriage").readNullable[LocalDate] and
      (JsPath \ "domicile").readNullable[String] and
      (JsPath \ "otherDomicile").readNullable[String]
    )(SurvivingSpouse.apply _)
  implicit val formats = Json.format[SurvivingSpouse]
}

case class OtherLandLocation(locationDescription: Option[String] = None)

object OtherLandLocation {
  implicit val formats = Json.format[OtherLandLocation]
}

case class AddressOrOtherLandLocation(address: Option[Address] = None,
                                      otherLandLocation: Option[OtherLandLocation] = None)

object AddressOrOtherLandLocation {
  implicit val formats = Json.format[AddressOrOtherLandLocation]
}

case class JointOwner(
                       // Person
                       title: Option[String] = None, firstName: Option[String] = None,
                       middleName: Option[String] = None,
                       lastName: Option[String] = None, dateOfBirth: Option[LocalDate] = None,
                       gender: Option[String] = None, nino: Option[String] = None, utr: Option[String] = None,
                       mainAddress: Option[Address] = None, OtherAddresses: Option[Set[OtherAddress]] = None,

                       // Organisation
                       name: Option[String] = None, ctUtr: Option[String] = None,
                       organisationAddress: Option[Address] = None,

                       // Charity
                       charityNumber: Option[String] = None, charityName: Option[String] = None,
                       charityCountry: Option[String] = None,

                       // Other
                       relationshipToDeceased: Option[String] = None,
                       percentageContribution: Option[String] = None, percentageOwned: Option[String] = None
                     )

object JointOwner {
  implicit val formats = Json.format[JointOwner]
}

case class JointOwnership(percentageOwned: Option[String] = None,
                          jointOwners: Option[Set[JointOwner]] = None,
                          dateOfJointOwnership: Option[LocalDate] = None,
                          percentageContribution: Option[String] = None,
                          valueOfShare: Option[String] = None)

object JointOwnership {
  implicit val formats = Json.format[JointOwnership]
}

case class Allocation(percentageShare: Option[String] = None, overrideAmount: Option[String] = None)

object Allocation {
  implicit val formats = Json.format[Allocation]
}

case class OtherBeneficiary(
                             // Person
                             title: Option[String] = None, firstName: Option[String] = None,
                             middleName: Option[String] = None,
                             lastName: Option[String] = None, dateOfBirth: Option[LocalDate] = None,
                             gender: Option[String] = None, nino: Option[String] = None, utr: Option[String] = None,
                             mainAddress: Option[Address] = None, OtherAddresses: Option[Set[OtherAddress]] = None,

                             // Organisation
                             name: Option[String] = None, ctUtr: Option[String] = None,
                             organisationAddress: Option[Address] = None,

                             // Charity
                             charityNumber: Option[String] = None, charityName: Option[String] = None,
                             charityCountry: Option[String] = None,

                             // Gift for national purpose
                             name1: Option[String] = None
                           )

object OtherBeneficiary {
  implicit val formats = Json.format[OtherBeneficiary]
}

case class Beneficiary(passingToSpouse: Option[String] = None,
                       otherBeneficiary: Option[OtherBeneficiary] = None)

object Beneficiary {
  implicit val beneficiaryReads: Reads[Beneficiary] = (
  (JsPath \ "passingToSpouse").readNullable[String] and
    (JsPath \ "otherBeneficiary").readNullable[OtherBeneficiary]
  )(Beneficiary.apply _)
  implicit val formats = Json.format[Beneficiary]
}


case class Exemption(exemptionType: Option[String] = None,
                     percentageAmount: Option[BigDecimal] = None,
                     overrideValue: Option[BigDecimal] = None)

object Exemption {
  implicit val formats = Json.format[Exemption]
}

case class Devolution(allocation: Option[Allocation] = None,
                      beneficiary: Option[Beneficiary] = None,
                      exemption: Option[Exemption] = None)

object Devolution {
  implicit val devolutionReads: Reads[Devolution] = (
    (JsPath \ "allocation").readNullable[Allocation] and
      (JsPath \ "beneficiary").readNullable[Beneficiary] and
      (JsPath \ "exemption").readNullable[Exemption]
    )(Devolution.apply _)
  implicit val formats = Json.format[Devolution]
}

case class Liability(liabilityType: Option[String] = None,
                     liabilityAmount: Option[BigDecimal] = None,
                     liabilityOwner: Option[String] = None)

object Liability {
  implicit val formats = Json.format[Liability]
}

case class Asset(
                  // General asset
                  assetCode: Option[String] = None,
                  assetDescription: Option[String] = None,
                  assetID: Option[String] = None,
                  assetTotalValue: Option[BigDecimal] = None,
                  howheld: Option[String] = None,
                  devolutions: Option[Set[Devolution]] = None,
                  liabilities: Option[Set[Liability]] = None,

                  // Property asset
                  propertyAddress: Option[AddressOrOtherLandLocation] = None,
                  tenure: Option[String] = None, tenancyType: Option[String] = None,
                  yearsLeftOnLease: Option[Int] = None,
                  yearsLeftOntenancyAgreement: Option[Int] = None,
                  professionalValuation: Option[Boolean] = None
                )

object Asset {
  implicit val assetReads: Reads[Asset] = (
    (JsPath \ "assetCode").readNullable[String] and
      (JsPath \ "assetDescription").readNullable[String] and
      (JsPath \ "assetID").readNullable[String] and
      (JsPath \ "assetTotalValue").readNullable[BigDecimal] and
      (JsPath \ "howheld").readNullable[String] and
      (JsPath \ "devolutions").readNullable[Set[Devolution]] and
      (JsPath \ "liabilities").readNullable[Set[Liability]] and
      (JsPath \ "propertyAddress").readNullable[AddressOrOtherLandLocation] and
      (JsPath \ "tenure").readNullable[String] and
      (JsPath \ "tenancyType").readNullable[String] and
      (JsPath \ "yearsLeftOnLease").readNullable[Int] and
      (JsPath \ "yearsLeftOntenancyAgreement").readNullable[Int] and
      (JsPath \ "professionalValuation").readNullable[Boolean]
    )(Asset.apply _)
  implicit val formats = Json.format[Asset]
}

case class InterestInOtherEstate(
                                  // Person
                                  title: Option[String] = None, firstName: Option[String] = None, middleName: Option[String] = None,
                                  lastName: Option[String] = None, dateOfBirth: Option[LocalDate] = None,
                                  gender: Option[String] = None, nino: Option[String] = None, utr: Option[String] = None,
                                  mainAddress: Option[Address] = None, OtherAddresses: Option[Set[OtherAddress]] = None,

                                  // Other
                                  otherEstateAssets: Option[Set[Asset]] = None
                                )

object InterestInOtherEstate {
  implicit val interestInOtherEstateReads: Reads[InterestInOtherEstate] = (
    (JsPath \ "title").readNullable[String] and
      (JsPath \ "firstName").readNullable[String] and
      (JsPath \ "middleName").readNullable[String] and
      (JsPath \ "lastName").readNullable[String] and
      (JsPath \ "dateOfBirth").readNullable[LocalDate] and
      (JsPath \ "gender").readNullable[String] and
      (JsPath \ "nino").readNullable[String] and
      (JsPath \ "utr").readNullable[String] and
      (JsPath \ "mainAddress").readNullable[Address] and
      (JsPath \ "otherAddress").readNullable[Set[OtherAddress]] and
      (JsPath \ "otherEstateAssets").readNullable[Set[Asset]]
    )(InterestInOtherEstate.apply _)
  implicit val formats = Json.format[InterestInOtherEstate]
}

case class Trustee(
                    // Person
                    title: Option[String] = None, firstName: Option[String] = None, middleName: Option[String] = None,
                    lastName: Option[String] = None, dateOfBirth: Option[LocalDate] = None,
                    gender: Option[String] = None, nino: Option[String] = None, utr: Option[String] = None,
                    mainAddress: Option[Address] = None,
                    OtherAddresses: Option[Set[OtherAddress]] = None,

                    // Organisation
                    name: Option[String] = None, ctUtr: Option[String] = None,
                    organisationAddress: Option[Address] = None
                  )

object Trustee {
  implicit val trusteeReads: Reads[Trustee] = (
    (JsPath \ "title").readNullable[String] and
      (JsPath \ "firstName").readNullable[String] and
      (JsPath \ "middleName").readNullable[String] and
      (JsPath \ "lastName").readNullable[String] and
      (JsPath \ "dateOfBirth").readNullable[LocalDate] and
      (JsPath \ "gender").readNullable[String] and
      (JsPath \ "nino").readNullable[String] and
      (JsPath \ "utr").readNullable[String] and
      (JsPath \ "mainAddress").readNullable[Address] and
      (JsPath \ "otherAddress").readNullable[Set[OtherAddress]] and
      (JsPath \ "name").readNullable[String] and
      (JsPath \ "ctUtr").readNullable[String] and
      (JsPath \ "organisationAddress").readNullable[Address]
    )(Trustee.apply _)
  implicit val formats = Json.format[Trustee]
}

case class Submitter(submitterRole: Option[String] = None)

object Submitter {
  implicit val formats = Json.format[Submitter]
}

// Deceased here differs from that in Event Registration
case class Deceased(survivingSpouse: Option[SurvivingSpouse] = None,
                    transferOfNilRateBand: Option[TransferOfNilRateBand] = None)

object Deceased {
  implicit val deceasedReads: Reads[Deceased] = (
    (JsPath \ "survingSpouse").readNullable[SurvivingSpouse] and
      (JsPath \ "transferOfNilRateBand").readNullable[TransferOfNilRateBand]
    )(Deceased.apply _)
  implicit val formats = Json.format[Deceased]
}

case class FreeEstate(estateAssets: Option[Set[Asset]] = None,
                      interestInOtherEstate: Option[InterestInOtherEstate] = None,
                      estateLiabilities: Option[Set[Liability]] = None,
                      estateExemptions: Option[Set[Exemption]] = None)

object FreeEstate {
  implicit val freeEstateReads: Reads[FreeEstate] = (
    (JsPath \ "estateAssets").readNullable[Set[Asset]] and
      (JsPath \ "interestInOtherEstate").readNullable[InterestInOtherEstate] and
      (JsPath \ "estateLiabilities").readNullable and
      (JsPath \ "estateExemptions").readNullable[Set[Exemption]]
    )(FreeEstate.apply _)
  implicit val formats = Json.format[FreeEstate]
}

case class Gift(
                 // General asset
                 assetCode: Option[String] = None,
                 assetDescription: Option[String] = None,
                 assetID: Option[String] = None,
                 assetTotalValue: Option[BigDecimal] = None,
                 howheld: Option[String] = None,
                 devolutions: Option[Set[Devolution]] = None,
                 liabilities: Option[Set[Liability]] = None,

                 // Property asset
                 propertyAddress: Option[AddressOrOtherLandLocation] = None,
                 tenure: Option[String] = None,
                 tenancyType: Option[String] = None,
                 yearsLeftOnLease: Option[Int] = None,
                 yearsLeftOntenancyAgreement: Option[Int] = None,
                 professionalValuation: Option[Boolean] = None,
                 voaValue: Option[String] = None,
                 jointOwnership: Option[JointOwnership] = None,

                 // Other
                 valuePrevOwned: Option[BigDecimal] = None,
                 percentageSharePrevOwned: Option[BigDecimal] = None,
                 valueRetained: Option[BigDecimal] = None,
                 percentageRetained: Option[BigDecimal] = None,
                 lossToEstate: Option[BigDecimal] = None,
                 dateOfGift: Option[LocalDate] = None
               )

object Gift {
  implicit val giftsReads: Reads[Gift] = (
    (JsPath \ "assetCode").readNullable[String] and
      (JsPath \ "assetDescription").readNullable[String] and
      (JsPath \ "assetID").readNullable[String] and
      (JsPath \ "assetTotalValue").readNullable[BigDecimal] and
      (JsPath \ "howheld").readNullable[String] and
      (JsPath \ "devolutions").readNullable[Set[Devolution]] and
      (JsPath \ "liabilities").readNullable[Set[Liability]] and
      (JsPath \ "propertyAddress").readNullable[AddressOrOtherLandLocation] and
      (JsPath \ "tenure").readNullable[String] and
      (JsPath \ "tenancyType").readNullable[String] and
      (JsPath \ "yearsLeftOnLease").readNullable[Int] and
      (JsPath \ "yearsLeftOntenancyAgreement").readNullable[Int] and
      (JsPath \ "professionalValuation").readNullable[Boolean] and
      (JsPath \ "voaValue").readNullable[String] and
      (JsPath \ "jointOwnership").readNullable[JointOwnership] and
      (JsPath \ "valuePrevOwned").readNullable[BigDecimal] and
      (JsPath \ "percentageSharePrevOwned").readNullable[BigDecimal] and
      (JsPath \ "valueRetained").readNullable[BigDecimal] and
      (JsPath \ "percentageRetained").readNullable[BigDecimal] and
      (JsPath \ "lossToEstate").readNullable[BigDecimal] and
      (JsPath \ "dateOfGift").readNullable[LocalDate]
    )(Gift.apply _)
  implicit val formats = Json.format[Gift]
}

case class Trust(trustName: Option[String] = None,
                 trustUtr: Option[String] = None,
                 trustees: Option[Set[Trustee]] = None,
                 trustAssets: Option[Set[Asset]] = None,
                 trustLiabilities: Option[Set[Liability]] = None,
                 trustExemptions: Option[Set[Exemption]] = None)

object Trust {
  implicit val trustReads: Reads[Trust] = (
    (JsPath \ "trustName").readNullable[String] and
      (JsPath \ "trsutUtr").readNullable[String] and
      (JsPath \ "trustees").readNullable[Set[Trustee]] and
      (JsPath \ "trustAssets").readNullable[Set[Asset]] and
      (JsPath \ "trstLiabilities").readNullable[Set[Liability]] and
      (JsPath \ "trustExemptions").readNullable[Set[Exemption]]
    )(Trust.apply _)
  implicit val formats = Json.format[Trust]
}

case class Declaration(reasonForBeingBelowLimit: Option[String] = None,
                       declarationAccepted: Option[Boolean] = None,
                       coExecutorsAccepted: Option[Boolean] = None,
                       declarationDate: Option[LocalDate] = None)

object Declaration {
  implicit val declarationReads: Reads[Declaration] = (
    (JsPath \ "reasonForBeingBelowLimit").readNullable[String] and
      (JsPath \ "declarationAccepted").readNullable[Boolean] and
      (JsPath \ "coExecutorsAccepted").readNullable[Boolean] and
      (JsPath \ "declarationDate").readNullable[LocalDate]
    )(Declaration.apply _)

  implicit val formats = Json.format[Declaration]
}

case class IHTReturn(acknowledgmentReference: Option[String] = None,
                     submitter: Option[Submitter] = None,
                     deceased: Option[Deceased] = None,
                     freeEstate: Option[FreeEstate] = None,
                     gifts: Option[Set[Set[Gift]]] = None,
                     trusts: Option[Set[Trust]] = None,
                     declaration: Option[Declaration] = None) {

  def totalAssetsValue =
    freeEstate.flatMap(_.estateAssets).fold(BigDecimal(0))(_.foldLeft(BigDecimal(0))(
      (a, b) => a + b.assetTotalValue.fold(BigDecimal(0))(identity)))

  def totalDebtsValue =
    freeEstate.flatMap(_.estateLiabilities).fold(BigDecimal(0))(_.foldLeft(BigDecimal(0))(
      (a, b) => a + b.liabilityAmount.fold(BigDecimal(0))(identity)))


  def totalExemptionsValue =
    freeEstate.flatMap(_.estateExemptions).fold(BigDecimal(0))(_.foldLeft(BigDecimal(0))(
      (a, b) => a + b.overrideValue.fold(BigDecimal(0))(identity)))

  def totalGiftsValue = gifts.fold[Set[Gift]](Set())(_.flatten)
    .foldLeft(BigDecimal(0))((a, b) => a + b.assetTotalValue.fold(BigDecimal(0))(identity))

  def totalTrustsValue = {
    trusts.fold(BigDecimal(0)) { (setOfTrust: Set[Trust]) =>
      val setOfAsset: Set[Asset] = setOfTrust.flatMap(_.trustAssets.fold[Set[Asset]](Set())(identity))
      setOfAsset.foldLeft(BigDecimal(0))((a, b) => a + b.assetTotalValue.fold(BigDecimal(0))(identity))
    }
  }
}

object IHTReturn {


  implicit val ihtReturnReads: Reads[IHTReturn] = (
    (JsPath \ "acknowledgmentReference").readNullable[String] and
      (JsPath \ "submitter").readNullable[Submitter] and
      (JsPath \ "deceased").readNullable[Deceased] and
      (JsPath \ "freeEstate").readNullable[FreeEstate] and
      (JsPath \ "gifts").readNullable[Set[Set[Gift]]] and
      (JsPath \ "trusts").readNullable[Set[Trust]] and
      (JsPath \ "declaration").readNullable[Declaration]
    )(IHTReturn.apply _)
  implicit val formats = Json.format[IHTReturn]

  def sortByGiftDate(ihtReturn: IHTReturn) = {
    val setOfGifts: Set[Gift] = ihtReturn.gifts.fold[Set[Gift]](Set())(_.flatten)
    val seqOfGifts: Seq[Gift] = setOfGifts.toSeq
    val sortedGifts: Seq[Gift] = seqOfGifts.sortBy(_.dateOfGift)
    val sortedSetGifts: Option[Set[Set[Gift]]] = if (sortedGifts.isEmpty) {
      None
    } else {
      Some(Set(sortedGifts.toSet))
    }
    ihtReturn copy (gifts = sortedSetGifts)
  }
}
