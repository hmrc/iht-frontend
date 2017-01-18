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

import iht.constants.{Constants, IhtProperties}
import models.des.iht_return.IHTReturn
import org.joda.time.LocalDate
import play.api.i18n.Messages

/**
  * Created by vineet on 13/06/16.
  */
object PdfFormatter {

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

  def transform(ihtReturn:IHTReturn): IHTReturn = {
    val optionSetAsset = ihtReturn.freeEstate.flatMap(_.estateAssets).map { setOfAssets =>
      setOfAssets.map { asset =>
        asset.assetCode.fold(asset){ ac =>
          Constants.ETMPAssetCodesToIHTMessageKeys.get(ac).fold(asset){ newAssetDescription =>
            asset.copy(assetDescription = Option(Messages(newAssetDescription)))
          }
        }
      }
    }
    val optionFreeEstate = ihtReturn.freeEstate.map(_ copy (estateAssets = optionSetAsset))
    ihtReturn copy (freeEstate = optionFreeEstate)
  }
}
