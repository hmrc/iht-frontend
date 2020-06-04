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

package iht.config

import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Environment
import uk.gov.hmrc.play.test.UnitSpec

class IhtPropertiesReaderTest extends UnitSpec with GuiceOneAppPerTest with MockitoSugar {
  lazy val ihtPropertyRetriever: IhtPropertyRetriever = new IhtPropertyRetriever {
    override lazy val environment: Environment = app.environment
  }

  "IhtPropertiesReaderTest" must {
    "should read the key and return appropriate value" in {
      val maxExecutors = ihtPropertyRetriever.getPropertyAsInt("maxCoExecutors")
      val ukIsoCountryCode = ihtPropertyRetriever.getProperty("ukIsoCountryCode")
      val govUkLink = ihtPropertyRetriever.getProperty("linkGovUkIht")

      assert(maxExecutors == 3,"Maximum executors value is 3")
      ukIsoCountryCode shouldBe "GB"
      assert(govUkLink=="https://www.gov.uk/valuing-estate-of-someone-who-died" , "Link value is https://www.gov.uk/valuing-estate-of-someone-who-died")
    }
  }

  "parseAssignmentsToSeqTuples" must {
    "parse correctly a valid seq of 2 assignments with spaces before or after key values" in {
      val result = ihtPropertyRetriever.parseAssignmentsToSeqTuples(
        "aaa  =bbb,ccc=  ddd"
      )
      result shouldBe Seq(
        ("aaa", "bbb"),
        ("ccc", "ddd")
      )
    }

    "parse correctly a valid seq of 1 assignment with spaces before and after key values" in {
      val result = ihtPropertyRetriever.parseAssignmentsToSeqTuples(
        "aaa  =   bbb"
      )
      result shouldBe Seq(
        ("aaa", "bbb")
      )
    }

    "parse correctly an empty string" in {
      val result = ihtPropertyRetriever.parseAssignmentsToSeqTuples(
        ""
      )
      result shouldBe Seq()
    }

    "throw an exception if invalid assignments are given (no equals symbols)" in {
      a[RuntimeException] mustBe thrownBy {
        ihtPropertyRetriever.parseAssignmentsToSeqTuples("aaa,bbb")
      }
    }

    "throw an exception if invalid assignments are given (too many equals symbols)" in {
      a[RuntimeException] mustBe thrownBy {
        ihtPropertyRetriever.parseAssignmentsToSeqTuples("aaa=bbb=ccc")
      }
    }
  }
}
