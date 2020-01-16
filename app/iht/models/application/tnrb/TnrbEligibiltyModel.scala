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

package iht.models.application.tnrb

import org.joda.time.LocalDate
import play.api.libs.json.{Json, OFormat}
import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._

case class TnrbEligibiltyModel(isPartnerLivingInUk: Option[Boolean],
                               isGiftMadeBeforeDeath: Option[Boolean],
                               isStateClaimAnyBusiness: Option[Boolean],
                               isPartnerGiftWithResToOther: Option[Boolean],
                               isPartnerBenFromTrust: Option[Boolean],
                               isEstateBelowIhtThresholdApplied: Option[Boolean],
                               isJointAssetPassed: Option[Boolean],
                               firstName: Option[String],
                               lastName: Option[String],
                               dateOfMarriage: Option[LocalDate],
                               dateOfPreDeceased: Option[LocalDate]) {

  def areAllQuestionsAnswered: Boolean = isPartnerLivingInUk.isDefined &&
    isGiftMadeBeforeDeath.isDefined &&
    isStateClaimAnyBusiness.isDefined &&
    isPartnerGiftWithResToOther.isDefined &&
    isPartnerBenFromTrust.isDefined &&
    isEstateBelowIhtThresholdApplied.isDefined &&
    isJointAssetPassed.isDefined &&
    firstName.isDefined &&
    lastName.isDefined &&
    dateOfMarriage.isDefined


  object Name {
    override def toString: String = firstName.getOrElse("") + " " + lastName.getOrElse("")
  }
}

object TnrbEligibiltyModel {
  implicit val formats: OFormat[TnrbEligibiltyModel] = Json.format[TnrbEligibiltyModel]
}
