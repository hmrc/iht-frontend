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
import javax.inject.Inject
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.hooks.{HttpHook, HttpHooks}
import uk.gov.hmrc.play.http.ws._

class DefaultWSHttp @Inject()(val actorSystem: ActorSystem) extends WSHttp {
  override val hooks: Seq[HttpHook] = NoneRequired
  override protected def configuration: Option[Config] = None
}

trait WSHttp extends
  HttpGet with WSGet with
  HttpPut with WSPut with
  HttpPatch with WSPatch with
  HttpPost with WSPost with
  HttpDelete with WSDelete with
  HttpHooks