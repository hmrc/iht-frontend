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

import java.io._

import iht.utils.CommonHelper._
import play.api.Play
import play.api.Play.current
import play.api.i18n.{I18nSupport, Messages}
import play.api.i18n.Messages.Implicits._
import play.i18n.MessagesApi

import scala.collection.immutable.ListMap
import scala.io.{BufferedSource, Source}
import scala.util.{Failure, Success, Try}

trait MessagesTidier {
  val messages: Map[String, Map[String, String]]

  def invertMap[A, B](m: Map[A, B]): Map[B, Set[A]] =
    m.groupBy(_._2).map(itemsGroupedByValue => (itemsGroupedByValue._1, itemsGroupedByValue._2.keys.toSet))

  def removeSummaryItems(m: Map[String, Set[String]]): Map[String, Set[String]] = m.map { item =>
    (item._1, item._2.filter((p: String) => !p.toString.endsWith(".summary")))
  }

  def removeNonSummaryItems(m: Map[String, Set[String]]): Map[String, Set[String]] = m.map { item =>
    (item._1, item._2.filter((p: String) => p.toString.endsWith(".summary")))
  }

  def removeUniques[A, B](m: Map[B, Set[A]]): Map[B, Set[A]] = m.filter(_._2.size > 1)

  val exclusions = Set("global.error.pageNotFound404.message",
    "global.error.badRequest400.message",
    "error.length.assets.policies",
    "error.currencyValue.incorrect",
    "error.currencyValue.length",
    "error.currency.giftsDetails.value",
    "error.length.giftsDetails.exemptions",
    "error.currency.giftsDetails.exemptions",
    "error.estateGifts.currencyValue.summary",
    "error.length.giftsDetails.value",
    "error.currency.assets.policies",
    "error.currency.giftsDetails.value.summary",
    "error.enter_valid_date",
    "error.enter_a_date"
  )

  def readmessagesApi(): Map[String, String] = {
    val aa: Map[String, String] = getOrException(messages.get("en"))
    aa.filter(xx => !exclusions.contains(xx._1))
  }

  /**
    * filterForSummaryItems: If None then returns all duplicates, if Some(true) filters out any items ending
    * in .summary, if Some(false) filters out any items not ending in .summary.
    */
  def detectDuplicateValues(filterForSummaryItems: Option[Boolean] = None): Map[String, Set[String]] = {

    // minutes key has been exempted from duplication as its usage is different in welsh, hence this
    // duplication is permitted
    val invertedMap = invertMap(readmessagesApi()).filterKeys(_!="minutes")

    removeUniques(
      filterForSummaryItems match {
        case None => invertedMap
        case Some(true) => removeSummaryItems(invertedMap)
        case _ => removeNonSummaryItems(invertedMap)
      }
    )
  }

  def createConsolidatedMap(tuples: Seq[(String, String)]): Map[String, Set[String]] =
    tuples.groupBy(_._1).map(itemsGroupedByValue => (itemsGroupedByValue._1, itemsGroupedByValue._2.map(_._2).toSet))

  def parseLine(line: String): Option[(String, String)] = {
    val elements = line.split("=")
    if (elements.size == 2) {
      Some(Tuple2(elements(0).trim, elements(1).trim))
    } else {
      None
    }
  }

  def readTuples(filePath: String): Seq[(String, String)] = {
    val inputStream = getOrException(Play.resourceAsStream(filePath),
      "Unable to find Play resource in class path: " + filePath)
    val bufferedSource = new BufferedSource(inputStream)
    bufferedSource.getLines().map(line => parseLine(line)).toSeq.flatten
  }

  def hasDuplicatedKeys(mapOfKeys: Map[String, Set[String]]): Boolean = mapOfKeys.exists(_._2.size > 1)

  def duplicatedKeys(mapOfKeys: Map[String, Set[String]]): Map[String, Set[String]] = mapOfKeys.filter(_._2.size > 1)

  def readMessageFile(filePath: String): Either[Map[String, Set[String]], Map[String, String]] = {
    val tuples = readTuples(filePath)
    val consolidatedMap = createConsolidatedMap(tuples)

    if (hasDuplicatedKeys(consolidatedMap)) {
      Left(duplicatedKeys(consolidatedMap))
    } else {
      Right(consolidatedMap.map { item => (item._1, item._2.toSeq.head) })
    }
  }

  def removeKeys(setOfKeys: Set[String], setOfKeysAndValues: Map[String, String]): Map[String, String] =
    setOfKeysAndValues.filter(xx => !setOfKeys.contains(xx._1))

  def joinAndPrettyPrintSearchAndReplaceResults(s: Set[(String, Int)],
                                                setOfTuples: Set[(String, String, String)],
                                                delimiter: String = "\n"): String = {

    val joinedResults: Set[(String, String, String, Int)] = s.map { ss =>
      val foundInSetOfKeysAndValues = getOrException(setOfTuples.find(xx => xx._2 == ss._1))
      (foundInSetOfKeysAndValues._1, foundInSetOfKeysAndValues._2, foundInSetOfKeysAndValues._3, ss._2)
    }

    if (joinedResults.isEmpty) {
      ""
    } else {
      val sb = new StringBuilder
      joinedResults.foreach { item =>
        sb ++= "\"" + item._1 + "\",\"" + item._2 + "\",\"" + item._3 + "\",\"" + item._4 + "\"" + delimiter
      }
      sb.delete(sb.length - delimiter.length, sb.length)
      sb.toString
    }
  }

  def prettyPrintSetOfStrings(s: Set[String], delimiter: String = ","): String =
    if (s.isEmpty) {
      ""
    } else {
      val sb = new StringBuilder
      s.foreach { item =>
        sb ++= "\"" + item + "\"" + delimiter
      }
      sb.delete(sb.length - delimiter.length, sb.length)
      sb.toString
    }

  def prettyPrintMapOfDuplicateKeys(m: Map[String, Set[String]],
                                    recordDelimiter: String = ";",
                                    columnDelimiter: String => String = _ => ",") = {
    val sb = new StringBuilder
    m.foreach { mapItem =>
      sb ++= "\"" + mapItem._1 + "\"" + "," +
        prettyPrintSetOfStrings(mapItem._2, columnDelimiter(mapItem._1)) + recordDelimiter
    }
    sb.deleteCharAt(sb.length - recordDelimiter.length)
    sb.toString
  }

  def prettyPrintMapOfKeys(m: Map[String, String]) = {
    val sb = new StringBuilder
    m.foreach { mapItem =>
      sb ++= "\"\"," + "\"" + mapItem._1 + "\"" + ",\"UNKNOWN\"\n"

    }
    sb.toString
  }

  def getRecursiveListOfFiles(dir: File): Array[File] = {
    val these = dir.listFiles
    if (these == null || these.isEmpty) {
      Array[File]()
    } else {
      these.filter(_.isFile) ++ these.filter(_.isDirectory).flatMap(getRecursiveListOfFiles)
    }
  }

  def getListOfScalaHtmlFiles(dir: String, extensions: Seq[String]): List[String] = {
    val splitDir: Array[String] = dir.split(";")
    var results = Seq[List[String]]()
    splitDir.foreach { d =>
      val listFiles = getRecursiveListOfFiles(new File(d)).toList.filter(x => extensions.exists(x.getName.endsWith(_)) && !x.getName.contains("MessagesTidier"))
      val yy = listFiles.map(_.getAbsolutePath)
      results = results :+ yy
    }
    var finalResults = List[String]()
    results.foreach { kk =>
      finalResults = finalResults ++ kk
    }
    finalResults
  }

  def readFileAsLines(file: String): Seq[String] = {
    val tt = Try(tempReadFileAsLines(file))
    tt match {
      case Success(s) => s
      case Failure(ex: java.nio.charset.MalformedInputException) =>
        tempReadFileAsLines(file, Some("ISO-8859-1"))
      case Failure(ex) => throw ex
    }
  }

  private def tempReadFileAsLines(file: String, codeset: Option[String] = None): Seq[String] = {
    val itString = if (codeset.isEmpty) {
      Source.fromFile(new File(file)).getLines()
    } else {
      Source.fromFile(new File(file), codeset.getOrElse("")).getLines()
    }

    var output: Seq[String] = Seq()
    itString.foreach { line =>
      output = output.:+(line)
    }
    output
  }

  def removeQuotes(s: String) = {
    if (s.startsWith("\"")) {
      val gg = s.substring(1)
      if (gg.endsWith("\"")) {
        gg.substring(0, gg.length - 1)
      } else {
        gg
      }
    } else {
      if (s.endsWith("\"")) {
        s.substring(0, s.length - 1)
      } else {
        s
      }
    }
  }

  def formatArrayAsTuple(as: Array[String]): (String, String, String) = {
    val l = as.length
    (if (l > 0) as(0) else "", if (l > 1) as(1) else "", if (l > 2) as(2) else "")
  }

  def formatArrayAsTuple(as: Seq[String]): (String, String, String) = {
    val l = as.length
    (if (l > 0) as.head else "", if (l > 1) as(1) else "", if (l > 2) as(2) else "")
  }

  def splitWithQuotes(s: String) = {

    var result: Seq[String] = Seq()

    var inQuotes = false
    var current = ""

    s.foreach { c =>
      if (c == '"') {
        inQuotes = !inQuotes
      }
      if (inQuotes) {
        current += c
      } else {
        if (c == ',') {
          result = result :+ current
          current = ""
        } else {
          current += c
        }
      }
    }
    if (!current.isEmpty) {
      result = result :+ current
    }
    result
  }

  def readReplaceKeysFromCommaDelimitedFile(file: String): Set[(String, String, String)] = {
    readFileAsLines(file).map { xx =>
      val parsed = splitWithQuotes(xx)
      formatArrayAsTuple(parsed)
    }.toSet
  }

  private def writeLinesToFile(file: String, lines: Seq[String]) = {
    val pw = new PrintWriter(new FileOutputStream(file, false))
    lines.foreach { line =>
      pw.println(line)
    }
    pw.close()
  }

  def writeToFile(file: String, lines: Seq[(String, String)]) = {
    val pw = new PrintWriter(new FileOutputStream(file, false))
    lines.foreach { (line: (String, String)) =>

      if (line._1.isEmpty) {
        pw.println(line._2)
      } else {
        pw.println(line._1.trim + " = " + line._2.trim)
      }
    }
    pw.close()
  }

  def mergeMaps(first: Map[String, Int], second: Map[String, Int]): Map[String, Int] = {
    val updatedFirstFromSecond = first.map { elementFromFirst =>
      val newIntVal = if (second.exists(_._1 == elementFromFirst._1)) {
        elementFromFirst._2 + second(elementFromFirst._1)
      } else {
        elementFromFirst._2
      }
      (elementFromFirst._1, newIntVal)
    }
    val secondMinusFirst = second.filter(elementFromSecond => !first.exists(_._1 == elementFromSecond._1))
    updatedFirstFromSecond ++ secondMinusFirst
  }

  def replaceInAllFolders(tuplesSet: Set[(String, String, String)],
                          filePathToSearchAndReplaceFolder: String,
                          statsOnly: Boolean = false,
                          extensions: Seq[String] = Seq("scala", "html")
                         ): Set[(String, Int)] = {
    val files = getListOfScalaHtmlFiles(filePathToSearchAndReplaceFolder, extensions)
    var mapOfItemsToReplaceCounts: Map[String, Int] = Map()
    files.foreach { file =>
      val fileAsLines: Seq[String] = readFileAsLines(file)
      val afterReplace: (Seq[String], Map[String, Int]) = replaceInText(fileAsLines, tuplesSet)
      if (!statsOnly) {
        writeLinesToFile(file, afterReplace._1)
      }
      mapOfItemsToReplaceCounts = mergeMaps(mapOfItemsToReplaceCounts, afterReplace._2)
    }
    mapOfItemsToReplaceCounts.toSet
  }

  def readAllMessagesKeyFile(filePath: String): Seq[(String, String)] = {
    val lines = readFileAsLines(filePath)
    lines.map { line =>
      val c = line.split("=")
      (if (c.nonEmpty) c(0).trim else "", line.trim)
    }
  }

  def filterForNewKeysAlreadyInUse(tuplesSet: Set[(String, String, String)], messagesFilePath: String): Set[String] = {
    val allMessagesKeyFile: Seq[(String, String)] = readAllMessagesKeyFile(messagesFilePath)
    val badKeys: Set[(String, String, String)] = tuplesSet.filter { tuple => tuple._2 != tuple._3 &&
      allMessagesKeyFile.exists(_._1 == tuple._3)
    }

    badKeys.map(xx => xx._3)
  }

  def mergeResultSets(sets: Set[Set[(String, Int)]]): Set[(String, Int)] = {
    sets.fold(Set()) { (a, b) =>
      merge(a, b)
    }
  }

  def merge(a: Set[(String, Int)], b: Set[(String, Int)]) = {
    val updatedA = a.map { xx =>
      val total = b.find(dd => dd._1 == xx._1).fold(xx._2)(yy => xx._2 + yy._2)
      (xx._1, total)
    }

    val bNotInA = b.filter(xx => !updatedA.exists(_._1 == xx._1))

    val updatedB = bNotInA.map { xx =>
      val total = a.find(dd => dd._1 == xx._1).fold(xx._2)(yy => xx._2 + yy._2)
      (xx._1, total)
    }

    updatedA ++ updatedB


  }

  private def writeMessagesToFile(file: String, messages: Seq[(String, String)]) = {
    val pw = new PrintWriter(new FileOutputStream(file, false))
    messages.foreach { msg =>
      pw.println(msg._2.trim)
    }
    pw.close()
  }

  def replaceInAllFoldersAndRemoveFromMessagesFile(tuplesSet: Set[(String, String, String)],
                                                   filePathToSearchAndReplaceFolder: String,
                                                   messagesFilePath: String,
                                                   statsOnly: Boolean = false,
                                                   messageFileOutputPath: Option[String] = None,
                                                   extensions: Seq[String]): Set[(String, Int)] = {

    val badKeys = filterForNewKeysAlreadyInUse(tuplesSet, messagesFilePath)
    if (badKeys.nonEmpty) {
      throw new RuntimeException("New keys in use: " + badKeys.toString)
    }

    val multiplyMappedKeys = tuplesSet.groupBy(_._1).filter { xx =>
      val ff: Map[String, Set[(String, String, String)]] = xx._2.groupBy(_._3)
      ff.size > 1
    }
    if (multiplyMappedKeys.nonEmpty) {
      throw new RuntimeException("Key values mapped to more than one new key: " + multiplyMappedKeys.toString)
    }

    val messagesFileOutputPath = messageFileOutputPath.fold(messagesFilePath)(identity)
    if (statsOnly && messagesFileOutputPath == messagesFilePath) {
      throw new RuntimeException("Stats only is true and message file paths are the same")
    }

    val fileTemp = new java.io.File(messagesFilePath)

    if (fileTemp.exists) {
      val itemsReplaced: Set[(String, Int)] = replaceInAllFolders(
        tuplesSet = tuplesSet,
        filePathToSearchAndReplaceFolder = filePathToSearchAndReplaceFolder,
        statsOnly = statsOnly,
        extensions)

      val keysToRemoveFromMessagesFile: Set[String] = itemsReplaced.filter(_._2 == 0).map(_._1.trim)
      val keysToUpdateInMessagesFile: Set[String] = itemsReplaced.filter(_._2 > 0).map(_._1.trim)

      val tuplesToUpdatInMessagesFile: Set[(String, String, String)] = tuplesSet.filter(t => keysToUpdateInMessagesFile.contains(t._2))

      val allMessagesKeyFile: Seq[(String, String)] = readAllMessagesKeyFile(messagesFilePath)

      val allMessagesKeyFileUnusedRemoved = allMessagesKeyFile.filter(xx => !keysToRemoveFromMessagesFile.contains(xx._1))
      val commentedOutUnused = allMessagesKeyFile.filter(xx => keysToRemoveFromMessagesFile.contains(xx._1)).map {
        (xx: (String, String)) => (xx._1, "###" + xx._2)
      }
      val allMessagesKeyFileFinalNotInclCommentedItems: Seq[(String, String)] = allMessagesKeyFileUnusedRemoved.map { messagesElement =>
        tuplesToUpdatInMessagesFile.find(_._2 == messagesElement._1) match {
          case None => messagesElement
          case Some(tuple) =>
            val splitMessage = messagesElement._2.split("=")
            val newItem = if (splitMessage.length > 1) splitMessage(1) else ""

            if (tuple._3 == "UNKNOWN") {
              // If new key is UNKNOWN then don't replace it in messages file
              messagesElement
            } else {
              // else replace it with new message key
              (tuple._2, tuple._3 + "=" + newItem)
            }
        }
      }

      val allMessagesKeyFileFinal = allMessagesKeyFileFinalNotInclCommentedItems ++ commentedOutUnused

      writeMessagesToFile(messagesFileOutputPath, allMessagesKeyFileFinal)
      itemsReplaced
    } else {
      throw new RuntimeException("Messages file " + fileTemp.getAbsolutePath + " not found")
    }
  }

  def sortErrormessagesApi(m: Map[String, String]): Seq[(String, String)] =
    m.toSeq.filter(tp => tp._1.contains("error") || tp._1.contains("validation")).sortBy(_._1)

  def sortIhtmessagesApi(m: Map[String, String]): Seq[(String, String)] =
    m.toSeq.filter(tp => tp._1.startsWith("iht.")).sortBy(_._1)

  def getNonErrorMessagesExclCommentedKeys(filePath: String): Seq[(String, String)] = {
    val allMessages: Seq[String] = readFileAsLines(filePath)

    val optionalTuples: Seq[Option[(String, String)]] = allMessages.map { line =>
      lazy val parsed = line.split("=")
      lazy val element1 = if (parsed.length > 0) parsed(0) else ""
      lazy val element2 = if (parsed.length > 1) parsed(1) else ""

      if (line.startsWith("#") && line.contains("=")) {
        None
      } else if (line.isEmpty || line.startsWith("#")) {
        Some(Tuple2("", line))
      } else if (!element1.contains("error") && !element1.contains("validation")) {
        Some(Tuple2(element1, element2))
      } else {
        None
      }
    }
    optionalTuples.flatten
  }

  def getNonIhtMessagesExclCommentedKeys(filePath: String): Seq[(String, String)] = {
    val allMessages: Seq[String] = readFileAsLines(filePath)

    val optionalTuples: Seq[Option[(String, String)]] = allMessages.map { line =>
      lazy val parsed = line.split("=")
      lazy val element1 = if (parsed.length > 0) parsed(0) else ""
      lazy val element2 = if (parsed.length > 1) parsed(1) else ""

      if (line.startsWith("#") && line.contains("=")) {
        None
      } else if (line.isEmpty || line.startsWith("#")) {
        Some(Tuple2("", line))
      } else if (!element1.startsWith("iht.")) {
        Some(Tuple2(element1, element2))
      } else {
        None
      }
    }
    optionalTuples.flatten
  }

  def getCommentedKeys(filePath: String): Seq[(String, String)] = {
    val allMessages: Seq[String] = readFileAsLines(filePath)

    val optionalTuples: Seq[Option[(String, String)]] = allMessages.map { line =>
      lazy val parsed = line.split("=")

      if (line.startsWith("#") && line.contains("=")) {
        Some(Tuple2("", line))
      } else {
        None
      }
    }
    optionalTuples.flatten
  }

  def replaceInText(textLines: Seq[String],
                    setOfItemsToReplace: Set[(String, String, String)]): (Seq[String], Map[String, Int]) = {
    def regexForhMessageKeys(key: String) = "([\\s\\n\\f\"\']+|^)(" + key + ")([\\s\\n\\f=\"\']+|$)"
    var transformedLines: Seq[String] = textLines
    val mapOfItemsToReplaceCounts: scala.collection.mutable.Map[String, Int] = scala.collection.mutable.Map()
    setOfItemsToReplace.foreach { item =>
      withValue(regexForhMessageKeys(item._2)) { regexForMessageKey =>
        withValue("$1" + item._3 + "$3") { regexReplaceValue =>
          if (item._2 == item._3 || item._3.isEmpty) {
            // If old and new key are the same or new key empty then don't try to replace just store 0 as count
            mapOfItemsToReplaceCounts(item._2) = 0
          } else {
            transformedLines = transformedLines.map { line =>
              val totalMatches = regexForMessageKey.r.findAllIn(line).count(_ => true)
              if (mapOfItemsToReplaceCounts.exists(_._1 == item._2)) {
                mapOfItemsToReplaceCounts(item._2) = mapOfItemsToReplaceCounts(item._2) + totalMatches
              } else {
                mapOfItemsToReplaceCounts(item._2) = totalMatches
              }

              val lineAfterReplacement = line.replaceAll(regexForMessageKey, regexReplaceValue)
              lineAfterReplacement
            }
          }
        }
      }
    }
    (transformedLines, mapOfItemsToReplaceCounts.toMap)
  }

  def compareMessageFileKeys(): Set[String] = {
    val english = readMessageFile("messages.en").right.get
    val welsh = readMessageFile("messages.cy").right.get
    val result = (english.keySet -- welsh.keySet) ++ (welsh.keySet -- english.keySet)
    if(result.nonEmpty) {
      val file = new File("/home/" + System.getProperty("user.name") + "/missingKeysAndValues.csv")
      val bw = new BufferedWriter(new FileWriter(file))
      result.toSeq.sorted.foreach(key => bw.write(key + " = " +
        (if(english.get(key).isEmpty) "English value missing \n"
        else if(welsh.get(key).isEmpty) "Welsh value is missing \n")))
      bw.close()
    }
    result
  }

}

object MessagesTidier extends MessagesTidier {
  override val messages: Map[String, Map[String, String]] = Messages.Implicits.applicationMessagesApi.messages
}
