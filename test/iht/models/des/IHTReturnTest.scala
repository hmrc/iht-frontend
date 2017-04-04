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
import iht.testhelpers.IHTReturnTestHelper._
import org.joda.time.LocalDate
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

class IHTReturnTest extends UnitSpec with FakeIhtApp with MockitoSugar {
  "IHTReturn" must {
   "total assets values" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      ihtReturn.totalAssetsValue shouldBe BigDecimal(754)
    }

    "total debts values" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      ihtReturn.totalDebtsValue shouldBe BigDecimal(340)
    }


    "total exemptions values" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      ihtReturn.totalExemptionsValue shouldBe BigDecimal(141)
    }

    "total gifts values" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      ihtReturn.totalGiftsValue shouldBe BigDecimal(27800)
    }

    "total trusts values" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      ihtReturn.totalTrustsValue shouldBe BigDecimal(17)
    }

    "total values of gifts excluding exemptions" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      ihtReturn.giftsTotalExclExemptions shouldBe BigDecimal(28000)
    }

    "total values of gifts exemption" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      ihtReturn.giftsExemptionsTotal shouldBe BigDecimal(200)
    }

    "total net value" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      ihtReturn.totalNetValue shouldBe BigDecimal(28073)
    }

    "show 650K as the threshold value when there is TNRB" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      ihtReturn.currentThreshold shouldBe BigDecimal(650000)
    }

    "show 325K as the threshold value when there is no TNRB" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      val deceasedValue = ihtReturn.deceased

      ihtReturn.copy(deceased = deceasedValue.map{
        x => x.copy(transferOfNilRateBand = None)
      }).currentThreshold shouldBe BigDecimal(325000)
    }
  }
}
