/*
 * Copyright 2020 HM Revenue & Customs
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

package iht.utils

import iht.FakeIhtApp
import org.scalatestplus.mockito.MockitoSugar

/**
 * Created by yusuf on 06/10/15.
 */
class UtilPackageTest extends FakeIhtApp with MockitoSugar {

  "UtilsPackage" must {
    "split IHT reference numbers into XXX XXXX XXXX XXXX" in {
      formattedIHTReference("ABC012345678A01") must be("ABC 0123 4567 8A01")
    }

    "split IHT reference numbers into XXX XXXX XXXX XX" in {
      formattedIHTReference("ABC0123678A01") must be("ABC 0123 678A 01")
    }


    "split IHT reference numbers into XXX XXXX XXXX" in {
      formattedIHTReference("ABC0123678A") must be("ABC 0123 678A")
    }

    "Split IHT reference numbers into XXX" in {
      formattedIHTReference("ABC") must be("ABC")
    }

    "Empty IHT reference must return empty string" in {
      formattedIHTReference("") must be ("")
    }

    "Split Probate reference numbers into XXXX XXXX XXX-XXX" in {
      formattedProbateReference("12345678A01-123") must be("1234 5678 A01-123")
    }

    "Split Probate reference numbers into XXXX XXXX XXX-X" in {
      formattedProbateReference("1234A001") must be("1234 A001")
    }

    "Split probate reference numbers into XXX" in {
      formattedProbateReference("123") must be("123")
    }

    "Empty probate reference must return empty string" in {
      formattedProbateReference("") must be ("")
    }


  }

}
