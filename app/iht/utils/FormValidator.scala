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
import org.joda.time.LocalDate
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.validation._
import play.api.data.{FieldMapping, FormError, Forms, Mapping}
import uk.gov.hmrc.play.validators.Validators._

import scala.collection.immutable.ListMap
import scala.util.Try

trait FormValidator {
  val ninoRegex = """(?i)(^$|^(?!BG|GB|KN|NK|NT|TN|ZZ)([A-Z]{2})[0-9]{6}[A-D]?$)"""
  // scalastyle:off line.size.limit
  val emailFormat =
  """(?i)[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"""
  val postCodeFormat = "(([gG][iI][rR] {0,}0[aA]{2})|((([a-pr-uwyzA-PR-UWYZ][a-hk-yA-HK-Y]?[0-9][0-9]?)|(([a-pr-uwyzA-PR-UWYZ][0-9][a-hjkstuwA-HJKSTUW])|([a-pr-uwyzA-PR-UWYZ][a-hk-yA-HK-Y][0-9][abehmnprv-yABEHMNPRV-Y]))) {0,}[0-9][abd-hjlnp-uw-zABD-HJLNP-UW-Z]{2}))"
  // scalastyle:on line.size.limit
  val phoneNoFormat = "^[A-Z0-9 \\)\\/\\(\\-\\*#]{1,27}$"
  private lazy val moneyFormatSimple = """^(\d{1,10}+([.]\d{1,2})?)$""".r

  lazy val countryCodes = IhtProperties.validCountryCodes

  def validateCountryCode(x: String) = countryCodes.contains(x.toUpperCase)

  def validateInternationalCountryCode(code: String) = internationalCountries.map(_._1).contains(code.toUpperCase)

  def isNotFutureDate = {
    date: LocalDate => !date.isAfter(LocalDate.now())
  }

  def isNotFutureOptionDate(x: Option[LocalDate]) = !x.get.isAfter(LocalDate.now())

  def isDobBeforeDod(dod: LocalDate, dob: LocalDate): Boolean = !dob.isAfter(dod)

  def existsInKeys = {
    (key: String, map: ListMap[String, String]) => map.keys.exists(v => (v == key))
  }

  def validateDeceasedDomicile = {
    country: String => (country == IhtProperties.domicileEnglandOrWales)
  }

  def validateApplicantCountry = {
    country: String => (country == IhtProperties.applicantCountryEnglandOrWales)
  }

  def optionalCurrencyFormatterWithoutFieldName = new Formatter[Option[BigDecimal]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[BigDecimal]] = {
      data.get(key) match {
        case Some(num) => {
          num.trim match {
            case "" => Right(None)
            case numTrimmed => {
              try {
                val bigDecimalMoney = BigDecimal(cleanMoneyString(numTrimmed))
                Right(Some(bigDecimalMoney))
              } catch {
                case e: NumberFormatException => {
                  if (numTrimmed.size > 10) {
                    Left(Seq(FormError(key, "error.currencyValue.length")))
                  } else {
                    Left(Seq(FormError(key, "error.currencyValue.incorrect")))
                  }
                }
              }
            }
          }
        }
        case _ => Right(None)
      }
    }

    override def unbind(key: String, value: Option[BigDecimal]): Map[String, String] = Map(key -> value.getOrElse("").toString)
  }


  def optionalCurrencyFormatterWithParameter(fieldName: String) = new Formatter[Option[BigDecimal]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[BigDecimal]] = {
      data.get(key) match {
        case Some(num) => {
          num.trim match {
            case "" => Right(None)
            case numTrimmed => {
              try {
                val bigDecimalMoney = BigDecimal(cleanMoneyString(numTrimmed))
                Right(Some(bigDecimalMoney))
              } catch {
                case e: NumberFormatException => {
                  if (numTrimmed.size > 10) {
                    Left(Seq(FormError(key, "error.length." + fieldName)))
                  } else {
                    Left(Seq(FormError(key, "error.currency." + fieldName)))
                  }
                }
              }
            }
          }
        }
        case _ => Right(None)
      }
    }

    override def unbind(key: String, value: Option[BigDecimal]): Map[String, String] = Map(key -> value.getOrElse("").toString)
  }

  def mandatoryCurrencyFormatterWithParameter(fieldName: String) = new Formatter[BigDecimal] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], BigDecimal] = {
      data.get(key) match {
        case Some(num) => {
          num.trim match {
            case numTrimmed if numTrimmed.length == 0 => Left(Seq(FormError(key, "error.blank." + fieldName)))
            case numTrimmed => {
              try {
                val bigDecimalMoney: BigDecimal = BigDecimal(cleanMoneyString(numTrimmed))
                Right(bigDecimalMoney)
              } catch {
                case e: NumberFormatException => {
                  if (numTrimmed.size > 10) {
                    Left(Seq(FormError(key, "error.length." + fieldName)))
                  } else {
                    Left(Seq(FormError(key, "error.currency." + fieldName)))
                  }
                }
              }
            }
          }
        }
        case _ => Left(Seq(FormError(key, "error.currency")))
      }
    }

    override def unbind(key: String, value: BigDecimal): Map[String, String] = Map(key -> value.toString)
  }

  def mandatoryCurrencyFormatterWithParameterProperties = new Formatter[BigDecimal] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], BigDecimal] = {
      data.get(key) match {
        case Some(num) => {
          num.trim match {
            case numTrimmed if numTrimmed.length == 0 => Left(Seq(FormError(key, "error.properties.blank")))
            case numTrimmed => {
              try {
                val bigDecimalMoney: BigDecimal = BigDecimal(cleanMoneyString(numTrimmed))
                Right(bigDecimalMoney)
              } catch {
                case e: NumberFormatException => {
                  if (numTrimmed.size > 10) {
                    Left(Seq(FormError(key, "error.currencyValue.length")))
                  } else {
                    Left(Seq(FormError(key, "error.currencyValue.incorrect")))
                  }
                }
              }
            }
          }
        }
        case _ => Left(Seq(FormError(key, "error.currency")))
      }
    }

    override def unbind(key: String, value: BigDecimal): Map[String, String] = Map(key -> value.toString)
  }

  def mandatoryPhoneNumber(blankValueMessageKey: String,
                           invalidLengthMessageKey: String,
                           invalidValueMessageKey: String) =
    Forms.of[String](mandatoryPhoneNumberFormatter(Some(blankValueMessageKey), invalidLengthMessageKey, invalidValueMessageKey))

  def optionalCurrencyWithoutFieldName =
    Forms.of[Option[BigDecimal]](optionalCurrencyFormatterWithoutFieldName)

  def optionalCurrencyWithParameter(fieldName: String) =
    Forms.of[Option[BigDecimal]](optionalCurrencyFormatterWithParameter(fieldName))

  def mandatoryCurrencyWithParameter(fieldName: String) =
    Forms.of[BigDecimal](mandatoryCurrencyFormatterWithParameter(fieldName))

  def mandatoryCurrencyWithParameterProperties =
    Forms.of[BigDecimal](mandatoryCurrencyFormatterWithParameterProperties)

  def stopOnFirstFail[T](constraints: Constraint[T]*) = Constraint { field: T =>
    constraints.toList dropWhile (_ (field) == Valid) match {
      case Nil => Valid
      case constraint :: _ => constraint(field)
    }
  }

  def cleanMoneyString(moneyString: String) =
    moneyFormatSimple.findFirstIn(moneyString.replace(",", "").replace("-", "")).getOrElse("")

  def ihtNonEmptyText(blankValueMessageKey: String): Mapping[String] = {
    text.verifying(blankValueMessageKey, t => t.trim.length > 0)
  }

  def mandatoryPhoneNumberFormatter(blankValueMessageKey: String,
                                    invalidLengthMessageKey: String,
                                    invalidValueMessageKey: String) = new Formatter[String] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
      data.get(key) match {
        case Some(n) =>
          val s = n.trim.toUpperCase
          val t = if (s.length > 0 && s.charAt(0) == '+') "00" + s.substring(1) else s
          t match {
            case p if p.length == 0 => Left(Seq(FormError(key, blankValueMessageKey)))
            case num => {
              if (num.length > IhtProperties.validationMaxLengthPhoneNo) {
                Left(Seq(FormError(key, invalidLengthMessageKey)))
              } else if (!validatePhoneNumber(num)) {
                Left(Seq(FormError(key, invalidValueMessageKey)))
              } else {
                Right(Some(num))
              }
            }
          }

        case _ => Left(Seq(FormError(key, blankValueMessageKey)))
      }
    }

    override def unbind(key: String, value: String): Map[String, String] = Map(key -> value)
  }

  private def phoneNumberOptionStringFormatter(blankValueMessageKey: String,
                                               invalidLengthMessageKey: String,
                                               invalidValueMessageKey: String) = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      data.get(key) match {
        case Some(n) =>
          val s = n.trim.toUpperCase
          val t = if (s.length > 0 && s.charAt(0) == '+') "00" + s.substring(1) else s
          t match {
            case p if p.length == 0 => Left(Seq(FormError(key, blankValueMessageKey)))
            case num =>
              if (num.length > IhtProperties.validationMaxLengthPhoneNo) {
                Left(Seq(FormError(key, invalidLengthMessageKey)))
              } else if (!validatePhoneNumber(num)) {
                Left(Seq(FormError(key, invalidValueMessageKey)))
              } else {
                Right(Some(num))
              }
          }

        case _ => Left(Seq(FormError(key, blankValueMessageKey)))
      }
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] = Map(key -> value.getOrElse(""))
  }

  def phoneNumberOptionString(blankValueMessageKey: String,
                              invalidLengthMessageKey: String,
                              invalidValueMessageKey: String): FieldMapping[Option[String]] =
    of(phoneNumberOptionStringFormatter(blankValueMessageKey, invalidLengthMessageKey, invalidValueMessageKey))

  private def validatePhoneNumber = {
    s: String => phoneNoFormat.r.findFirstIn(s) match {
      case Some(x) => true
      case None => false
    }
  }

  def containsValidPostCodeCharacters(value: String): Boolean =
    !postCodeFormat.r.findFirstIn(value).isEmpty


  def ihtIsPostcodeLengthValid(value: String) = {
    value.length <= IhtProperties.validationMaxLengthPostcode && isPostcodeLengthValid(value)
  }

  def ihtEmailWithDomain(errorMessageKeyInvalidFormat: String = "error.email") =
    Constraints.pattern(emailFormat.r, "constraint.email", errorMessageKeyInvalidFormat)


  /**
    * Perform various validation checks on date formed by the three date
    * component values passed in. Any of the option message keys passed in
    * that are None indicate that the corresponding validation should not be
    * performed, if there's a message key specified then that key is returned
    * if that validation rule is violated.
    *
    * @param year
    * @param month
    * @param day
    * @param optionMaxDate   The maximum valid date
    * @param blankMessageKey Message key to use if no value has been entered in the
    *                        date components.
    * @param monthDayMaxKey  Message key to use if month > 12 or day > 31
    * @param invalidCharKey  Message key to use if an invalid character is found in
    *                        any of the components.
    * @param futureDateKey   Message key to use if a future date is composed by these
    *                        components.
    * @param maxDateKey      Message key to use if date constructed is greater than that
    *                        passed in via optionMaxDate
    * @return Message key of violated validation rule.
    */
  def validateDate(year: String, month: String, day: String,
                   optionMaxDate: Option[LocalDate] = None,
                   blankMessageKey: Option[String] = None,
                   monthDayMaxKey: Option[String] = None,
                   invalidCharKey: Option[String] = None,
                   futureDateKey: Option[String] = None,
                   maxDateKey: Option[String] = None
                  ): Option[String] = {
    val optionDate = CommonHelper.createDate(Some(year), Some(month), Some(day))

    if (blankMessageKey.isDefined && year.length == 0 && month.length == 0 && day.length == 0) {
      blankMessageKey
    } else if ((monthDayMaxKey.isDefined && !isDayAndMonthLessThanMax(month, day)) || year.length < 4) {
      monthDayMaxKey
    } else {
      val optionError = validateDateInvalidCharAndFutureDate(year, month, day, invalidCharKey, futureDateKey)
      lazy val optionMaxDateError = optionMaxDate.flatMap(maxDate => optionDate.flatMap(date =>
        if (date isAfter maxDate) maxDateKey else None))
      optionError.fold[Option[String]](optionMaxDateError)(error => Some(error))
    }
  }

  def validateDateInvalidCharAndFutureDate(year: String, month: String, day: String,
                                           invalidCharKey: Option[String] = None,
                                           futureDateKey: Option[String] = None): Option[String] = {
    val date = CommonHelper.createDate(Some(year), Some(month), Some(day))

    if (invalidCharKey.isDefined && !date.isDefined) {
      invalidCharKey
    } else if (futureDateKey.isDefined && !CommonHelper.isNotFutureDate(CommonHelper.getOrException(date))) {
      futureDateKey
    } else {
      None
    }
  }

  def isDayAndMonthLessThanMax(month: String, day: String) = {
    val tm = Try(month.trim.toInt)
    val td = Try(day.trim.toInt)
    val m = tm.getOrElse(-1)
    val d = td.getOrElse(-1)
    m <= 12 && d <= 31
  }
}
