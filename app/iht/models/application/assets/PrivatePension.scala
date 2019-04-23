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

package iht.models.application.assets

import iht.models.application.basicElements.EstateElement
import play.api.libs.json.Json


case class PrivatePension(isChanged: Option[Boolean],
                          value: Option[BigDecimal],
                          isOwned: Option[Boolean] = None) extends EstateElement {
  def isComplete: Option[Boolean] =
    (isOwned, isChanged, value) match {
      case (None, None, None) => None
      case (Some(true), Some(false), Some(_)) => Some(true)
      case (Some(true), Some(true), _) => Some(true)
      case (Some(false), _, _) => Some(true)
      case _ => Some(false)
    }
}

object PrivatePension {
  implicit val formats = Json.format[PrivatePension]
}
