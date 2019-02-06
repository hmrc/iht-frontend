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

package iht.utils

import iht.FakeIhtApp
import iht.testhelpers.CommonBuilder
import org.scalatest.mock.MockitoSugar
import play.twirl.api.Html
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{Accounts, ConfidenceLevel, CredentialStrength}
import uk.gov.hmrc.play.frontend.auth.{AuthContext, LoggedInUser, Principal}
import uk.gov.hmrc.play.test.UnitSpec

class StringHelperTest extends FakeIhtApp with MockitoSugar {
  "parseAssignmentsToSeqTuples" must {
    "parse correctly a valid seq of 2 assignments with spaces before or after key values" in {
      val result = StringHelper.parseAssignmentsToSeqTuples(
        "aaa  =bbb,ccc=  ddd"
      )
      result mustBe Seq(
        ("aaa", "bbb"),
        ("ccc", "ddd")
      )
    }

    "parse correctly a valid seq of 1 assignment with spaces before and after key values" in {
      val result = StringHelper.parseAssignmentsToSeqTuples(
        "aaa  =   bbb"
      )
      result mustBe Seq(
        ("aaa", "bbb")
      )
    }

    "parse correctly an empty string" in {
      val result = StringHelper.parseAssignmentsToSeqTuples(
        ""
      )
      result mustBe Seq()
    }

    "throw an exception if invalid assignments are given (no equals symbols)" in {
      a[RuntimeException] mustBe thrownBy {
        StringHelper.parseAssignmentsToSeqTuples("aaa,bbb")
      }
    }

    "throw an exception if invalid assignments are given (too many equals symbols)" in {
      a[RuntimeException] mustBe thrownBy {
        StringHelper.parseAssignmentsToSeqTuples("aaa=bbb=ccc")
      }
    }
  }

  "split" must {
    "return portions and delimiters" in {
      val expectedResult = Seq(
        "one" -> Some(' '),
        "two" -> Some(' '),
        "three" -> Some('-'),
        "four" -> Some(' '),
        "five" -> Some(' '),
        "six" -> Some('-'),
        "seven" -> None
      )
      val result: Seq[(String, Option[Char])] = StringHelper.split("one two three-four five six-seven", Seq(' ', '-'))
      result mustBe expectedResult
    }

    "return portions and delimiters where ends with -" in {
      val expectedResult = Seq(
        "one" -> Some(' '),
        "two" -> Some(' '),
        "three" -> Some('-'),
        "four" -> Some(' '),
        "five" -> Some(' '),
        "six" -> Some('-'),
        "" -> None
      )
      val result: Seq[(String, Option[Char])] = StringHelper.split("one two three-four five six-", Seq(' ', '-'))
      result mustBe expectedResult
    }

    "return empty Seq where empty string" in {
      val expectedResult = Seq.empty
      val result: Seq[(String, Option[Char])] = StringHelper.split("", Seq(' ', '-'))
      result mustBe expectedResult
    }

    "return portions and delimiters where one space only" in {
      val expectedResult = Seq(
        "" -> Some(' '),
        "" -> None
      )
      val result: Seq[(String, Option[Char])] = StringHelper.split(" ", Seq(' ', '-'))
      result mustBe expectedResult
    }

    "return portions and delimiters where one space and one -" in {
      val expectedResult = Seq(
        "" -> Some(' '),
        "" -> Some('-'),
        "" -> None
      )
      val result: Seq[(String, Option[Char])] = StringHelper.split(" -", Seq(' ', '-'))
      result mustBe expectedResult
    }
  }

  "splitAndMapElements" must {
    "map elements using space and dash as delimiters" in {
      val expectedResult = "xxx two xxx-three xxx four xxx-xxx five xxx six"
      val result = StringHelper.splitAndMapElements("one two one-three one four one-one five one six", Seq(' ', '-'), x => if (x == "one") "xxx" else x)
      result mustBe expectedResult
    }
  }

  "parseOldAndNewDatesFormats" must {
    "return the string passed in if in YYYY-MM-DD format" in {
      StringHelper.parseOldAndNewDatesFormats("2000-11-13") mustBe "2000-11-13"
    }
    "return the string passed in if in YYYY-M-DD format" in {
      StringHelper.parseOldAndNewDatesFormats("2000-1-13") mustBe "2000-1-13"
    }
    "give exception in if in YYYY- -DD format" in {
      a[RuntimeException] mustBe thrownBy {
        StringHelper.parseOldAndNewDatesFormats("2000- -13")
      }
    }
    "return the string converted to new format if date is in old format DD Month YYYY" in {
      StringHelper.parseOldAndNewDatesFormats("5 April 2008") mustBe "2008-04-05"
    }
    "return the string converted to new format if date is in old format D MMM YYYY" in {
      StringHelper.parseOldAndNewDatesFormats("5 Apr 2008") mustBe "2008-04-05"
    }
    "return the string converted to new format if date is in old format DD MMM YYYY" in {
      StringHelper.parseOldAndNewDatesFormats("05 Apr 2008") mustBe "2008-04-05"
    }
  }

  "trimAndUpperCaseNino should return correctly formatted nino" in {
    val nino = CommonBuilder.DefaultNino
    val result = StringHelper.trimAndUpperCaseNino(" " + nino.toLowerCase + " ")
    result mustBe nino
  }

  "generateAcknowledgeReference should not contain a dash" in {
    val result = StringHelper.generateAcknowledgeReference
    result mustNot contain("-")
  }

  "booleanToYesNo should return Yes as a String" in {
    val result = StringHelper.booleanToYesNo(boolean = true)
    result must be("Yes")
  }

  "booleanToYesNo should return No as a String" in {
    val result = StringHelper.booleanToYesNo(boolean = false)
    result must be("No")
  }

  /*
  * Test the input status to proper status format
  * e.g.  - Input In review
  *         Output In review
  */
  "Must convert the application status in proper format" in {

    val inputStatus = "Awaiting Return"
    val result = StringHelper.formatStatus(inputStatus)

    assert(result.equals("Awaiting return"), "Reformatted status is Awaiting return")

  }

  "format status must format a status" in {
    val formattedStatus = StringHelper.formatStatus("All gOod")
    assert(formattedStatus == "All good")
  }

  "format status must replace kickout with in progress" in {
    val formattedStatus = StringHelper.formatStatus(ApplicationStatus.KickOut)
    val formattedInProgress = StringHelper.formatStatus(ApplicationStatus.InProgress)
    assert(formattedStatus == formattedInProgress)
  }

  "format status must capitalise the first letter of the first word" in {
    val formattedStatus = StringHelper.formatStatus("lower CASES")
    assert(formattedStatus == "Lower cases")
  }
}
