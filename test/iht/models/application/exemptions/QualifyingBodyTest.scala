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

package iht.models.application.exemptions

import iht.testhelpers.CommonBuilder
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec


class QualifyingBodyTest extends UnitSpec with MockitoSugar{

  "isComplete" must {

    "return true when QualifyingBody is complete" in {
      val qualifyingBody = CommonBuilder.buildQualifyingBody.copy(id = Some("1"),
        name = Some("test"),
        totalValue = Some(BigDecimal(1000)))

      qualifyingBody.isComplete shouldBe true
    }

    "return false when all but one of fields is None" in {
      val qualifyingBody = CommonBuilder.buildQualifyingBody.copy(
        id = Some("1"),
        name = Some("test"),
        totalValue = None)

      qualifyingBody.isComplete shouldBe false
    }

    "return false when all the fields are None" in {
        CommonBuilder.buildQualifyingBody.isComplete shouldBe false
    }
  }
}
