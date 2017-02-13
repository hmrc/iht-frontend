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

package iht.config

import iht.FakeIhtApp
import iht.constants.IhtProperties
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerTest
import uk.gov.hmrc.play.test.UnitSpec

/**
 *
 * Created by Vineet Tyagi on 29/09/15.
 *
 */
class IhtPropertiesReaderTest extends UnitSpec with OneAppPerTest with MockitoSugar{

  "IhtPropertiesReaderTest" must {

    "should read the key and return appropriate value" in {
      val maxExecutors = IhtProperties.maxCoExecutors
      val ukIsoCountryCode = IhtProperties.ukIsoCountryCode
      val govUkLink = IhtProperties.linkGovUkIht

      assert(maxExecutors == 3,"Maximum executors value is 3")
      ukIsoCountryCode shouldBe "GB"
      assert(govUkLink=="https://www.gov.uk/inheritance-tax" , "Link value is https://www.gov.uk/inheritance-tax")
    }
  }
}
