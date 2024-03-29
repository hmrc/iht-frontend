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
import iht.models.RegistrationDetails
import play.api.data.format.Formatter
import play.api.data.{FieldMapping, FormError, Forms}
import play.api.i18n.Messages

import scala.collection.immutable.ListMap
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

object IhtFormValidator extends IhtFormValidator

trait IhtFormValidator extends FormValidator {

  def getGiftValueOrErrors(errors: ListBuffer[FormError], data: Map[String, String], key: String): Either[List[FormError], Option[String]] = {
    if (errors.isEmpty) {
      try {
        data.get(key) match {
          case Some(value) => Right(Some(value))
          case None => Right(None)
        }
      } catch {
        case _: IllegalArgumentException => Left(List(FormError(key, "error.invalid")))
      }
    } else {
      Left(errors.toList)
    }
  }

  def validateGiftsDetails(valueKey: String, exemptionsValueKey: String) = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]) = {
      import scala.util.Try
      val errors = new scala.collection.mutable.ListBuffer[FormError]()
      val value: String = toCurrency(data.get(valueKey)).replace(",", "").trim
      val exemptionsValue: String = toCurrency(data.get(exemptionsValueKey)).replace(",", "").trim
      val theValue: Try[BigDecimal] = Try(BigDecimal(cleanMoneyString(value)))
      val theExemptionsValue: Try[BigDecimal] = Try(BigDecimal(cleanMoneyString(exemptionsValue)))
      (theValue, theExemptionsValue) match {
        case (Success(v), Success(ev)) if v < ev =>
          errors += FormError(exemptionsValueKey, "error.giftsDetails.exceedsGivenAway")
        case (_, Success(_)) if value.isEmpty => errors += FormError(valueKey, "error.giftsDetails.noValue")
        case _ => ()
      }
      getGiftValueOrErrors(errors, data, key)
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] = {
      Map(key -> value.map(_.toString).getOrElse(""))
    }
  }

  def validateBasicEstateElementLiabilities(currencyValueKey: String) = new Formatter[Option[Boolean]] {
    override def bind(key: String, data: Map[String, String]) = {
      val errors = new scala.collection.mutable.ListBuffer[FormError]()
      val currencyValue = toCurrency(data.get(currencyValueKey))

      if (currencyValue == "") {
        errors += FormError(currencyValueKey, "error.value.blank")
      }

      if (errors.isEmpty) {
        try {
          data.get(key) match {
            case Some(value) => Right(Some(value.toBoolean))
            case None => Right(None)
          }
        } catch {
          case _: IllegalArgumentException => Left(List(FormError(key, "error.invalid")))
        }
      } else {
        Left(errors.toList)
      }
    }

    override def unbind(key: String, value: Option[Boolean]): Map[String, String] = {
      Map(key -> value.map(_.toString).getOrElse(""))
    }
  }

  def validateDeclaration = new Formatter[Boolean] {
    override def bind(key: String, data: Map[String, String]) = {
      val errors = new scala.collection.mutable.ListBuffer[FormError]()
      val isDeclared = toBoolean(data.get(key))

      if (!isDeclared.getOrElse(false)) {
        errors += FormError(key, "error.declaration.unconfirmed")
      }

      if (errors.isEmpty) {
        try {
          data.get(key) match {
            case Some(value) => Right(value.toBoolean)
            case None => Right(false)
          }
        } catch {
          case _: IllegalArgumentException => Left(List(FormError(key, "error.invalid")))
        }
      } else {
        Left(errors.toList)
      }
    }

    override def unbind(key: String, value: Boolean): Map[String, String] = {
      Map(key -> value.toString)
    }
  }

  private def getAddrDetails(data: Map[String, String],
                             addr1Key: String,
                             addr2Key: String,
                             addr3Key: String,
                             addr4Key: String,
                             postcodeKey: String,
                             countryCodeKey: String) = {
    (data.getOrElse(addr1Key, ""),
      data.getOrElse(addr2Key, ""),
      data.getOrElse(addr3Key, ""),
      data.getOrElse(addr4Key, ""),
      data.getOrElse(postcodeKey, ""),
      data.getOrElse(countryCodeKey, ""))
  }

  private def getIntlAddrDetails(data: Map[String, String],
                                 addr1Key: String,
                                 addr2Key: String,
                                 addr3Key: String,
                                 addr4Key: String,
                                 countryCodeKey: String) = {
    (data.getOrElse(addr1Key, ""),
      data.getOrElse(addr2Key, ""),
      data.getOrElse(addr3Key, ""),
      data.getOrElse(addr4Key, ""),
      data.getOrElse(countryCodeKey, ""))
  }

  private def validateOptionalAddressLine(addrKey: String, addr: String, maxLength: Int,
                                          invalidAddressLineMessageKey: String,
                                          invalidChars: String,
                                          errors: scala.collection.mutable.ListBuffer[FormError]): Unit = {
    addr match {
      case a if a.length > maxLength => errors += FormError(addrKey, invalidAddressLineMessageKey)
      case a if nameAndAddressRegex.findFirstIn(a).fold(true)(_ => false) =>
        errors += FormError(addrKey, invalidChars)
      case _ =>
    }
  }

  private def validateMandatoryAddressLine(addrKey: String, addr: String, maxLength: Int,
                                           blankMessageKey: String,
                                           invalidAddressLineMessageKey: String,
                                           invalidChars: String,
                                           errors: scala.collection.mutable.ListBuffer[FormError]): Unit = {
    addr match {
      case a if a.length == 0 => errors += FormError(addrKey, blankMessageKey)
      case a if a.length > maxLength => errors += FormError(addrKey, invalidAddressLineMessageKey)
      case a if nameAndAddressRegex.findFirstIn(a).fold(true)(_ => false) =>
        errors += FormError(addrKey, invalidChars)
      case _ =>
    }
  }

  private def validatePostcode(postcode: String, postcodeKey: String, blankPostcodeMessageKey: String,
                               invalidPostcodeMessageKey: String,
                               errors: scala.collection.mutable.ListBuffer[FormError])(implicit appConfig: AppConfig) = {
    postcode match {
      case a if a.length == 0 => errors += FormError(postcodeKey, blankPostcodeMessageKey)
      case a if a.length > 0 && !containsValidPostCodeCharacters(a) =>
        errors += FormError(postcodeKey, invalidPostcodeMessageKey)
      case a if a.length > 0 && !ihtIsPostcodeLengthValid(a) =>
        errors += FormError(postcodeKey, invalidPostcodeMessageKey)
      case _ =>
    }
  }

  private def validateIntlCountryCode(countryCodeKey: String, countryCode: String, errorMessageKey: String,
                                      errors: scala.collection.mutable.ListBuffer[FormError])(implicit messages: Messages, appConfig: AppConfig) = {
    countryCode match {
      case a if a.length == 0 => errors += FormError(countryCodeKey, errorMessageKey)
      case a if !validateInternationalCountryCode(a)(messages, appConfig) => errors += FormError(countryCodeKey, errorMessageKey)
      case _ =>
    }
  }

  def ihtAddress(addr2Key: String, addr3Key: String, addr4Key: String,
                 postcodeKey: String, countryCodeKey: String, allLinesBlankMessageKey: String,
                 blankFirstTwoAddrLinesMessageKey: String, invalidAddressLineMessageKey: String,
                 invalidCharsMessageKey: String,
                 blankPostcodeMessageKey: String, invalidPostcodeMessageKey: String,
                 blankCountryCode: String)(implicit appConfig: AppConfig) = new Formatter[String] {
    override def bind(key: String, data: Map[String, String]) = {
      val errors = new scala.collection.mutable.ListBuffer[FormError]()
      val addr = getAddrDetails(data, key, addr2Key, addr3Key, addr4Key, postcodeKey, countryCodeKey)

      if (addr._1.length == 0 && addr._2.length == 0) {
        errors += FormError(key, allLinesBlankMessageKey)
        errors += FormError(addr2Key, "")
      } else {
        validateMandatoryAddressLine(key, addr._1,
          appConfig.validationMaxLengthAddresslines,
          blankFirstTwoAddrLinesMessageKey,
          invalidAddressLineMessageKey,
          invalidCharsMessageKey,
          errors)
        validateMandatoryAddressLine(addr2Key, addr._2,
          appConfig.validationMaxLengthAddresslines,
          blankFirstTwoAddrLinesMessageKey,
          invalidAddressLineMessageKey,
          invalidCharsMessageKey,
          errors)
        validateOptionalAddressLine(addr3Key, addr._3,
          appConfig.validationMaxLengthAddresslines,
          invalidAddressLineMessageKey,
          invalidCharsMessageKey,
          errors)
        validateOptionalAddressLine(addr4Key, addr._4,
          appConfig.validationMaxLengthAddresslines,
          invalidAddressLineMessageKey,
          invalidCharsMessageKey,
          errors)
      }
      if (addr._6.length == 0 || addr._6 == appConfig.ukIsoCountryCode) {
        validatePostcode(addr._5, postcodeKey, blankPostcodeMessageKey, invalidPostcodeMessageKey, errors)
      }
      if (errors.isEmpty) {
        Right(addr._1)
      } else {
        Left(errors.toList)
      }
    }

    override def unbind(key: String, value: String): Map[String, String] = {
      Map(key -> value.toString)
    }
  }

  def ihtInternationalAddress(addr2Key: String, addr3Key: String, addr4Key: String,
                              countryCodeKey: String, allLinesBlankMessageKey: String,
                              blankFirstTwoAddrLinesMessageKey: String,
                              invalidAddressLineMessageKey: String, invalidChars: String,
                              blankCountryCode: String,
                              blankBothFirstTwoAddrLinesMessageKey: Option[String] = None)
                             (implicit messages: Messages, appConfig: AppConfig) = new Formatter[String] {
    override def bind(key: String, data: Map[String, String]) = {
      val errors = new scala.collection.mutable.ListBuffer[FormError]()
      val addr = getIntlAddrDetails(data, key, addr2Key, addr3Key, addr4Key, countryCodeKey)
      if (addr._1.length == 0 && addr._2.length == 0) {
        errors += FormError(key, allLinesBlankMessageKey)
        errors += FormError(addr2Key, "")
      } else if (blankBothFirstTwoAddrLinesMessageKey.isDefined &&
        addr._1.length == 0 && addr._2.length == 0) {
        errors += FormError(key, CommonHelper.getOrException(blankBothFirstTwoAddrLinesMessageKey))
        errors += FormError(addr2Key, "")
      } else {
        validateMandatoryAddressLine(key, addr._1,
          appConfig.validationMaxLengthAddresslines,
          blankFirstTwoAddrLinesMessageKey,
          invalidAddressLineMessageKey,
          invalidChars,
          errors)
        validateMandatoryAddressLine(addr2Key, addr._2,
          appConfig.validationMaxLengthAddresslines,
          blankFirstTwoAddrLinesMessageKey,
          invalidAddressLineMessageKey,
          invalidChars,
          errors)
        validateOptionalAddressLine(addr3Key, addr._3,
          appConfig.validationMaxLengthAddresslines,
          invalidAddressLineMessageKey,
          invalidChars,
          errors)
        validateOptionalAddressLine(addr4Key, addr._4,
          appConfig.validationMaxLengthAddresslines,
          invalidAddressLineMessageKey,
          invalidChars,
          errors)
      }
      validateIntlCountryCode(countryCodeKey, addr._5, blankCountryCode, errors)(messages, appConfig)
      if (errors.isEmpty) {
        Right(addr._1)
      } else {
        Left(errors.toList)
      }
    }

    override def unbind(key: String, value: String): Map[String, String] = {
      Map(key -> value.toString)
    }
  }

  def ihtRadio(noSelectionMessagesKey: String, items: ListMap[String, String] = ListMap.empty) = new Formatter[String] {
    override def bind(key: String, data: Map[String, String]) = {
      val errors = new scala.collection.mutable.ListBuffer[FormError]()
      val radioValue = data.get(key)
      if (radioValue.isEmpty) {
        errors += FormError(key, noSelectionMessagesKey)
      } else {
        if (items != ListMap.empty && !IhtFormValidator.existsInKeys(radioValue.getOrElse(""), items)) {
          errors += FormError(key, "Invalid item selected")
        }
      }

      //
      if (errors.isEmpty) {
        try {
          data.get(key) match {
            case Some(value) => Right(value)
            case None => Right("")
          }
        } catch {
          case _: IllegalArgumentException => Left(List(FormError(key, "error.invalid")))
        }
      } else {
        Left(errors.toList)
      }
    }

    override def unbind(key: String, value: String): Map[String, String] = {
      Map(key -> value.toString)
    }
  }

  def radioOptionString(noSelectionMessagesKey: String, items: ListMap[String, String] = ListMap.empty) = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]) = {
      Try(data.get(key)) match {
        case Success(Some(value)) if IhtFormValidator.existsInKeys(value, items) => Right(Some(value))
        case Success(Some(_)) => Left(List(FormError(key, "error.invalid")))
        case Success(None) => Left(List(FormError(key, noSelectionMessagesKey)))
        case Failure(_) => Left(List(FormError(key, "error.invalid")))
      }
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] = {
      Map(key -> value.map(_.toString).getOrElse(""))
    }
  }

  def name(maxLength: Int,
         blankMessageKey: String,
         invalidLengthMessageKey: String,
         invalidCharsMessageKey: String): FieldMapping[String] =
    Forms.of(nameFormatter(maxLength, blankMessageKey, invalidLengthMessageKey, invalidCharsMessageKey))


  private def nameFormatter(maxLength: Int,
                   blankMessageKey: String,
                   invalidLengthMessageKey: String,
                   invalidCharsMessageKey: String) = new Formatter[String] {
    override def bind(key: String, data: Map[String, String]) = {
      val errors = new scala.collection.mutable.ListBuffer[FormError]()

      val name = data.get(key)

      checkForNameError(
        key = key,
        maxLength = maxLength,
        blankMessageKey = blankMessageKey,
        invalidLengthMessageKey = invalidLengthMessageKey,
        invalidCharsMessageKey = invalidCharsMessageKey,
        name = name) match {
        case Some(error) => errors += error
        case _ =>
      }

      if (errors.isEmpty) {
        try {
          data.get(key) match {
            case Some(value) => Right(Some(value))
            case None => Right(None)
          }
        } catch {
          case _: IllegalArgumentException => Left(List(FormError(key, "error.invalid")))
        }
      } else {
        Left(errors.toList)
      }
    }

    override def unbind(key: String, value: String): Map[String, String] = {
      Map(key -> value)
    }
  }

  def checkForNameError(key: String,
                        maxLength: Int,
                        blankMessageKey: String,
                        invalidLengthMessageKey: String,
                        invalidCharsMessageKey: String,
                        name: Option[String]): Option[FormError] = {
    name.getOrElse("") match {
      case a if a.isEmpty => Some(FormError(key, blankMessageKey))
      case a if a.length > maxLength => Some(FormError(key, invalidLengthMessageKey))
      case a if nameAndAddressRegex.findFirstIn(a).fold(true)(_ => false) => Some(FormError(key, invalidCharsMessageKey))
      case _ => None
    }
  }

  /**
    * Validate the First and LastName in Tnrb Partner Page
    */
  def validatePartnerName(lastNameKey: String)(implicit appConfig: AppConfig) = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]) = {
      val errors = new scala.collection.mutable.ListBuffer[FormError]()

      val firstName = data.get(key)
      val lastName = data.get(lastNameKey)

      checkForNameError(
        key = key,
        maxLength = appConfig.validationMaxLengthFirstName,
        blankMessageKey = "error.firstName.give",
        invalidLengthMessageKey = "error.firstName.giveUsingXCharsOrLess",
        invalidCharsMessageKey = "error.firstName.giveUsingOnlyValidChars",
        name = firstName) match {
        case Some(error) => errors += error
        case _ =>
      }

      checkForNameError(
        key = lastNameKey,
        maxLength = appConfig.validationMaxLengthLastName,
        blankMessageKey = "error.lastName.give",
        invalidLengthMessageKey = "error.lastName.giveUsingXCharsOrLess",
        invalidCharsMessageKey = "error.lastName.giveUsingOnlyValidChars",
        name = lastName) match {
        case Some(error) => errors += error
        case _ =>
      }

      if (errors.isEmpty) {
        try {
          data.get(key) match {
            case Some(value) => Right(Some(value))
            case None => Right(None)
          }
        } catch {
          case _: IllegalArgumentException => Left(List(FormError(key, "error.invalid")))
        }
      } else {
        Left(errors.toList)
      }
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] = {
      Map(key -> value.map(_.toString).getOrElse(""))
    }
  }

  private def optionalYesNoQuestionFormatter(blankMessageKey: String) = new Formatter[Option[Boolean]] {
    override def bind(key: String, data: Map[String, String]) = {
      val dataItem = data.get(key)
      val dataItemValue = toBoolean(dataItem)

      if (dataItemValue.isEmpty) {
        Left(Seq(FormError(key, blankMessageKey)))
      } else {
        try {
          dataItem match {
            case Some(value) => Right(Some(value.toBoolean))
            case None => Right(None)
          }
        } catch {
          case _: IllegalArgumentException => Left(List(FormError(key, "error.invalid")))
        }
      }

    }

    override def unbind(key: String, value: Option[Boolean]): Map[String, String] = {
      Map(key -> value.map(_.toString).getOrElse(""))
    }
  }

  def yesNoQuestion(blankMessageKey: String) = Forms.of(optionalYesNoQuestionFormatter(blankMessageKey))

  private def ninoFormatter(blankMessageKey: String, lengthMessageKey: String, formatMessageKey: String)
                           (implicit appConfig: AppConfig) =
    new Formatter[String] {
      override def bind(key: String, data: Map[String, String]) = {
        val value = data.get(key).fold("")(identity)
        lazy val valueMinusSpaces = value.replaceAll("\\s", "")
        value match {
          case n if n.isEmpty => Left(Seq(FormError(key, blankMessageKey)))
          case _ if valueMinusSpaces.length < appConfig.validationMinLengthNINO ||
            valueMinusSpaces.length > appConfig.validationMaxLengthNINO => Left(Seq(FormError(key, lengthMessageKey)))
          case _ if !valueMinusSpaces.matches(ninoRegex) => Left(Seq(FormError(key, formatMessageKey)))
          case n => Right(n)
        }
      }

      override def unbind(key: String, value: String): Map[String, String] = {
        Map(key -> value.toString)
      }
    }

  def nino(blankMessageKey: String,
           lengthMessageKey: String,
           formatMessageKey: String)(implicit appConfig: AppConfig) =
    Forms.of(ninoFormatter(blankMessageKey, lengthMessageKey, formatMessageKey))

  def nino(implicit appConfig: AppConfig): FieldMapping[String] =
    nino("error.nino.give", "error.nino.giveUsing8Or9Characters", "error.nino.giveUsingOnlyLettersAndNumbers")

  private def normalizeNino(s: String): String = s.replaceAll("\\s", "").toUpperCase
  private def ninoForCoExecutorFormatter(blankMessageKey: String, lengthMessageKey: String,
                                         formatMessageKey: String, coExecutorIDKey: String,
                                         oRegDetails: Option[RegistrationDetails], loginNino: String)
                                        (implicit appConfig: AppConfig) = new Formatter[String] {

    def ninoIsNotLogin(nino: String): Boolean = {
      normalizeNino(nino) != loginNino
    }

    def ninoIsUnique(nino: String, excludingCoExecutorID: Option[String], oRegDetails: Option[RegistrationDetails]): Boolean = {
      oRegDetails forall (ninoAlreadyInRegDetails(normalizeNino(nino), excludingCoExecutorID, _))
    }

    def ninoAlreadyInRegDetails(nino: String, excludingCoExecutorID: Option[String], rd: RegistrationDetails): Boolean = {
      rd.applicantDetails.flatMap(_.nino).fold(true)(normalizeNino(_) != nino) &&
        rd.deceasedDetails.flatMap(_.nino).fold(true)(normalizeNino(_) != nino) &&
        !rd.coExecutors.filter(_.id != excludingCoExecutorID).exists(x => normalizeNino(x.nino) == nino)
    }

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
      val value = data.get(key).fold("")(identity)

      ninoFormatter(blankMessageKey, lengthMessageKey, formatMessageKey).bind(key, data) match {
        case Right(_) =>
          val coExecutorID: Option[String] = data.get(coExecutorIDKey)

          (ninoIsNotLogin(value), ninoIsUnique(value, coExecutorID, oRegDetails)) match {
            case (false, _) => Left(Seq(FormError(key, "error.nino.coexec.sameaslogin")))
            case (_, false) => Left(Seq(FormError(key, "error.nino.alreadyGiven")))
            case _          => Right(value)
          }
        case Left(errors) => Left(errors)
      }
    }

    override def unbind(key: String, value: String): Map[String, String] = {
      Map(key -> value.toString)
    }
  }

  private def ninoForDeceasedFormatter(blankMessageKey: String, lengthMessageKey: String,
                                       formatMessageKey: String, oRegDetails: Option[RegistrationDetails])
                                      (implicit appConfig: AppConfig) = new Formatter[String] {

    def ninoIsUnique(nino: String): Boolean = {
      val normalizedNino = normalizeNino(nino)
      oRegDetails.forall {
        rd =>
          rd.applicantDetails.flatMap(_.nino).fold(true)(normalizeNino(_) != normalizedNino) &&
          !rd.coExecutors.exists(coExecutor => normalizeNino(coExecutor.nino).contains(normalizedNino))
      }
    }

    override def bind(key: String, data: Map[String, String]) = {
      val value = data.get(key).fold("")(identity)

      ninoFormatter(blankMessageKey, lengthMessageKey, formatMessageKey).bind(key, data) match {
        case Right(_) =>
          if (ninoIsUnique(value)) {
            Right(value)
          } else {
            Left(Seq(FormError(key, "error.nino.alreadyGiven")))
          }
        case Left(errors) => Left(errors)
      }
    }

    override def unbind(key: String, value: String): Map[String, String] = {
      Map(key -> value.toString)
    }
  }


  def ninoForCoExecutor(blankMessageKey: String, lengthMessageKey: String, formatMessageKey: String,
                        coExecutorIDKey: String, oRegDetails: Option[RegistrationDetails], loginNino: String)
                       (implicit appConfig: AppConfig): FieldMapping[String] =
    Forms.of(ninoForCoExecutorFormatter(blankMessageKey, lengthMessageKey, formatMessageKey, coExecutorIDKey, oRegDetails, loginNino))

  def ninoForDeceased(blankMessageKey: String, lengthMessageKey: String, formatMessageKey: String,
                      oRegDetails: Option[RegistrationDetails])
                     (implicit appConfig: AppConfig): FieldMapping[String] =
    Forms.of(ninoForDeceasedFormatter(blankMessageKey, lengthMessageKey, formatMessageKey, oRegDetails))

}
