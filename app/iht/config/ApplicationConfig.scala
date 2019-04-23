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

import iht.constants.IhtProperties
import javax.inject.Inject
import play.api.Environment
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.util.Try

class DefaultAppConfig @Inject()(val servicesConfig: ServicesConfig,
                                 val propertiesReader: IhtPropertyRetriever,
                                 val environment: Environment) extends AppConfig

trait AppConfig extends IhtProperties {
  val servicesConfig: ServicesConfig

  def readFromConfig(key: String): String = servicesConfig.getString(key).trim
  def readOrEmpty(key: String): String = servicesConfig.getString(key)

  lazy val analyticsToken: Option[String] = Some(servicesConfig.getString("google-analytics.token"))
  lazy val analyticsHost: String = Try(servicesConfig.getString("google-analytics.host")).getOrElse("auto")

  private lazy val contactFrontendService = servicesConfig.baseUrl("contact-frontend")
  private lazy val contactFrontendHost = Try(servicesConfig.getString("microservice.services.contact-frontend.host")).getOrElse("")

  lazy val contactFormServiceIdentifier = "IHT"
  lazy val contactFrontendPartialBaseUrl = s"$contactFrontendService"
  lazy val reportAProblemPartialUrl = s"$contactFrontendHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactFrontendHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val feedbackSurvey: String = readFromConfig(s"feedback-survey-frontend.url")

  lazy val runningEnvironment: String = Try(servicesConfig.getString("current-environment")).getOrElse("local")

  lazy val timeOutSeconds: Int = Try(servicesConfig.getString("session.timeoutSeconds")).getOrElse("900").toInt
  lazy val timeOutCountdownSeconds: Int = Try(servicesConfig.getString("session.time-out-countdown-seconds")).getOrElse("300").toInt
  lazy val refreshInterval: Int = timeOutSeconds + 10
  lazy val enableRefresh: Boolean = Try(servicesConfig.getBoolean("enableRefresh")).getOrElse(true)

  //IV redirect urls.
  lazy val postSignInRedirectUrlRegistration: String = readFromConfig("microservice.iv.login-pass.registration.url")
  lazy val postIVRedirectUrlRegistration: String = readFromConfig("microservice.iv.verification-pass.registration.url")
  lazy val notAuthorisedRedirectUrlRegistration: String = readFromConfig("microservice.iv.not-authorised-callback.registration.url")
  lazy val postSignInRedirectUrlApplication: String = readFromConfig("microservice.iv.login-pass.application.url")
  lazy val postIVRedirectUrlApplication: String = readFromConfig("microservice.iv.verification-pass.application.url")
  lazy val notAuthorisedRedirectUrlApplication: String = readFromConfig("microservice.iv.not-authorised-callback.application.url")

  //IV hosts.
  lazy val ivUrlJourney:String = servicesConfig.baseUrl("identity-verification") + "/mdtp/journey/journeyId/"
  lazy val ivUrl: String =  readOrEmpty("microservice.iv.identity-verification-frontend.host")
  lazy val ivUrlUplift:String = s"$ivUrl/mdtp/uplift?origin=IHT&"
  lazy val ggSignInUrl: String = readFromConfig("microservice.iv.government-gateway-sign-in.host")
  lazy val ggSignInFullUrlRegistration: String = s"$ggSignInUrl?" +
    s"continue=${URLEncoder.encode(postSignInRedirectUrlRegistration, "UTF-8")}&origin=iht-frontend"
  lazy val ggSignInFullUrlApplication: String = s"$ggSignInUrl?" +
    s"continue=${URLEncoder.encode(postSignInRedirectUrlApplication, "UTF-8")}&origin=iht-frontend"

  //IV confidence level.
  lazy val ivUpliftConfidenceLevel: Int = Try(servicesConfig.getString("iv-uplift.confidence-level")).getOrElse("50").toInt
  // If you want to the default visibility for Welsh language toggle then you need to add this key in the respective env
  // Default visibility - off in PROD and on in every other env
  lazy val isWelshEnabled: Boolean = Try(servicesConfig.getBoolean("welsh.enabled")).getOrElse(runningEnvironment != "PROD")
}