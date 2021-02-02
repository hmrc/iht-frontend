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

package iht.connectors

import iht.connector.IhtConnectorImpl
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.CommonBuilder
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatestplus.mockito._
import play.api.libs.json.{JsValue, Writes}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import play.api.test.Helpers._

import scala.concurrent.{ExecutionContext, Future}

class IhtConnectorTest extends ApplicationControllerTest with MockitoSugar {
  val mockHttpClient = mock[DefaultHttpClient]
  val mockServicesConfig = mock[ServicesConfig]

  val connector = new IhtConnectorImpl(mockHttpClient, mockServicesConfig, mockAppConfig)

  val defaultIHTReference = "12345"
  val defaultNino = "AA000000A"
  val applicationDetails = CommonBuilder.buildApplicationDetails2


  val successReturnId = "someid:12345678"
  val dummySuccessRespone = HttpResponse(200, successReturnId)
  val dummyInternalErrorResponse = HttpResponse(500, "No registration details found")
  val dummyForbiddenResponse = HttpResponse(403, "Submitter is not the lead executor")

  "submitApplication" should {
    "should return a return ID when call to IHT was successful" in {

      when(mockHttpClient.POST
      (any[String], any[JsValue], any[Seq[(String, String)]])
      (any[Writes[JsValue]], any[HttpReads[HttpResponse]], any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(dummySuccessRespone))

      val result = await(connector.submitApplication(defaultIHTReference, defaultNino, applicationDetails)(HeaderCarrier(), createFakeRequest()))
      result mustBe Some("12345678")
    }

    "should throw an exception when call to IHT returns a 500" in {

      when(mockHttpClient.POST
      (any[String], any[JsValue], any[Seq[(String, String)]])
      (any[Writes[JsValue]], any[HttpReads[HttpResponse]], any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(dummyInternalErrorResponse))

      lazy val result = await(connector.submitApplication(defaultIHTReference, defaultNino, applicationDetails)(HeaderCarrier(), createFakeRequest()))

      val thrown = intercept[Exception] {
        result
      }
      assert(thrown.getMessage === "Problem with the submission of the application details")
    }

    "should return None when call to IHT returns a 403" in {

      when(mockHttpClient.POST
      (any[String], any[JsValue], any[Seq[(String, String)]])
      (any[Writes[JsValue]], any[HttpReads[HttpResponse]], any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(UpstreamErrorResponse.apply("", 403, 500, Map.empty)))

      lazy val result = await(connector.submitApplication(defaultIHTReference, defaultNino, applicationDetails)(HeaderCarrier(), createFakeRequest()))

      result mustBe None
    }
  }
}
