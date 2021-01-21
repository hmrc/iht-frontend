/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.Logger
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.{ExecutionContext, Future}

class CitizenDetailsConnectorImpl @Inject()(val http: DefaultHttpClient,
                                            val config: ServicesConfig) extends CitizenDetailsConnector {
  lazy val serviceUrl: String = config.baseUrl("citizen-details")
}

trait CitizenDetailsConnector {
  def http: HttpGet with HttpPost with HttpPut with HttpDelete

  def serviceUrl: String

  def getCitizenDetails(nino: Nino)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[CidPerson] = {
    Logger.info("Calling Citizen Details service to retrieve personal details")
    http.GET[CidPerson](s"$serviceUrl/citizen-details/nino/$nino")
  }
}

