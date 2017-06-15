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

import iht.forms.FormTestHelper
import iht.testhelpers.CommonBuilder
import iht.testhelpers.IHTReturnTestHelper.buildIHTReturnCorrespondingToApplicationDetailsAllFields
import models.des.iht_return.Asset
import org.joda.time.LocalDate
import play.api.i18n.{Messages, MessagesApi}

import scala.collection.immutable.ListMap

/**
  * Created by david-beer on 21/11/16.
  */
class PdfFormatterTest extends FormTestHelper {

  val regDetails = CommonBuilder.buildRegistrationDetails1

  "display value" must {
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
      "Rolled up other assets" -> "page.iht.application.assets.main-section.other.title",
      "Deceased's residence" -> "page.iht.application.assets.propertyType.deceasedHome.label"
    )
    val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "")
    val expectedSetOfAssets = ihtReturn.freeEstate.flatMap(_.estateAssets)
      .fold[Set[Asset]](Set.empty)(identity).map{ asset =>
      val newAssetDescription = asset.assetDescription.map(x=>
        etmpTitlesMappedToPDFMessageKeys.get(x) match {
          case None => x
          case Some(newMessageKey) => messagesApi(newMessageKey, CommonBuilder.DefaultString)
        }
      )
      asset copy(assetDescription = newAssetDescription)
    }
    val result = PdfFormatter.transform(ihtReturn, CommonBuilder.DefaultString, messages)
    val setOfAssets = result.freeEstate.flatMap(_.estateAssets).fold[Set[Asset]](Set.empty)(identity)
    setOfAssets shouldBe expectedSetOfAssets
  }

  "transform" must {
    "transform the marital status" in {
      val rd = PdfFormatter.transform(CommonBuilder.buildRegistrationDetails4, messages)
      val result = rd.deceasedDetails.flatMap(_.maritalStatus).fold("")(identity)
      result shouldBe messagesApi("page.iht.registration.deceasedDetails.maritalStatus.civilPartnership.label")
    }

    "map the tenure value from messages file" in {
      val appDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = CommonBuilder.buildPropertyList)
      val appDetailsAfterFormatting = PdfFormatter.transform(appDetails, regDetails, messages)

      val result = appDetailsAfterFormatting.propertyList.head.tenure
      result shouldBe Some(messagesApi("page.iht.application.assets.tenure.freehold.label"))
    }

    "map the property type value from messages file" in {
      val appDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = CommonBuilder.buildPropertyList)
      val appDetailsAfterFormatting = PdfFormatter.transform(appDetails, regDetails, messages)

      val result = appDetailsAfterFormatting.propertyList.head.propertyType
      result shouldBe Some(messagesApi("page.iht.application.assets.propertyType.deceasedHome.label"))
    }

    "map the property ownership value from messages file" in {
      val appDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = CommonBuilder.buildPropertyList)
      val regDetails = CommonBuilder.buildRegistrationDetails1
      val appDetailsAfterFormatting = PdfFormatter.transform(appDetails, regDetails, messages)

      val result = appDetailsAfterFormatting.propertyList.head.typeOfOwnership
      result shouldBe Some(messagesApi("page.iht.application.assets.typeOfOwnership.deceasedOnly.label",
        regDetails.deceasedDetails.fold("")(_.name)))
    }
  }
}
