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

import javax.inject.Inject

import iht.constants.IhtProperties
import org.joda.time.LocalDate
import play.api.data.Forms._
import play.api.data._
import play.api.data.format.Formatter
import play.api.data.validation._
import uk.gov.hmrc.play.validators.Validators._

import scala.collection.immutable.ListMap

class FormValidator @Inject() (ihtProperties:IhtProperties) {
  protected val ninoRegex = """(?i)(^$|^(?!BG|GB|KN|NK|NT|TN|ZZ)([A-Z]{2})[0-9]{6}[A-D]?$)"""
  // scalastyle:off line.size.limit
  protected val postCodeFormat = "(([gG][iI][rR] {0,}0[aA]{2})|((([a-pr-uwyzA-PR-UWYZ][a-hk-yA-HK-Y]?[0-9][0-9]?)|(([a-pr-uwyzA-PR-UWYZ][0-9][a-hjkstuwA-HJKSTUW])|([a-pr-uwyzA-PR-UWYZ][a-hk-yA-HK-Y][0-9][abehmnprv-yABEHMNPRV-Y]))) {0,}[0-9][abd-hjlnp-uw-zABD-HJLNP-UW-Z]{2}))"
  // scalastyle:on line.size.limit
  protected val phoneNoFormat = "^[A-Z0-9 \\)\\/\\(\\-\\*#]{1,27}$"
  protected lazy val moneyFormatSimple = """^(\d{1,10}+([.]\d{1,2})?)$""".r

  lazy val countryCodes = ihtProperties.validCountryCodes

  def validateCountryCode(x: String) = countryCodes.contains(x.toUpperCase)

  def validateInternationalCountryCode(code: String) = internationalCountries.map(_._1).contains(code.toUpperCase)

  def isNotFutureDate = {
    date: LocalDate => !date.isAfter(LocalDate.now())
  }

  def isDobBeforeDod(dod: LocalDate, dob: LocalDate): Boolean = !dob.isAfter(dod)

  def existsInKeys = {
    (key: String, map: ListMap[String, String]) => map.keys.exists(v => (v == key))
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

  def mandatoryPhoneNumber(blankValueMessageKey: String,
                           invalidLengthMessageKey: String,
                           invalidValueMessageKey: String) =
    Forms.of[String](mandatoryPhoneNumberFormatter(Some(blankValueMessageKey), invalidLengthMessageKey, invalidValueMessageKey))

  def optionalCurrencyWithoutFieldName =
    Forms.of[Option[BigDecimal]](optionalCurrencyFormatterWithoutFieldName)

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
              if (num.length > ihtProperties.validationMaxLengthPhoneNo) {
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
              if (num.length > ihtProperties.validationMaxLengthPhoneNo) {
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
    value.length <= ihtProperties.validationMaxLengthPostcode && isPostcodeLengthValid(value)
  }


  def addDeceasedNameToAllFormErrors[T](form: Form[T], deceasedName: String) = {
    val errors = form.errors.map { error =>
      new FormError(error.key, error.messages, Seq(deceasedName))
    }
    Form(
      form.mapping,
      form.data,
      errors,
      form.value)
  }
}
