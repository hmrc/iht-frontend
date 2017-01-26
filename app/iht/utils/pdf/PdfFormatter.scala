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

import javax.inject.Inject

import iht.constants.{Constants, IhtProperties}
import iht.utils.CommonHelper
import models.des.iht_return.{Asset, Exemption, IHTReturn, Liability}
import org.joda.time.LocalDate
import play.api.i18n.{I18nSupport, Messages, MessagesApi}

import scala.collection.immutable.{ListMap, SortedSet}

/**
  * Created by vineet on 13/06/16.
  */
class PdfFormatter @Inject()(val messagesApi: MessagesApi) extends I18nSupport {

  def getDateForDisplay(inputDate: String): String = {

    val jodaDate = LocalDate.parse(inputDate)
    jodaDate.toString(IhtProperties.dateFormatForDisplay)
  }

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

  def transform(ihtReturn:IHTReturn): IHTReturn = {
    val optionSetAsset = updateETMPOptionSet[Asset](ihtReturn.freeEstate.flatMap(_.estateAssets),
      _.assetCode,
      Constants.ETMPAssetCodesToIHTMessageKeys,
      (asset, newDescription) => asset.copy(assetDescription = Option(Messages(newDescription)))
    )

    val optionSetExemption = updateETMPOptionSet[Exemption](ihtReturn.freeEstate.flatMap(_.estateExemptions),
      _.exemptionType,
      Constants.ETMPExemptionTypesToIHTMessageKeys,
      (exemption, newDescription) => exemption.copy(exemptionType = Option(Messages(newDescription)))
    )

    val optionFreeEstate = ihtReturn.freeEstate.map(_ copy (
      estateAssets = optionSetAsset,
      estateExemptions = optionSetExemption
      )
    )

    ihtReturn copy (freeEstate = optionFreeEstate)
  }
}
