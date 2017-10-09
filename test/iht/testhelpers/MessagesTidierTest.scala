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

package iht.testhelpers

import java.io.File

import iht.FakeIhtApp
import iht.utils.CommonHelper
import play.api.Play
import play.api.libs.Files.TemporaryFile
import uk.gov.hmrc.play.test.UnitSpec

class MessagesTidierTest extends UnitSpec with FakeIhtApp {

  val messagesFile = "/messages"
  val mockedMessagesFileWithoutDuplicateKeys = "/messages_without_duplicates"
  val mockedMessagesFileWithDuplicateKeys = "/messages_with_duplicates"

  val mockedMessagesFileWithoutDuplicateKeysAsSeqOfTuples = Seq(
    ("a.b.c", "one"),
    ("d.e.f", "two"),
    ("g.h.i", "one"),
    ("i.a.s", "ool"),
    ("a.x.v", "one"),
    ("u.w.q", "two")
  )
  val mockedMessagesFileWithDuplicateKeysAsMap = Map("a.b.c" -> Set("one", "three"), "i.a.s" -> Set("ool", "wibble"))

  val mockedMessagesWithDuplicateValuesAsMap =
    Map("a.b.c" -> "one",
      "d.e.f" -> "two",
      "g.h.i" -> "one",
      "i.a.s" -> "ool",
      "a.x.v" -> "one",
      "u.w.q" -> "two")
  val mockedMessagesWithDuplicateValuesAsInvertedMap =
    Map("one" -> Set("a.b.c", "g.h.i", "a.x.v"),
      "two" -> Set("d.e.f", "u.w.q"),
      "ool" -> Set("i.a.s"))

  val mockedMessagesWithDuplicateValuesAsInvertedMapWithSummaryItems =
    Map("one" -> Set("a.b.c", "g.h.i", "a.x.v", "g.g.g.summary"),
      "two" -> Set("d.e.f", "aaa.summary", "u.w.q"),
      "ool" -> Set("i.a.s"))

  val mockedMessagesWithDuplicateValuesAsInvertedMapUniquesRemoved =
    Map("one" -> Set("a.b.c", "g.h.i", "a.x.v"),
      "two" -> Set("d.e.f", "u.w.q"))

  val mockedMessagesWithDuplicateKeysAsSeqOfTuples = Seq(("aaa", "bbb"), ("ccc", "ddd"), ("aaa", "andy"))
  val mockedMessagesWithDuplicateKeysAsMapOfSets = Map("aaa" -> Set("bbb", "andy"), "ccc" -> Set("ddd"))
  val mockedMessagesWithoutDuplicateKeysAsMapOfSets = Map("aaa" -> Set("bbb"), "ccc" -> Set("ddd"))

  val mockedMessagesTidier = new MessagesTidier {
    override val messages: Map[String, Map[String, String]] = Map("en" -> mockedMessagesWithDuplicateValuesAsMap)

    val mockedEnglishMessages = Map(
      "a.b.c" -> "one",
      "d.e.f" -> "two",
      "g.h.i" -> "one",
      "i.a.s" -> "ool",
      "a.x.v" -> "one",
      "u.w.q" -> "two"
    )

    val mockedEnglishMessagesForFailure = Map(
      "a.b.c" -> "one",
      "d.e.f" -> "two",
      "g.h.i" -> "one",
      "i.a.s" -> "ool",
      "a.x.v" -> "one"
    )

    val mockedWelshMessages = Map(
      "a.b.c" -> "ffl",
      "d.e.f" -> "llf",
      "g.h.i" -> "flfl",
      "i.a.s" -> "ylfl",
      "a.x.v" -> "yfl'f",
      "u.w.q" -> "flff'f"
    )

    val mockedWelshMessagesForFailure = Map(
      "a.b.c" -> "ffl",
      "d.e.f" -> "llf",
      "g.h.i" -> "flfl",
      "i.a.s" -> "ylfl",
      "u.w.q" -> "flff'f"
    )

    override def compareMessageFileKeys(): Set[String] = (mockedEnglishMessages.keySet -- mockedWelshMessages.keySet) ++
        (mockedWelshMessages.keySet -- mockedEnglishMessages.keySet)

    def compareMessageFileKeysWelshFailure: Set[String] = (mockedEnglishMessages.keySet -- mockedWelshMessagesForFailure.keySet) ++
        (mockedWelshMessagesForFailure.keySet -- mockedEnglishMessages.keySet)

    def compareMessageFileKeysEnglishFailure: Set[String] = (mockedEnglishMessagesForFailure.keySet -- mockedWelshMessages.keySet) ++
        (mockedWelshMessages.keySet -- mockedEnglishMessagesForFailure.keySet)
  }

  "Using mocked messages data" must {

    "readTuples" must {
      "generate correct tuples from mocked file" in {
        val result = mockedMessagesTidier.readTuples(mockedMessagesFileWithoutDuplicateKeys)
        result shouldBe mockedMessagesFileWithoutDuplicateKeysAsSeqOfTuples
      }
    }

    "parseLine" must {
      "parse a line correctly" in {
        val result: Option[(String, String)] = mockedMessagesTidier.parseLine("aaaa=bbbb")
        result shouldBe Some(("aaaa", "bbbb"))
      }

      "parse a line correctly where spaces before and after equals" in {
        val result: Option[(String, String)] = mockedMessagesTidier.parseLine("aaaa = bbbb")
        result shouldBe Some(("aaaa", "bbbb"))
      }

      "parse a line correctly where not in a=b format" in {
        val result: Option[(String, String)] = mockedMessagesTidier.parseLine("#waaaaa")
        result shouldBe None
      }

      "parse a blank line correctly" in {
        val result: Option[(String, String)] = mockedMessagesTidier.parseLine("\n")
        result shouldBe None
      }
    }

    "createConsolidatedMap" must {
      "create a map where key is first string of a tuple and value is a set of values" in {
        val tuples = Seq(("aaa", "bbb"), ("ccc", "ddd"))
        mockedMessagesTidier.createConsolidatedMap(tuples) shouldBe Map("aaa" -> Set("bbb"), "ccc" -> Set("ddd"))
      }

      "create a map where key is first string of a tuple and value is a set of duplicate values" in {
        mockedMessagesTidier
          .createConsolidatedMap(mockedMessagesWithDuplicateKeysAsSeqOfTuples) shouldBe mockedMessagesWithDuplicateKeysAsMapOfSets
      }
    }

    "hasDuplicatedKeys" must {
      "return true if there are duplicate keys" in {
        mockedMessagesTidier.hasDuplicatedKeys(mockedMessagesWithDuplicateKeysAsMapOfSets) shouldBe true
      }

      "return false if there are no duplicate keys" in {
        mockedMessagesTidier.hasDuplicatedKeys(mockedMessagesWithoutDuplicateKeysAsMapOfSets) shouldBe false
      }
    }

    "readMessageFile" must {
      "read a file of key=message lines and produce a Right(map) if there are no duplicated keys" in {
        mockedMessagesTidier.readMessageFile(mockedMessagesFileWithoutDuplicateKeys) shouldBe Right(mockedMessagesWithDuplicateValuesAsMap)
      }

      "readMessageFile" must {
        "read a file of key=message lines and produce a Left(map) if there are duplicated keys" in {
          mockedMessagesTidier.readMessageFile(mockedMessagesFileWithDuplicateKeys) shouldBe Left(mockedMessagesFileWithDuplicateKeysAsMap)
        }
      }
    }

    "prettyPrint" must {
      "return empty string for empty set" in {
        mockedMessagesTidier.prettyPrintSetOfStrings(Set()) shouldBe ""
      }

      "return all items in set with commas in between" in {
        mockedMessagesTidier.prettyPrintSetOfStrings(Set("one", "two", "three")) shouldBe "\"one\",\"two\",\"three\""
      }
    }
  }

  "Using real messages file" must {
    "readMessageFile" must {
      "not contain any duplicate keys" in {
        val result = MessagesTidier.readMessageFile(messagesFile)
        if (result.isRight) {
          assert(true)
        } else {
          val duplicates: Map[String, Set[String]] = result.left.getOrElse(throw new RuntimeException("Problem with left"))
          fail("Messages file contains duplicate keys: " + MessagesTidier.prettyPrintMapOfDuplicateKeys(duplicates))
        }
      }
    }

    "compareMessageFileKeys" must {

      "not fail when mocked english and welsh message files have the same keys" in {
        val diff: Set[String] = mockedMessagesTidier.compareMessageFileKeys()
        assert(diff == Set.empty)
      }

      "fail when mocked english and welsh message files do not have the same keys" in {
        val diff: Set[String] = mockedMessagesTidier.compareMessageFileKeysWelshFailure
        val diff2 = mockedMessagesTidier.compareMessageFileKeysEnglishFailure
        assert(diff != Set.empty && diff2 != Set.empty)
      }

      "not fail with real messages files" in {
        val result = MessagesTidier.compareMessageFileKeys()
        assert(result.isEmpty, "\n \n There are message keys missing from messages.en and/or messages.cy - " +
          "see the file /home/" + System.getProperty("user.name") + "/missingKeysAndValues.csv for more info")
      }

    }

  }
}
