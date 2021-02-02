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

import iht.config.AppConfig
import iht.models.enums.IdentityVerificationResult.IdentityVerificationResult
import javax.inject.Inject
import play.api.libs.Files.logger
import play.api.libs.json.{JsPath, Json, JsonValidationError, OFormat}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class IdentityVerificationConnectorImpl @Inject()(val http: DefaultHttpClient,
                                                  val appConfig: AppConfig) extends IdentityVerificationConnector

trait IdentityVerificationConnector {
  def http: HttpClient
  val appConfig: AppConfig

  private case class IdentityVerificationResponse(result: IdentityVerificationResult)
  private implicit val formats: OFormat[IdentityVerificationResponse] = Json.format[IdentityVerificationResponse]

  def identityVerificationResponse(journeyId: String)(implicit hc: HeaderCarrier, executionContext: ExecutionContext):
    Future[IdentityVerificationResult] = {
    val url = appConfig.ivUrlJourney + journeyId
    logger.debug(s"Calling identity verification frontend service with url: $url")
    http.GET[HttpResponse](url).flatMap { httpResponse =>
      logger.debug(Json.prettyPrint(httpResponse.json))
      httpResponse.json.validate[IdentityVerificationResponse].fold(
        errs => Future.failed(new JsonValidationException(s"Unable to deserialise: ${formatJsonErrors(errs)}")),
        valid => {
          logger.debug("result " + valid.result.id)
          Future.successful(valid.result)
        }
      )
    }
  }

  private def formatJsonErrors(errors: Seq[(JsPath, Seq[JsonValidationError])]): String = {
    errors.map(p => p._1 + " - " + p._2.map(_.message).mkString(",")).mkString(" | ")
  }

  private class JsonValidationException(message: String) extends Exception(message)
}
