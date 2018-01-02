/*
 * Copyright 2018 HM Revenue & Customs
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

package iht.models.application.assets

import iht.testhelpers.CommonBuilder
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by vineet on 03/11/16.
  */
class StockAndShareTest extends UnitSpec with MockitoSugar{

  "totalValue" must {

    "returns correct total value of stock and share" in {
      val stockAndShare = CommonBuilder.buildStockAndShare.copy(valueListed = Some(BigDecimal(10000)),
        valueNotListed = Some(BigDecimal(20000)))

      stockAndShare.totalValue shouldBe Some(BigDecimal(30000))
    }

    "returns None, if values for listed and notListed are None" in {
      val stockAndShare = CommonBuilder.buildStockAndShare

      stockAndShare.totalValue shouldBe empty
    }
  }

  "isValueEntered" must {

    "returns true if both valueListed and valueNotListed or any of it are entered " in {
      val stockAndShare = CommonBuilder.buildStockAndShare.copy(valueListed = Some(BigDecimal(10000)),
        valueNotListed = None)

      stockAndShare.isValueEntered shouldBe true
    }

    "returns false if values for listed and notListed shares are not entered" in {
      val stockAndShare = CommonBuilder.buildStockAndShare

      stockAndShare.isValueEntered shouldBe false
    }
  }

  "isComplete" must {

    "returns Some(true) if StockAndShare is complete" in {
      val stockAndShare = CommonBuilder.buildStockAndShare.copy(isListed = Some(true),valueListed = Some(BigDecimal(10000)),
        isNotListed = Some(false), valueNotListed = None)

      stockAndShare.isComplete shouldBe Some(true)
    }

    "returns Some(false) if StockAndShare is not complete" in {
      val stockAndShare = CommonBuilder.buildStockAndShare.copy(isListed = Some(true),valueListed = Some(BigDecimal(10000)),
        isNotListed = Some(true), valueNotListed = None)

      stockAndShare.isComplete shouldBe Some(false)
    }

    "returns None if every field is None in StockAndShare" in {
      val stockAndShare = CommonBuilder.buildStockAndShare
      stockAndShare.isComplete shouldBe empty
    }
  }

}
