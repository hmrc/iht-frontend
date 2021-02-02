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

package iht.utils.pdf

import iht.FakeIhtApp
import javax.xml.transform.stream.StreamSource
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment

/**
  * Created by david-beer on 28/10/16.
  */
class StylesheetResolverTest extends FakeIhtApp with MockitoSugar {

  class Setup {
    val resourceStreamResolver: BaseResourceStreamResolver = new BaseResourceStreamResolver {
      override val environment: Environment = app.environment
    }
  }

  "Must return a valid StreamSource" in new Setup {
    val inputResource = "/pdf/templates/postsubmission/iht-return.xsl"
    val result = resourceStreamResolver.resolvePath(inputResource)

    result mustBe a[StreamSource]
    result mustNot equal(null)
  }

  "Must throw exception if Resource is not valid" in new Setup {
    val inputResource = "/invalid-resource"
    a[RuntimeException] shouldBe thrownBy {
      resourceStreamResolver.resolvePath(inputResource)
    }
  }
}
