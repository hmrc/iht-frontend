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

import iht.constants.FieldMappings
import iht.models.{ApplicantDetails, UkAddress}
import iht.utils.IhtFormValidator
import iht.utils.IhtFormValidator._
import play.api.data.{Form, Mapping}
import play.api.data.Forms._
import play.api.i18n.{Lang, Messages}

object ApplicantForms {

  val applyingForProbateForm = Form(
    mapping(
      "isApplyingForProbate" -> yesNoQuestion("error.applicantIsApplyingForProbate.select")
    )
    (
      (isApplyingForProbate) => ApplicantDetails(isApplyingForProbate = isApplyingForProbate)
    )
    (
      (a: ApplicantDetails) => Some(a.isApplyingForProbate)
    )
  )

  val areYouAnExecutorForm = Form(
    mapping(
      "areYouAnExecutor" -> yesNoQuestion("error.applicantAreYouAnExecutor.select")
    )
    (
      (isAnExecutor) => ApplicantDetails(isAnExecutor = isAnExecutor)
    )
    (
      (a: ApplicantDetails) => Some(a.isAnExecutor)
    )
  )

  def probateLocationForm(implicit messages: Messages) = Form(
    mapping(
      "country" -> of(radioOptionString("error.applicantProbateLocation.select", FieldMappings.applicantCountryMap))
    )
    (
      (country) => ApplicantDetails(country = country)
    )
    (
      (applicantDetails: ApplicantDetails) => Some(applicantDetails.country)
    )
  )

  val applicantTellUsAboutYourselfForm = Form(
    mapping(
      "phoneNo" -> phoneNumberOptionString("error.phoneNumber.give",
        "error.phoneNumber.giveUsing27CharactersOrLess", "error.phoneNumber.giveUsingOnlyLettersAndNumbers"),
      "doesLiveInUK" -> yesNoQuestion("error.address.isInUK.give")
    )
    (
      (phoneNo, doesLiveInUK) => ApplicantDetails(None, None, None, None, None, None, phoneNo, None, None,
        doesLiveInUK, None)
    )
    (
      (applicantDetails: ApplicantDetails) => Some((applicantDetails.phoneNo, applicantDetails.doesLiveInUK))
    )
  )

  val applicantTellUsAboutYourselfEditForm = Form(
    mapping(
      "phoneNo" -> phoneNumberOptionString("error.phoneNumber.give",
        "error.phoneNumber.giveUsing27CharactersOrLess", "error.phoneNumber.giveUsingOnlyLettersAndNumbers")
    )
    (
      (phoneNo) => ApplicantDetails(None, None, None, None, None, None, phoneNo, None, None, None, None)
    )
    (
      (applicantDetails: ApplicantDetails) => Some(applicantDetails.phoneNo)
    )
  )

  def addressMappingInternational(lang: Lang, messages: Messages): Mapping[UkAddress] = mapping(
    "ukAddressLine1" -> of(ihtInternationalAddress("ukAddressLine2", "ukAddressLine3",
      "ukAddressLine4", "countryCode",
      "error.address.give", "error.address.giveInLine1And2",
      "error.address.giveUsing35CharsOrLess",
      "error.address.giveUsingOnlyValidChars",
      "error.country.select")(lang, messages)),
    "ukAddressLine2" -> text,
    "ukAddressLine3" -> optional(text),
    "ukAddressLine4" -> optional(text),
    "countryCode" -> optional(text)
  )(UkAddress.applyInternational)(UkAddress.unapplyInternational)

  val addressMappingUk = mapping(
    "ukAddressLine1" -> of(ihtAddress("ukAddressLine2", "ukAddressLine3",
      "ukAddressLine4", "postCode", "countryCode",
      "error.address.give", "error.address.giveInLine1And2",
      "error.address.giveUsing35CharsOrLess", "error.address.giveUsingOnlyValidChars",
      "error.address.givePostcode", "error.address.givePostcodeUsingNumbersAndLetters",
      "error.country.select")),
    "ukAddressLine2" -> text,
    "ukAddressLine3" -> optional(text),
    "ukAddressLine4" -> optional(text),
    "postCode" -> text
  )(UkAddress.applyUk)(UkAddress.unapplyUk)

  val applicantAddressUkForm = Form(addressMappingUk)
  def applicantAddressAbroadForm(implicit lang: Lang, messages: Messages) = Form(addressMappingInternational(lang, messages))

}
