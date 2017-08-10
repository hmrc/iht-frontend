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

import javax.inject.{Singleton, Inject}
import iht.constants.FieldMappings.maritalStatusMap
import iht.constants.{Constants, FieldMappings}
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.models.des.ihtReturn.{Exemption, Asset, IHTReturn}
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

  /*
   * Get country name from country code
   */
  def countryName(countryCode: String): String = {
    val input = s"country.$countryCode"
    Messages(s"country.$countryCode") match {
      case `input` => {
        ""
      }
      case x => x
    }
  }

  def updateETMPOptionSet[B](optionSetOfB:Option[Set[B]],
                             getExprToLookupAsOption:B=>Option[String],
                             lookupItems:ListMap[String,String],
                             applyLookedUpItemToB:(B,String)=>B):Option[Set[B]] =
    optionSetOfB.map(_.map(b => getExprToLookupAsOption(b).fold(b)(ac =>
        lookupItems.get(ac).fold(b)(newValue => applyLookedUpItemToB(b, newValue)))))

  def updateETMPOptionSeq[B](optionSetOfB:Option[Seq[B]],
                             getExprToLookupAsOption:B=>Option[String],
                             lookupItems:ListMap[String,String],
                             applyLookedUpItemToB:(B,String)=>B):Option[Seq[B]] =
    optionSetOfB.map(_.map(b => getExprToLookupAsOption(b).fold(b)(ac =>
      lookupItems.get(ac).fold(b)(newValue => applyLookedUpItemToB(b, newValue)))))

  def transform(ihtReturn:IHTReturn, deceasedName: String, messages: Messages): IHTReturn = {
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

    val optionFreeEstate = ihtReturn.freeEstate.map(_ copy (
      estateAssets = optionSetAsset,
      estateExemptions = optionSeqExemption
      )
    )

    ihtReturn copy (freeEstate = optionFreeEstate)
  }

  def transform(rd: RegistrationDetails, messages: Messages): RegistrationDetails = {
    val optionDeceasedDetails = rd.deceasedDetails.map { dd =>
      dd copy (maritalStatus = dd.maritalStatus.map(ms => maritalStatusMap(messages)(ms)))
    }
    rd copy (deceasedDetails = optionDeceasedDetails)
  }

  def transform(ad: ApplicationDetails, rd: RegistrationDetails, messages: Messages): ApplicationDetails = {
    val deceasedName = rd.deceasedDetails.fold(messages("iht.theDeceased"))(_.name)

    val transformedSeqProperties = ad.propertyList.map { p =>
      val optionTransformedTenure: Option[String] = p.tenure.map(t => FieldMappings.tenures(deceasedName)(messages)(t)._1)
      val optionTransformedHowheld: Option[String] = p.typeOfOwnership.map{
                hh => FieldMappings.typesOfOwnership(deceasedName)(messages)(hh)._1
      }
      val optionTransformedPropertyType: Option[String] = p.propertyType.map(pt => FieldMappings.propertyType(messages)(pt))

      p copy (tenure = optionTransformedTenure, typeOfOwnership = optionTransformedHowheld, propertyType = optionTransformedPropertyType)
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
  def estateOverviewDisplayMode(ad:ApplicationDetails):Int = {
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
  // scalastyle:on magic.number
}
