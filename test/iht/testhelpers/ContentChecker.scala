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

object ContentChecker {

  private val isNotGovUk: String => Boolean = _ != "GOV.UK"
  private val doesNotContainAnAtSign: String => Boolean = !_.contains("@")
  private val doesNotContainAForwardSlash: String => Boolean = !_.contains("/")

  /* A dotted string is a series of characters other than digits, followed by a dot, followed by non space characters.
   * the digits are included in the first part to explicitly exclude strings like £12.00. Removing string sthat fit this
   * pattern but are not of interest such as emails and URLs are handled by a second filtering stage.
   */
  private val dottedString = """[^\s|^\d]+\.[^\s]+""".r

  def findMessageKeys(content: String): Seq[String] = {
    (dottedString findAllIn content).toSeq filter (s => isNotGovUk(s) &&
                                                        doesNotContainAnAtSign(s) &&
                                                        doesNotContainAForwardSlash(s))
  }

  def stripLineBreaks(inputCopy: String) = {
    inputCopy.replace("\n", "")
  }
}
