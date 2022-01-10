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

package iht.controllers

import iht.FakeIhtApp

class ReverseIVWayfinderControllerTest extends FakeIhtApp {
  val testPrefixWithoutASlash = "test-prefix"
  val testPrefixWithASlash = "test-prefix/"
  val testJourneyId = Some("test-journey-id")
  "ReverseIVWayfinderController#_defaultPrefix" must {
    "produce a forward slash if the prefix lacks one" in {

      val reverseIVWayfinderController = new ReverseIVWayfinderController(testPrefixWithoutASlash)

      reverseIVWayfinderController._defaultPrefix mustBe "/"
    }
    "not produce a forward slash if a prefix already has one" in {

      val reverseIVWayfinderController = new ReverseIVWayfinderController(testPrefixWithASlash)

      reverseIVWayfinderController._defaultPrefix mustBe ""
    }
  }
  "ReverseIVWayfinderController#verificationPass" must {

    "generate a call with a url that starts with the given prefix and ends with 'registration/verification-pass'" in {

      val resultCall = new ReverseIVWayfinderController(testPrefixWithASlash).verificationPass()

      resultCall.url mustBe testPrefixWithASlash + "registration/verification-pass"

    }
  }

  "ReverseIVWayfinderController#loginPass" must {

    "generate a call with a url that starts with the given prefix and ends with 'registration/login-pass'" in {

      val resultCall = new ReverseIVWayfinderController(testPrefixWithASlash).loginPass()

      resultCall.url mustBe testPrefixWithASlash + "registration/login-pass"

    }
  }
}
