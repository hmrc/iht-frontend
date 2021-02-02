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

package iht.controllers

import iht.FakeIhtApp

class ReverseIVUpliftFailureControllerTest extends FakeIhtApp {
  val testPrefixWithoutASlash = "test-prefix"
  val testPrefixWithASlash = "test-prefix/"
  val testJourneyId = Some("test-journey-id")
  "ReverseIVUpliftFailureController#_defaultPrefix" must {
    "produce a forward slash if the prefix lacks one" in {
      val reverseIVUpliftFailureController = new ReverseIVUpliftFailureController(testPrefixWithoutASlash)
      reverseIVUpliftFailureController._defaultPrefix mustBe "/"
    }
    "not produce a forward slash if a prefix already has one" in {
      val reverseIVUpliftFailureController = new ReverseIVUpliftFailureController(testPrefixWithASlash)
      reverseIVUpliftFailureController._defaultPrefix mustBe ""
    }
  }
  "ReverseIVUpliftFailureController#showNotAuthorisedApplication" must {
    "create a call to a url with the given journey id appended to it as a query string" in {
      val resultCall = new ReverseIVUpliftFailureController(testPrefixWithoutASlash).showNotAuthorisedApplication(testJourneyId)

      resultCall.url.split('?').last mustBe ("journeyId=" + testJourneyId.get)
    }
  }
  "ReverseIVUpliftFailureController#showNotAuthorisedRegistration" must {
    "create a call to a url with the given journey id appended to it as a query string" in {
      val resultCall = new ReverseIVUpliftFailureController(testPrefixWithoutASlash).showNotAuthorisedRegistration(testJourneyId)

      resultCall.url.split('?').last mustBe ("journeyId=" + testJourneyId.get)
    }
  }
}
