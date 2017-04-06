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

package iht.connector

import iht.config.WSHttp
import iht.exceptions.NoRegistrationDetailsException
import iht.models._
import iht.models.application.{ApplicationDetails, ProbateDetails}
import iht.utils.CommonHelper
import play.api.Logger
import play.api.libs.json.{JsValue, Json, Reads, Writes}
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.config.{AppName, ServicesConfig}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


object SessionHttpCaching extends SessionCache with AppName with ServicesConfig {
  override lazy val http = WSHttp
  override lazy val defaultSource = appName
  override lazy val baseUri = baseUrl("cachable.session-cache")
  override lazy val domain = getConfString("cachable.session-cache.domain", throw new Exception(s"Could not find config 'cachable.session-cache.domain'"))
}

object CachingConnector extends CachingConnector

trait CachingConnector {

  private val registrationDetailsFormKey = "registrationDetails"
  private val applicationDetailsFormKey = "applicationDetails"
  private val kickoutDetailsKey = "kickoutDetails"
  private val allAssetsKey = "allAssets"
  private val allLiabilitiesKey = "allLiabilities"
  private val propertyListKey = "propertyList"
  private val probateDetailsKey = "probateDetails"

  def delete(key: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Any] = {
    SessionHttpCaching.fetch.map {
      case Some(x) => {
        Await.ready(SessionHttpCaching.remove(), Duration.Inf)
        val changedCacheData = x.data - key
        Await.ready(Future.sequence(changedCacheData.map(z => readdData(z._1, z._2))), Duration.Inf)
      }
      case None => Future.successful(None)
    }
  }

  private def readdData(key: String, data: JsValue)(implicit hc: HeaderCarrier, ec: ExecutionContext) = {
    key match {
      case `registrationDetailsFormKey` => {
        storeRegistrationDetails(Json.fromJson[RegistrationDetails](data).get)
      }
      case `kickoutDetailsKey` => {
        storeKickoutDetails(Json.fromJson[KickoutDetails](data).get)
      }
      case _ => {
        storeSingleValue(key, Json.fromJson[String](data).get)
      }
    }
  }

  def storeRegistrationDetails(data: RegistrationDetails)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[RegistrationDetails]] = {
    storeChangeData[RegistrationDetails] (registrationDetailsFormKey, data)
  }

  //
  //  def getExistingRegistrationDetails(implicit ec: ExecutionContext, headerCarrier: HeaderCarrier): RegistrationDetails = {
  //    val optionRD: Option[RegistrationDetails] = Await.result(getRegistrationDetails, Duration.Inf)
  //    optionRD.fold(throw new NoRegistrationDetailsException)(identity)
  //  }

  def getRegistrationDetails(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[RegistrationDetails]] = {
    getChangeData[RegistrationDetails](registrationDetailsFormKey)
  }

  def storeApplicationDetails(data: ApplicationDetails)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[ApplicationDetails]] = {
    storeChangeData[ApplicationDetails](applicationDetailsFormKey, data)
  }

  def getApplicationDetails(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[ApplicationDetails]] = {
    getChangeData[ApplicationDetails](applicationDetailsFormKey)
  }

  def storeSingleValue(formKey: String, data: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[String]] = {
    storeChangeData[String](formKey, data)
  }

  def getSingleValue(formKey: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[String]] = {
    getChangeData[String](formKey)
  }

  def storeKickoutDetails(data: KickoutDetails)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[KickoutDetails]] = {
    storeChangeData[KickoutDetails](kickoutDetailsKey, data)
  }

  def getKickoutDetails(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[KickoutDetails]] = {
    getChangeData[KickoutDetails](kickoutDetailsKey)
  }

  /*
   * Store the Probate Details in Key store
   */

  def storeProbateDetails(data: ProbateDetails)(implicit hc: HeaderCarrier, ec: ExecutionContext):
  Future[Option[ProbateDetails]] = {
    storeChangeData[ProbateDetails](probateDetailsKey, data)
  }

  /*
   * fetch the Probate Details from Key store
   */

  def getProbateDetails(implicit hc: HeaderCarrier, ec: ExecutionContext):
  Future[Option[ProbateDetails]] = {
    getChangeData[ProbateDetails](probateDetailsKey)
  }


  private def getChangeData[A](formKey: String)(implicit hc: HeaderCarrier, reads: Reads[A], ec: ExecutionContext): Future[Option[A]] = {
    SessionHttpCaching.fetchAndGetEntry[A](formKey)
  }

  private def storeChangeData[A](formKey: String, data: A)(implicit hc: HeaderCarrier, writes: Writes[A], reads: Reads[A], ec: ExecutionContext) = {
    SessionHttpCaching.cache[A](formKey, data) flatMap {
      case data => Future.successful(data.getEntry[A](formKey))
    }
  }

  private def clearChangeData[A](formKey: String, clearedData: A)
                                (implicit hc: HeaderCarrier, writes: Writes[A], reads: Reads[A], ec: ExecutionContext) = {
    SessionHttpCaching.cache[A](formKey, clearedData)
  }

  /**
   * Store a string value in keystore an item using the specified key.
   */
  def storeSingleValueSync(formKey: String, data: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Option[String] = {
    val futureOptionString: Future[Option[String]] = Await.ready(storeSingleValue(formKey, data),
      Duration.Inf)
    val optionTryOptionString: Option[Try[Option[String]]] = futureOptionString.value
    optionTryOptionString.fold(throw new RuntimeException("Can't store single value: None returned")) {
      case Success(x) => x
      case Failure(x) => throw new RuntimeException("Can't store single value:" + x.getMessage)
    }
  }

  /**
   * Get from keystore the String value with the specified key. Returns None if the
   * item does not exist in keystore.
   */
  def getSingleValueSync(formKey: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Option[String] = {
    val futureOptionString: Future[Option[String]] = Await.ready(getSingleValue(formKey),
      Duration.Inf)
    val optionTryOptionString: Option[Try[Option[String]]] = futureOptionString.value

    optionTryOptionString.fold(throw new RuntimeException("Can't get single value: None returned")) {
      case Success(x) => x
      case Failure(x) => throw new RuntimeException("Can't return single value:" + x.getMessage)
    }
  }

  /**
   * Delete from keystore a String item with the specified key.
   * If the item does not exist then no exception should be thrown.
   */
  def deleteSingleValueSync(key:String)(implicit hc: HeaderCarrier, ec: ExecutionContext):Unit = {
    getSingleValueSync(key) match {
      case None => {}
      case Some(_) => {
        val futureOptionString = Await.ready(delete(key), Duration.Inf)
        val optionTryOptionString = futureOptionString.value
        optionTryOptionString.map(
          _ match {
            case Success(x) => x
            case Failure(x) => {
              throw new RuntimeException("Can't delete single value:" + x.getMessage)
            }
          }
        )
      }
    }
  }

  //
  // The following three methods store, get and delete values of any class
  // to or from keystore.
  //

  /**
   * Store an item of any class to keystore using the specified key.
   */
  def store[A](formKey: String, data: A)(implicit hc: HeaderCarrier, ec: ExecutionContext, writes: Writes[A], reads: Reads[A]): Future[Option[A]] = {
    storeChangeData[A](formKey, data)
  }

  /**
   * Get an item of any class with the specified key from keystore.
   */
  def get[A](formKey: String)(implicit hc: HeaderCarrier, ec: ExecutionContext, writes: Writes[A], reads: Reads[A]): Future[Option[A]] = {
    getChangeData[A](formKey)
  }

  /**
   * Delete an item of any class with specified key from keystore.
   * Couldn't call this 'delete' as there's already a method with this
   * name in this file.
   */
  def deleteNonSync[A](key: String)(implicit hc: HeaderCarrier, ec: ExecutionContext,
                                    writes: Writes[A], reads: Reads[A]): Future[Any] = {
    SessionHttpCaching.fetch.map {
      case Some(x) => {
        Await.ready(SessionHttpCaching.remove(), Duration.Inf)
        val changedCacheData = x.data - key
        Await.ready(Future.sequence(changedCacheData.map(z => store[A](z._1, Json.fromJson[A](z._2).get))), Duration.Inf)
      }
      case None => Future.successful(None)
    }
  }

  //
  // The following three methods are synchronous versions of the above three methods.
  //

  /**
   * Synchronous store to keystore of an item using the specified key.
   */
  def storeSync[A](formKey: String, data: A)(implicit hc: HeaderCarrier, ec: ExecutionContext, writes: Writes[A], reads: Reads[A]): Option[A] = {
    val futureOptionString = Await.ready(store(formKey, data),
      Duration.Inf)
    val optionTryOptionString = futureOptionString.value
    optionTryOptionString.flatMap(
      _ match {
        case Success(x) => x
        case Failure(x) => throw new RuntimeException("Can't store value:" + x.getMessage)
      }
    )
  }

  /**
   * Synchronous get from keystore for item with specified key.
   */
  def getSync[A](formKey: String)(implicit hc: HeaderCarrier, ec: ExecutionContext, writes: Writes[A], reads: Reads[A]): Option[A] = {
    val futureOptionString = Await.ready(get[A](formKey),
      Duration.Inf)
    val optionTryOptionString = futureOptionString.value

    optionTryOptionString.flatMap(
      _ match {
        case Success(x) => x
        case Failure(x) => throw new RuntimeException("Can't return value:" + x.getMessage)
      }
    )
  }

  /**
   * Synchronous deletion of an item with specified key from keystore.
   * If no item exists then it should not throw an exception.
   */
  def deleteSync[A](key: String)(implicit hc: HeaderCarrier, ec: ExecutionContext, writes: Writes[A], reads: Reads[A]) = {
    getSync[A](key) match {
      case None => {}
      case Some(_) => {
        val futureOptionString = Await.ready(deleteNonSync[A](key), Duration.Inf)
        val optionTryOptionString = futureOptionString.value
        optionTryOptionString.map(
          _ match {
            case Success(x) => x
            case Failure(x) => {
              throw new RuntimeException("Can't delete value:" + x.getMessage)
            }
          }
        )
      }
    }
  }
}
