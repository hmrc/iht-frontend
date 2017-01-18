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

package iht.models.application.debts

import iht.utils.CommonHelper
import play.api.libs.json.Json

case class AllLiabilities(funeralExpenses: Option[BasicEstateElementLiabilities] = None,
                          trust: Option[BasicEstateElementLiabilities] = None,
                          debtsOutsideUk: Option[BasicEstateElementLiabilities] = None,
                          jointlyOwned: Option[BasicEstateElementLiabilities] = None,
                          other: Option[BasicEstateElementLiabilities] = None,
                          mortgages: Option[MortgageEstateElement] = None) {

  def areAllDebtsSectionsAnsweredNo =
    !Seq(funeralExpenses, trust, debtsOutsideUk, jointlyOwned, other).exists { x => x.flatMap(_.isOwned).fold(true)(identity) }

  def doesAnyDebtSectionHaveAValue =
    Seq(funeralExpenses, trust, debtsOutsideUk, jointlyOwned, other).flatten.flatMap(_.value).nonEmpty ||
      mortgages.flatMap(_.totalValue).isDefined

  def areAllDebtsExceptMortgagesCompleted =
    CommonHelper.aggregateOfSeqOfOption(
      Seq(funeralExpenses, trust, debtsOutsideUk, jointlyOwned, other).map (x => x.flatMap(_.isComplete))
    )

  def totalValue(): BigDecimal = funeralExpenses.flatMap(_.value).getOrElse(BigDecimal(0)) +
    trust.flatMap(_.value).getOrElse(BigDecimal(0)) +
    debtsOutsideUk.flatMap(_.value).getOrElse(BigDecimal(0)) +
    jointlyOwned.flatMap(_.value).getOrElse(BigDecimal(0)) +
    other.flatMap(_.value).getOrElse(BigDecimal(0)) +
    mortgageValue

  def isEmpty = {
    val sizeOfMortgageList = mortgages.map(_.mortgageList.size).fold(0)(identity)
    funeralExpenses.flatMap(_.value).isEmpty && trust.flatMap(_.value).isEmpty &&
      debtsOutsideUk.flatMap(_.value).isEmpty && jointlyOwned.flatMap(_.value).isEmpty &&
      other.flatMap(_.value).isEmpty && sizeOfMortgageList == 0
  }

  def mortgageValue: BigDecimal = {
    val mort = mortgages.getOrElse(new MortgageEstateElement(Some(false), Nil)).mortgageList

    mort match {
      case x: List[Mortgage] if x.nonEmpty => {
        x.flatMap(_.value).sum
      }
      case _ => BigDecimal(0)
    }
  }

}

object AllLiabilities {
  implicit val formats = Json.format[AllLiabilities]
}
