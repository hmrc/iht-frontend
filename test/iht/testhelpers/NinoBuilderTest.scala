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

package iht.testhelpers

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by yasar on 24/10/16.
  */
class NinoBuilderTest extends UnitSpec{

  "randomNino" must{
    "create a valid nino" in{
      assert(Nino.isValid(NinoBuilder.randomNino.toString()))
    }
  }

  "addSpacesToNino" must{
    "add a space after the second character in nino" in{
      assert(NinoBuilder.addSpacesToNino(NinoBuilder.randomNino.nino).charAt(2) == ' ')
    }
  }
}
