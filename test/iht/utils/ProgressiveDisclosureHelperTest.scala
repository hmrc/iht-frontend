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

package iht.utils

import uk.gov.hmrc.play.test.UnitSpec

class ProgressiveDisclosureHelperTest extends UnitSpec {

  "The ProgressiveDisclosure Helper" should{
    "return an empty string" when{
      "given an uri that is too short for getContactInfo" in{
        ProgressiveDisclosureHelper.getContactInfo("") shouldBe ""
      }

      "given an uri that is too short for getHelpInfo" in{
        ProgressiveDisclosureHelper.getHelpInfo("") shouldBe Seq()
      }
    }

    "return the right messages key" when{
      "given a valid uri for getContactInfo" in{
        ProgressiveDisclosureHelper.getContactInfo("test/test/estate-report") shouldBe "site.progressiveDisclosure.application.contact"
        ProgressiveDisclosureHelper.getContactInfo("test/test/registration") shouldBe "site.progressiveDisclosure.registration.contact"
        ProgressiveDisclosureHelper.getContactInfo("test/test/default") shouldBe "site.progressiveDisclosure.preRegistration.contact"
      }

      "given a valid uri for getHelpInfo" in{
        ProgressiveDisclosureHelper.getHelpInfo("test/test/estate-report") shouldBe Seq("site.progressiveDisclosure.application.help", "site.progressiveDisclosure.application.linkText")
        ProgressiveDisclosureHelper.getHelpInfo("test/test/registration") shouldBe Seq()
        ProgressiveDisclosureHelper.getHelpInfo("test/test/default") shouldBe Seq("site.progressiveDisclosure.preRegistration.help")
      }
    }

    "return the right boolean" when{
      "given a location that is too short" in{
        ProgressiveDisclosureHelper.checkLocationLength("test") shouldBe false
      }

      "given a location uri that is greater than 2" in{
        ProgressiveDisclosureHelper.checkLocationLength("test/test/example") shouldBe true
      }
    }
  }
}
