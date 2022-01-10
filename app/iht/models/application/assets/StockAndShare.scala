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

package iht.models.application.assets

import iht.models.application.basicElements.EstateElement
import play.api.libs.json.Json


case class StockAndShare(valueNotListed: Option[BigDecimal],
                         valueListed: Option[BigDecimal],
                         value: Option[BigDecimal],
                         isNotListed: Option[Boolean]= None,
                         isListed: Option[Boolean] = None) extends EstateElement{

  override def totalValue: Option[BigDecimal]={
    (valueListed,valueNotListed) match{
      case(None,None) => None
      case(a,b) => Some(a.getOrElse(BigDecimal(0)) + b.getOrElse(BigDecimal(0)))
    }
  }

  override def isValueEntered: Boolean = {
    (valueNotListed, valueListed) match{
      case(None,None) => false
      case _ => true
    }
  }

  def isComplete: Option[Boolean] =
    (isListed, valueListed, isNotListed, valueNotListed) match {
      case (None, None, None, None) => None
      case (Some(true), Some(_), Some(true), Some(_)) => Some(true)
      case (Some(false), _, Some(false), _) => Some(true)
      case (Some(true), Some(_), Some(false), _) => Some(true)
      case (Some(false), _, Some(true), Some(_)) => Some(true)
      case _ => Some(false)
    }

}

object StockAndShare {
  implicit val formats = Json.format[StockAndShare]
}
