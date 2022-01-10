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

package iht.utils

import iht.config.AppConfig
import iht.models._
import iht.views.html.ihtHelpers
import play.api.i18n.Messages

object DeceasedInfoHelper {

  def determineStatusToUse(desStatus: String, secureStorageStatus: String): String = {
    desStatus match {
      case ApplicationStatus.AwaitingReturn => secureStorageStatus
      case _                                => desStatus
    }
  }

  val isThereADateOfDeath: Predicate = (rd, _) => rd.deceasedDateOfDeath.isDefined
  val isThereADeceasedDomicile: Predicate = (rd, _) => rd.deceasedDetails.flatMap(_.domicile).isDefined
  val isThereADeceasedFirstName: Predicate = (rd, _) => rd.deceasedDetails.flatMap(_.firstName).isDefined
  val isDeceasedAddressQuestionAnswered: Predicate = (rd, _) => rd.deceasedDetails.flatMap(_.isAddressInUK).isDefined
  val isThereADeceasedAddress: Predicate = (rd, _) => rd.deceasedDetails.flatMap(_.ukAddress).isDefined

  def getDeceasedNameOrDefaultString(regDetails: RegistrationDetails, wrapName: Boolean = false)
                                    (implicit messages: Messages, appConfig: AppConfig): String = {
    val nameView = new ihtHelpers.custom.name()
    if (wrapName) {
      nameView(regDetails.deceasedDetails.fold(messages("iht.the.deceased"))(_.name)).toString
    } else {
      regDetails.deceasedDetails.fold(messages("iht.the.deceased"))(_.name)
    }
  }

  def getDeceasedNameOrDefaultString(deceasedName: Option[String])(implicit messages: Messages): String = {
    deceasedName.fold(messages("iht.the.deceased")) { identity }
  }
}
