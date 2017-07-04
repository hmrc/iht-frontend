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

package iht.forms.registration

import iht.constants._
import iht.forms.mappings.DateMapping
import iht.models._
import iht.utils.IhtFormValidator
import iht.utils.IhtFormValidator._
import play.api.data.Forms._
import play.api.data.{Form, Mapping}
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

object CoExecutorForms extends CoExecutorForms

trait CoExecutorForms {
  def ihtFormValidator: IhtFormValidator = IhtFormValidator
  val addressMappingCoexecInternational: Mapping[UkAddress] = mapping(
    "ukAddressLine1" -> of(ihtInternationalAddress("ukAddressLine2", "ukAddressLine3",
      "ukAddressLine4", "countryCode",
      "error.address.give", "error.address.giveInLine1And2",
      "error.address.giveUsing35CharsOrLess",
      "error.country.select")),
    "ukAddressLine2" -> text,
    "ukAddressLine3" -> optional(text),
    "ukAddressLine4" -> optional(text),
    "countryCode" -> optional(text)
  )(UkAddress.applyInternational)(UkAddress.unapplyInternational)

  val addressMappingCoexecUk = mapping(
    "ukAddressLine1" -> of(ihtAddress("ukAddressLine2", "ukAddressLine3",
      "ukAddressLine4", "postCode", "countryCode",
      "error.address.give", "error.address.giveInLine1And2",
      "error.address.giveUsing35CharsOrLess", "error.address.givePostcode",
      "error.address.givePostcodeUsingNumbersAndLetters", "error.country.select")),
    "ukAddressLine2" -> text,
    "ukAddressLine3" -> optional(text),
    "ukAddressLine4" -> optional(text),
    "postCode" -> text
  )(UkAddress.applyUk)(UkAddress.unapplyUk)

  val coExecutorAddressUkForm = Form(addressMappingCoexecUk)
  val coExecutorAddressAbroadForm = Form(addressMappingCoexecInternational)

  def coExecutorPersonalDetailsForm(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext) = Form(
    mapping(
      "id" -> optional(text),
      "firstName" -> ihtNonEmptyText("error.firstName.give")
        .verifying("error.firstName.giveUsingXCharsOrLess",
          f => f.length <= IhtProperties.validationMaxLengthFirstName),
      "lastName" -> ihtNonEmptyText("error.lastName.give")
        .verifying("error.lastName.giveUsingXCharsOrLess",
          f => f.length <= IhtProperties.validationMaxLengthLastName),
      "dateOfBirth" -> DateMapping(
        "error.dateOfBirth.giveFull",
        "error.dateOfBirth.giveCorrectDateUsingOnlyNumbers",
        "error.dateOfBirth.giveCorrectDay",
        "error.dateOfBirth.giveCorrectDayForMonth",
        "error.dateOfBirth.giveCorrectMonth",
        "error.dateOfBirth.giveCorrectYear",
        "error.dateOfBirth.giveFull",
        "error.dateOfBirth.giveNoneFuture",
        "error.dateOfBirth.giveCorrectDayMonth",
        "error.dateOfBirth.giveCorrectDayYear",
        "error.dateOfBirth.giveCorrectMonthYear"
      ),
      "nino" -> ihtFormValidator.ninoForCoExecutor(
        "error.nino.give","error.nino.giveUsing8Or9Characters","error.nino.giveUsingOnlyLettersAndNumbers",
        "id"),
      "phoneNo" -> mandatoryPhoneNumber(
        "error.phoneNumber.give",
        "error.phoneNumber.giveUsing27CharactersOrLess",
        "error.phoneNumber.giveUsingOnlyLettersAndNumbers"
      ),
      "isAddressInUk" -> yesNoQuestion("error.address.isInUK.give")
    )
    (
      (id, firstName, lastName, dateOfBirth, nino, phoneNo, isAddressInUk) =>
        CoExecutor(id, firstName, None, lastName, dateOfBirth, nino, None, None, ContactDetails(phoneNo), None,
          isAddressInUk)
    )
    (
      (c: CoExecutor) => Some(Tuple7(c.id, c.firstName, c.lastName, c.dateOfBirth, c.nino, c.contactDetails.phoneNo,
        c.isAddressInUk))
    )
  )

  def coExecutorPersonalDetailsEditForm(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext) = Form(
    mapping(
      "id" -> optional(text),
      "firstName" -> ihtNonEmptyText("error.firstName.give")
        .verifying("error.firstName.giveUsingXCharsOrLess",
          f => f.length <= IhtProperties.validationMaxLengthFirstName),
      "lastName" -> ihtNonEmptyText("error.lastName.give")
        .verifying("error.lastName.giveUsingXCharsOrLess",
          f => f.length <= IhtProperties.validationMaxLengthLastName),
      "dateOfBirth" -> DateMapping(
        "error.dateOfBirth.giveFull",
        "error.dateOfBirth.giveCorrectDateUsingOnlyNumbers",
        "error.dateOfBirth.giveCorrectDay",
        "error.dateOfBirth.giveCorrectDayForMonth",
        "error.dateOfBirth.giveCorrectMonth",
        "error.dateOfBirth.giveCorrectYear",
        "error.dateOfBirth.giveFull",
        "error.dateOfBirth.giveNoneFuture",
        "error.dateOfBirth.giveCorrectDayMonth",
        "error.dateOfBirth.giveCorrectDayYear",
        "error.dateOfBirth.giveCorrectMonthYear"
      ),
      "nino" -> ihtFormValidator.ninoForCoExecutor(
        "error.nino.give","error.nino.giveUsing8Or9Characters","error.nino.giveUsingOnlyLettersAndNumbers",
        "id"),
      "phoneNo" -> mandatoryPhoneNumber(
        "error.phoneNumber.give",
        "error.phoneNumber.giveUsing27CharactersOrLess",
        "error.phoneNumber.giveUsingOnlyLettersAndNumbers"
      )
    )
    (
      (id, firstName, lastName, dateOfBirth, nino, phoneNo) =>
        CoExecutor(id, firstName, None, lastName, dateOfBirth, nino, None, None, ContactDetails(phoneNo), None, None)
    )
    (
      (c: CoExecutor) => Some(Tuple6(c.id, c.firstName, c.lastName, c.dateOfBirth, c.nino, c.contactDetails.phoneNo))
    )
  )

  val othersApplyingForProbateForm = Form(
    single(
      "areOthersApplyingForProbate" -> yesNoQuestion("error.applicant.selectIfAnyoneElseApplyingForProbate")
    )
  )

  val executorOverviewForm = Form(
    single(
      "addMoreCoExecutors" -> yesNoQuestion("error.applicant.selectIfAnyoneElseApplyingForProbate")
    )
  )

  val deleteConfirmationForm = Form(
    single(
      "hidden" -> optional(text)
    )
  )
}
