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
import iht.config.AppConfig
import iht.testhelpers.CommonBuilder
import org.scalatest.mock.MockitoSugar

class StringHelperTest extends FakeIhtApp with MockitoSugar with StringHelper {
  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

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
      val result: Seq[(String, Option[Char])] = split("one two three-four five six-seven", Seq(' ', '-'))
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
      val result: Seq[(String, Option[Char])] = split("one two three-four five six-", Seq(' ', '-'))
      result mustBe expectedResult
    }

    "return empty Seq where empty string" in {
      val expectedResult = Seq.empty
      val result: Seq[(String, Option[Char])] = split("", Seq(' ', '-'))
      result mustBe expectedResult
    }

    "return portions and delimiters where one space only" in {
      val expectedResult = Seq(
        "" -> Some(' '),
        "" -> None
      )
      val result: Seq[(String, Option[Char])] = split(" ", Seq(' ', '-'))
      result mustBe expectedResult
    }

    "return portions and delimiters where one space and one -" in {
      val expectedResult = Seq(
        "" -> Some(' '),
        "" -> Some('-'),
        "" -> None
      )
      val result: Seq[(String, Option[Char])] = split(" -", Seq(' ', '-'))
      result mustBe expectedResult
    }
  }

  "splitAndMapElements" must {
    "map elements using space and dash as delimiters" in {
      val expectedResult = "xxx two xxx-three xxx four xxx-xxx five xxx six"
      val result = splitAndMapElements("one two one-three one four one-one five one six", Seq(' ', '-'), x => if (x == "one") "xxx" else x)
      result mustBe expectedResult
    }
  }

  "parseOldAndNewDatesFormats" must {
    "return the string passed in if in YYYY-MM-DD format" in {
      parseOldAndNewDatesFormats("2000-11-13") mustBe "2000-11-13"
    }
    "return the string passed in if in YYYY-M-DD format" in {
      parseOldAndNewDatesFormats("2000-1-13") mustBe "2000-1-13"
    }
    "give exception in if in YYYY- -DD format" in {
      a[RuntimeException] mustBe thrownBy {
        parseOldAndNewDatesFormats("2000- -13")
      }
    }
    "return the string converted to new format if date is in old format DD Month YYYY" in {
      parseOldAndNewDatesFormats("5 April 2008") mustBe "2008-04-05"
    }
    "return the string converted to new format if date is in old format D MMM YYYY" in {
      parseOldAndNewDatesFormats("5 Apr 2008") mustBe "2008-04-05"
    }
    "return the string converted to new format if date is in old format DD MMM YYYY" in {
      parseOldAndNewDatesFormats("05 Apr 2008") mustBe "2008-04-05"
    }
  }

  "trimAndUpperCaseNino should return correctly formatted nino" in {
    val nino = CommonBuilder.DefaultNino
    val result = trimAndUpperCaseNino(" " + nino.toLowerCase + " ")
    result mustBe nino
  }

  "generateAcknowledgeReference should not contain a dash" in {
    val result = generateAcknowledgeReference
    result mustNot contain("-")
  }

  "booleanToYesNo should return Yes as a String" in {
    val result = booleanToYesNo(boolean = true)
    result must be("Yes")
  }

  "booleanToYesNo should return No as a String" in {
    val result = booleanToYesNo(boolean = false)
    result must be("No")
  }

  /*
  * Test the input status to proper status format
  * e.g.  - Input In review
  *         Output In review
  */
  "Must convert the application status in proper format" in {

    val inputStatus = "Awaiting Return"
    val result = formatStatus(inputStatus)

    assert(result.equals("Awaiting return"), "Reformatted status is Awaiting return")

  }

  "format status must format a status" in {
    val formattedStatus = formatStatus("All gOod")
    assert(formattedStatus == "All good")
  }

  "format status must replace kickout with in progress" in {
    val formattedStatus = formatStatus(ApplicationStatus.KickOut)
    val formattedInProgress = formatStatus(ApplicationStatus.InProgress)
    assert(formattedStatus == formattedInProgress)
  }

  "format status must capitalise the first letter of the first word" in {
    val formattedStatus = formatStatus("lower CASES")
    assert(formattedStatus == "Lower cases")
  }
}
