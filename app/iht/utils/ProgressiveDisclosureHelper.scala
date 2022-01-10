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

object ProgressiveDisclosureHelper {
  def getDisclosureInfo(location: String): (String, Seq[String]) = {
    if (checkIfError(location)) {
      ("site.progressiveDisclosure.preRegistration.contact", Seq("site.progressiveDisclosure.preRegistration.help"))
    }else if (checkLocationLength(location)) {
      location.split("/").take(3).last match {
        case "estate-report" => ("site.progressiveDisclosure.application.contact", Seq("site.progressiveDisclosure.application.help.start", "site.progressiveDisclosure.application.linkText", "site.progressiveDisclosure.application.help.end"))
        case "registration" => ("site.progressiveDisclosure.registration.contact", Seq())
        case _ => ("site.progressiveDisclosure.preRegistration.contact", Seq("site.progressiveDisclosure.preRegistration.help"))
      }
    } else {("", Seq())}
  }

  def checkIfError(location: String): Boolean = location.contains("timeout") || location.contains("identity-verification-problem")

  def checkLocationLength(location: String): Boolean = location.split("/").length > 2
}
