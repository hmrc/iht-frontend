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

import javax.inject.Inject
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.hooks.HttpHooks
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.config.{AppName, RunMode, ServicesConfig}
import uk.gov.hmrc.play.frontend.config.LoadAuditingConfig
import uk.gov.hmrc.play.frontend.filters.SessionCookieCryptoFilter
import uk.gov.hmrc.play.http.ws._
import uk.gov.hmrc.play.partials.{CachedStaticHtmlPartialRetriever, FormPartialRetriever}

object WsAllMethods extends WSHttp with HttpAuditing with AppName with RunMode {
  override lazy val auditConnector = IhtAuditConnector
  override val hooks = Seq (AuditingHook)
}

object IhtAuditConnector extends AuditConnector with RunMode {
  override lazy val auditingConfig = LoadAuditingConfig("auditing")
}

trait Hooks extends HttpHooks with HttpAuditing {
  override val hooks = Seq(AuditingHook)
  override lazy val auditConnector: AuditConnector = IhtAuditConnector
}
trait WSHttp extends HttpGet with WSGet with HttpPut with WSPut with HttpPost with WSPost with HttpDelete with WSDelete with Hooks with AppName
object WSHttp extends WSHttp

object CachedStaticHtmlPartialProvider extends CachedStaticHtmlPartialRetriever {
  override val httpGet = WSHttp
}

object IhtFormPartialRetriever extends FormPartialRetriever {
  override def crypto = SessionCookieCryptoFilter.encrypt
  override val httpGet = WsAllMethods
}

object FrontendAuthConnector extends PlayAuthConnector with ServicesConfig with WSHttp {
  val serviceUrl: String = baseUrl("auth")
  lazy val http = WSHttp
}
