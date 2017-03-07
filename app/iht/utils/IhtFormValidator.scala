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

import iht.constants.IhtProperties
import iht.models.UkAddress
import play.api.data.{FieldMapping, FormError, Forms}
import play.api.data.format.Formatter

import scala.collection.immutable.ListMap
import scala.util.{Failure, Success, Try}
import iht.utils.CommonHelper._

object IhtFormValidator extends FormValidator {
  def validateGiftsDetails(valueKey: String, exemptionsValueKey: String) = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]) = {
      import scala.util.Try
      val errors = new scala.collection.mutable.ListBuffer[FormError]()
      val value = toCurrency(data.get(valueKey)).replace(",", "")
      val exemptionsValue = toCurrency(data.get(exemptionsValueKey)).replace(",", "")

      val theValue = Try(BigDecimal(cleanMoneyString(value.trim)))
      val theExemptionsValue = Try(BigDecimal(cleanMoneyString(exemptionsValue.trim)))

      if (theValue.isSuccess && theExemptionsValue.isSuccess) {
        if (BigDecimal(value) < BigDecimal(exemptionsValue)) {
          errors += FormError(exemptionsValueKey, "error.giftsDetails.exceedsGivenAway")
        } else if (BigDecimal(exemptionsValue) > IhtProperties.giftsInYearMaxExemptionsValue) {
          errors += FormError(exemptionsValueKey, "error.giftsDetails.exceedsLimit")
        }
      } else if (value.length == 0 && theExemptionsValue.isSuccess) {
        errors += FormError(valueKey, "error.giftsDetails.noValue")
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

  def validateBasicEstateElementLiabilities(currencyValueKey: String) = new Formatter[Option[Boolean]] {
    override def bind(key: String, data: Map[String, String]) = {
      val errors = new scala.collection.mutable.ListBuffer[FormError]()
      val isOwned = toBoolean(data.get(key))
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

  private def getAddrDetailsAsAddr(data: Map[String, String],
                                   addr1Key: String,
                                   addr2Key: String,
                                   addr3Key: String,
                                   addr4Key: String,
                                   postcodeKey: String,
                                   countryCodeKey: String): UkAddress = {
    UkAddress(
      data.getOrElse(addr1Key, ""),
      data.getOrElse(addr2Key, ""),
      data.get(addr3Key),
      data.get(addr4Key),
      data.getOrElse(postcodeKey, ""),
      data.getOrElse(countryCodeKey, IhtProperties.ukIsoCountryCode))
  }

  private def validateOptionalAddressLine(addrKey: String, addr: String, maxLength: Int,
                                          invalidAddressLineMessageKey: String,
                                          errors: scala.collection.mutable.ListBuffer[FormError]): Unit = {
    addr match {
      case a if a.length > maxLength => errors += FormError(addrKey, invalidAddressLineMessageKey)
      case _ => {}
    }
  }

  private def validateMandatoryAddressLine(addrKey: String, addr: String, maxLength: Int,
                                           blankMessageKey: String,
                                           invalidAddressLineMessageKey: String,
                                           errors: scala.collection.mutable.ListBuffer[FormError]): Unit = {
    addr match {
      case a if a.length == 0 => errors += FormError(addrKey, blankMessageKey)
      case a if a.length > maxLength => errors += FormError(addrKey, invalidAddressLineMessageKey)
      case _ => {}
    }
  }

  private def validatePostcode(postcode: String, postcodeKey: String, blankPostcodeMessageKey: String,
                               invalidPostcodeMessageKey: String,
                               errors: scala.collection.mutable.ListBuffer[FormError]) = {
    postcode match {
      case a if a.length == 0 => errors += FormError(postcodeKey, blankPostcodeMessageKey)
      case a if a.length > 0 && !containsValidPostCodeCharacters(a) =>
        errors += FormError(postcodeKey, invalidPostcodeMessageKey)
      case a if a.length > 0 && !ihtIsPostcodeLengthValid(a) =>
        errors += FormError(postcodeKey, invalidPostcodeMessageKey)
      case _ => {}
    }
  }

  private def validateIntlCountryCode(countryCodeKey: String, countryCode: String, errorMessageKey: String,
                                      errors: scala.collection.mutable.ListBuffer[FormError]) = {
    countryCode match {
      case a if a.length == 0 => errors += FormError(countryCodeKey, errorMessageKey)
      case a if !validateInternationalCountryCode(a) => errors += FormError(countryCodeKey, errorMessageKey)
      case _ => {}
    }
  }

  private def getCountryCodeErrors(countryCode: String, countryCodeKey: String, blankCountryCodeMessageKey: String,
                                   invalidCountryCodeMessageKey: String): Option[FormError] = {
    if (countryCode.trim.isEmpty) {
      Some(FormError(countryCodeKey, blankCountryCodeMessageKey))
    } else if (countryCode != IhtProperties.ukIsoCountryCode) {
      Some(FormError(countryCodeKey, invalidCountryCodeMessageKey))
    } else {
      None
    }
  }

  def ihtAddress(addr2Key: String, addr3Key: String, addr4Key: String,
                 postcodeKey: String, countryCodeKey: String, allLinesBlankMessageKey: String,
                 blankFirstTwoAddrLinesMessageKey: String, invalidAddressLineMessageKey: String,
                 blankPostcodeMessageKey: String, invalidPostcodeMessageKey: String,
                 blankCountryCode: String) = new Formatter[String] {
    override def bind(key: String, data: Map[String, String]) = {
      val errors = new scala.collection.mutable.ListBuffer[FormError]()
      val addr = getAddrDetails(data, key, addr2Key, addr3Key, addr4Key, postcodeKey, countryCodeKey)

      if (addr._1.length == 0 && addr._2.length == 0) {
        errors += FormError(key, allLinesBlankMessageKey)
        errors += FormError(addr2Key, "")
      } else {
        validateMandatoryAddressLine(key, addr._1,
          IhtProperties.validationMaxLengthAddresslines, blankFirstTwoAddrLinesMessageKey,
          invalidAddressLineMessageKey, errors)
        validateMandatoryAddressLine(addr2Key, addr._2,
          IhtProperties.validationMaxLengthAddresslines, blankFirstTwoAddrLinesMessageKey,
          invalidAddressLineMessageKey, errors)
        validateOptionalAddressLine(addr3Key, addr._3,
          IhtProperties.validationMaxLengthAddresslines, invalidAddressLineMessageKey, errors)
        validateOptionalAddressLine(addr4Key, addr._4,
          IhtProperties.validationMaxLengthAddresslines, invalidAddressLineMessageKey, errors)
      }

      if (addr._6.length == 0 || addr._6 == IhtProperties.ukIsoCountryCode) {
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
                 blankFirstTwoAddrLinesMessageKey: String, invalidAddressLineMessageKey: String,
                 blankCountryCode: String,
                 blankBothFirstTwoAddrLinesMessageKey: Option[String] = None) = new Formatter[String] {
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
          IhtProperties.validationMaxLengthAddresslines, blankFirstTwoAddrLinesMessageKey,
          invalidAddressLineMessageKey, errors)
        validateMandatoryAddressLine(addr2Key, addr._2,
          IhtProperties.validationMaxLengthAddresslines, blankFirstTwoAddrLinesMessageKey,
          invalidAddressLineMessageKey, errors)
        validateOptionalAddressLine(addr3Key, addr._3,
          IhtProperties.validationMaxLengthAddresslines, invalidAddressLineMessageKey, errors)
        validateOptionalAddressLine(addr4Key, addr._4,
          IhtProperties.validationMaxLengthAddresslines, invalidAddressLineMessageKey, errors)
      }

      validateIntlCountryCode(countryCodeKey, addr._5, blankCountryCode, errors)

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
      if (radioValue == None) {
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

  /**
    * Validate the First and LastName in Tnrb Partner Page
    */
  def validatePartnerName(lastNameKey: String) = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]) = {
      val errors = new scala.collection.mutable.ListBuffer[FormError]()

      val firstName = data.get(key)
      val lastName = data.get(lastNameKey)


      if (firstName.getOrElse("").isEmpty) {
        errors += FormError(key, "error.firstName.give")
      } else if (CommonHelper.getOrException(firstName).length > IhtProperties.validationMaxLengthFirstName) {
        errors += FormError(key, "error.firstName.giveUsingXCharsOrLess")
      }
      if (lastName.getOrElse("").isEmpty) {
        errors += FormError(lastNameKey, "error.lastName.give")
      } else if (CommonHelper.getOrException(lastName).length > IhtProperties.validationMaxLengthLastName) {
        errors += FormError(lastNameKey, "error.lastName.giveUsingXCharsOrLess")
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

  private def ninoFormatter( blankMessageKey: String, lengthMessageKey: String, formatMessageKey: String) =
    new Formatter[String] {
      override def bind(key: String, data: Map[String, String]) = {
        val value = data.get(key).fold("")(identity)
        lazy val valueMinusSpaces = value.replaceAll("\\s", "")
        value match {
          case n if n.isEmpty => Left(Seq(FormError(key, blankMessageKey)))
          case n if valueMinusSpaces.length < IhtProperties.validationMinLengthNINO ||
            valueMinusSpaces.length > IhtProperties.validationMaxLengthNINO => Left(Seq(FormError(key, lengthMessageKey)))
          case n if !valueMinusSpaces.matches(ninoRegex) => Left(Seq(FormError(key, formatMessageKey)))
          case n => Right(n)
        }
      }

      override def unbind(key: String, value: String): Map[String, String] = {
        Map(key -> value.toString)
      }
    }

  def nino(blankMessageKey: String,
           lengthMessageKey: String,
           formatMessageKey: String) = Forms.of(ninoFormatter(blankMessageKey, lengthMessageKey, formatMessageKey))

  val nino: FieldMapping[String] = nino("error.nino.give","error.nino.giveUsing8Or9Characters","error.nino.giveUsingOnlyLettersAndNumbers")

  private def optionalYesNoQuestionFormatter(blankMessageKey: String) = new Formatter[Option[Boolean]] {
    override def bind(key: String, data: Map[String, String]) = {
      val errors = new scala.collection.mutable.ListBuffer[FormError]()
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

  def yesNoQuestion(blankMessageKey:String) = Forms.of(optionalYesNoQuestionFormatter(blankMessageKey))
}
