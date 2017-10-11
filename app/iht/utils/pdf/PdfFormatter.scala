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

package iht.utils.pdf

import iht.constants.FieldMappings.{applicantCountryMap, domicileMap, maritalStatusMap}
import iht.constants.{Constants, FieldMappings, IhtProperties}
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.models.application.basicElements.{BasicEstateElement, ShareableBasicEstateElement}
import iht.models.des.ihtReturn._
import iht.models.{ApplicantDetails, RegistrationDetails, UkAddress}
import iht.utils.{CommonHelper, GiftsHelper}
import org.joda.time.LocalDate
import play.api.i18n.Messages

import scala.collection.immutable.ListMap

/**
  * Created by vineet on 13/06/16.
  */
object PdfFormatter {

  def getYearFromDate(inputDate: String): Int = {
    val jodadate = LocalDate.parse(inputDate)
    jodadate.getYear
  }

  def updateETMPOptionSet[B](optionSetOfB: Option[Set[B]],
                             getExprToLookupAsOption: B => Option[String],
                             lookupItems: ListMap[String, String],
                             applyLookedUpItemToB: (B, String) => B): Option[Set[B]] =
    optionSetOfB.map(_.map(b => getExprToLookupAsOption(b).fold(b)(ac =>
      lookupItems.get(ac).fold(b)(newValue => applyLookedUpItemToB(b, newValue)))))

  def updateETMPOptionSeq[B](optionSetOfB: Option[Seq[B]],
                             getExprToLookupAsOption: B => Option[String],
                             lookupItems: ListMap[String, String],
                             applyLookedUpItemToB: (B, String) => B): Option[Seq[B]] =
    optionSetOfB.map(_.map(b => getExprToLookupAsOption(b).fold(b)(ac =>
      lookupItems.get(ac).fold(b)(newValue => applyLookedUpItemToB(b, newValue)))))

  def combineGiftSets(masterSet: Seq[Gift], subSet: Seq[Gift]): Seq[Gift] = {
    masterSet.map { masterGift =>
      subSet.find(_.dateOfGift == masterGift.dateOfGift) match {
        case None => masterGift
        case Some(g) => g
      }
    }
  }

  // scalastyle:off magic.number
  def padGifts(setOfGifts: Seq[Gift], dateOfDeath: LocalDate): Seq[Gift] = {
    val allPreviousYearsGifts: Seq[Gift] = GiftsHelper.createPreviousYearsGiftsLists(dateOfDeath).map { previousYearsGifts =>
      val endDate = previousYearsGifts.endDate.map(s => LocalDate.parse(s))
      val giftValueOrZero = Option(previousYearsGifts.value.fold(BigDecimal(0))(identity))
      val exemptionValueOrZero = Option(previousYearsGifts.exemptions.fold(BigDecimal(0))(identity))
      val netGiftValue = giftValueOrZero.fold(BigDecimal(0))(identity) - exemptionValueOrZero.fold(BigDecimal(0))(identity)

      Gift(
        assetCode = Some("9095"),
        assetDescription = Some("Rolled up gifts"),
        assetID = Some("null"),
        assetTotalValue = Some(netGiftValue),
        valuePrevOwned = giftValueOrZero,
        percentageSharePrevOwned = Some(BigDecimal(100)),
        valueRetained = Some(BigDecimal(0)),
        percentageRetained = Some(BigDecimal(0)),
        howheld = Some("Standard"),
        lossToEstate = Some(netGiftValue),
        dateOfGift = endDate
      )
    }
    combineGiftSets(allPreviousYearsGifts, setOfGifts)
  }

  private def updateFromAssetShareableBasicEstateElement(currentAsset: Asset, optionShareableBasicEstateElement: Option[ShareableBasicEstateElement]) = {
    currentAsset.howheld match {
      case Some("Standard") =>
        optionShareableBasicEstateElement.map(_ copy(
          isOwned = Some(true),
          value = currentAsset.assetTotalValue
        )
        )
      case _ =>
        optionShareableBasicEstateElement.map(_ copy(
          isOwnedShare = Some(true),
          shareValue = currentAsset.assetTotalValue
        )
        )
    }
  }

  private def updateFromAssetPrivatePension(currentAsset: Asset, optionPrivatePension: Option[PrivatePension]) = {
    optionPrivatePension.map(_ copy(
      isOwned = Some(true),
      value = currentAsset.assetTotalValue
    )
    )
  }

  private def updateFromAssetStockAndShareNotListed(currentAsset: Asset, optionStockAndShare: Option[StockAndShare]) = {
        optionStockAndShare.map(_ copy(
          isNotListed = Some(true),
          valueNotListed = currentAsset.assetTotalValue
        )
        )
  }

  private def updateFromAssetStockAndShareListed(currentAsset: Asset, optionStockAndShare: Option[StockAndShare]) = {
    optionStockAndShare.map(_ copy(
      isListed = Some(true),
      valueListed = currentAsset.assetTotalValue
    )
    )
  }

  private def updateFromAssetBasicEstateElement(currentAsset: Asset, optionBasicEstateElement: Option[BasicEstateElement]) = {
    optionBasicEstateElement.map(_ copy(
          isOwned = Some(true),
          value = currentAsset.assetTotalValue
        )
        )
  }

  private def updateFromAssetHeldInTrust(currentAsset: Asset, optionHeldInTrust: Option[HeldInTrust]) = {
    optionHeldInTrust.map(_ copy(
      isOwned = Some(true),
      value = currentAsset.assetTotalValue
    )
    )
  }

  private def updateFromAssetInsurancePolicy(currentAsset: Asset, optionInsurancePolicy: Option[InsurancePolicy]) = {
    currentAsset.howheld match {
      case Some("Standard") =>
        optionInsurancePolicy.map(_ copy(
          policyInDeceasedName = Some(true),
          value = currentAsset.assetTotalValue
        )
        )
      case _ =>
        optionInsurancePolicy.map(_ copy(
          isJointlyOwned = Some(true),
          shareValue = currentAsset.assetTotalValue
        )
        )
    }
  }

  private def transformAssets1(currentAllAssets: AllAssets, currentAsset:Asset): Option[AllAssets] = {
    currentAsset.assetCode match {
      case Some("9001") => Some(currentAllAssets copy (money = updateFromAssetShareableBasicEstateElement(currentAsset, currentAllAssets.money)))
      case Some("9004") => Some(currentAllAssets copy (household = updateFromAssetShareableBasicEstateElement(currentAsset, currentAllAssets.money)))
      case Some("9005") => Some(currentAllAssets copy (privatePension = updateFromAssetPrivatePension(currentAsset, currentAllAssets.privatePension)))
      case Some("9008") => Some(currentAllAssets copy (stockAndShare = updateFromAssetStockAndShareListed(currentAsset, currentAllAssets.stockAndShare)))
      case Some("9010") => Some(currentAllAssets copy (stockAndShare = updateFromAssetStockAndShareNotListed(currentAsset, currentAllAssets.stockAndShare)))
      case Some("9006") => Some(currentAllAssets copy (insurancePolicy = updateFromAssetInsurancePolicy(currentAsset, currentAllAssets.insurancePolicy)))
      case _ => None
    }
  }

  private def transformAssets2(currentAllAssets: AllAssets, currentAsset:Asset): Option[AllAssets] = {
    currentAsset.assetCode match {
      case Some("9021") => Some(currentAllAssets copy (businessInterest = updateFromAssetBasicEstateElement(currentAsset, currentAllAssets.businessInterest)))
      case Some("9099") => Some(currentAllAssets copy (nominated = updateFromAssetBasicEstateElement(currentAsset, currentAllAssets.nominated)))
      case Some("9097") => Some(currentAllAssets copy (heldInTrust = updateFromAssetHeldInTrust(currentAsset, currentAllAssets.heldInTrust)))
      case Some("9098") => Some(currentAllAssets copy (foreign = updateFromAssetBasicEstateElement(currentAsset, currentAllAssets.foreign)))
      case Some("9013") => Some(currentAllAssets copy (moneyOwed = updateFromAssetBasicEstateElement(currentAsset, currentAllAssets.moneyOwed)))
      case Some("9015") => Some(currentAllAssets copy (other = updateFromAssetBasicEstateElement(currentAsset, currentAllAssets.other)))
      case Some("0016") => Some(currentAllAssets copy (properties = Some(Properties(isOwned = Some(true))) ))
      case _ => None
    }
  }

  private def propertyFromAsset(asset: Asset, nextId:Option[String]): Property = {
    val optionUkAddress = asset.propertyAddress.flatMap(_.address).map { addr =>
      UkAddress(addr.addressLine1, addr.addressLine2, addr.addressLine3, addr.addressLine4, addr.postalCode, addr.countryCode)
    }

    val IHTReturnHowHeld = ListMap(
      "Standard" -> "Deceased only",
      "Joint - Beneficial Joint Tenants" -> "Joint",
      "Joint - Tenants In Common" -> "In common" )

    val optionTypeOfOwnership = asset.howheld.flatMap( hh => IHTReturnHowHeld.get(hh))

    val optionPropertyType = asset.assetCode.map {
      case "0016" => IhtProperties.propertyTypeDeceasedHome
      case "0017" => IhtProperties.propertyTypeOtherResidentialBuilding
      case "0018" => IhtProperties.propertyTypeNonResidential
    }

    Property(
      id = nextId,
      address = optionUkAddress,
      propertyType = optionPropertyType,
      typeOfOwnership = optionTypeOfOwnership,
      tenure = asset.tenure,
      value = asset.assetTotalValue
    )
  }

  private def addIfProperty(currentProperties: List[Property], currentAsset:Asset): Option[List[Property]] = {
    def nextId = Some((currentProperties.size + 1).toString)
    currentAsset.assetCode match {
      case Some("0016") =>
        Some(currentProperties :+ propertyFromAsset(currentAsset, nextId))
      case Some("0017") =>
        Some(currentProperties :+ propertyFromAsset(currentAsset, nextId))
      case Some("0018") =>
        Some(currentProperties :+ propertyFromAsset(currentAsset, nextId))
      case _ => None
    }
  }

  private val optionEmptyShareableBasicEstateElement = Some(ShareableBasicEstateElement(
    value = None,
    shareValue = None,
    isOwned = Some(false),
    isOwnedShare = Some(false)
  ))

  private val optionEmptyStockAndShare = Some(StockAndShare(
    value = None,
    valueNotListed = None,
    valueListed = None,
    isListed = Some(false),
    isNotListed = Some(false)
  ))

  private val optionEmptyBasicEstateElement = Some(BasicEstateElement(
    value = None,
    isOwned = Some(false)
  ))

  private val optionEmptyHeldInTrust = Some(HeldInTrust(
    isMoreThanOne = None,
    value = None,
    isOwned = Some(false)
  ))

  private val optionEmptyPrivatePension = Some(PrivatePension(
    isChanged = Some(false),
    value = None,
    isOwned = Some(false)
  ))

  private val optionEmptyInsurance = Some(InsurancePolicy(
    isAnnuitiesBought = None,
    isInsurancePremiumsPayedForSomeoneElse = None,
    value = None,
    shareValue = None,
    policyInDeceasedName = Some(false),
    isJointlyOwned = Some(false),
    isInTrust = None,
    coveredByExemption = None,
    sevenYearsBefore = None,
    moreThanMaxValue = None
  ))

  private def tranformAssets(optionSetAsset: Option[Set[Asset]]): AllAssets = {
    val emptyAllAssets: AllAssets = AllAssets(
      money = optionEmptyShareableBasicEstateElement,
      household = optionEmptyShareableBasicEstateElement,
      privatePension = optionEmptyPrivatePension,
      stockAndShare = optionEmptyStockAndShare,
      insurancePolicy = optionEmptyInsurance,
      businessInterest = optionEmptyBasicEstateElement,
      nominated = optionEmptyBasicEstateElement,
      heldInTrust = optionEmptyHeldInTrust,
      foreign = optionEmptyBasicEstateElement,
      moneyOwed = optionEmptyBasicEstateElement,
      other = optionEmptyBasicEstateElement,
      properties = Some(Properties(isOwned = Some(false)))
    )
    optionSetAsset.map { actualAssetSet =>
      actualAssetSet.foldLeft[AllAssets](emptyAllAssets) { (currentAllAssets, currentAsset) =>
        transformAssets1(currentAllAssets, currentAsset)
          .fold(transformAssets2(currentAllAssets, currentAsset))(Some(_))
          .fold(currentAllAssets)(identity)
      }
    }.fold(emptyAllAssets)(identity)
  }

  private def transformProperties(optionSetAsset: Option[Set[Asset]]): List[Property] = {
    val emptyProperties = List[Property]()
    optionSetAsset.map { actualAssetSet =>
      actualAssetSet.foldLeft[List[Property]](emptyProperties) { (currentProperties, currentAsset) =>
        addIfProperty(currentProperties, currentAsset)
          .fold(currentProperties)(identity)
      }
    }.fold(emptyProperties)(identity)
  }

  def createApplicationDetails(optionSetAsset: Option[Set[Asset]], optionSetTrust: Option[Set[Trust]]): ApplicationDetails = {
    val allAssetsNonTrust = tranformAssets(optionSetAsset)
    val propertyList = transformProperties(optionSetAsset)
    val allAssets = optionSetTrust.map { actualTrustSet =>
      actualTrustSet.foldLeft[AllAssets](allAssetsNonTrust) { (currentAllAssets, currentTrust) =>
        val allAssetsForTrusts = tranformAssets(currentTrust.trustAssets)
        currentAllAssets copy (heldInTrust = allAssetsForTrusts.heldInTrust)
      }
    }.fold(allAssetsNonTrust)(identity)
    ApplicationDetails(
      allAssets = Some(allAssets),
      propertyList = propertyList
    )
  }

  def transform(ihtReturn: IHTReturn, registrationDetails: RegistrationDetails, messages: Messages): IHTReturn = {
    val deceasedName: String = registrationDetails.deceasedDetails.fold("")(_.name)
    val dateOfDeath: LocalDate = CommonHelper.getOrException(registrationDetails.deceasedDateOfDeath.map(_.dateOfDeath))

    val optionSetAsset = updateETMPOptionSet[Asset](ihtReturn.freeEstate.flatMap(_.estateAssets),
      _.assetCode,
      Constants.ETMPAssetCodesToIHTMessageKeys,
      (asset, newDescription) => asset.copy(assetDescription = Option(messages(newDescription, deceasedName)))
    )

    val optionSeqExemption = updateETMPOptionSeq[Exemption](ihtReturn.freeEstate.flatMap(_.estateExemptions),
      _.exemptionType,
      Constants.ETMPExemptionTypesToIHTMessageKeys,
      (exemption, newDescription) => exemption.copy(exemptionType = Option(messages(newDescription, deceasedName)))
    )

    val optionFreeEstate = ihtReturn.freeEstate.map(_ copy(
      estateAssets = optionSetAsset,
      estateExemptions = optionSeqExemption
    )
    )

    val optionSetSetGift = ihtReturn.gifts.map(_.map(setGift => padGifts(setGift, dateOfDeath)))

    ihtReturn copy(
      freeEstate = optionFreeEstate,
      gifts = optionSetSetGift
    )
  }

  private def transformCountryCodeToCountryName(countryCode: String, messages: Messages): String = {
    val input = s"country.$countryCode"
    messages(s"country.$countryCode") match {
      case `input` =>
        ""
      case x => x
    }
  }

  def transform(rd: RegistrationDetails, messages: Messages): RegistrationDetails = {
    val optionDeceasedDetails = rd.deceasedDetails.map { dd =>
      dd copy(
        maritalStatus = dd.maritalStatus.map(ms => maritalStatusMap(messages)(ms)),
        domicile = dd.domicile.map(ms => domicileMap(messages)(ms)),
        ukAddress = dd.ukAddress.map { (addr: UkAddress) =>
          addr copy (
            countryCode = transformCountryCodeToCountryName(addr.countryCode, messages)
            )
        }
      )
    }

    val coExecutors = rd.coExecutors.map { coExec =>
      coExec copy (
        ukAddress = coExec.ukAddress.map { (addr: UkAddress) =>
          addr copy (
            countryCode = transformCountryCodeToCountryName(addr.countryCode, messages)
            )
        }
        )
    }

    val optionApplicantDetails: Option[ApplicantDetails] = rd.applicantDetails.map { ad =>
      ad copy(
        country = ad.country.map(ms => applicantCountryMap(messages)(ms)),
        ukAddress = ad.ukAddress.map { (addr: UkAddress) =>
          addr copy (
            countryCode = transformCountryCodeToCountryName(addr.countryCode, messages)
            )
        }
      )
    }

    rd copy(deceasedDetails = optionDeceasedDetails, applicantDetails = optionApplicantDetails, coExecutors = coExecutors)
  }

  def transform(ad: ApplicationDetails, rd: RegistrationDetails, messages: Messages): ApplicationDetails = {
    val deceasedName = rd.deceasedDetails.fold(messages("iht.theDeceased"))(_.name)

    val transformedSeqProperties = ad.propertyList.map { p =>
      val optionTransformedTenure: Option[String] = p.tenure.map(t => FieldMappings.tenures(deceasedName)(messages)(t)._1)
      val optionTransformedHowheld: Option[String] = p.typeOfOwnership.map {
        hh => FieldMappings.typesOfOwnership(deceasedName)(messages)(hh)._1
      }
      val optionTransformedPropertyType: Option[String] = p.propertyType.map(pt => FieldMappings.propertyType(messages)(pt))

      p copy(tenure = optionTransformedTenure, typeOfOwnership = optionTransformedHowheld, propertyType = optionTransformedPropertyType)
    }
    ad copy (propertyList = transformedSeqProperties)
  }

  /**
    * Calculate the display mode for the estate overview section of the pre-submission PDF. This
    * tells the PDF template how and where the different sections are to be displayed.
    *
    * 1) Base (assets, gifts, debts)
    * 2) Base + TNRB threshold increased + exemptions locked
    * 3) Base + TNRB threshold not increased OR TNRB not completed + exemptions unlocked but 0
    * 4) Base + TNRB threshold increased + exemptions unlocked but 0
    * 5) Base + TNRB threshold increased + exemptions unlocked and > 0
    * 6) Base + TNRB threshold not increased OR TNRB not completed + exemptions unlocked and > 0
    *
    * If none of scenarios 2)-6) apply then it should fall back to scenario 1).
    */
  // scalastyle:off magic.number
  def estateOverviewDisplayMode(ad: ApplicationDetails): Int = {
    val isExemptionsUnlocked: Boolean = ad.hasSeenExemptionGuidance.fold(false)(identity)
    val exemptionsValue = ad.totalExemptionsValue
    val isTnrbCompleted = ad.isSuccessfulTnrbCase
    val zero = BigDecimal(0)
    (isTnrbCompleted, isExemptionsUnlocked, exemptionsValue) match {
      case (true, false, _) => 2
      case (false, true, `zero`) => 3
      case (true, true, `zero`) => 4
      case (true, true, _) => 5
      case (false, true, _) => 6
      case _ => 1
    }
  }

  def estateOverviewDisplayModeForPostPdf(ihtReturn: IHTReturn) = {
    val totalExemptionsValue = ihtReturn.totalExemptionsValue
    if (totalExemptionsValue > 0) {
      Constants.DisplayModeExemption
    } else {
      Constants.DisplayModeNoExemption
    }
  }
  // scalastyle:on magic.number
}
