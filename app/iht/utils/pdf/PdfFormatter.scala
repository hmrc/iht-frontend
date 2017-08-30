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

import javax.inject.{Inject, Singleton}

import iht.utils.GiftsHelper
import iht.constants.FieldMappings.maritalStatusMap
import iht.constants.FieldMappings.domicileMap
import iht.constants.FieldMappings.applicantCountryMap
import iht.constants.{Constants, FieldMappings}
import iht.models.{ApplicantDetails, RegistrationDetails, UkAddress}
import iht.models.application.ApplicationDetails
import iht.models.application.gifts.PreviousYearsGifts
import iht.models.des.ihtReturn.{Asset, Exemption, Gift, IHTReturn}
import iht.utils.{CommonHelper, GiftsHelper}
import iht.views.html.filter
import iht.views.html.filter.domicile
import org.joda.time.LocalDate
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

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
        ukAddress = dd.ukAddress.map{ (addr: UkAddress) =>
          addr copy (
            countryCode = transformCountryCodeToCountryName(addr.countryCode, messages)
            )
        }
      )
    }

    val coExecutors = rd.coExecutors.map { coExec =>
      coExec copy (
        ukAddress = coExec.ukAddress.map{ (addr: UkAddress) =>
          addr copy (
            countryCode = transformCountryCodeToCountryName(addr.countryCode, messages)
            )
        }
      )
    }

    val optionApplicantDetails: Option[ApplicantDetails] = rd.applicantDetails.map { ad =>
      ad copy (
        country = ad.country.map(ms => applicantCountryMap(messages)(ms)),
        ukAddress = ad.ukAddress.map{ (addr: UkAddress) =>
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
