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

package iht.forms.registration

import iht.constants.{FieldMappings, IhtProperties}
import iht.forms.mappings.DateMapping
import iht.models.{DeceasedDateOfDeath, DeceasedDetails, RegistrationDetails, UkAddress}
import iht.utils.IhtFormValidator
import iht.utils.IhtFormValidator._
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc.Request

import scala.concurrent.ExecutionContext
import uk.gov.hmrc.http.HeaderCarrier

object DeceasedForms extends DeceasedForms

trait DeceasedForms {

  def ihtFormValidator: IhtFormValidator = IhtFormValidator

  val deceasedDateOfDeathForm = Form(
    mapping(
      "dateOfDeath" -> DateMapping.dateOfDeath
    )(DeceasedDateOfDeath.apply)(DeceasedDateOfDeath.unapply)
  )

  def deceasedPermanentHomeForm(implicit messages: Messages): Form[DeceasedDetails] = Form(
    mapping(
      "domicile" -> of(radioOptionString("error.deceasedPermanentHome.selectLocation", FieldMappings.domicileMap))
    )
    (
      (domicile) => DeceasedDetails(None, None, None, None, None, None, domicile, None, None)
    )
    (
      (deceasedDetails: DeceasedDetails) => Some(deceasedDetails.domicile)
    )
  )

  def aboutDeceasedForm(dateOfDeath: LocalDate = LocalDate.now(), oRegDetails: Option[RegistrationDetails] = None)
                       (implicit messages: Messages, request: Request[_], hc: HeaderCarrier, ec: ExecutionContext) = Form(
    mapping(
      "firstName" -> ihtNonEmptyText("error.firstName.give")
        .verifying("error.firstName.giveUsingXCharsOrLess", f => f.length <= IhtProperties.validationMaxLengthFirstName)
        .verifying("error.firstName.giveUsingOnlyValidChars", f => nameAndAddressRegex.findFirstIn(f).fold(false)(_=>true)),
      "lastName" -> ihtNonEmptyText("error.lastName.give")
        .verifying("error.lastName.giveUsingXCharsOrLess", f => f.length <= IhtProperties.validationMaxLengthLastName)
        .verifying("error.lastName.giveUsingOnlyValidChars", f => nameAndAddressRegex.findFirstIn(f).fold(false)(_=>true)),
      "nino" -> ihtFormValidator.ninoForDeceased(
        "error.nino.give","error.nino.giveUsing8Or9Characters","error.nino.giveUsingOnlyLettersAndNumbers", oRegDetails),
      "dateOfBirth" -> DateMapping.dateOfBirth.verifying("error.deceasedDateOfBirth.giveBeforeDateOfDeath", x => isDobBeforeDod(dateOfDeath, x)),
      "maritalStatus" -> of(radioOptionString("error.deceasedMaritalStatus.select", FieldMappings.maritalStatusMap(messages))))
    (
      (firstName, lastName, nino, dateOfBirth, maritalStatus) =>
        DeceasedDetails(Some(firstName), None, Some(lastName), Some(nino), None, Some(dateOfBirth), None, maritalStatus, None)
    )(
      (deceasedDetails: DeceasedDetails) => {
        Some((deceasedDetails.firstName.getOrElse(""),
          deceasedDetails.lastName.getOrElse(""),
          deceasedDetails.nino.getOrElse(""),
          deceasedDetails.dateOfBirth.orNull,
          deceasedDetails.maritalStatus))
      }
    )
  )

  val deceasedAddressQuestionForm = Form(
    mapping(
      "isAddressInUk" -> yesNoQuestion("error.address.wasInUK.give")
    )
    (
      (isAddressInUk) => DeceasedDetails(None, None, None, None, None, None, None, None, isAddressInUk)
    )
    (
      (deceasedDetails: DeceasedDetails) => Some(deceasedDetails.isAddressInUK)
    )
  )

  val deceasedAddressDetailsUKForm = Form(
    mapping(
      "ukAddress.ukAddressLine1" -> of(ihtAddress(
        "ukAddress.ukAddressLine2", "ukAddress.ukAddressLine3",
        "ukAddress.ukAddressLine4", "ukAddress.postCode", "ukAddress.countryCode",
        "error.address.give", "error.address.giveInLine1And2",
        "error.address.giveUsing35CharsOrLess", "error.address.giveUsingOnlyValidChars",
        "error.address.givePostcode",
        "error.address.givePostcodeUsingNumbersAndLetters", "error.country.select"
      ))  ,
      "ukAddress.ukAddressLine2" -> text,
      "ukAddress.ukAddressLine3" -> optional(text),
      "ukAddress.ukAddressLine4" -> optional(text),
      "ukAddress.postCode" -> text,
      "ukAddress.countryCode" -> default(text, "GB")
    )
    (
      (addressLine1, addressLine2, addressLine3, addressLine4, postCode, countryCode) => DeceasedDetails(None, None, None, None,
        Some(UkAddress(addressLine1, addressLine2, addressLine3, addressLine4, postCode, countryCode)), None, None, None, None)
    )
    (
      (deceasedDetails: DeceasedDetails) => Option(Tuple6(
        deceasedDetails.ukAddress.map(_.ukAddressLine1).getOrElse(""),
        deceasedDetails.ukAddress.map(_.ukAddressLine2).getOrElse(""),
        deceasedDetails.ukAddress.map(_.ukAddressLine3).getOrElse(Some("")),
        deceasedDetails.ukAddress.map(_.ukAddressLine4).getOrElse(Some("")),
        deceasedDetails.ukAddress.map(_.postCode).getOrElse(""),
        deceasedDetails.ukAddress.map(_.countryCode).getOrElse("GB"))
      )
    )
  )

  private lazy val addressRegex = """^[A-Za-z0-9,. \(\)\&\-']*$""".r

  val deceasedAddressDetailsOutsideUKForm = Form(
    mapping(
      "ukAddress.ukAddressLine1" -> ihtNonEmptyText("error.address.give")
        .verifying("error.address.giveUsing35CharsOrLess", x => x.trim.length < 36)
        .verifying("error.address.giveUsingOnlyValidChars", f => addressRegex.findFirstIn(f).fold(false)(_=>true)),
      "ukAddress.ukAddressLine2" -> ihtNonEmptyText("error.address.give")
        .verifying("error.address.giveUsing35CharsOrLess", x => x.trim.length < 36)
        .verifying("error.address.giveUsingOnlyValidChars", f => addressRegex.findFirstIn(f).fold(false)(_=>true)),
      "ukAddress.ukAddressLine3" -> optional(text)
        .verifying("error.address.giveUsing35CharsOrLess", x => x.getOrElse("").trim.length < 36)
        .verifying("error.address.giveUsingOnlyValidChars", f => f.fold(true)(xx => addressRegex.findFirstIn(xx).fold(false)(_=>true))),
      "ukAddress.ukAddressLine4" -> optional(text)
        .verifying("error.address.giveUsing35CharsOrLess", x => x.getOrElse("").trim.length < 36)
        .verifying("error.address.giveUsingOnlyValidChars", f => f.fold(true)(xx => addressRegex.findFirstIn(xx).fold(false)(_=>true))),
      "ukAddress.countryCode" -> ihtNonEmptyText("error.country.select")
    )
    (
      (addressLine1, addressLine2, addressLine3, addressLine4, countryCode) => DeceasedDetails(None, None, None, None,
        Some(UkAddress(addressLine1, addressLine2, addressLine3, addressLine4, "", countryCode)), None, None, None, None)
    )
    (
      (deceasedDetails: DeceasedDetails) => Option(Tuple5(
        deceasedDetails.ukAddress.map(_.ukAddressLine1).getOrElse(""),
        deceasedDetails.ukAddress.map(_.ukAddressLine2).getOrElse(""),
        deceasedDetails.ukAddress.map(_.ukAddressLine3).getOrElse(Some("")),
        deceasedDetails.ukAddress.map(_.ukAddressLine4).getOrElse(Some("")),
        deceasedDetails.ukAddress.map(_.countryCode).getOrElse(""))
      )
    )
  )
}
