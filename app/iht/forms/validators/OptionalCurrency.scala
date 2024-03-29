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

package iht.forms.validators

import play.api.data.format.Formatter
import play.api.data.{FormError, Forms}

object OptionalCurrency extends Currency {
  /**
    * @param errorLengthKey                 if length > 10
    * @param errorInvalidCharsKey           e.g. $%^GG^,  AAbc
    * @param errorInvalidPenceKey           e.g. 6.898 or 885.50.60
    * @param errorInvalidSpacesKey          e.g. 33 45
    * @param errorInvalidCommaPositionKey   if comma in wrong place
    */
  private def optionalCurrencyFormatter(errorLengthKey: String,
                                        errorInvalidCharsKey: String,
                                        errorInvalidPenceKey: String,
                                        errorInvalidSpacesKey: String,
                                        errorInvalidCommaPositionKey: String) = new Formatter[Option[BigDecimal]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[BigDecimal]] =
      data.get(key).fold("")(identity) match {
        case v if v.trim.isEmpty => Right(None)
        case v =>
          validationErrors(key, v, errorLengthKey, errorInvalidCharsKey, errorInvalidPenceKey,
                                    errorInvalidSpacesKey, errorInvalidCommaPositionKey: String) match {
            case Some(s) => Left(s)
            case None => Right(Option(BigDecimal(cleanMoneyString(v))))
          }
      }

    override def unbind(key: String, value: Option[BigDecimal]): Map[String, String] =
      Map(key -> value.getOrElse("").toString)
  }

  def apply(errorLengthKey: String = "error.estateReport.value.giveLessThanEleven",
            errorInvalidCharsKey: String = "error.estateReport.value.giveValueUsingNumbers",
            errorInvalidPenceKey: String = "error.estateReport.value.giveCorrectNumberOfPence",
            errorInvalidSpacesKey: String = "error.estateReport.value.giveWithNoSpaces",
            errorInvalidCommaPositionKey: String  = "error.estateReport.value.giveWithCorrectComma") =
    Forms.of(optionalCurrencyFormatter(errorLengthKey,
                                      errorInvalidCharsKey,
                                      errorInvalidPenceKey,
                                      errorInvalidSpacesKey,
                                      errorInvalidCommaPositionKey))
}
