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

package iht.connector

import iht.models.CidPerson
import javax.inject.Inject
import play.api.Mode.Mode
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

class CitizenDetailsConnectorImpl @Inject()(override val http: HttpClient,
                                            val environment: Environment,
                                            val config: Configuration) extends CitizenDetailsConnector with ServicesConfig {
  lazy val serviceUrl: String = baseUrl("citizen-details")
  val mode: Mode = environment.mode
  override def runModeConfiguration: Configuration = config
}

trait CitizenDetailsConnector {
  def http: HttpGet with HttpPost with HttpPut with HttpDelete

  def serviceUrl: String

  def getCitizenDetails(nino: Nino)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[CidPerson] = {
    Logger.info("Calling Citizen Details service to retrieve personal details")
    http.GET[CidPerson](s"$serviceUrl/citizen-details/nino/$nino")
  }
}

