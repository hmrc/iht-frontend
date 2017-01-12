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
import models.des.iht_return.Asset
import org.joda.time.LocalDate
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec

import scala.collection.immutable.ListMap

class IHTReturnTest extends UnitSpec with FakeIhtApp with MockitoSugar {
  "IHTReturn" must {
    "total assets values" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      ihtReturn.totalAssetsValue shouldBe BigDecimal(754)
    }

    "total debts values" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      ihtReturn.totalDebtsValue shouldBe BigDecimal(110)
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
  }
}
