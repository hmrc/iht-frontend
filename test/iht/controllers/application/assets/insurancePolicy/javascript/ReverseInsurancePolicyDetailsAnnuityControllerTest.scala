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

package iht.controllers.application.assets.insurancePolicy.javascript

import iht.FakeIhtApp
import play.api.routing.JavaScriptReverseRoute

class ReverseInsurancePolicyDetailsAnnuityControllerTest extends FakeIhtApp {
  val testPrefixWithASlash = "test-prefix/"
  val testPrefixWithoutASlash = "test-prefix"
  "ReverseInsurancePolicyDetailsAnnuityController#_defaultPrefix" must {
    "add a forward slash to a prefix if it lacks one" in {
      val reverseInsurancePolicyDetailsAnnuityController =
        new ReverseInsurancePolicyDetailsAnnuityController(testPrefixWithoutASlash)

      reverseInsurancePolicyDetailsAnnuityController._defaultPrefix mustBe "/"

    }
  }

  "ReverseInsurancePolicyDetailsAnnuityController#_defaultPrefix" must {
    "not add a forward slash to a prefix that already has one" in {
      val reverseInsurancePolicyDetailsAnnuityController =
        new ReverseInsurancePolicyDetailsAnnuityController(testPrefixWithASlash)

      reverseInsurancePolicyDetailsAnnuityController._defaultPrefix mustBe ""

    }
  }

  val reverseInsurancePolicyDetailsAnnuityController = new ReverseInsurancePolicyDetailsAnnuityController(testPrefixWithASlash)

  "ReverseInsurancePolicyDetailsAnnuityController#onSubmit" must {

    "generate a javascript reverse route" in {

      assert(reverseInsurancePolicyDetailsAnnuityController.onSubmit.isInstanceOf[JavaScriptReverseRoute])

    }

  }

  "ReverseInsurancePolicyDetailsAnnuityController#onPageLoad" must {

    "generate a javascript reverse route" in {

      assert(reverseInsurancePolicyDetailsAnnuityController.onPageLoad.isInstanceOf[JavaScriptReverseRoute])

    }

  }
}
