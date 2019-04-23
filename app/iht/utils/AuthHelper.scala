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

import java.net.URLEncoder

import iht.config.AppConfig

object IhtSection extends Enumeration {
  val Registration, Application = Value
}

trait AuthHelper {
  val appConfig: AppConfig

  def getIVUrlForFailedConfidenceLevel(ihtSection: IhtSection.Value, requiredConfidenceLevel: Int): String = {
    lazy val ivUpliftUrl = appConfig.ivUrlUplift

    val (postSignInUrl, notAuthorisedUrl) = ihtSection match {
      case IhtSection.Registration => (appConfig.postIVRedirectUrlRegistration, appConfig.notAuthorisedRedirectUrlRegistration)
      case IhtSection.Application => (appConfig.postIVRedirectUrlApplication, appConfig.notAuthorisedRedirectUrlApplication)
      case _ => throw new RuntimeException("Could not figure out composite page visibility predicate")
    }

    ivUpliftUrl +
      s"completionURL=${URLEncoder.encode(postSignInUrl, "UTF-8")}" +
      s"&failureURL=${URLEncoder.encode(notAuthorisedUrl, "UTF-8")}" +
      s"&confidenceLevel=$requiredConfidenceLevel"
  }


  def getIhtSignInUrl: String = appConfig.ggSignInUrl

  def getIhtContinueUrl(ihtSection: IhtSection.Value): String = ihtSection match {
    case IhtSection.Registration => appConfig.postSignInRedirectUrlRegistration
    case IhtSection.Application => appConfig.postSignInRedirectUrlApplication
    case _ => throw new RuntimeException("Could not figure out tax regime")
  }
}
