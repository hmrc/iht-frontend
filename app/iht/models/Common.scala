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

package iht.models

import iht.config.AppConfig
import iht.constants.IhtProperties
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, LocalDate}
import play.api.libs.json.Json
import uk.gov.hmrc.domain.TaxIds._
import uk.gov.hmrc.domain.{Nino, SerialisableTaxId, TaxIds}
import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._

/**
 * Created by yasar on 2/4/15.
 */

case class UkAddress(ukAddressLine1:String, ukAddressLine2:String,
                     ukAddressLine3:Option[String],ukAddressLine4:Option[String],
                     postCode:String, countryCode: String = "GB")

object UkAddress {
  implicit val formats = Json.format[UkAddress]

  def applyInternational(line1: String, line2: String, line3: Option[String], line4: Option[String], countryCode: Option[String]) =
    new UkAddress(line1, line2, line3, line4, "", countryCode.getOrElse("GB"))

  def unapplyInternational(address: UkAddress): Option[(String, String, Option[String], Option[String], Option[String])] =
    Some((address.ukAddressLine1, address.ukAddressLine2, address.ukAddressLine3, address.ukAddressLine4, Some(address.countryCode)))

  def applyUk(line1: String, line2: String, line3: Option[String], line4: Option[String], postCode: String)(implicit appConfig: AppConfig) =
    new UkAddress(line1, line2, line3, line4, postCode, appConfig.ukIsoCountryCode)

  def unapplyUk(address: UkAddress): Option[(String, String, Option[String], Option[String], String)] =
  Some((address.ukAddressLine1, address.ukAddressLine2, address.ukAddressLine3, address.ukAddressLine4, address.postCode))
}

case class ContactDetails(phoneNo: String, email: Option[String] = None)

object ContactDetails {
  implicit val formats = Json.format[ContactDetails]
}

case class Person(
                   firstName: Option[String],
                   middleName: Option[String],
                   lastName: Option[String],
                   initials: Option[String],
                   title: Option[String],
                   honours: Option[String],
                   sex: Option[String],
                   dateOfBirth: Option[DateTime],
                   nino: Option[Nino]
                   ) {

  lazy val shortName = for (f <- firstName; l <- lastName) yield List(f, l).mkString(" ")
  lazy val fullName = List(title, firstName, middleName, lastName, honours).flatten.mkString(" ")
}

object Person {
  implicit val formats = Json.format[Person]
}

object Joda {
  implicit def dateTimeOrdering: Ordering[LocalDate] = Ordering.fromLessThan(_ isAfter _)
}

case class QuestionnaireModel(feelingAboutExperience: Option[Int], easyToUse: Option[Int],
                              howCanYouImprove: Option[String], fullName: Option[String],
                              contactDetails: Option[String], stageInService: Option[String],
                              intendToReturn: Option[Boolean])

object TaxIdsFormat {
  private def ninoBuilder(value: String) = if (Nino.isValid(value)) Nino(value) else throw new InvalidNinoException(value: String)
  private val ninoSerialiser = SerialisableTaxId("nino", ninoBuilder)
  private val saUtrSerialiser = defaultSerialisableIds.filter(id => Set("sautr").contains(id.taxIdName))
  private val saUtrAndNinoSerialiser = saUtrSerialiser :+ ninoSerialiser
  val formattableTaxIds = format(saUtrAndNinoSerialiser: _*)
  val formattableTaxIdsWithNoNino = format(saUtrSerialiser: _*)
}

case class CidPerson(name: Option[CidNames], ids: TaxIds, dateOfBirth: Option[String]){

  def firstName: Option[String] = {
    for {
      cidNames <- name
      cidName <- cidNames.current
      firstName <- cidName.firstName
    } yield firstName
  }

  def lastName: Option[String] = {
    for {
      cidNames <- name
      cidName <- cidNames.current
      lastName <- cidName.lastName
    } yield lastName
  }

  def dateOfBirthLocalDate :Option[LocalDate]={
    val dateFormatter = DateTimeFormat.forPattern("ddMMyyyy")
    dateOfBirth.map( dob => LocalDate.parse(dob, dateFormatter))
  }

  def nino: Option[Nino] = {
    ids.nino
  }
}

object CidPerson {
  implicit val taxIdsFormat = TaxIdsFormat.formattableTaxIds
  implicit val cidNameformat = Json.format[CidName]
  implicit val cidNamesformat = Json.format[CidNames]
  implicit val format = Json.format[CidPerson]
}

case class CidNames(current: Option[CidName], previous:Option[List[CidName]])

case class CidName(firstName: Option[String], lastName: Option[String])

case class InvalidNinoException(value: String) extends Exception(s"Invalid NINO value: $value")
