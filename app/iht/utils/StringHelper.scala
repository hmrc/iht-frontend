/*
 * Copyright 2016 HM Revenue & Customs
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

import play.api.i18n.Messages

object StringHelper {
  val StartOfPrefix = 0
  val EndOfPrefix = 2
  val SuffixCharacter = 8
  val FirstNumberStart = 2
  val FirstNumberEnd = 4
  val SecondNumberStart = 4
  val SecondNumberEnd = 6
  val ThirdNumberStart = 6
  val ThirdNumberEnd = 8


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

  def isAllDigits(x: String) = x forall Character.isDigit

  def yesNoFormat(v: Option[Boolean]): String = v match {
    case Some(true) => Messages("iht.yes")
    case Some(false) => Messages("iht.no")
    case _ => ""
  }
}
