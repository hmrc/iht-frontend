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

import akka.actor.ActorSystem
import com.typesafe.config.Config
import play.api.Mode.Mode
import play.api.{Configuration, Play}
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.hooks.HttpHooks
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.config.{AppName, RunMode, ServicesConfig}
import uk.gov.hmrc.play.frontend.config.LoadAuditingConfig
import uk.gov.hmrc.play.frontend.filters.SessionCookieCryptoFilter
import uk.gov.hmrc.play.http.ws._
import uk.gov.hmrc.play.partials.{CachedStaticHtmlPartialRetriever, FormPartialRetriever}

trait WiringConfig extends RunMode {
  lazy val runModeConfiguration: Configuration = Play.current.configuration
  lazy val appNameConfiguration: Configuration = Play.current.configuration
  lazy val actorSystem: ActorSystem = Play.current.actorSystem
  lazy val mode: Mode = Play.current.mode
}

object WsAllMethods extends WSHttp with HttpAuditing with AppName with WiringConfig {
  override lazy val auditConnector: IhtAuditConnector.type = IhtAuditConnector
  override val hooks: Seq[WsAllMethods.AuditingHook.type] = Seq(AuditingHook)

  override protected def configuration: Option[Config] = Some(runModeConfiguration.underlying)
}

object IhtAuditConnector extends AuditConnector with WiringConfig {
  override lazy val auditingConfig = LoadAuditingConfig("auditing")
}

trait Hooks extends HttpHooks with HttpAuditing {
  override val hooks = Seq(AuditingHook)
  override lazy val auditConnector: AuditConnector = IhtAuditConnector
}
trait WSHttp extends HttpGet with WSGet with HttpPut with WSPut with HttpPost with WSPost with HttpDelete with WSDelete with Hooks with WiringConfig
object WSHttp extends WSHttp {
  override def appName: String = AppName(Play.current.configuration).appName
  override protected def configuration: Option[Config] = Some(runModeConfiguration.underlying)
}

object CachedStaticHtmlPartialProvider extends CachedStaticHtmlPartialRetriever {
  override val httpGet = WSHttp
}

object IhtFormPartialRetriever extends FormPartialRetriever {
  override def crypto: String => String = (cookie: String) => new SessionCookieCryptoFilter(
    new ApplicationCrypto(Play.current.configuration.underlying)
  ).encrypt(cookie)
  override val httpGet = WsAllMethods
}

object FrontendAuthConnector extends PlayAuthConnector with ServicesConfig with WSHttp with WiringConfig {
  val serviceUrl: String = baseUrl("auth")
  lazy val http = WSHttp

  override protected def configuration: Option[Config] = Some(runModeConfiguration.underlying)
  override def appName: String = AppName(Play.current.configuration).appName
}
