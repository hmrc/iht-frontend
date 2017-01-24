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

package iht

import iht.config.{ApplicationConfig => Config}
import iht.constants.IhtProperties
import iht.models.RegistrationDetails
import play.api.data.Form
import play.api.i18n.Messages

/**
 * Created by yasar on 5/5/15.
 */
package object utils {
  type Predicate = (RegistrationDetails, String) => Boolean

  implicit def toBoolean(s: Option[String]): Option[Boolean] = s match {
    case None => None
    case Some("true") => Some(true)
    case _ => Some(false)
  }

  implicit def toCurrency(currencyValue: Option[String]): String = {
    val s = currencyValue.getOrElse("")
    // Ensure that if zero value entered into currency field, it is treated as if no value entered.
    try {
      if (s.isEmpty) {
        ""
      } else {
        s
      }
    } catch {
      case e: NumberFormatException => s
    }
  }

  /**
   * Gets correct role for display
   */
  def additionalApplicantType(role:String): String = {

    val roleAdmin = IhtProperties.roleAdministrator
    val roleLeadExecutor = IhtProperties.roleLeadExecutor

    if (role == roleAdmin) {
      IhtProperties.roleAdministrator
    } else if (role == roleLeadExecutor) {
      IhtProperties.roleExecutor
    } else {
      role
    }
  }

  /*
   * Get sequence of country code and country name
   */
  def countryCodes(implicit lang: play.api.i18n.Lang): Seq[(String, String)] = {
    lazy val countryCodes = IhtProperties.validCountryCodes
    countryCodes.map(x => (x, Messages(s"country.$x"))).sortWith(_._2 < _._2)
  }

  def internationalCountries(implicit lang: play.api.i18n.Lang): Seq[(String, String)] =
    countryCodes filter {case(key, _) => key != IhtProperties.ukIsoCountryCode}

  /*
   * Get country name from country code
   */
  def countryName(countryCode: String)(implicit lang: play.api.i18n.Lang): String = {
    val input = s"country.$countryCode"
    Messages(s"country.$countryCode") match {
      case `input` => {
        ""
      }
      case x => x
    }
  }

  /**
   * Formats the input string as below
   * Ex - Input ABC00000012A01
   *      Output ABC 0000 0012 A01
   * @param ihtRef
   * @return String
   */
  // scalastyle:off magic.number
  def formattedIHTReference(ihtRef: String) = {
    if (ihtRef.length>0) {
      val ihtRefFirstSplit = ihtRef.trim.splitAt(3)
      val ihtRefSplitTail = ihtRefFirstSplit._2.grouped(4)
      s"${ihtRefFirstSplit._1} ${ihtRefSplitTail.mkString(" ")}".trim
    } else {
      ihtRef
    }
  }

  /**
   * Formats the input string as below
   * Ex - Input 12345678A01-123
   *      Output 1234 5678 A01-123
   * @param probateRef
   * @return String
   */
  def formattedProbateReference(probateRef: String) = {
    if (probateRef.length>0){
      val probateRefFirstSplit = probateRef.trim.splitAt(4)
      val probateRefFirstSplitTail = probateRefFirstSplit._2.splitAt(4)
      s"${probateRefFirstSplit._1} ${probateRefFirstSplitTail._1.mkString} ${probateRefFirstSplitTail._2.mkString}".trim
    }
    else {
      probateRef
    }
  }
}
