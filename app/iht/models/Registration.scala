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

package iht.models

import java.util.{List => JList}

import iht.constants.IhtProperties
import iht.utils.{StringHelper, ApplicationStatus => AppStatus}
import org.joda.time.LocalDate
import play.api.Logger
import play.api.libs.json.Json

/**
  * Created by yasar on 3/10/15.
  */

case class ApplicantDetails(firstName: Option[String] = None,
                            middleName: Option[String] = None,
                            lastName: Option[String] = None,
                            nino: Option[String] = None,
                            dateOfBirth: Option[LocalDate] = None,
                            ukAddress: Option[UkAddress] = None,
                            phoneNo: Option[String] = None,
                            country: Option[String] = None,
                            role: Option[String] = Some(ihtProperties.roleLeadExecutor),
                            doesLiveInUK: Option[Boolean] = None,
                            isApplyingForProbate: Option[Boolean] = None
                           ) {

  val name = firstName.getOrElse("") + " " + lastName.getOrElse("")

  def ninoFormatted = nino match {
    case Some(n) => StringHelper.ninoFormat(n)
    case None => ""
  }
}

object ApplicantDetails {
  implicit val formats = Json.format[ApplicantDetails]
}

case class DeceasedDetails(firstName: Option[String] = None,
                           middleName: Option[String] = None,
                           lastName: Option[String] = None,
                           nino: Option[String] = None,
                           ukAddress: Option[UkAddress] = None,
                           dateOfBirth: Option[LocalDate] = None,
                           domicile: Option[String] = None,
                           maritalStatus: Option[String] = None,
                           isAddressInUK: Option[Boolean] = None) {

  val toData = List(List("Name", firstName + " " + lastName, "Change this"), List("Date of Birth", dateOfBirth, "Change this"))
  val name = firstName.fold("")(identity) + " " + lastName.fold("")(identity)
  val dod = new LocalDate

  def isCompleted = firstName.isDefined && lastName.isDefined &&
    nino.isDefined && ukAddress.isDefined &&
    dateOfBirth.isDefined && domicile.isDefined && maritalStatus.isDefined

  def ninoFormatted = nino match {
    case Some(n) => StringHelper.ninoFormat(n)
    case None => ""
  }
}

object DeceasedDetails {
  implicit val formats = Json.format[DeceasedDetails]

}

case class DeceasedDateOfDeath(dateOfDeath: LocalDate)

object DeceasedDateOfDeath {
  implicit val formats = Json.format[DeceasedDateOfDeath]
}

//Classes for Add/Edit/Delete CoExecutors Starts

case class CoExecutor(id: Option[String] = None,
                      firstName: String,
                      middleName: Option[String] = None,
                      lastName: String,
                      dateOfBirth: LocalDate,
                      nino: String,
                      utr: Option[String] = None,
                      ukAddress: Option[UkAddress] = None,
                      contactDetails: ContactDetails,
                      role: Option[String] = Some(ihtProperties.roleExecutor),
                      isAddressInUk: Option[Boolean] = None) {


  val name = firstName + " " + lastName

  def updatePersonalDetails(coExec: CoExecutor): CoExecutor =
    CoExecutor(id, coExec.firstName, middleName, coExec.lastName, coExec.dateOfBirth, coExec.nino,
      utr, ukAddress, ContactDetails(coExec.contactDetails.phoneNo, contactDetails.email), role, isAddressInUk)

  def ninoFormatted = StringHelper.ninoFormat(nino)
}

object CoExecutor {
  implicit val formats = Json.format[CoExecutor]

}

//Classes for Add/Edit/Delete CoExecutors Ends

//Model for Return Details from E.T.M.P.

case class ReturnDetails(returnDate: Option[String],
                         returnId: Option[String],
                         returnVersionNumber: Option[String],
                         submitterRole: String)

object ReturnDetails {
  implicit val formats = Json.format[ReturnDetails]
}

// Should be in the bottom of the page due to object initialisation
case class RegistrationDetails(deceasedDateOfDeath: Option[DeceasedDateOfDeath],
                               applicantDetails: Option[ApplicantDetails],
                               deceasedDetails: Option[DeceasedDetails],
                               coExecutors: Seq[CoExecutor] = Seq(),
                               ihtReference: Option[String] = None,
                               status: String = AppStatus.AwaitingReturn,
                               acknowledgmentReference: String = "",
                               returns: Seq[ReturnDetails] = Seq(),
                               areOthersApplyingForProbate: Option[Boolean] = None) {

  def updatedReturnId: String = {
    if (returns.size == 1) {
      returns.seq.head.returnId.getOrElse("")
    } else {
      Logger.warn("Returns size is either zero or more than one " + returns)
      throw new RuntimeException("Returns must contain only one record ")
    }
  }
}

object RegistrationDetails {
  implicit val formats = Json.format[RegistrationDetails]
}

case class KickoutDetails(role: String, probateLocation: String)

object KickoutDetails {
  implicit val formats = Json.format[KickoutDetails]
}
