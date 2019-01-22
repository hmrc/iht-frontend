/*
 * Copyright 2019 HM Revenue & Customs
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

package iht.utils.pdf

import javax.xml.transform.stream.StreamSource

import iht.FakeIhtApp
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by david-beer on 28/10/16.
  */
class StylesheetResolverTest extends FakeIhtApp with MockitoSugar {

  "Must return a valid StreamSource" in {
    val inputResource = "/pdf/templates/postsubmission/iht-return.xsl"
    val result = new StylesheetResolver().resolve(inputResource, "")

    result mustBe a[StreamSource]
    result mustNot equal(null)
  }

  "Must throw exception if Resource is not valid" in {
    val inputResource = "/invalid-resource"
    a[RuntimeException] shouldBe thrownBy {
      new StylesheetResolver().resolve(inputResource, "")
    }
  }
}
