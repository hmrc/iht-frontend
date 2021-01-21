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

package iht.controllers.application.assets.household.javascript

import iht.FakeIhtApp
import play.api.routing.JavaScriptReverseRoute

class ReverseHouseholdJointlyOwnedControllerTest extends FakeIhtApp {
  val testPrefixWithASlash = "test-prefix/"
  val testPrefixWithoutASlash = "test-prefix"
  "ReverseHouseholdJointlyOwnedController#_defaultPrefix" must {
    "add a forward slash to a prefix if it lacks one" in {

      val reverseHouseholdJointlyOwnedController = new ReverseHouseholdJointlyOwnedController(testPrefixWithoutASlash)

      reverseHouseholdJointlyOwnedController._defaultPrefix mustBe "/"

    }
    "not add a forward slash to a prefix that already has one" in {

      val reverseHouseholdJointlyOwnedController = new ReverseHouseholdJointlyOwnedController(testPrefixWithASlash)

      reverseHouseholdJointlyOwnedController._defaultPrefix mustBe ""

    }
  }
  val reverseHouseholdJointlyOwnedController = new ReverseHouseholdJointlyOwnedController(testPrefixWithASlash)

  "ReverseHouseholdJointlyOwnedController#onSubmit" must {
    "generate a javascript reverse route" in {

      assert(reverseHouseholdJointlyOwnedController.onSubmit.isInstanceOf[JavaScriptReverseRoute])

    }
  }
  "ReverseHouseholdJointlyOwnedController#onPageLoad" must {
    "generate a javascript reverse route" in {

      assert(reverseHouseholdJointlyOwnedController.onPageLoad.isInstanceOf[JavaScriptReverseRoute])

    }
  }
}
