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

import iht.models._
import iht.views.html._
import play.api.i18n.{Lang, Messages}
import play.twirl.api.Html

object AddressHelper {

  val isThereAnApplicantAddress: Predicate = (rd, _) => rd.applicantDetails.flatMap(_.ukAddress).isDefined

  def addressFormater(applicantAddress: UkAddress)(implicit messages: Messages): String = {
    var address: String = ihtHelpers.custom.name(applicantAddress.ukAddressLine1.toString) +
      " \n" + ihtHelpers.custom.name(applicantAddress.ukAddressLine2.toString).toString.replace("\n", "")

    if (applicantAddress.ukAddressLine3.isDefined) {
      address += " \n" + ihtHelpers.custom.name(applicantAddress.ukAddressLine3.getOrElse("").toString).toString.replace("\n", "")
    }

    if (applicantAddress.ukAddressLine4.isDefined) {
      address += " \n" + ihtHelpers.custom.name(applicantAddress.ukAddressLine4.getOrElse("").toString).toString.replace("\n", "")
    }

    if (applicantAddress.postCode.toString != "") {
      address += " \n" + applicantAddress.postCode.toString
    }

    if (countryName(applicantAddress.countryCode) != "" && applicantAddress.countryCode != "GB") {
      address += " \n" + countryName(applicantAddress.countryCode)
    }

    address.toString().trim()
  }

  def addressLayout(address: UkAddress)(implicit messages: Messages): Html = {
    Html(AddressHelper.addressFormater(address).replace("\n", "<br/>"))
  }

}
