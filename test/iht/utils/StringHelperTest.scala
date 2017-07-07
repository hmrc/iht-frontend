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

package iht.utils

import iht.FakeIhtApp
import iht.testhelpers.CommonBuilder
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

class StringHelperTest extends UnitSpec with FakeIhtApp with MockitoSugar {
  "parseAssignmentsToSeqTuples" must {
    "parse correctly a valid seq of 2 assignments with spaces before or after key values" in {
      val result = StringHelper.parseAssignmentsToSeqTuples(
        "aaa  =bbb,ccc=  ddd"
      )
      result shouldBe Seq(
        ("aaa", "bbb"),
        ("ccc", "ddd")
      )
    }

    "parse correctly a valid seq of 1 assignment with spaces before and after key values" in {
      val result = StringHelper.parseAssignmentsToSeqTuples(
        "aaa  =   bbb"
      )
      result shouldBe Seq(
        ("aaa", "bbb")
      )
    }

    "parse correctly an empty string" in {
      val result = StringHelper.parseAssignmentsToSeqTuples(
        ""
      )
      result shouldBe Seq()
    }

    "throw an exception if invalid assignments are given (no equals symbols)" in {
      a[RuntimeException] shouldBe thrownBy {
        StringHelper.parseAssignmentsToSeqTuples("aaa,bbb")
      }
    }

    "throw an exception if invalid assignments are given (too many equals symbols)" in {
      a[RuntimeException] shouldBe thrownBy {
        StringHelper.parseAssignmentsToSeqTuples("aaa=bbb=ccc")
      }
    }
  }

  "parseOldAndNewDatesFormats" must{
    "return the string passed in if in YYYY-MM-DD format" in {
      StringHelper.parseOldAndNewDatesFormats("2000-11-13") shouldBe "2000-11-13"
    }
    "return the string passed in if in YYYY-M-DD format" in {
      StringHelper.parseOldAndNewDatesFormats("2000-1-13") shouldBe "2000-1-13"
    }
    "give exception in if in YYYY- -DD format" in {
      a [RuntimeException] shouldBe thrownBy {
        StringHelper.parseOldAndNewDatesFormats("2000- -13")
      }
    }
    "return the string converted to new format if date is in old format DD Month YYYY" in {
        StringHelper.parseOldAndNewDatesFormats("5 April 2008") shouldBe "2008-04-05"
    }
    "return the string converted to new format if date is in old format D MMM YYYY" in {
      StringHelper.parseOldAndNewDatesFormats("5 Apr 2008") shouldBe "2008-04-05"
    }
    "return the string converted to new format if date is in old format DD MMM YYYY" in {
      StringHelper.parseOldAndNewDatesFormats("05 Apr 2008") shouldBe "2008-04-05"
    }
  }


  "trimAndUpperCaseNino should return correctly formatted nino" in {
    val nino = CommonBuilder.DefaultNino
    val result = StringHelper.trimAndUpperCaseNino(" " + nino.toLowerCase + " ")
    result shouldBe nino
  }


  "generateAcknowledgeReference should not contain a dash" in {
    val result = StringHelper.generateAcknowledgeReference
    result shouldNot contain("-")
  }
}
