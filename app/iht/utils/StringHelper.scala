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

import java.util.Locale
import java.util.UUID.randomUUID

import iht.constants.IhtProperties
import iht.utils.CommonHelper.withValue
import iht.views.html.ihtHelpers.custom.name
import org.joda.time.format.DateTimeFormat
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.twirl.api.Html
import uk.gov.hmrc.play.frontend.auth.AuthContext

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

object StringHelper {
  private val StartOfPrefix = 0
  private val EndOfPrefix = 2
  private val SuffixCharacter = 8
  private val FirstNumberStart = 2
  private val FirstNumberEnd = 4
  private val SecondNumberStart = 4
  private val SecondNumberEnd = 6
  private val ThirdNumberStart = 6
  private val ThirdNumberEnd = 8
  private val emptyString = ""

  def ninoFormat(s: String) = {
    if (s.length >= 9) {
      val str = s.replace(" ", "")
      (str.substring(StartOfPrefix, EndOfPrefix)
        + " " + str.substring(FirstNumberStart, FirstNumberEnd)
        + " " + str.substring(SecondNumberStart, SecondNumberEnd)
        + " " + str.substring(ThirdNumberStart, ThirdNumberEnd)
        + " " + str.substring(SuffixCharacter)).toUpperCase
    } else {
      s
    }
  }

  def yesNoFormat(v: Option[Boolean]): String = v match {
    case Some(true) => Messages("iht.yes")
    case Some(false) => Messages("iht.no")
    case _ => ""
  }

  /**
    * Parses a string in the form xx=yy,aa=cc,... into a seq of
    * tuples.
    */
  def parseAssignmentsToSeqTuples(listOfAssignments:String): Seq[(String, String)] = {
    if (listOfAssignments.trim.length == 0) {
      Seq()
    } else {
      val splitItems: Array[String] = listOfAssignments.split(",")
      splitItems.toSeq.map { property =>
        withValue(property.trim.split("=")) { splitProperty =>
          if(splitProperty.length == 2) {
            Tuple2(splitProperty(0).trim, splitProperty(1).trim)
          } else {
            throw new RuntimeException("Invalid property-value assignment: " + splitProperty)
          }
        }
      }
    }
  }

  def parseOldAndNewDatesFormats(date:String): String ={
    val newFormat = DateTimeFormat.forPattern("yyyy-MM-dd").withLocale(Locale.ENGLISH)
    Try(newFormat.parseDateTime(date)) match {
      case Success(s) => date
      case Failure(_) =>
        val oldFormat = DateTimeFormat.forPattern("d MMM yyyy").withLocale(Locale.ENGLISH)
        Try(oldFormat.parseDateTime(date)) match {
          case Success(s) => s.toString(newFormat)
          case Failure(ex) => throw ex
        }
    }
  }

  def trimAndUpperCaseNino(nino: String) = {
    nino.trim.replace(" ", "").toUpperCase
  }

  def generateAcknowledgeReference: String = {
    randomUUID.toString().replaceAll("-", "")
  }

  /**
    * Convert the second element of array (Array created by input string) to Lowercase
    */
  def formatStatus(inputStatus: String) = {

    val arrayStatus = inputStatus match {
      case ApplicationStatus.KickOut => ApplicationStatus.InProgress.split(" ")
      case ApplicationStatus.ClearanceGranted => ApplicationStatus.Closed.split(" ")
      case _ => inputStatus.split(" ")
    }

    val firstPhase = arrayStatus.head

    if (arrayStatus.length > 1) {
      (firstPhase.replace(firstPhase.charAt(0), firstPhase.charAt(0).toUpper) + " " + arrayStatus.last.toLowerCase).trim
    } else {
      firstPhase.trim
    }
  }

  def getNino(userNino: Option[String]): String = {
    userNino.getOrElse(throw new RuntimeException("User account could not be retrieved!"))
  }

  def booleanToYesNo(boolean: Boolean): String = {
    boolean match {
      case true => "Yes"
      case false => "No"
    }
  }

  /**
    * Takes a string and checks its constituent parts against a max length (nameRestrictLength)
    * String is split on spaces and hyphens to exclude strings which would split to new lines anyway
    * Returns true if a part of the string is over the alloted length
    * Allows for measures to be taken to prevent long names breaking the page layout
    */
  def isNameLong(name: String): Boolean = {
    var restrictName: Boolean = false;
    val nameArr = name.split(" ");
    for (namePart <- nameArr) {
      var subparts = namePart.split("-")
      for (subpart <- subparts) {
        if (subpart.length > IhtProperties.nameRestrictLength) {
          restrictName = true;
        }
      }
    }
    restrictName
  }

  def addApostrophe(name: String): String = name + "'" + (if (name.endsWith("s")) "" else "s")

  /**
    * Split string using specified delimiters and produce seq of each element
    * accompanied by the delimiter used after it.
    */
  def split(s: String, delimiters: Seq[Char]): Seq[(String, Option[Char])] = {
    if (s.isEmpty) {
      Seq.empty
    } else {
      var result = new ListBuffer[(String, Option[Char])]()
      var current = emptyString
      s.foreach { c =>
        delimiters.find(_ == c) match {
          case None => current += c
          case Some(found) =>
            result = result :+ Tuple2(current, Some(found))
            current = emptyString
        }
      }
      result :+ Tuple2(current, None)
    }
  }

  /**
    * Split string using the specified delimiters and map each element to
    * another string using the specified function then reconstruct the string
    * and return it.
    */
  def splitAndMapElements(s:String, separators: Seq[Char], func: String => String): String = {
    val mappedElements = split(s, separators).map { element =>
      val nameComponent = element._1
      Tuple2(func(nameComponent), element._2)
    }
    mappedElements.foldLeft(emptyString){ (op1, op2) =>
        op1 + op2._1 + op2._2.fold("")(_.toString)
    }
  }
}
