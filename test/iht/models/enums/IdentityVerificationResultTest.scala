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

package iht.models.enums

import iht.FakeIhtApp
import iht.models.enums.IdentityVerificationResult.IdentityVerificationResult
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec
import play.api.libs.json._

class IdentityVerificationResultTest extends FakeIhtApp with MockitoSugar {
  val failedMatchingJson = Json.parse(""""FailedMatching"""")
  "IdentityVerificationResult" must {
    "read in json values correctly" in {
        val result: JsResult[IdentityVerificationResult] = failedMatchingJson.validate[IdentityVerificationResult]
      result mustBe JsSuccess[IdentityVerificationResult](IdentityVerificationResult.FailedMatching)
    }
    "write out json values correctly" in {
      val jsValue: JsValue = Json.toJson(IdentityVerificationResult.FailedMatching)
      jsValue mustBe failedMatchingJson
    }
  }
}
