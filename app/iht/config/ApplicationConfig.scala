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

package iht.config

import java.net.URLEncoder

import javax.inject.Inject
import uk.gov.hmrc.play.config.ServicesConfig

trait AppConfig extends ServicesConfig {
  def readFromConfig(key: String): String = getString(key)

  /**
    *This method is used where the environment is not Local and separate host and port values are not needed
    */
  def readOrEmpty(key: String): String = getConfString(key, "")

  lazy val analyticsToken: Option[String] = Option(getString("google-analytics.token"))
  lazy val analyticsHost: String = getConfString("google-analytics.host", "auto")

  private lazy val contactFrontendService = baseUrl("contact-frontend")
  private lazy val contactFrontendHost = getConfString("microservice.services.contact-frontend.host", "")

  lazy val contactFormServiceIdentifier = "IHT"
  lazy val contactFrontendPartialBaseUrl = s"$contactFrontendService"
  lazy val reportAProblemPartialUrl = s"$contactFrontendHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactFrontendHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val feedbackSurvey: String = readFromConfig(s"feedback-survey-frontend.url")

  lazy val runningEnvironment: String =  getConfString("current-environment", "local")

  lazy val timeOutSeconds: Int = getConfString("session.timeoutSeconds", "900").toInt
  lazy val timeOutCountdownSeconds: Int = getConfString("session.time-out-countdown-seconds", "300").toInt
  lazy val refreshInterval: Int = timeOutSeconds + 10
  lazy val enableRefresh: Boolean = getConfBool("enableRefresh", true)

  //IV redirect urls.
  lazy val postSignInRedirectUrlRegistration: String = readFromConfig("microservice.iv.login-pass.registration.url")
  lazy val postIVRedirectUrlRegistration: String = readFromConfig("microservice.iv.verification-pass.registration.url")
  lazy val notAuthorisedRedirectUrlRegistration: String = readFromConfig("microservice.iv.not-authorised-callback.registration.url")
  lazy val postSignInRedirectUrlApplication: String = readFromConfig("microservice.iv.login-pass.application.url")
  lazy val postIVRedirectUrlApplication: String = readFromConfig("microservice.iv.verification-pass.application.url")
  lazy val notAuthorisedRedirectUrlApplication: String = readFromConfig("microservice.iv.not-authorised-callback.application.url")

  //IV hosts.
  lazy val ivUrlJourney:String = baseUrl("identity-verification") + "/mdtp/journey/journeyId/"
  lazy val ivUrl: String =  readOrEmpty("microservice.iv.identity-verification-frontend.host")
  lazy val ivUrlUplift:String = s"$ivUrl/mdtp/uplift?origin=IHT&"
  lazy val ggSignInUrl: String = readFromConfig("microservice.iv.government-gateway-sign-in.host")
  lazy val ggSignInFullUrlRegistration: String = s"$ggSignInUrl?" +
    s"continue=${URLEncoder.encode(postSignInRedirectUrlRegistration, "UTF-8")}&origin=iht-frontend"
  lazy val ggSignInFullUrlApplication: String = s"$ggSignInUrl?" +
    s"continue=${URLEncoder.encode(postSignInRedirectUrlApplication, "UTF-8")}&origin=iht-frontend"

  //IV confidence level.
  lazy val ivUpliftConfidenceLevel: Int = getConfString("iv-uplift.confidence-level", "50").toInt
  // If you want to the default visibility for Welsh language toggle then you need to add this key in the respective env
  // Default visibility - off in PROD and on in every other env
  lazy val isWelshEnabled: Boolean  = getConfBool("welsh.enabled", runningEnvironment != "PROD")
}

class TestAppConfig @Inject()() extends AppConfig

object ApplicationConfig extends AppConfig
