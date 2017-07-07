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

import iht.connector.CachingConnector
import iht.models._
import iht.views.html._
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Request
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global._

object DeceasedInfoHelper {
  private val DateRangeMonths = 24
  def cachingConnector: CachingConnector = CachingConnector

  import uk.gov.hmrc.play.http.HeaderCarrier

  def determineStatusToUse(desStatus: String, secureStorageStatus: String): String = {
    desStatus match {
      case (ApplicationStatus.AwaitingReturn) => secureStorageStatus
      case (_) => desStatus
    }
  }

  val isThereADateOfDeath: Predicate = (rd, _) => rd.deceasedDateOfDeath.isDefined
  val isThereADeceasedDomicile: Predicate = (rd, _) => rd.deceasedDetails.flatMap(_.domicile).isDefined
  val isThereADeceasedFirstName: Predicate = (rd, _) => rd.deceasedDetails.flatMap(_.firstName).isDefined
  val isDeceasedAddressQuestionAnswered: Predicate = (rd, _) => rd.deceasedDetails.flatMap(_.isAddressInUK).isDefined
  val isThereADeceasedAddress: Predicate = (rd, _) => rd.deceasedDetails.flatMap(_.ukAddress).isDefined

  def getDeceasedNameOrDefaultString(regDetails: RegistrationDetails,
                                     wrapName: Boolean = false): String =
    if (wrapName) {
      ihtHelpers.custom.name(regDetails.deceasedDetails.fold(Messages("iht.the.deceased"))(_.name)).toString
    } else {
      regDetails.deceasedDetails.fold(Messages("iht.the.deceased"))(_.name)
    }

  def getDeceasedNameOrDefaultString(deceasedName: Option[String]): String = {
    deceasedName.fold(Messages("iht.the.deceased")) { identity }
  }
}
