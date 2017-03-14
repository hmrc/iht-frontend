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

package iht.models.des

import iht.FakeIhtApp
import iht.models.application.tnrb.{TnrbEligibiltyModel, WidowCheck}
import iht.testhelpers.IHTReturnTestHelper
import iht.testhelpers.IHTReturnTestHelper._
import models.des.iht_return._
import org.joda.time.LocalDate
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

class IHTReturnTest extends UnitSpec with FakeIhtApp with MockitoSugar {
  def buildIHTReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")

  "IHTReturn" must {
    "total assets values" in {
      val ihtReturn = buildIHTReturn
      ihtReturn.totalAssetsValue shouldBe BigDecimal(754)
    }

    "total debts values" in {
      val ihtReturn = buildIHTReturn
      ihtReturn.totalDebtsValue shouldBe BigDecimal(110)
    }


    "total exemptions values" in {
      val ihtReturn = buildIHTReturn
      ihtReturn.totalExemptionsValue shouldBe BigDecimal(141)
    }

    "total gifts values" in {
      val ihtReturn = buildIHTReturn
      ihtReturn.totalGiftsValue shouldBe BigDecimal(27800)
    }

    "total trusts values" in {
      val ihtReturn = buildIHTReturn
      ihtReturn.totalTrustsValue shouldBe BigDecimal(17)
    }
  }

  "toOptionTNRBEligibilityModel" must {
    "convert correctly where there are values" in {
      val ihtReturn = buildIHTReturn
      val optionSpouse: Option[Spouse] = ihtReturn.deceased.flatMap(_.transferOfNilRateBand).map(_.deceasedSpouses.head).flatMap(_.spouse)
      val expectedFirstName: Option[String] = optionSpouse.flatMap(_.firstName)
      val expectedLastName: Option[String] = optionSpouse.flatMap(_.lastName)
      val expectedDateOfMarriage: Option[LocalDate] = optionSpouse.flatMap(_.dateOfMarriage)
      val expectedDateOfPreDeceased: Option[LocalDate] = optionSpouse.flatMap(_.dateOfDeath)

      val expectedResult = Some(
        TnrbEligibiltyModel(
          isPartnerLivingInUk = Some(true),
          isGiftMadeBeforeDeath = Some(false),
          isStateClaimAnyBusiness = Some(true),
          isPartnerGiftWithResToOther = Some(false),
          isPartnerBenFromTrust = Some(true),
          isEstateBelowIhtThresholdApplied = Some(false),
          isJointAssetPassed = Some(true),
          firstName = expectedFirstName,
          lastName = expectedLastName,
          dateOfMarriage = expectedDateOfMarriage,
          dateOfPreDeceased = expectedDateOfPreDeceased
        )
      )
      val result = ihtReturn.toOptionTNRBEligibilityModel
      result shouldBe expectedResult
    }

    "convert correctly where there is no deceased value" in {
      val ihtReturn: IHTReturn = buildIHTReturn.copy(deceased = None)
      val expectedResult = None
      val result = ihtReturn.toOptionTNRBEligibilityModel
      result shouldBe expectedResult
    }
  }

  "toOptionWidowCheck" must {
    "convert correctly where there are values" in {
      val ihtReturn = buildIHTReturn
      val optionSpouse: Option[Spouse] = ihtReturn.deceased.flatMap(_.transferOfNilRateBand).map(_.deceasedSpouses.head).flatMap(_.spouse)
      val expectedDate: Option[LocalDate] = optionSpouse.flatMap(_.dateOfDeath)
      val result: Option[WidowCheck] = ihtReturn.toOptionWidowCheck
      result shouldBe Some(WidowCheck(Some(true), expectedDate))
    }

    "convert correctly where there is no deceased value" in {
      val ihtReturn = buildIHTReturn.copy(deceased = None)
      val result: Option[WidowCheck] = ihtReturn.toOptionWidowCheck
      result shouldBe None
    }

    "convert correctly where there is a deceased value but no predeceased date of death" in {
      val deceasedWithNoDOD: Deceased = IHTReturnTestHelper.buildTNRB(None)
      val ihtReturn: IHTReturn = buildIHTReturn.copy(deceased = Some(deceasedWithNoDOD))
      val result: Option[WidowCheck] = ihtReturn.toOptionWidowCheck
      result shouldBe Some(WidowCheck(Some(false), None))
    }
  }
}
