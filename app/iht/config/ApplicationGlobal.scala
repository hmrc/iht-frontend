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
import play.api.i18n.{Lang, Messages}
import play.api.i18n.Messages.Implicits._
import play.api.mvc._
import play.api.{Application, Configuration, Logger, Play}
import play.twirl.api.Html
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.http.Upstream5xxResponse
import uk.gov.hmrc.play.config.{AppName, ControllerConfig, RunMode}
import uk.gov.hmrc.play.frontend.bootstrap.DefaultFrontendGlobal
import uk.gov.hmrc.play.frontend.filters.{FrontendAuditFilter, FrontendLoggingFilter, MicroserviceFilterSupport}
import uk.gov.hmrc.play.partials.FormPartialRetriever
import play.api.mvc.Results._


object ApplicationGlobal extends DefaultFrontendGlobal with RunMode {

  override val auditConnector = IhtAuditConnector
  override val loggingFilter = IhtLoggingFilter
  override val frontendAuditFilter = IhtAuditFilter
  implicit val formPartialRetriever: FormPartialRetriever = IhtFormPartialRetriever

  private implicit def rhToRequest(rh: RequestHeader): Request[_] = Request(rh, "")

  override def onStart(app: Application) {
    super.onStart(app)
    ApplicationCrypto.verifyConfiguration()
  }

  def desInternalServerErrorTemplate(implicit request: Request[_]): Html = {
    implicit val lang: Lang = new Lang(s"${request.cookies.get("PLAY_LANG").map(_.value).getOrElse("en")}")
    request.uri match {
      case s: String if s.contains("/registration/") => iht.views.html.registration.registration_generic_error()
      case s: String if s.contains("/estate-report/") => iht.views.html.application.application_generic_error()
      case _ =>     standardErrorTemplate(
        Messages("global.error.InternalServerError500.title"),
        Messages("global.error.InternalServerError500.heading"),
        Messages("global.error.InternalServerError500.message")
      )
    }
  }

  override def resolveError(rh: RequestHeader, ex: Throwable): Result =
    ex match {
    case e: Upstream5xxResponse if e.upstreamResponseCode == 502 &&
      e.message.contains("500 response returned from DES") => InternalServerError(desInternalServerErrorTemplate(rh))
    case _ => super.resolveError(rh, ex)
  }



  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: Request[_]): Html = {
    implicit val lang: Lang = new Lang(s"${request.cookies.get("PLAY_LANG").map(_.value).getOrElse("en")}")
    iht.views.html.iht_error_template()
  }


  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig("microservice.metrics")
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
