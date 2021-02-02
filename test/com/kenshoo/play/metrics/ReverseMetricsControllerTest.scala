/*
 * Copyright 2021 HM Revenue & Customs
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

package com.kenshoo.play.metrics

import iht.FakeIhtApp
import play.api.mvc.Call

class ReverseMetricsControllerTest extends FakeIhtApp {

  val testPrefixWithoutASlash = "test-prefix"
  val testPrefixWithASlash = "test-prefix/"
  "ReverseMetricsController#_defaultPrefix" must {
    "add a forward slash to a prefix if it lacks one" in {
      val reverseMetricsController = new ReverseMetricsController(_prefix = testPrefixWithoutASlash)
      reverseMetricsController._defaultPrefix mustBe "/"
    }

    "not add a forward slash to a prefix that already has one" in {
      val reverseMetricsController = new ReverseMetricsController(_prefix = testPrefixWithASlash)
      reverseMetricsController._defaultPrefix mustBe ""

    }
  }

  "ReverseMetricsController#metrics" must {
    "generate a call" in {
      val reverseMetricsController = new ReverseMetricsController(_prefix = testPrefixWithASlash)
      assert(reverseMetricsController.metrics().isInstanceOf[Call])
    }
  }

}
