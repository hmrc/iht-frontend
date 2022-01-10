/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.Environment

import scala.io.BufferedSource

trait MessagesTidier {
  val messages: Map[String, Map[String, String]]

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

  def readTuples(filePath: String)(implicit env: Environment): Seq[(String, String)] = {
    val inputStream = getOrException(env.resourceAsStream(filePath),
      "Unable to find Play resource in class path: " + filePath)
    val bufferedSource = new BufferedSource(inputStream)
    bufferedSource.getLines().map(line => parseLine(line)).toSeq.flatten
  }

  def hasDuplicatedKeys(mapOfKeys: Map[String, Set[String]]): Boolean = mapOfKeys.exists(_._2.size > 1)

  def duplicatedKeys(mapOfKeys: Map[String, Set[String]]): Map[String, Set[String]] = mapOfKeys.filter(_._2.size > 1)

  def readMessageFile(filePath: String)(implicit env: Environment): Either[Map[String, Set[String]], Map[String, String]] = {
    val tuples = readTuples(filePath)
    val consolidatedMap = createConsolidatedMap(tuples)

    if (hasDuplicatedKeys(consolidatedMap)) {
      Left(duplicatedKeys(consolidatedMap))
    } else {
      Right(consolidatedMap.map { item => (item._1, item._2.toSeq.head) })
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
                                    columnDelimiter: String => String = _ => ","): String = {
    val sb = new StringBuilder
    m.foreach { mapItem =>
      sb ++= "\"" + mapItem._1 + "\"" + "," +
        prettyPrintSetOfStrings(mapItem._2, columnDelimiter(mapItem._1)) + recordDelimiter
    }
    sb.deleteCharAt(sb.length - recordDelimiter.length)
    sb.toString
  }

  def prettyPrintMapOfKeys(m: Map[String, String]): String = {
    val sb = new StringBuilder
    m.foreach { mapItem =>
      sb ++= "\"\"," + "\"" + mapItem._1 + "\"" + ",\"UNKNOWN\"\n"

    }
    sb.toString
  }

  def compareMessageFileKeys()(implicit env: Environment): Set[String] = {
    val english = readMessageFile("messages.en").right.get
    val welsh = readMessageFile("messages.cy").right.get
    val result = (english.keySet -- welsh.keySet) ++ (welsh.keySet -- english.keySet)
    if(result.nonEmpty) {
      val file = new File("/home/" + System.getProperty("user.name") + "/missingKeysAndValues.csv")
      val bw = new BufferedWriter(new FileWriter(file))
      result.toSeq.sorted.foreach(key => bw.write(key + " = " +
        (if(english.get(key).isEmpty) { "English value missing \n" }
        else if(welsh.get(key).isEmpty) { "Welsh value is missing \n" })))
      bw.close()
    }
    result
  }

}