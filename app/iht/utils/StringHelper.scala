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

import play.api.i18n.Messages
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

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
}
