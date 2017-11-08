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

import iht.controllers.application.ApplicationControllerTest
import iht.models.KickoutDetails
import iht.testhelpers.CommonBuilder
import play.api.libs.json._
import play.api.test.{FakeHeaders, FakeRequest}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.SessionId

import scala.concurrent.ExecutionContext.Implicits.global

class CachingConnectorTest extends ApplicationControllerTest {

  implicit val headerCarrier = FakeHeaders()
  implicit val request = FakeRequest()
  implicit val hc = HeaderCarrier(sessionId = Some(SessionId("1")))

  val connector: CachingConnector = CachingConnector

  "storeData" must {

    "return a None when null value is passed as data" in {
      val key: String = "key"
      val data: JsValue = JsNull
      val result = connector.storeData(key, data)
      await(result).shouldBe(None)
    }

    "return Some String when a String is passed as data" in {
     val key = "test"
     val data = Json.toJson("test")
     val result = connector.storeData(key, data)
     await(result).shouldBe(Some("test"))
    }

    "return Some KickoutDetails when a KickoutDetails is passed as data" in {
      val key = "kickoutDetails"
      val data: JsValue = Json.toJson(KickoutDetails("test", "testing"))
      val result = connector.storeData(key, data)
      await(result).shouldBe(Some(KickoutDetails("test", "testing")))
    }

    "return Some RegistrationDetails when a RegistrationDetails is passed as data" in {
      val key = "registrationDetails"
      val data: JsValue = Json.toJson(CommonBuilder.buildRegistrationDetails4)
      val result = connector.storeData(key, data)
      await(result).shouldBe(Some(CommonBuilder.buildRegistrationDetails4))
    }

  }

}
