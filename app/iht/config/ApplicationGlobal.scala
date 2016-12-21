/*
 * Copyright 2016 HM Revenue & Customs
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

import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import play.api.mvc._
import play.api.{Application, Configuration, Play}
import play.filters.headers._
import play.twirl.api.Html
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.play.audit.filters.FrontendAuditFilter
import uk.gov.hmrc.play.config.{AppName, ControllerConfig, RunMode}
import uk.gov.hmrc.play.frontend.bootstrap.DefaultFrontendGlobal
import uk.gov.hmrc.play.http.logging.filters.FrontendLoggingFilter

object ApplicationGlobal extends DefaultFrontendGlobal with RunMode {

  override val auditConnector = IhtAuditConnector
  override val loggingFilter = IhtLoggingFilter
  override val frontendAuditFilter = IhtAuditFilter

  override def onStart(app: Application) {
    super.onStart(app)
    ApplicationCrypto.verifyConfiguration()
  }

  override def doFilter(a: EssentialAction): EssentialAction = {
    val securityFilter = {
      val configuration = play.api.Play.current.configuration
      val securityHeadersConfig:DefaultSecurityHeadersConfig = new SecurityHeadersParser().parse(configuration).asInstanceOf[DefaultSecurityHeadersConfig]
      val sameOriginConfig:SecurityHeadersConfig = securityHeadersConfig.copy(
        frameOptions = Some("SAMEORIGIN"),
        xssProtection = Some("1"),
        contentTypeOptions = Some("nosniff"),
        None,
        None)
      SecurityHeadersFilter(sameOriginConfig)
    }
    Filters(super.doFilter(a), Seq(securityFilter):_*)
  }

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: Request[_]): Html =
    iht.views.html.iht_error_template(pageTitle, heading, message)

  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig(s"$env.microservice.metrics")
}

object ControllerConfiguration extends ControllerConfig {
  lazy val controllerConfigs = Play.current.configuration.underlying.as[Config]("controllers")
}

object IhtLoggingFilter extends FrontendLoggingFilter {
  override def controllerNeedsLogging(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsLogging
}

object IhtAuditFilter extends FrontendAuditFilter with RunMode with AppName {

  override lazy val maskedFormFields = Seq.empty

  override lazy val applicationPort = None

  override lazy val auditConnector = IhtAuditConnector

  override def controllerNeedsAuditing(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsAuditing
}
