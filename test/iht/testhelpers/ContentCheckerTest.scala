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

import iht.FakeIhtApp
import play.api.Play
import uk.gov.hmrc.play.test.UnitSpec
import iht.utils.CommonHelper
import scala.io.Source._

class ContentCheckerTest extends FakeIhtApp {

  def getResourceAsFilePath(filePath: String) = {
    val url = CommonHelper.getOrException(app.environment.resource(filePath),
      "Unable to find Play resource in class path: " + filePath)
    url.getFile
  }

  "findMessageKeys" must {
    "find no dotted strings in content if there are no dotted strings in the content" in {
      val content = "the quick brown fox jumped over the lazy dog"

      ContentChecker.findMessageKeys(content) mustBe Nil
    }

    "find a dotted strings in content if there is dotted strings in the content" in {
      val content = "the.quick brown fox jumped over.the.lazy.dog so there."

      ContentChecker.findMessageKeys(content) mustBe Seq("the.quick", "over.the.lazy.dog")
    }

    "find a dotted strings in content if there is dotted strings in the content excluding GOV.UK elements" in {
      val content = "the.quick brown fox GOV.UK over.the.lazy.dog so there."

      ContentChecker.findMessageKeys(content) mustBe Seq("the.quick", "over.the.lazy.dog")
    }

    "find a dotted strings in content if there is dotted strings in the content including elements that include but are " +
      "not GOV.UK" in {
      val content = "the.quick brown fox a.GOV.UK.b over.the.lazy.dog so there."

      ContentChecker.findMessageKeys(content) mustBe Seq("the.quick", "a.GOV.UK.b", "over.the.lazy.dog")
    }

    "find dotted strings in content excluding monetary values" in {
      val content = "the.quick brown fox a.GOV.UK.b over.the.lazy.dog £12.00 there."

      ContentChecker.findMessageKeys(content) mustBe Seq("the.quick", "a.GOV.UK.b", "over.the.lazy.dog")
    }

    "find dotted strings excluding emails" in {
      val content = "the.quick brown fox a.GOV.UK.b over.the.lazy.dog £12.00 there. some.one@example.com " +
        "someone.else@example.com this.that.theOther some.one@example.com"

      ContentChecker.findMessageKeys(content) mustBe Seq("the.quick", "a.GOV.UK.b", "over.the.lazy.dog", "this.that.theOther")
    }

    "find dotted strings excluding forward slashes" in {
      val content = "//the.quick brown fox a.GOV.UK.b over.the.lazy.dog £12.00 there. /someone/example.com " +
        "someone.else@example.com this.that.theOther some.one@example.com"

      ContentChecker.findMessageKeys(content) mustBe Seq("a.GOV.UK.b", "over.the.lazy.dog", "this.that.theOther")
    }

    "stripLineBreaks should return string without line breaks" ignore {
      val content = fromFile(getResourceAsFilePath("formatted_string")).mkString
      ContentChecker.stripLineBreaks(content) must include("Line oneLine two")
    }
  }
}
