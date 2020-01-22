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

package iht.models.application.gifts

import play.api.libs.json.Json



case class AllGifts(
                     isGivenAway: Option[Boolean] = None,
                     isReservation: Option[Boolean],
                     isToTrust: Option[Boolean],
                     isGivenInLast7Years: Option[Boolean],
                     action: Option[String]){

  def isGiftsSectionCompletedWithNoValue = (isGivenAway,isReservation,isToTrust,isGivenInLast7Years) match{

    case (Some(false),Some(false),Some(false),Some(false)) => true
    case _ => false
  }

  def isStarted: Boolean = isGivenAway.isDefined
}

object AllGifts {
  implicit val formats = Json.format[AllGifts]
}
