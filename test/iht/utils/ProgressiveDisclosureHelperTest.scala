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

package iht.utils

import common.CommonPlaySpec

class ProgressiveDisclosureHelperTest extends CommonPlaySpec {

  "The ProgressiveDisclosure Helper" should{
    "return an empty string" when{
      "given an uri that is too short for getDisclosureInfo" in{
        ProgressiveDisclosureHelper.getDisclosureInfo("") shouldBe (("", Seq()))
      }
    }

    "return the right messages key" when{
      "given a valid uri for getDisclosureInfo" in{
        ProgressiveDisclosureHelper.getDisclosureInfo("test/test/estate-report") shouldBe (("site.progressiveDisclosure.application.contact", Seq("site.progressiveDisclosure.application.help.start", "site.progressiveDisclosure.application.linkText", "site.progressiveDisclosure.application.help.end")))
        ProgressiveDisclosureHelper.getDisclosureInfo("test/test/registration") shouldBe (("site.progressiveDisclosure.registration.contact", Seq()))
        ProgressiveDisclosureHelper.getDisclosureInfo("test/test/default") shouldBe (("site.progressiveDisclosure.preRegistration.contact", Seq("site.progressiveDisclosure.preRegistration.help")))
      }
    }

    "return the right boolean" when{
      "given a location uri that is too short" in{
        ProgressiveDisclosureHelper.checkLocationLength("test") shouldBe false
      }

      "given a location uri that is greater than 2" in{
        ProgressiveDisclosureHelper.checkLocationLength("test/test/example") shouldBe true
      }

      "given a location uri that is not an error page" in{
        ProgressiveDisclosureHelper.checkIfError("test") shouldBe false
      }

      "given a location uri that is an error page" in{
        ProgressiveDisclosureHelper.checkIfError("/registration/identity-verification-problem") shouldBe true
        ProgressiveDisclosureHelper.checkIfError("/estatereport/identity-verification-problem") shouldBe true
        ProgressiveDisclosureHelper.checkIfError("test/test/timeout") shouldBe true
      }
    }
  }
}