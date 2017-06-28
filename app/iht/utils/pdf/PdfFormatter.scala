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

import javax.inject.Singleton

import iht.constants.FieldMappings.maritalStatusMap
import iht.constants.{Constants, FieldMappings}
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import models.des.iht_return.{Asset, Exemption, IHTReturn}
import org.joda.time.LocalDate
import play.api.i18n.Messages

import scala.collection.immutable.ListMap

@Singleton
class PdfFormatter {

  def getYearFromDate(inputDate: String): Int = {
    val jodadate = LocalDate.parse(inputDate)
    jodadate.getYear
  }

  /*
   * Get country name from country code
   */
  def countryName(countryCode: String)(implicit messages: Messages): String = {
    val input = s"country.$countryCode"
    messages(s"country.$countryCode") match {
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
      constants.ETMPAssetCodesToIHTMessageKeys,
      (asset, newDescription) => asset.copy(assetDescription = Option(messages(newDescription, deceasedName)))
    )

    val optionSeqExemption = updateETMPOptionSeq[Exemption](ihtReturn.freeEstate.flatMap(_.estateExemptions),
      _.exemptionType,
      constants.ETMPExemptionTypesToIHTMessageKeys,
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
}
