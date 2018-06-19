/*
 * Copyright 2018 HM Revenue & Customs
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

  def getContactInfo(location: String): String = {
    if (checkLocationLength(location)) {
      location.split("/").take(3).last match {
        case "estate-report" => "site.progressiveDisclosure.application.contact"
        case "registration" => "site.progressiveDisclosure.registration.contact"
        case _ => "site.progressiveDisclosure.preRegistration.contact"
      }
    } else { "" }
  }


  def getHelpInfo(location: String): Seq[String] = {
    if (checkLocationLength(location)) {
      location.split("/").take(3).last match {
        case "estate-report" => Seq("site.progressiveDisclosure.application.help", "site.progressiveDisclosure.application.linkText")
        case "registration" => Seq()
        case _ => Seq("site.progressiveDisclosure.preRegistration.help")
      }
    } else { Seq() }
  }

  def checkLocationLength(location: String): Boolean = location.split("/").length > 2
}


