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

package iht

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.utils.IhtSection
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import play.api.mvc.Request

import scala.concurrent.ExecutionContext
import uk.gov.hmrc.http.HeaderCarrier

/**
  * Created by yasar on 10/2/15.
  */
trait TestUtils {

  implicit val headnapper = ArgumentCaptor.forClass(classOf[HeaderCarrier])
  implicit val exenapper = ArgumentCaptor.forClass(classOf[ExecutionContext])
  implicit val reqnapper = ArgumentCaptor.forClass(classOf[Request[_]])

  def buildLoginUrl(ihtSection: IhtSection.Value)(implicit appConfig: AppConfig) = ihtSection match {
    case IhtSection.Registration => appConfig.ggSignInFullUrlRegistration
    case IhtSection.Application => appConfig.ggSignInFullUrlApplication
    case _ => throw new RuntimeException("Auth mechanism could not be retrieved from conf")
  }

  def verifyAndReturnStoredRegistationDetails(mockCachingConnector: CachingConnector): RegistrationDetails = {
    val captor = ArgumentCaptor.forClass(classOf[RegistrationDetails])
    verify(mockCachingConnector)
      .storeRegistrationDetails(captor.capture)(headnapper.capture, exenapper.capture)
    captor.getValue
  }

  def verifyAndReturnSavedApplicationDetails(mockIhtConnector: IhtConnector): ApplicationDetails = {
    val captor = ArgumentCaptor.forClass(classOf[ApplicationDetails])
    val stringnapper0 = ArgumentCaptor.forClass(classOf[String])
    val stringnapper1 = ArgumentCaptor.forClass(classOf[String])

    verify(mockIhtConnector)
      .saveApplication(stringnapper0.capture, captor.capture, stringnapper1.capture) (headnapper.capture, reqnapper.capture)
    captor.getValue
  }

  def verifyAndReturnStoredSingleValue(mockCachingConnector: CachingConnector): (String, String) = {
    val captor = ArgumentCaptor.forClass(classOf[String])
    val keyCaptor = ArgumentCaptor.forClass(classOf[String])
    verify(mockCachingConnector).storeSingleValue(keyCaptor.capture, captor.capture)(headnapper.capture, exenapper.capture)
    (keyCaptor.getValue, captor.getValue)
  }

  def verifyDeleteKeyFromStore(mockCachingConnector: CachingConnector): String = {
    val keyCaptor = ArgumentCaptor.forClass(classOf[String])
    verify(mockCachingConnector).cacheDelete(keyCaptor.capture)(headnapper.capture, exenapper.capture)
    keyCaptor.getValue
  }
}
