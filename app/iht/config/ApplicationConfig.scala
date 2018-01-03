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

package iht.config

import java.net.URLEncoder

import play.api.Play._
import uk.gov.hmrc.play.config.ServicesConfig

trait AppConfig {
  val analyticsToken: Option[String]
  val analyticsHost: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val betaFeedbackUrl: String
  val betaFeedbackUnauthenticatedUrl: String
  val refreshInterval: Int
  val timeOutSeconds: Int
  val timeOutCountdownSeconds: Int
  val enableRefresh : Boolean
  val postSignInRedirectUrlRegistration: String
  val notAuthorisedRedirectUrlRegistration:String
  val postSignInRedirectUrlApplication: String
  val notAuthorisedRedirectUrlApplication:String
  val ivUpliftConfidenceLevel: Int
  val ivUrl: String
  val ggSignInUrl: String
  val ggSignInFullUrlRegistration: String
  val ggSignInFullUrlApplication: String
  val ivUrlJourney:String
  val ivUrlUplift:String
  val runningEnvironment: String
  val isWelshEnabled: Boolean
}

object ApplicationConfig extends AppConfig with ServicesConfig {

  def readFromConfig(key: String) = configuration.getString(key).getOrElse(throw new Exception(s"Missing key: $key")).toString.trim

  /**
    *This method is used where the environment is not Local and separate host and port values are not needed
    */
  def readOrEmpty(key: String) = configuration.getString(key).getOrElse("")

  override lazy val analyticsToken: Option[String] = configuration.getString("google-analytics.token")
  override lazy val analyticsHost: String = configuration.getString("google-analytics.host").getOrElse("auto")

  private lazy val contactFrontendService = baseUrl("contact-frontend")

  override lazy val reportAProblemPartialUrl = s"$contactFrontendService/contact/problem_reports"
  override lazy val reportAProblemNonJSUrl = s"$contactFrontendService/contact/problem_reports_nonjs?service=iht"

  override lazy val betaFeedbackUrl = "/contact/beta-feedback"
  override lazy val betaFeedbackUnauthenticatedUrl = "/contact/beta-feedback-unauthenticated"

  override val runningEnvironment: String =  configuration.getString("current-environment").getOrElse("local")

  override lazy val timeOutSeconds = configuration.getString("session.timeoutSeconds").getOrElse("900").toInt
  override lazy val timeOutCountdownSeconds = configuration.getString("session.time-out-countdown-seconds").getOrElse("300").toInt
  override lazy val refreshInterval = timeOutSeconds + 10
  override lazy val enableRefresh = configuration.getBoolean("enableRefresh").getOrElse(true)

  //IV redirect urls.
  override lazy val postSignInRedirectUrlRegistration = readFromConfig(s"$env.microservice.iv.login-callback.registration.url")
  override lazy val notAuthorisedRedirectUrlRegistration = readFromConfig(s"$env.microservice.iv.not-authorised-callback.registration.url")
  override lazy val postSignInRedirectUrlApplication = readFromConfig(s"$env.microservice.iv.login-callback.application.url")
  override lazy val notAuthorisedRedirectUrlApplication = readFromConfig(s"$env.microservice.iv.not-authorised-callback.application.url")

  //IV hosts.
  override val ivUrlJourney:String = baseUrl("identity-verification") + "/mdtp/journey/journeyId/"
  override val ivUrl: String =  readOrEmpty(s"$env.microservice.iv.identity-verification-frontend.host")
  override val ivUrlUplift:String = s"$ivUrl/mdtp/uplift?origin=IHT&"
  override val ggSignInUrl: String = readFromConfig(s"$env.microservice.iv.government-gateway-sign-in.host")
  override val ggSignInFullUrlRegistration: String = s"$ggSignInUrl?" +
    s"continue=${URLEncoder.encode(postSignInRedirectUrlRegistration, "UTF-8")}&origin=iht-frontend&accountType=individual"
  override val ggSignInFullUrlApplication: String = s"$ggSignInUrl?" +
    s"continue=${URLEncoder.encode(postSignInRedirectUrlApplication, "UTF-8")}&origin=iht-frontend&accountType=individual"

  //IV confidence level.
  override lazy val ivUpliftConfidenceLevel: Int = configuration.getString("iv-uplift.confidence-level").getOrElse("50").toInt
  // If you want to override the default visibility for Welsh language toggle then you need to add this key in the respective env
  // Default visibility - off in PROD and on in every other env
  override val isWelshEnabled: Boolean  = configuration.getBoolean("welsh.enabled").getOrElse(runningEnvironment != "PROD")
}
