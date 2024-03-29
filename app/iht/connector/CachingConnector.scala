/*
 * Copyright 2022 HM Revenue & Customs
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

import java.io

import iht.models._
import iht.models.application.{ApplicationDetails, ProbateDetails}
import javax.inject.{Inject, Named}
import play.api.libs.json._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class CachingConnectorImpl @Inject()(val http: DefaultHttpClient,
                                     config: ServicesConfig,
                                     @Named("appName") val defaultSource: String) extends CachingConnector {
  override lazy val baseUri: String = config.baseUrl("cachable.session-cache")
  override lazy val domain: String = config.getConfString("cachable.session-cache.domain",
    throw new Exception(s"Could not find config 'cachable.session-cache.domain'"))
}

trait CachingConnector extends SessionCache {
  private val registrationDetailsFormKey = "registrationDetails"
  private val applicationDetailsFormKey = "applicationDetails"
  private val kickoutDetailsKey = "kickoutDetails"
  private val probateDetailsKey = "probateDetails"

  private def getChangeData[A](formKey: String)(implicit hc: HeaderCarrier, reads: Reads[A], ec: ExecutionContext): Future[Option[A]] =
    fetchAndGetEntry[A](formKey)

  private def storeChangeData[A](formKey: String, data: A)(implicit hc: HeaderCarrier, writes: Writes[A], reads: Reads[A], ec: ExecutionContext) =
    cache[A](formKey, data) flatMap { cachedData =>
      Future.successful(cachedData.getEntry[A](formKey))
    }

  def storeKickoutDetails(data: KickoutDetails)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[KickoutDetails]] =
    storeChangeData[KickoutDetails](kickoutDetailsKey, data)

  def storeData(key: String, data: JsValue)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[io.Serializable]] = {
        key match {
          case `registrationDetailsFormKey` =>
            data.validate[RegistrationDetails] match {
              case JsError(_) => Future.successful(None)
              case JsSuccess(reg, _) => storeRegistrationDetails(reg)
            }
          case `kickoutDetailsKey` =>
            data.validate[KickoutDetails] match {
              case JsError(_) => Future.successful(None)
              case JsSuccess(kickoutDetails, _) => storeKickoutDetails(kickoutDetails)
            }
          case _ =>
            data.validate[String] match {
              case JsError(_) => Future.successful(None)
              case JsSuccess(value, _) => storeSingleValue(key, value)
            }
        }
    }

  def cacheDelete(key: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Any] = {
    fetch.flatMap {
      case Some(x) =>
        remove().map { response =>
          response.status match {
            case 204 =>
              val changedCacheData = x.data - key
              for {
                value <- changedCacheData.map(z => storeData(z._1, z._2))
              } yield {
                value
              }
            case _ =>
              Future.successful(None)
          }
        }
      case None =>
        Future.successful(None)
    }
  }

  def storeRegistrationDetails(data: RegistrationDetails)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[RegistrationDetails]] =
    storeChangeData[RegistrationDetails](registrationDetailsFormKey, data)

  def getRegistrationDetails(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[RegistrationDetails]] =
    getChangeData[RegistrationDetails](registrationDetailsFormKey)

  def storeApplicationDetails(data: ApplicationDetails)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[ApplicationDetails]] =
    storeChangeData[ApplicationDetails](applicationDetailsFormKey, data)

  def getApplicationDetails(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[ApplicationDetails]] =
    getChangeData[ApplicationDetails](applicationDetailsFormKey)

  def storeSingleValue(formKey: String, data: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[String]] =
    storeChangeData[String](formKey, data)

  def getSingleValue(formKey: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[String]] =
    getChangeData[String](formKey)

  def storeProbateDetails(data: ProbateDetails)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[ProbateDetails]] =
    storeChangeData[ProbateDetails](probateDetailsKey, data)

  def getProbateDetails(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[ProbateDetails]] =
    getChangeData[ProbateDetails](probateDetailsKey)

  def deleteSingleValue(key: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Unit ={
    getSingleValue(key).map{value =>
      if(value.isDefined){
        for{
          deleteOutcome <- cacheDelete(key)
        } yield {
          deleteOutcome match {
            case Success(x) => x
            case Failure(x) =>
              throw new RuntimeException("Can't delete single value:" + x.getMessage)
          }
        }
      }
    }
  }
}
