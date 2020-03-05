/*
 * Copyright 2020 HM Revenue & Customs
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

package iht.models.application.exemptions

import iht.utils.CommonHelper
import play.api.i18n.Messages
import play.api.libs.json.Json

case class Charity(id: Option[String],
                   name: Option[String],
                   number: Option[String],
                   totalValue: Option[BigDecimal]) {

  def isComplete = CommonHelper.isSectionComplete(Seq(id, name, number, totalValue))

  /**
    * If there is no charity name then returns an Option of an appropriate message to indicate this. If there is
    * a name then it returns None. The possible scenarios are listed below:-
    *
    * Scenario 1: No charity name, no charity number, charity value => "No charity name or number added", charity value displayed
    * Scenario 2: Charity name, no charity number, no charity value => Charity name
    * Scenario 3: No charity name, charity number, no charity value => "No charity name added"
    * Scenario 4: Charity name, no charity number, charity value => Charity name, charity value displayed
    * Scenario 5: No charity name, charity number, charity value => "No charity name given", charity value displayed
    */
  def nameValidationMessage()(implicit messages: Messages): Option[String] = {
    (name, number, totalValue) match {
      case (None, None, Some(_)) => Some(messages("site.noCharityNameAndNumberGiven")) // scenario 1
      case (Some(_), _, _) => None // scenarios 2 & 4
      case (None, _, _) => Some(messages("site.noCharityNameGiven")) // scenario 3 & 5
    }
  }
}

object Charity {
  implicit val formats = Json.format[Charity]
}
