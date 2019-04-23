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

package iht.models.des.ihtReturn

import models.des.Address
import org.joda.time.LocalDate
import play.api.libs.json.Json
import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._


case class Spouse( // Person
                   title: Option[String] = None,
                   firstName: Option[String] = None,
                   middleName: Option[String] = None,
                   lastName: Option[String] = None,
                   dateOfBirth: Option[LocalDate] = None,
                   gender: Option[String] = None,
                   nino: Option[String] = None,
                   utr: Option[String] = None,
                   mainAddress: Option[Address] = None,
                   // Other
                   dateOfMarriage: Option[LocalDate] = None,
                   dateOfDeath: Option[LocalDate] = None
                 )

object Spouse {
  implicit val formats = Json.format[Spouse]
}
