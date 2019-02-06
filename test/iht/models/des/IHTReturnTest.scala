/*
 * Copyright 2019 HM Revenue & Customs
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
import iht.models.des.ihtReturn.{Gift, IHTReturn}
import iht.testhelpers.CommonBuilder
import iht.testhelpers.IHTReturnTestHelper._
import org.joda.time.LocalDate
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

class IHTReturnTest extends FakeIhtApp with MockitoSugar {
  "IHTReturn" must {
   "total assets values" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      ihtReturn.totalAssetsValue mustBe BigDecimal(754)
    }

    "total debts values" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      ihtReturn.totalDebtsValue mustBe BigDecimal(340)
    }


    "total exemptions values" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      ihtReturn.totalExemptionsValue mustBe BigDecimal(169)
    }

    "total gifts values" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      ihtReturn.totalGiftsValue mustBe BigDecimal(27800)
    }

    "total trusts values" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      ihtReturn.totalTrustsValue mustBe BigDecimal(17)
    }

    "total values of gifts excluding exemptions" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      ihtReturn.giftsTotalExclExemptions mustBe BigDecimal(28000)
    }

    "total values of gifts exemption" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      ihtReturn.giftsExemptionsTotal mustBe BigDecimal(200)
    }

    "total net value" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      ihtReturn.totalNetValue mustBe BigDecimal(28062)
    }

    "show 650K as the threshold value when there is TNRB" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      ihtReturn.currentThreshold mustBe BigDecimal(650000)
    }

    "show 325K as the threshold value when there is no TNRB" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      val deceasedValue = ihtReturn.deceased

      ihtReturn.copy(deceased = deceasedValue.map{
        x => x.copy(transferOfNilRateBand = None)
      }).currentThreshold mustBe BigDecimal(325000)
    }

    "sum assets correctly" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      ihtReturn.totalForAssetIDs(Set("9004", "9001")) mustBe BigDecimal(21)
    }

    "sum and group exemptions correctly" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      val expectedResult = Map("Spouse" -> BigDecimal(25), "Charity" -> BigDecimal(83), "GNCP" -> BigDecimal(61))
      ihtReturn.exemptionTotalsByExemptionType mustBe expectedResult
    }
  }

  "sortByGiftDate" must {
    "sort correctly" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      val expectedDates = Set(Seq[Option[LocalDate]](
        Some(new LocalDate("2011-04-05")),
        Some(new LocalDate("2010-04-05")),
        Some(new LocalDate("2009-04-05")),
        Some(new LocalDate("2008-04-05")),
        Some(new LocalDate("2007-04-05")),
        Some(new LocalDate("2006-04-05")),
        Some(new LocalDate("2005-04-05"))
      ))

      val sortedIHTReturn: IHTReturn = IHTReturn.sortByGiftDate(ihtReturn)
      val actualDates: Set[Seq[Option[LocalDate]]] = sortedIHTReturn.gifts.map(_.map(_.toSeq.map(_.dateOfGift)))
        .fold[Set[Seq[Option[LocalDate]]]](Set.empty)(identity)
      actualDates mustBe expectedDates
    }
  }

  "isTnrbApplicable" must {
    "return false when there is no tnrb" in {

      val ihtReturnWihNoTnrb = CommonBuilder.buildIHTReturn
      ihtReturnWihNoTnrb.isTnrbApplicable mustBe false
    }

    "return true when there is no tnrb" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")

      ihtReturn.isTnrbApplicable mustBe true
    }
  }
}
