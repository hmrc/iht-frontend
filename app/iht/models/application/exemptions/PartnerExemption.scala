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

package iht.models.application.exemptions

import iht.utils.CommonHelper
import org.joda.time.LocalDate
import play.api.libs.json.Json

case class PartnerExemption(
                             isAssetForDeceasedPartner: Option[Boolean],
                             isPartnerHomeInUK: Option[Boolean],
                             firstName: Option[String],
                             lastName: Option[String],
                             dateOfBirth: Option[LocalDate],
                             nino: Option[String],
                             totalAssets: Option[BigDecimal]) {
  def name:Option[String] =
    if (firstName.isDefined && lastName.isDefined) {
      Some(CommonHelper.getOrException(firstName) + " " + CommonHelper.getOrException(lastName))
    } else {
      None
    }

  def isComplete: Option[Boolean] =
    isAssetForDeceasedPartner match {
      case Some(true) => isPartnerHomeInUK.fold(Some(false))(x => Some(x && firstName.isDefined &&
        lastName.isDefined && dateOfBirth.isDefined && nino.isDefined && totalAssets.isDefined))
      case Some(false) => Some(true)
      case _ => None
    }
}

object PartnerExemption {
  implicit val formats = Json.format[PartnerExemption]
}
