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

import java.io.{BufferedWriter, File, FileWriter}

import iht.FakeIhtApp
import iht.utils.CommonHelper
import play.api.Play
import play.api.libs.Files.TemporaryFile
import uk.gov.hmrc.play.test.UnitSpec

class MessagesTidierTest extends UnitSpec with FakeIhtApp {

  val runTestsThatUseFileSystem = false
  val runTestsThatSortErrorMessages = false
  val runTestsThatSortIhtMessages = false
  val runTestsThatReplaceMessageKeys = false
  val runTestsThatGenerateAllErrorMessageKeysForReplace = false

  val tempMessageOutputFile = TemporaryFile("messages").file.getAbsolutePath
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
  }

  def getResourceAsFilePath(filePath: String) = {
    val url = CommonHelper.getOrException(Play.resource(filePath),
      "Unable to find Play resource in class path: " + filePath)
    url.getFile
  }

  def getMessagesFilePath = {
    Play.application.getFile("conf/messages").getAbsolutePath
  }


  def writeToFile(text: String, fileName: String) = {
    val file = new File(fileName)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(text)
    bw.close()
  }

  def writeDuplicatesToFile(result: Map[String, Set[String]],
                            totalMessageKeysCorrespondingToDuplicateValues: Int,
                            fileName: String, typeOfDuplicate: String) = {
    val outputTitle = getOutputTitle(result.size, totalMessageKeysCorrespondingToDuplicateValues, typeOfDuplicate)
    val failureMessageForFile = outputTitle + ":-\n" +
      MessagesTidier.prettyPrintMapOfDuplicateKeys(result, "\n", xx => "\n" + "\"" + xx + "\"" + ",")
    writeToFile(failureMessageForFile, fileName)
  }

  def getOutputTitle(noOfDuplicateValues: Int, noOfRelatedKeys: Int, typeOfDuplicate: String) =
    "Messages file contains " + noOfDuplicateValues + " " + typeOfDuplicate +
      " duplicate values spanning across " +
      noOfRelatedKeys + " message keys"

  def deleteTempFile() = new File(tempMessageOutputFile).delete

  "Using mocked messages data" must {
    "invertMap" must {
      "swap keys and value keeping keys with a duplicate value in teh same set" in {
        mockedMessagesTidier
          .invertMap(mockedMessagesWithDuplicateValuesAsMap) should be(mockedMessagesWithDuplicateValuesAsInvertedMap)
      }
    }

    "removeUniques" must {
      "produce a map where only the sets of size greater than one remain" in {
        mockedMessagesTidier
          .removeUniques(mockedMessagesWithDuplicateValuesAsInvertedMap) should be(mockedMessagesWithDuplicateValuesAsInvertedMapUniquesRemoved)
      }
    }

    "readFile" must {
      "produce a map of string to string from a testfile of lines k=v" in {
        val result: Map[String, String] = mockedMessagesTidier.readmessagesApi()
        result shouldBe mockedMessagesWithDuplicateValuesAsMap
      }
    }

    "detectDuplicateValues" must {
      "find a duplicated key in dummy messages" in {
        val result: Map[String, Set[String]] = mockedMessagesTidier.detectDuplicateValues()
        result shouldBe mockedMessagesWithDuplicateValuesAsInvertedMapUniquesRemoved
      }
    }

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

    "removeSummaryItems" must {
      "remove keys ending in .summary" in {
        mockedMessagesTidier
          .removeSummaryItems(mockedMessagesWithDuplicateValuesAsInvertedMapWithSummaryItems) should
          be(mockedMessagesWithDuplicateValuesAsInvertedMap)
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

    "prettyPrintMapOfDuplicateKeys" must {
      "return all items in map" in {
        val result = mockedMessagesTidier.prettyPrintMapOfDuplicateKeys(mockedMessagesFileWithDuplicateKeysAsMap)
        result shouldBe "\"a.b.c\",\"one\",\"three\";\"i.a.s\",\"ool\",\"wibble\""
      }
    }

    "replaceInSeq" must {
      "replace all instances of each item in a set of strings" in {
        val tuplesSet = Set(("", "aa.bb.cc", "AAA.BBB.CCC"), ("", "dd.ee.ff", "DDD.EEE.FFF"), ("", "cat", "chicken"))
        val text = Seq(
          "the cat sat on aa.bb.cc the mat",
          "the mat fell aa.bb.cc on the cat",
          "dd.ee.ff fell on the cat",
          "the cats aaa.bb.cc was messagesApi(\"aaa.bb.cc\") flat",
          "the cat aa.bb.ccc was messagesApi(\"aa.bb.ccc\") flat",
          "the cat aa.bb.cc was dd.ee.ff flat",
          "the bigcat was a cat and messagesApi(\"dd.ee.ff\")"
        )
        val expectedText: Seq[String] =
          Seq(
            "the chicken sat on AAA.BBB.CCC the mat",
            "the mat fell AAA.BBB.CCC on the chicken",
            "DDD.EEE.FFF fell on the chicken",
            "the cats aaa.bb.cc was messagesApi(\"aaa.bb.cc\") flat",
            "the chicken aa.bb.ccc was messagesApi(\"aa.bb.ccc\") flat",
            "the chicken AAA.BBB.CCC was DDD.EEE.FFF flat",
            "the bigcat was a chicken and messagesApi(\"DDD.EEE.FFF\")"
          )
        val expectedStats: Map[String, Int] =
          Map(
            ("aa.bb.cc", 3),
            ("dd.ee.ff", 3),
            ("cat", 6)
          )
        val expectedResult = (expectedText, expectedStats)
        val result: (Seq[String], Map[String, Int]) = mockedMessagesTidier.replaceInText(text, tuplesSet)
        result shouldBe expectedResult
      }
    }

    "mergeMaps" must {
      "merge correctly" in {
        val first: Map[String, Int] = Map(
          ("a", 33),
          ("b", 45),
          ("c", 100),
          ("d", 99)
        )
        val second: Map[String, Int] = Map(
          ("g", 29),
          ("a", 87),
          ("c", 54)
        )
        val expectedResult: Map[String, Int] = Map(
          ("a", 120),
          ("b", 45),
          ("c", 154),
          ("d", 99),
          ("g", 29)
        )
        mockedMessagesTidier.mergeMaps(first, second) shouldBe expectedResult
      }
    }

    "removeKeysFromMessagesFile" must {
      "remove specified keys from file" in {
        val mapOfKeyValues = Map(
          "a.b.c" -> "one",
          "d.e.f" -> "two",
          "g.h.i" -> "one",
          "i.a.s" -> "ool",
          "a.x.v" -> "one",
          "u.w.q" -> "two"
        )
        val expectedResult = Map(
          "a.b.c" -> "one",
          "d.e.f" -> "two",
          "a.x.v" -> "one",
          "u.w.q" -> "two"
        )
        val result = mockedMessagesTidier
          .removeKeys(Set("g.h.i", "i.a.s"), mapOfKeyValues)
        result shouldBe expectedResult
      }
    }

    /*
      The following tests should only be run locally since they use the local filesystem. On dev/prod they
      probably won't work, as in these environments everything runs from jar files.
     */
    if (runTestsThatUseFileSystem) {
      "readReplaceKeysFromCommaDelimitedFile" must {
        "read in keys from file" in {
          val expectedResult = Set(
            ("one message", "validation.error.blank.deceased.dateofbirth", "error.dateOfBirth.blank"),
            ("two message", "validation.error.blank.add-exec.dateofbirth", "error.dateOfBirth.blank"),
            ("three message", "validation.error.blank.exemptions.partner.dateofbirth", "error.dateOfBirth.blank"),
            ("four message", "validation.error.invalid.exemptions.partner.last.name", "error.lastName.length"),
            ("five message", "validation.error.invalid.tnrbEligibility.partner.lastName", "error.lastName.length"),
            ("six message", "validation.error.invalid.tnrbEligibility.partner.last.name", "error.lastName.length")
          )
          val result: Set[(String, String, String)] = mockedMessagesTidier
            .readReplaceKeysFromCommaDelimitedFile(getResourceAsFilePath("message_keys_to_replace"))
          result shouldBe expectedResult
        }
      }

      "replaceInAllFolders" must {
        "on call to method to replace all instances of each item in set of tuples returns correct stats" in {
          val tuplesSet = Set(("", "aa.bb.cc", "AAA.BBB.CCC"), ("", "dd.ee.ff", "DDD.EEE.FFF"))
          val filePathToSearchAndReplaceFolder = getResourceAsFilePath("searchandreplace")
          val expectedResult = Set(("aa.bb.cc", 6), ("dd.ee.ff", 4))
          val result = mockedMessagesTidier.replaceInAllFolders(
            tuplesSet = tuplesSet,
            filePathToSearchAndReplaceFolder = filePathToSearchAndReplaceFolder,
            statsOnly = true,
            extensions = Seq("txt")
          )
          result shouldBe expectedResult
        }

        "not try to replace if new key is same as old" in {
          val tuplesSet = Set(("", "aa.bb.cc", "aa.bb.cc"), ("", "dd.ee.ff", "DDD.EEE.FFF"))
          val filePathToSearchAndReplaceFolder = getResourceAsFilePath("searchandreplace")
          val expectedResult = Set(("aa.bb.cc", 0), ("dd.ee.ff", 4))
          val result = mockedMessagesTidier.replaceInAllFolders(
            tuplesSet = tuplesSet,
            filePathToSearchAndReplaceFolder = filePathToSearchAndReplaceFolder,
            statsOnly = true,
            extensions = Seq("txt")
          )
          result shouldBe expectedResult
        }
      }

      "replaceInAllFoldersAndRemoveFromMessagesFile" must {

        "throw exception if any of new keys are mapped to from more than one message" in {
          val tuplesSet = Set(("waa", "aa.bb.cc", "newkey"), ("waa", "dd.ee.ff", "newkey"), ("waa", "ff.jj.kk", "pp.bb.vv"))
          val filePathToSearchAndReplaceFolder = getResourceAsFilePath("searchandreplace")
          val expectedResult = Set(("aa.bb.cc", 0), ("dd.ee.ff", 4))

          a[RuntimeException] shouldBe thrownBy {
            mockedMessagesTidier.replaceInAllFoldersAndRemoveFromMessagesFile(
              tuplesSet = tuplesSet,
              filePathToSearchAndReplaceFolder = filePathToSearchAndReplaceFolder,
              messagesFilePath = getResourceAsFilePath("messages_file"),
              statsOnly = true,
              messageFileOutputPath = Some(tempMessageOutputFile),
              extensions = Seq("txt")
            )
          }
        }

        "if all keys present find correct no of candidate replacements and update the messages file appropriately" in {
          val tuplesSet = Set(("", "aa.bb.cc", "AAA.BBB.CCC"),
            ("a", "dd.ee.ff", "UNKNOWN"),
            ("b", "validation.error.invalid.add-exec.phone.no", "HAHAHAH"),
            ("c", "gg.hh.ii", "AAAAAAAAA"),
            ("d", "g.h.i", "BBBBBBBB"),
            ("e", "u.w.q", "CCCCCCCCCC")
          )
          val filePathToSearchAndReplaceFolder = getResourceAsFilePath("searchandreplace")
          val expectedResult = Set(("aa.bb.cc", 6), ("dd.ee.ff", 4), ("validation.error.invalid.add-exec.phone.no", 1),
            ("gg.hh.ii", 0), ("g.h.i", 0), ("u.w.q", 0))
          val result1 = mockedMessagesTidier.replaceInAllFoldersAndRemoveFromMessagesFile(
            tuplesSet = tuplesSet,
            filePathToSearchAndReplaceFolder = filePathToSearchAndReplaceFolder,
            messagesFilePath = getResourceAsFilePath("messages_file"),
            statsOnly = true,
            messageFileOutputPath = Some(tempMessageOutputFile),
            extensions = Seq("txt")
          )
          result1 shouldBe expectedResult

          val expectedMessagesFileResult =
            Seq(
              "a.b.c=one",
              "",
              "#A COMMENT",
              "# This is a comment",
              "d.e.f=two",
              "AAA.BBB.CCC=WAAAA",
              "",
              "i.a.s=ool",
              "a.x.v=  one",
              "HAHAHAH=YYYYY",
              "dd.ee.ff=WHEE",
              "", "", "", "", "",
              "###gg.hh.ii=LLLLLL",
              "###g.h.i=one",
              "###u.w.q  =two"
            )

          val result2: Seq[String] = mockedMessagesTidier.readFileAsLines(tempMessageOutputFile)
          result2 shouldBe expectedMessagesFileResult
          deleteTempFile()
        }

        "if all keys not present find correct no of candidate replacements, remove from messages file " +
          "any with no replacements and update the rest appropriately" in {
          val tuplesSet = Set(
            ("a", "aa.bb.cc", "AAA.BBB.CCC"),
            ("b", "dd.ee.ff", "DDD.EEE.FFF"),
            ("c", "gg.hh.ii", "GG.HH.II"))
          val filePathToSearchAndReplaceFolder = getResourceAsFilePath("searchandreplace")
          val expectedResult = Set(("aa.bb.cc", 6), ("dd.ee.ff", 4), ("gg.hh.ii", 0))
          val resultOfReplacements = mockedMessagesTidier.replaceInAllFoldersAndRemoveFromMessagesFile(
            tuplesSet = tuplesSet,
            filePathToSearchAndReplaceFolder = filePathToSearchAndReplaceFolder,
            messagesFilePath = getResourceAsFilePath("messages_file"),
            statsOnly = true,
            messageFileOutputPath = Some(tempMessageOutputFile),
            extensions = Seq("txt")
          )

          resultOfReplacements shouldBe expectedResult

          val expectedMessagesFileResult = Seq(
            "a.b.c=one",
            "",
            "#A COMMENT",
            "# This is a comment",
            "d.e.f=two",
            "g.h.i=one",
            "AAA.BBB.CCC=WAAAA",
            "",
            "i.a.s=ool",
            "a.x.v=  one",
            "validation.error.invalid.add-exec.phone.no  =YYYYY",
            "DDD.EEE.FFF=WHEE",
            "u.w.q  =two",
            "", "", "", "", "",
            "###gg.hh.ii=LLLLLL")

          mockedMessagesTidier.readFileAsLines(tempMessageOutputFile) shouldBe expectedMessagesFileResult
          deleteTempFile()
        }

        "if any of new keys exists already in messages file then return a -1 in the tuple" in {
          val tuplesSet = Set(
            ("", "aa.bb.cc", "AAA.BBB.CCC"),
            ("", "dd.ee.ff", "DDD.EEE.FFF"),
            ("", "gg.hh.ii", "GG.HH.II"),
            ("", "jj.vv.bb", "WAAA.WAAA.WAAA")
          )
          val filePathToSearchAndReplaceFolder = "searchandreplace/"
          val expectedResult = Set(("aa.bb.cc", 6), ("dd.ee.ff", 4), ("gg.hh.ii", 0), ("jj.vv.bb", -1))

          a[RuntimeException] shouldBe thrownBy {
            mockedMessagesTidier.replaceInAllFoldersAndRemoveFromMessagesFile(
              tuplesSet = tuplesSet,
              filePathToSearchAndReplaceFolder = filePathToSearchAndReplaceFolder,
              messagesFilePath = getResourceAsFilePath("messages_file_key_in_use"),
              statsOnly = true,
              messageFileOutputPath = Some(tempMessageOutputFile),
              extensions = Seq("txt")
            )
          }
          deleteTempFile()
        }
      }

      "filterForNewKeysAlreadyInUse" must {
        "find any new key already in use" in {
          val tuples: Set[(String, String, String)] = Set(("", "aa.bb.cc", "ww.ee.ee"), ("", "dd.ee.ff", "xxxxxxx.xsddd.ddd"),
            ("", "gg.hh.ii", "i.dont.care"), ("", "jj.vv.bb", "WAAA.WAAA.WAAA"))
          val expectedResult = Set("WAAA.WAAA.WAAA")
          val result = mockedMessagesTidier.filterForNewKeysAlreadyInUse(
            tuplesSet = tuples,
            messagesFilePath = getResourceAsFilePath("messages_file_key_in_use")
          )

          result shouldBe expectedResult
        }
      }

      "sortErrorMessages" must {
        "create a sequence of tuples where the first element of each tuple is in sorted order" in {
          val inputMap = Map("error.b.c" -> "one",
            "error.e.f" -> "two",
            "a.error.c" -> "four",
            "g.validation.i" -> "one",
            "i.a.s" -> "ool",
            "error.x.v" -> "one",
            "u.w.q" -> "two")
          val expectedResult: Seq[(String, String)] =
            Seq(("a.error.c", "four"),
              ("error.b.c", "one"),
              ("error.e.f", "two"),
              ("error.x.v", "one"),
              ("g.validation.i", "one"))
          mockedMessagesTidier.sortErrormessagesApi(inputMap) shouldBe expectedResult
        }
      }

      "getNonErrorMessagesExclCommentedKeys" must {
        "get all non error related messages from the messages file passed in as well as comments" in {
          val inputFilePath = getResourceAsFilePath("messages_file_with_errors")
          val expectedResult: Seq[(String, String)] =
            Seq(
              "i.a.s" -> "ool",
              "" -> "",
              "" -> "",
              "" -> "#WAA",
              "u.w.q" -> "two",
              "" -> ""
            )
          val result: Seq[(String, String)] = mockedMessagesTidier.getNonErrorMessagesExclCommentedKeys(inputFilePath)
          result shouldBe expectedResult
        }
      }

      "getCommentedKeys" must {
        "get all non error related messages from the messages file passed in as well as comments" in {
          val inputFilePath = getResourceAsFilePath("messages_file_with_errors")
          val expectedResult: Seq[(String, String)] =
            Seq(
              "" -> "#HELLO=WORLD"
            )
          val result: Seq[(String, String)] = mockedMessagesTidier.getCommentedKeys(inputFilePath)
          result shouldBe expectedResult
        }
      }
    }

    "splitWithQuotes" must {
      "work" in {
        val result = MessagesTidier.splitWithQuotes("\"a,b,c\",\"d,e,f\",\"g,h,i\"")
        result shouldBe Seq("\"a,b,c\"", "\"d,e,f\"", "\"g,h,i\"")
      }
      "work 2" in {
        val result = MessagesTidier.splitWithQuotes("a,b,c,d")
        result shouldBe Seq("a", "b", "c", "d")
      }
      "work 3" in {
        val result = MessagesTidier.splitWithQuotes("")
        result shouldBe Seq()
      }
    }
  }

  "Using real messages file" must {
    "readMessageFile" must {
      "not contain any duplicate keys" in {
        val result = MessagesTidier.readMessageFile(messagesFile)
        if (result.isRight) {
          if (runTestsThatGenerateAllErrorMessageKeysForReplace) {
            val gg: Map[String, String] = result.right.get.filter(_._1.startsWith("error."))
            val ff: String = MessagesTidier.prettyPrintMapOfKeys(gg)
            println( "\n*******\n" + ff)
          }
          assert(true)
        } else {
          val duplicates: Map[String, Set[String]] = result.left.getOrElse(throw new RuntimeException("Problem with left"))
          fail("Messages file contains duplicate keys: " + MessagesTidier.prettyPrintMapOfDuplicateKeys(duplicates))
        }
      }
    }

    "detectDuplicateValues" must {
      "find all duplicated error-related values" in {
        val result: Map[String, Set[String]] =
          MessagesTidier.detectDuplicateValues(filterForSummaryItems = None)
            .filter(_._2.exists(xx => xx.startsWith("error.")))
        if (result.isEmpty) {
          assert(true)
        } else {
          val typeOfDuplicates = "error-related"
          val totalMessageKeysCorrespondingToDuplicateValues = result.map(_._2.size).sum
          // Uncomment this line if you want the duplicates output to a text file
          //          writeDuplicatesToFile(result, totalMessageKeysCorrespondingToDuplicateValues,
          //            "/home/grant/Desktop/duplicate_values_error_related.txt", typeOfDuplicates)
          val failureMessageForConsole = getOutputTitle(result.size, totalMessageKeysCorrespondingToDuplicateValues,
            typeOfDuplicates) + ":-" + MessagesTidier.prettyPrintMapOfDuplicateKeys(result, ";")
          fail(failureMessageForConsole)
        }
      }

      "find all duplicated non-error-related values" in {
        val result = MessagesTidier.detectDuplicateValues()
          .filter(!_._2.exists(xx => xx.startsWith("error.") || xx.startsWith("global.error.")))
        if (result.isEmpty) {
          assert(true)
        } else {
          val typeOfDuplicates = "non-error-related"
          val totalMessageKeysCorrespondingToDuplicateValues = result.map(_._2.size).sum
          // Uncomment this line if you want the duplicates output to a text file
//          writeDuplicatesToFile(result, totalMessageKeysCorrespondingToDuplicateValues,
//            "/home/grant/Desktop/duplicate_values_non_error-related.txt", typeOfDuplicates)
          val failureMessageForConsole = getOutputTitle(result.size, totalMessageKeysCorrespondingToDuplicateValues,
            typeOfDuplicates) + ":-" + MessagesTidier.prettyPrintMapOfDuplicateKeys(result, ";")
          fail(failureMessageForConsole)
        }
      }
    }

    /*
        This test actually changes the contents of the source code files so it will only work the first time it is run.
        A new copy of the messages file will be output. This can be copied over the top of the existing messages file.
    */
    if (runTestsThatReplaceMessageKeys) {
      "replaceInAllFoldersAndRemoveFromMessagesFile" must {
        "replace all message keys" in {
          val commonBaseFolder = "/home/grant"
          val sourceBaseFolder = s"$commonBaseFolder/Applications/hmrc-development-environment/hmrc/iht-frontend"
          val workFolder = s"$commonBaseFolder/Desktop"

          val tuplesSet: Set[(String, String, String)] = mockedMessagesTidier
            .readReplaceKeysFromCommaDelimitedFile(s"$workFolder/Duplicate message key values - error-related.csv")

          println("\n##############################")
          println("\nTotal message keys to replace: " + tuplesSet.size)

          val tuplesSetQuotesRemoved = tuplesSet.map { xx =>
            (MessagesTidier.removeQuotes(xx._1), MessagesTidier.removeQuotes(xx._2), MessagesTidier.removeQuotes(xx._3))
          }

          val filePathToSearchAndReplaceFolder = s"$sourceBaseFolder/app;$sourceBaseFolder/test;$sourceBaseFolder/conf/pdf/templates"
          val result: Set[(String, Int)] = MessagesTidier.replaceInAllFoldersAndRemoveFromMessagesFile(
            tuplesSet = tuplesSetQuotesRemoved,
            filePathToSearchAndReplaceFolder = filePathToSearchAndReplaceFolder,
            messagesFilePath = getMessagesFilePath,
            statsOnly = true,
            messageFileOutputPath = Some(s"$workFolder/messages_output_real"),
            extensions = Seq("scala", "html", "xsl")
          )

          println("\n###############################")
          println("\nRESULT OF REPLACES")
          println("\n  Total keys with no useages in the code: " + result.count(_._2 == 0))
          println("\n  Total keys with useages in the code: " + result.count(_._2 > 0))
          println("\n\nDETAILS")
          println("\n" + MessagesTidier.joinAndPrettyPrintSearchAndReplaceResults(result, tuplesSetQuotesRemoved))
          println("\n###############################")

          assert(true)
        }
      }
    }

    if (runTestsThatSortErrorMessages) {
      "sortErrorMessages" must {
        "create a sequence of tuples where the first element of eachj tuple is in sorted order" in {
          //mockedMessagesTidier.writeToFile("/home/grant/Desktop/waa.txt", result)

          val messagesAsTuples: Map[String, String] = MessagesTidier.readmessagesApi()
          val sortedErrorMessages = MessagesTidier.sortErrormessagesApi(messagesAsTuples)
          mockedMessagesTidier.writeToFile("/home/grant/Desktop/waa1.txt", sortedErrorMessages)

          val inputFilePath = getResourceAsFilePath("messages")

          val result1: Seq[(String, String)] = MessagesTidier.getNonErrorMessagesExclCommentedKeys(inputFilePath)
          mockedMessagesTidier.writeToFile("/home/grant/Desktop/waa2.txt", result1)

          val result2: Seq[(String, String)] = MessagesTidier.getCommentedKeys(inputFilePath)
          mockedMessagesTidier.writeToFile("/home/grant/Desktop/waa3.txt", result2)

          true shouldBe true
        }
      }
    }

    if (runTestsThatSortIhtMessages) {
      "sortIhtMessages" must {
        "create a sequence of tuples where the first element of eachj tuple is in sorted order" in {
          //mockedMessagesTidier.writeToFile("/home/grant/Desktop/waa.txt", result)

          val messagesAsTuples: Map[String, String] = MessagesTidier.readmessagesApi()
          val sortedErrorMessages = MessagesTidier.sortErrormessagesApi(messagesAsTuples)
          mockedMessagesTidier.writeToFile("/home/grant/Desktop/waa1.txt", sortedErrorMessages)

          val inputFilePath = getResourceAsFilePath("messages")

          val result1: Seq[(String, String)] = MessagesTidier.getNonErrorMessagesExclCommentedKeys(inputFilePath)
          mockedMessagesTidier.writeToFile("/home/grant/Desktop/waa2.txt", result1)

          val result2: Seq[(String, String)] = MessagesTidier.getCommentedKeys(inputFilePath)
          mockedMessagesTidier.writeToFile("/home/grant/Desktop/waa3.txt", result2)

          true shouldBe true
        }
      }
    }
  }
}
