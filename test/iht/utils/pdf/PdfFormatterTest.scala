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

import iht.FakeIhtApp
import iht.testhelpers.IHTReturnTestHelper.buildIHTReturnCorrespondingToApplicationDetailsAllFields
import models.des.iht_return.Asset
import org.joda.time.LocalDate
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec

import scala.collection.immutable.ListMap

/**
  * Created by david-beer on 21/11/16.
  */
class PdfFormatterTest extends UnitSpec with FakeIhtApp with MockitoSugar {

  "disply value" must {

    "must return date in format of d MMMM yyyy" in {
      val result = PdfFormatter.getDateForDisplay("1975-10-24")

      result shouldBe "24 October 1975"
    }

    "must throw exception on invalid date" in {
      a[IllegalArgumentException] shouldBe thrownBy {
        PdfFormatter.getDateForDisplay("20 1019")
      }
    }

    "must return the year from specified date" in {
      val result = PdfFormatter.getYearFromDate("1990-06-05")
      result shouldBe 1990
    }

    "must return Australia fo AU" in {
      val result = PdfFormatter.countryName("AU")
      result shouldBe "Australia"
    }
  }

  "transform asset descriptions correctly for display" in {
    val etmpTitlesMappedToPDFMessageKeys = ListMap(
      "Rolled up bank and building society accounts" -> "iht.estateReport.assets.money.upperCaseInitial",
      "Rolled up household and personal goods" -> "iht.estateReport.assets.householdAndPersonalItems.title",
      "Rolled up pensions" -> "iht.estateReport.assets.privatePensions",
      "Rolled up unlisted stocks and shares" -> "iht.estateReport.assets.stocksAndSharesNotListed",
      "Rolled up quoted stocks and shares" -> "iht.estateReport.assets.stocksAndSharesListed",
      "Rolled up life assurance policies" -> "iht.estateReport.assets.insurancePolicies",
      "Rolled up business assets" -> "iht.estateReport.assets.businessInterests.title",
      "Rolled up nominated assets" -> "iht.estateReport.assets.nominated",
      "Rolled up trust assets" -> "iht.estateReport.assets.heldInATrust.title",
      "Rolled up foreign assets" -> "iht.estateReport.assets.foreign.title",
      "Rolled up money owed to deceased" -> "iht.estateReport.assets.moneyOwed",
      "Rolled up other assets" -> "iht.estateReport.assets.other.title"
    )
    val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "")
    val expectedSetOfAssets = ihtReturn.freeEstate.flatMap(_.estateAssets)
      .fold[Set[Asset]](Set.empty)(identity).map{ asset =>
      val newAssetDescription = asset.assetDescription.map(x=>
        etmpTitlesMappedToPDFMessageKeys.get(x) match {
          case None => x
          case Some(newMessageKey) => Messages(newMessageKey)
        }
      )
      asset copy(assetDescription = newAssetDescription)
    }
    val result = PdfFormatter.transform(ihtReturn)
    val setOfAssets = result.freeEstate.flatMap(_.estateAssets).fold[Set[Asset]](Set.empty)(identity)
    setOfAssets shouldBe expectedSetOfAssets
  }

}
