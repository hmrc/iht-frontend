/*
 * Copyright 2022 HM Revenue & Customs
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

package com.kenshoo.play.metrics.javascript

import iht.FakeIhtApp

class ReverseMetricsControllerTest extends FakeIhtApp {
  "ReverseMetricsController#_defaultPrefix" must {
    "produce a forward slash if the prefix lacks one" in {
      val testPrefix = "test-prefix"
      val reverseMetricsController = new ReverseMetricsController(_prefix = testPrefix)
      reverseMetricsController._defaultPrefix mustEqual "/"
    }

    "not produce a forward slash if the prefix already has one" in {
      val testPrefixWithASlash = "test-prefix/"
      val reverseMetricsController = new ReverseMetricsController(_prefix = testPrefixWithASlash)
      reverseMetricsController._defaultPrefix mustEqual ""
    }
  }
}
