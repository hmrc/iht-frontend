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

package iht.utils.pdf

import iht.config.AppConfig
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.models.application.basicElements.{BasicEstateElement, ShareableBasicEstateElement}
import iht.models.des.ihtReturn.{Asset, Exemption, IHTReturn, Trust}
import iht.models.{RegistrationDetails, UkAddress}
import iht.utils.CommonHelper
import iht.utils.pdf.PdfFormatter.{padGifts, updateETMPOptionSeq, updateETMPOptionSet}
import org.joda.time.LocalDate
import play.api.i18n.Messages

import scala.collection.immutable.ListMap

trait PdfHelper {
  implicit val appConfig: AppConfig

  private lazy val ETMPAssetCodesToIHTMessageKeys = ListMap(
    appConfig.ETMPAssetCodeMoney -> "iht.estateReport.assets.money.upperCaseInitial",
    appConfig.ETMPAssetCodeHouseHold -> "iht.estateReport.assets.householdAndPersonalItems.title",
    appConfig.ETMPAssetCodePrivatePension -> "iht.estateReport.assets.privatePensions",
    appConfig.ETMPAssetCodeStockShareNotListed -> "iht.estateReport.assets.stocksAndSharesNotListed",
    appConfig.ETMPAssetCodeStockShareListed -> "iht.estateReport.assets.stocksAndSharesListed",
    appConfig.ETMPAssetCodeInsurancePolicy -> "iht.estateReport.assets.insurancePolicies",
    appConfig.ETMPAssetCodeBusinessInterest -> "iht.estateReport.assets.businessInterests.title",
    appConfig.ETMPAssetCodeNominatedAsset -> "iht.estateReport.assets.nominated",
    appConfig.ETMPAssetCodeForeignAsset -> "iht.estateReport.assets.foreign.title",
    appConfig.ETMPAssetCodeMoneyOwed -> "iht.estateReport.assets.moneyOwed",
    appConfig.ETMPAssetCodeOtherAsset -> "page.iht.application.assets.main-section.other.title",
    appConfig.ETMPAssetCodeTrust -> "iht.estateReport.assets.heldInATrust.title",
    appConfig.ETMPAssetCodeDeceasedsHome -> "page.iht.application.assets.propertyType.deceasedHome.label"
  )

  private lazy val ETMPExemptionTypesToIHTMessageKeys = ListMap(
    appConfig.ETMPExemptionTypeCharity -> "pdf.exemption.charity",
    appConfig.ETMPExemptionTypeSpouse -> "pdf.exemption.spouse",
    appConfig.ETMPExemptionTypeGNCP -> "pdf.exemption.otherQualifyingBodies"
  )

  def transform(ihtReturn: IHTReturn, registrationDetails: RegistrationDetails, messages: Messages): IHTReturn = {
    val deceasedName: String = registrationDetails.deceasedDetails.fold("")(_.name)
    val dateOfDeath: LocalDate = CommonHelper.getOrException(registrationDetails.deceasedDateOfDeath.map(_.dateOfDeath))

    val optionSetAsset = updateETMPOptionSet[Asset](ihtReturn.freeEstate.flatMap(_.estateAssets),
      _.assetCode,
      ETMPAssetCodesToIHTMessageKeys,
      (asset, newDescription) => asset.copy(assetDescription = Option(messages(newDescription, deceasedName)))
    )

    val optionSeqExemption = updateETMPOptionSeq[Exemption](ihtReturn.freeEstate.flatMap(_.estateExemptions),
      _.exemptionType,
      ETMPExemptionTypesToIHTMessageKeys,
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
      case Some("9004") => Some(currentAllAssets copy (household = updateFromAssetShareableBasicEstateElement(currentAsset, currentAllAssets.household)))
      case Some("9005") => Some(currentAllAssets copy (privatePension = updateFromAssetPrivatePension(currentAsset, currentAllAssets.privatePension)))
      case Some("9008") => Some(currentAllAssets copy (stockAndShare = updateFromAssetStockAndShareListed(currentAsset, currentAllAssets.stockAndShare)))
      case Some("9010") => Some(currentAllAssets copy (stockAndShare = updateFromAssetStockAndShareNotListed(currentAsset, currentAllAssets.stockAndShare)))
      case Some("9006") => Some(currentAllAssets copy (insurancePolicy = updateFromAssetInsurancePolicy(currentAsset, currentAllAssets.insurancePolicy)))
      case _ => None
    }
  }

  private[utils] def transformAssets2(currentAllAssets: AllAssets, currentAsset:Asset): Option[AllAssets] = {
    currentAsset.assetCode match {
      case Some("9021") => Some(currentAllAssets copy (businessInterest = updateFromAssetBasicEstateElement(currentAsset, currentAllAssets.businessInterest)))
      case Some("9099") => Some(currentAllAssets copy (nominated = updateFromAssetBasicEstateElement(currentAsset, currentAllAssets.nominated)))
      case Some("9097") => Some(currentAllAssets copy (heldInTrust = updateFromAssetHeldInTrust(currentAsset, currentAllAssets.heldInTrust)))
      case Some("9098") => Some(currentAllAssets copy (foreign = updateFromAssetBasicEstateElement(currentAsset, currentAllAssets.foreign)))
      case Some("9013") => Some(currentAllAssets copy (moneyOwed = updateFromAssetBasicEstateElement(currentAsset, currentAllAssets.moneyOwed)))
      case Some("9015") => Some(currentAllAssets copy (other = updateFromAssetBasicEstateElement(currentAsset, currentAllAssets.other)))
      case Some("0016" | "0017" | "0018") => Some(currentAllAssets copy (properties = Some(Properties(isOwned = Some(true))) ))
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

  private def propertyFromAsset(asset: Asset, nextId:Option[String]): Property = {
    val optionUkAddress = asset.propertyAddress.map { addr =>
      UkAddress(addr.addressLine1, addr.addressLine2, addr.addressLine3, addr.addressLine4, addr.postalCode, addr.countryCode)
    }

    val IHTReturnHowHeld = ListMap(
      "Standard" -> "Deceased only",
      "Joint - Beneficial Joint Tenants" -> "Joint",
      "Joint - Tenants In Common" -> "In common" )

    val optionTypeOfOwnership = asset.howheld.flatMap( hh => IHTReturnHowHeld.get(hh))

    val optionPropertyType = asset.assetCode.map {
      case "0016" => appConfig.propertyTypeDeceasedHome
      case "0017" => appConfig.propertyTypeOtherResidentialBuilding
      case "0018" => appConfig.propertyTypeNonResidential
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
}
