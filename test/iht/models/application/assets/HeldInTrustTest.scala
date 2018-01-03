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

package iht.models.application.assets

import iht.testhelpers.CommonBuilder
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by vineet on 03/11/16.
  */
class HeldInTrustTest extends UnitSpec with MockitoSugar{

  "isComplete" must {

    "returns Some(true) if HeldInTrust is complete" in {
      val heldInTrust = CommonBuilder.buildAssetsHeldInTrust.copy(isOwned = Some(false),
        isMoreThanOne = None, value = None)
      heldInTrust.isComplete shouldBe Some(true)
    }

    "returns Some(false) if HeldInTrust is not complete" in {
      val heldInTrust = CommonBuilder.buildAssetsHeldInTrust.copy(isOwned = None,
        isMoreThanOne = Some(false), value = Some(BigDecimal(10000)))
      heldInTrust.isComplete shouldBe Some(false)
    }

    "returns None if every field is None in HeldInTrust" in {
      val heldInTrust = CommonBuilder.buildAssetsHeldInTrust
      heldInTrust.isComplete shouldBe empty
    }
  }
}
