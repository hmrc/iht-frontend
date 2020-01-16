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

import play.api.libs.json.Json

case class AllExemptions(
                          partner: Option[PartnerExemption] = None,
                          charity: Option[BasicExemptionElement] = None,
                          qualifyingBody: Option[BasicExemptionElement] = None) {

  def isExemptionsSectionCompletedWithNoValue = (partner, charity, qualifyingBody) match {
    case (Some(PartnerExemption(Some(false), _, _, _, _, _, _)),
    Some(BasicExemptionElement(Some(false))), Some(BasicExemptionElement(Some(false)))) => true
    case _ => false
  }

  def isExemptionsSectionCompletedWithoutPartnerExemptionWithNoValue = (charity, qualifyingBody) match {
    case (Some(BasicExemptionElement(Some(false))), Some(BasicExemptionElement(Some(false)))) => true
    case _ => false
  }
}

object AllExemptions {
  implicit val formats = Json.format[AllExemptions]
}
