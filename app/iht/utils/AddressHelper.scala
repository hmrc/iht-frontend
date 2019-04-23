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

package iht.utils

import iht.config.AppConfig
import iht.models._
import iht.views.html.ihtHelpers.custom._
import play.api.i18n.Messages
import play.twirl.api.Html

object AddressHelper {
  val isThereAnApplicantAddress: Predicate = (rd, _) => rd.applicantDetails.flatMap(_.ukAddress).isDefined

  private implicit class HtmlOps(html: Html) {
    def formatHtml: String = {
      html.toString.replace("\n", "")
    }
  }

  def addressFormatter(address: UkAddress)(implicit messages: Messages, appConfig: AppConfig): String =
    Seq(
      Some(name(address.ukAddressLine1)),
      Some(name(address.ukAddressLine2).formatHtml),
      address.ukAddressLine3.map(line => name(line).formatHtml),
      address.ukAddressLine4.map(line => name(line).formatHtml),
      Some(address.postCode).filter(_.nonEmpty),
      Some(address.countryCode).filter(code => code.nonEmpty && code != "GB").map(countryName)
    ).flatten.mkString("\n").trim

  def addressLayout(address: UkAddress)(implicit messages: Messages, appConfig: AppConfig): Html =
    Html(AddressHelper.addressFormatter(address).replace("\n", "<br/>"))
}
