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

import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc._
import play.api.{Application, Configuration, Play}
import play.twirl.api.Html
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.play.config.{AppName, ControllerConfig, RunMode}
import uk.gov.hmrc.play.frontend.bootstrap.DefaultFrontendGlobal
import uk.gov.hmrc.play.frontend.filters.{FrontendAuditFilter, FrontendLoggingFilter, MicroserviceFilterSupport}
import uk.gov.hmrc.play.partials.FormPartialRetriever

object ApplicationGlobal extends DefaultFrontendGlobal with RunMode {

  override val auditConnector = IhtAuditConnector
  override val loggingFilter = IhtLoggingFilter
  override val frontendAuditFilter = IhtAuditFilter
  implicit val formPartialRetriever: FormPartialRetriever = IhtFormPartialRetriever

  override def onStart(app: Application) {
    super.onStart(app)
    ApplicationCrypto.verifyConfiguration()
  }

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: Request[_]): Html =
    request.uri match {
      case s: String if s.contains("/registration/") => iht.views.html.registration.registration_generic_error()
      case s: String if s.contains("/estate-report/") => iht.views.html.application.application_generic_error()
      case _ => iht.views.html.iht_error_template(pageTitle, heading, message)
    }

  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig(s"$env.microservice.metrics")
}

object ControllerConfiguration extends ControllerConfig {
  lazy val controllerConfigs = Play.current.configuration.underlying.as[Config]("controllers")
}

object IhtLoggingFilter extends FrontendLoggingFilter with MicroserviceFilterSupport {
  override def controllerNeedsLogging(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsLogging
}

object IhtAuditFilter extends FrontendAuditFilter with RunMode with AppName with MicroserviceFilterSupport {

  override lazy val maskedFormFields = Seq.empty

  override lazy val applicationPort = None

  override lazy val auditConnector = IhtAuditConnector

  override def controllerNeedsAuditing(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsAuditing
}
