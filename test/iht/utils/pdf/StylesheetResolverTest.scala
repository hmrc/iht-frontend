/*
 * Copyright 2017 HM Revenue & Customs
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

import java.net.URI
import javax.xml.transform.stream.StreamSource

import iht.FakeIhtApp
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by david-beer on 28/10/16.
  */
class StylesheetResolverTest extends UnitSpec with FakeIhtApp with MockitoSugar{

  "Must return a valid StreamSource" in {
    val inputResource = "/pdf/templates/application/return/iht-return.xsl"
    val result = new StylesheetResolver().resolve(inputResource, "")

    result shouldBe a[StreamSource]
    result shouldNot equal(null)
  }

  "Must return null if Resource is not valid" in {
    val inputResource = "/invalid-resource"
    val result = new StylesheetResolver().resolve(inputResource, "")

    result shouldEqual null
  }
}
