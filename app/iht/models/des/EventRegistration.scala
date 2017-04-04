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

package models.des

import iht.models.{RegistrationDetails, UkAddress}
import iht.utils.CommonHelper._
import org.joda.time.LocalDate
import play.api.libs.json.Json

case class Address(
                    addressLine1:String,
                    addressLine2:String,
                    addressLine3:Option[String],
                    addressLine4:Option[String],
                    postalCode:String,
                    countryCode:String) {
}

object Address {
  implicit val formats = Json.format[Address]
}

case class ContactDetails( phoneNumber:Option[String],
                           mobileNumber:Option[String],
                           faxNumber:Option[String],
                           emailAddress:Option[String],
                           dxNumber:Option[String])

object ContactDetails {
  implicit val formats = Json.format[ContactDetails]
}

case class CoExecutor( firstName:Option[String],
                       lastName:Option[String],
                       nino:Option[String],
                       utr:Option[String],
                       dateOfBirth:Option[String],
                       mainAddress:Option[Address],
                       contactDetails:Option[ContactDetails])

object CoExecutor {
  implicit val formats = Json.format[CoExecutor]
}

case class LeadExecutor( firstName:String,
                         lastName:String,
                         nino:Option[String],
                         dateOfBirth:String,
                         mainAddress:Address,
                         probateLocation:Option[String],
                         contactDetails:Option[ContactDetails])

object LeadExecutor {
  implicit val formats = Json.format[LeadExecutor]
}

case class Deceased( title:Option[String],
                     firstName:String,
                     middleName:Option[String],
                     lastName:String,
                     dateOfBirth:String,
                     gender:Option[String],
                     nino:Option[String],
                     mainAddress:Address,
                     dateOfDeath:String,
                     domicile:String,
                     otherDomicile:Option[String],
                     occupation:Option[String],
                     maritalStatus:String)

object Deceased {
  implicit val formats = Json.format[Deceased]
}

case class Event(eventType:String, entryType:String)

object Event {
  implicit val formats = Json.format[Event]
}

case class EventRegistration( acknowledgmentReference:Option[String],
                              event:Option[Event],
                              leadExecutor:Option[LeadExecutor],
                              coExecutors:Option[Seq[CoExecutor]],
                              deceased:Option[Deceased]) {
}

object EventRegistration {

  /**
   * Convert front-end model into event registration (DES) model.
   * All fields are options of strings, since the sole raison d'etre
   * of this model is the production of JSON formatted output.
   * @param rd Front-end model.
   * @return Event registration model.
   */
  def fromRegistrationDetails(rd:RegistrationDetails) :EventRegistration = {
    lazy val applicantDetails = getOrException(rd.applicantDetails)
    lazy val deceasedDetails = getOrException(rd.deceasedDetails)
    EventRegistration(acknowledgmentReference = Some(rd.acknowledgmentReference),
      event=Some(Event("death", "Free Estate")),
      leadExecutor=Some(LeadExecutor(firstName=getOrException(applicantDetails.firstName),
        lastName=getOrException(applicantDetails.lastName),
        nino=Some(getOrException(applicantDetails.nino).toUpperCase.replaceAll(" ", "")),
        dateOfBirth=fromRegistrationDetailsDateToString(getOrException(applicantDetails.dateOfBirth)),
        mainAddress=fromRegistrationDetailsUKAddressToAddress(getOrException(applicantDetails.ukAddress)),
        probateLocation=applicantDetails.country,
        contactDetails=Some(ContactDetails(applicantDetails.phoneNo,None,None,None,None))
      )),
      coExecutors=fromRegistrationDetailsCoExecutors(rd),
      deceased=Some(Deceased(title=None,
        firstName = getOrException(deceasedDetails.firstName),
        middleName=deceasedDetails.middleName,
        lastName = getOrException(deceasedDetails.lastName),
        dateOfBirth=fromRegistrationDetailsDateToString(getOrException(deceasedDetails.dateOfBirth)),
        gender=None,
        nino=Some(getOrException(deceasedDetails.nino).toUpperCase.replaceAll(" ", "")),
        mainAddress=fromRegistrationDetailsUKAddressToAddress(getOrException(deceasedDetails.ukAddress)),
        dateOfDeath=fromRegistrationDetailsDateToString(getOrException(rd.deceasedDateOfDeath).dateOfDeath),
        domicile=getOrException(deceasedDetails.domicile),
        otherDomicile=None,
        occupation=None,
        maritalStatus=getOrException(deceasedDetails.maritalStatus))))
  }

  private def fromRegistrationDetailsCoExecutors(rd:RegistrationDetails) : Option[Seq[CoExecutor]] = {
    val t = for(e<-rd.coExecutors) yield
    CoExecutor(Some(e.firstName),
      Some(e.lastName),
      Some(e.nino.toUpperCase.replaceAll(" ", "")),
      e.utr,
      Some(fromRegistrationDetailsDateToString(e.dateOfBirth)),
      Some(fromRegistrationDetailsUKAddressToAddress(e.ukAddress.getOrElse(UkAddress("", "", None, None, "")))),
      Some(fromRegistrationDetailsContactDetailsToContactDetails(e.contactDetails)))
    if (t.size==0) None else Some(t)
  }

  private def fromRegistrationDetailsDateToString( ld: LocalDate ) = {
    ld.toString("YYYY-MM-dd")
  }

  private def fromRegistrationDetailsUKAddressToAddress( ua: UkAddress ): Address =
    Address(ua.ukAddressLine1,
      ua.ukAddressLine2,
      ua.ukAddressLine3,
      ua.ukAddressLine4,
      ua.postCode,
      ua.countryCode)

  private def fromRegistrationDetailsContactDetailsToContactDetails( cd: iht.models.ContactDetails ) =
    models.des.ContactDetails(Some(cd.phoneNo), None, None, cd.email, None)

  implicit val formats = Json.format[EventRegistration]
}
