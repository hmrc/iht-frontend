/*
 * Copyright 2017 HM Revenue & Customs
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

package iht.services.http

import javax.inject.{Inject, Singleton}

import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.HttpAuditing.AuditingHook
import uk.gov.hmrc.play.config.{RunMode, AppName}
import uk.gov.hmrc.play.http.ws.WSHttp
import iht.config.IhtAuditConnector
import uk.gov.hmrc.play.audit.http
/**
  * Created by vineet on 02/08/17.
  */
@Singleton
class WsAllMethods @Inject() (override val auditConnector: IhtAuditConnector) extends WSHttp with HttpAuditing with AppName with RunMode {
  override val hooks = Seq (AuditingHook)
}
