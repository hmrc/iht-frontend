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

import java.net.URI

import iht.FakeIhtApp
import org.apache.xmlgraphics.io.Resource
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment

/**
  * Created by david-beer on 27/10/16.
  */
class FopURIResolverTest extends FakeIhtApp with MockitoSugar {

  class Setup {
    val fopURIResolver = new FopURIResolver {
      override val environment: Environment = app.environment
    }
  }

  "Must return a valid Resource" in new Setup {
    val inputResource = new URI("/pdf/templates/clearance/main.xsl")
    val result = fopURIResolver.getResource(inputResource)

    result mustBe a[Resource]
  }

  "Must throw an exception if Resource is not valid" in new Setup {
    val inputResource = new URI("/invalid-resource")
    a[RuntimeException] shouldBe thrownBy {
      fopURIResolver.getResource(inputResource)
    }
  }
}
