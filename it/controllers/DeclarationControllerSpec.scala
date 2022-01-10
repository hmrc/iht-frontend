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

package controllers

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.declaration.DeclarationController
import iht.forms.ApplicationForms.declarationForm
import iht.metrics.IhtMetrics
import iht.views.html.application.declaration.declaration
import iht.views.html.estateReports.estateReports_error_serviceUnavailable
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsFormUrlEncoded, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever
import utils.WiremockHelper.{wiremockHost, wiremockPort}
import utils.{IntegrationBaseSpec, TestDataUtil}

import scala.concurrent.Future
import scala.util.Try


class DeclarationControllerSpec extends IntegrationBaseSpec with MockitoSugar with TestDataUtil {

  lazy val applicantDetailsForm: Form[Boolean] = declarationForm.fill(true)
  lazy val fakeRequest: FakeRequest[AnyContentAsFormUrlEncoded] = createFakeRequest().withFormUrlEncodedBody(applicantDetailsForm.data.toSeq: _*)

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtMetrics = mock[IhtMetrics]
  val mockFormPartialRetriever = mock[FormPartialRetriever]
  lazy val mockAuthConnector = createFakeAuthConnector()
  lazy val mockIhtConnector = app.injector.instanceOf[IhtConnector]
  lazy val mockCC = app.injector.instanceOf[MessagesControllerComponents]
  lazy val mockAppConfig = app.injector.instanceOf[AppConfig]

  protected abstract class TestController extends FrontendController(mockCC) with DeclarationController {
    override val cc: MessagesControllerComponents = mockCC
    override implicit val appConfig: AppConfig = mockAppConfig
    override val declarationView: declaration = app.injector.instanceOf[declaration]
    override val estateReportsErrorServiceUnavailableView: estateReports_error_serviceUnavailable = app.injector.instanceOf[estateReports_error_serviceUnavailable]
  }

  lazy val controller: TestController = new TestController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = mockAuthConnector
    override lazy val metrics: IhtMetrics = mock[IhtMetrics]
  }

  "Calling onSubmit" when {
    when(mockCachingConnector.getRegistrationDetails(any(), any()))
      .thenReturn(Future.successful(Some(testRegistrationDetails)))

    when(mockCachingConnector.storeProbateDetails(any())(any(), any()))
      .thenReturn(Future.successful(Some(testProbateDetails)))

    "a successful application submission occurs" should {

      def result() = {
        WireMock.configureFor(wiremockHost, wiremockPort)
        stubGet("/iht/AA123456A/home/caseDetails/ABC1234567890", OK, Json.toJson(testRegistrationDetails).toString())
        stubGet("/iht/AA123456A/application/get/ABC1234567890/AAABBBCCC", OK, Json.toJson(testApplicationDetails).toString())
        stubPost("/iht/AA123456A/application/save/AAABBBCCC", OK, Json.toJson(testApplicationDetails).toString())
        stubPost("/iht/AA123456A/ABC1234567890/application/submit", OK, "XX123456789X")
        stubGet("/iht/AA123456A/application/delete/ABC1234567890", OK, "")
        stubGet("/iht/AA123456A/application/probateDetails/ABC1234567890/XX123456789X", OK, Json.toJson(testProbateDetails).toString())
        controller.onSubmit(fakeRequest)
      }

      "call the case registration details once" in {
        await {
          result()
        }
        verify(getRequestedFor(urlEqualTo("/iht/AA123456A/home/caseDetails/ABC1234567890")))
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/home/caseDetails/ABC1234567890")).build()).getCount shouldBe 1
      }

      "retrieve the application details once" in {
        await {
          result()
        }
        verify(getRequestedFor(urlEqualTo("/iht/AA123456A/application/get/ABC1234567890/AAABBBCCC")))
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/application/get/ABC1234567890/AAABBBCCC")).build()).getCount shouldBe 1
      }

      "save the application details once" in {
        await {
          result()
        }
        verify(postRequestedFor(urlEqualTo("/iht/AA123456A/application/save/AAABBBCCC"))
        .withRequestBody(equalToJson(Json.toJson(testSaveApplicationDetails).toString())))
        wireMockServer.countRequestsMatching(postRequestedFor(urlEqualTo("/iht/AA123456A/application/save/AAABBBCCC")).build()).getCount shouldBe 1
      }

      "submit the application details once" in {
        await {
          result()
        }
        verify(postRequestedFor(urlEqualTo("/iht/AA123456A/ABC1234567890/application/submit"))
        .withRequestBody(equalToJson(Json.toJson(testSaveApplicationDetails).toString())))
        wireMockServer.countRequestsMatching(postRequestedFor(urlEqualTo("/iht/AA123456A/ABC1234567890/application/submit")).build()).getCount shouldBe 1
      }

      "delete the old application details once" in {
        await {
          result()
        }
        verify(getRequestedFor(urlEqualTo("/iht/AA123456A/application/delete/ABC1234567890")))
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/application/delete/ABC1234567890")).build()).getCount shouldBe 1
      }

      "retrieve the probate details once" in {
        await {
          result()
        }
        verify(getRequestedFor(urlEqualTo("/iht/AA123456A/application/probateDetails/ABC1234567890/XX123456789X")))
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/application/probateDetails/ABC1234567890/XX123456789X")).build()).getCount shouldBe 1
      }

      "return the correct result" in {
        val res = await {
          result()
        }

        status(res) shouldBe SEE_OTHER
        redirectLocation(res) shouldBe Some(iht.controllers.application.declaration.routes.DeclarationReceivedController.onPageLoad().url)
      }
    }

    "a successful application submission occurs that fails to fetch the probate details" should {

      def result() = Try { await {
        WireMock.configureFor(wiremockHost, wiremockPort)
        stubGet("/iht/AA123456A/home/caseDetails/ABC1234567890", OK, Json.toJson(testRegistrationDetails).toString())
        stubGet("/iht/AA123456A/application/get/ABC1234567890/AAABBBCCC", OK, Json.toJson(testApplicationDetails).toString())
        stubPost("/iht/AA123456A/application/save/AAABBBCCC", OK, Json.toJson(testApplicationDetails).toString())
        stubPost("/iht/AA123456A/ABC1234567890/application/submit", OK, "XX123456789X")
        stubGet("/iht/AA123456A/application/delete/ABC1234567890", OK, "")
        stubGet("/iht/AA123456A/application/probateDetails/ABC1234567890/XX123456789X", INTERNAL_SERVER_ERROR, "")
        controller.onSubmit(fakeRequest)
      }}

      "call the case registration details once" in {
        result()
        verify(getRequestedFor(urlEqualTo("/iht/AA123456A/home/caseDetails/ABC1234567890")))
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/home/caseDetails/ABC1234567890")).build()).getCount shouldBe 1
      }

      "retrieve the application details once" in {
        result()
        verify(getRequestedFor(urlEqualTo("/iht/AA123456A/application/get/ABC1234567890/AAABBBCCC")))
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/application/get/ABC1234567890/AAABBBCCC")).build()).getCount shouldBe 1
      }

      "save the application details once" in {
        result()
        verify(postRequestedFor(urlEqualTo("/iht/AA123456A/application/save/AAABBBCCC"))
          .withRequestBody(equalToJson(Json.toJson(testSaveApplicationDetails).toString())))
        wireMockServer.countRequestsMatching(postRequestedFor(urlEqualTo("/iht/AA123456A/application/save/AAABBBCCC")).build()).getCount shouldBe 1
      }

      "submit the application details once" in {
        result()
        verify(postRequestedFor(urlEqualTo("/iht/AA123456A/ABC1234567890/application/submit"))
          .withRequestBody(equalToJson(Json.toJson(testSaveApplicationDetails).toString())))
        wireMockServer.countRequestsMatching(postRequestedFor(urlEqualTo("/iht/AA123456A/ABC1234567890/application/submit")).build()).getCount shouldBe 1
      }

      "delete the old application details once" in {
        result()
        verify(getRequestedFor(urlEqualTo("/iht/AA123456A/application/delete/ABC1234567890")))
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/application/delete/ABC1234567890")).build()).getCount shouldBe 1
      }

      "attempt to retrieve the probate details once" in {
        result()
        verify(getRequestedFor(urlEqualTo("/iht/AA123456A/application/probateDetails/ABC1234567890/XX123456789X")))
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/application/probateDetails/ABC1234567890/XX123456789X")).build()).getCount shouldBe 1
      }

      "return the correct result" in {
        status(result().get) shouldBe SEE_OTHER
        redirectLocation(result().get) shouldBe Some(iht.controllers.application.declaration.routes.DeclarationReceivedController.onPageLoad().url)
      }
    }

    "a successful application submission occurs that fails to delete the application details" should {

      def result() = Try { await {
        WireMock.configureFor(wiremockHost, wiremockPort)
        stubGet("/iht/AA123456A/home/caseDetails/ABC1234567890", OK, Json.toJson(testRegistrationDetails).toString())
        stubGet("/iht/AA123456A/application/get/ABC1234567890/AAABBBCCC", OK, Json.toJson(testApplicationDetails).toString())
        stubPost("/iht/AA123456A/application/save/AAABBBCCC", OK, Json.toJson(testApplicationDetails).toString())
        stubPost("/iht/AA123456A/ABC1234567890/application/submit", OK, "XX123456789X")
        stubGet("/iht/AA123456A/application/delete/ABC1234567890", INTERNAL_SERVER_ERROR, "")
        stubGet("/iht/AA123456A/application/probateDetails/ABC1234567890/XX123456789X", INTERNAL_SERVER_ERROR, "")
        controller.onSubmit(fakeRequest)
      }}

      "call the case registration details once" in {
        result()
        verify(getRequestedFor(urlEqualTo("/iht/AA123456A/home/caseDetails/ABC1234567890")))
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/home/caseDetails/ABC1234567890")).build()).getCount shouldBe 1
      }

      "retrieve the application details once" in {
        result()
        verify(getRequestedFor(urlEqualTo("/iht/AA123456A/application/get/ABC1234567890/AAABBBCCC")))
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/application/get/ABC1234567890/AAABBBCCC")).build()).getCount shouldBe 1
      }

      "save the application details once" in {
        result()
        verify(postRequestedFor(urlEqualTo("/iht/AA123456A/application/save/AAABBBCCC"))
          .withRequestBody(equalToJson(Json.toJson(testSaveApplicationDetails).toString())))
        wireMockServer.countRequestsMatching(postRequestedFor(urlEqualTo("/iht/AA123456A/application/save/AAABBBCCC")).build()).getCount shouldBe 1
      }

      "submit the application details once" in {
        result()
        verify(postRequestedFor(urlEqualTo("/iht/AA123456A/ABC1234567890/application/submit"))
          .withRequestBody(equalToJson(Json.toJson(testSaveApplicationDetails).toString())))
        wireMockServer.countRequestsMatching(postRequestedFor(urlEqualTo("/iht/AA123456A/ABC1234567890/application/submit")).build()).getCount shouldBe 1
      }

      "attempt to delete the old application details once" in {
        result()
        verify(getRequestedFor(urlEqualTo("/iht/AA123456A/application/delete/ABC1234567890")))
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/application/delete/ABC1234567890")).build()).getCount shouldBe 1
      }

      "attempt to retrieve the probate details once" in {
        result()
        verify(getRequestedFor(urlEqualTo("/iht/AA123456A/application/probateDetails/ABC1234567890/XX123456789X")))
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/application/probateDetails/ABC1234567890/XX123456789X")).build()).getCount shouldBe 1
      }

      "return the correct result" in {
        status(result().get) shouldBe SEE_OTHER
        redirectLocation(result().get) shouldBe Some(iht.controllers.application.declaration.routes.DeclarationReceivedController.onPageLoad().url)
      }
    }

    "a failed application submission occurs on submission" should {

      def result() = Try { await {
        WireMock.configureFor(wiremockHost, wiremockPort)
        stubGet("/iht/AA123456A/home/caseDetails/ABC1234567890", OK, Json.toJson(testRegistrationDetails).toString())
        stubGet("/iht/AA123456A/application/get/ABC1234567890/AAABBBCCC", OK, Json.toJson(testApplicationDetails).toString())
        stubPost("/iht/AA123456A/application/save/AAABBBCCC", OK, Json.toJson(testApplicationDetails).toString())
        stubPost("/iht/AA123456A/ABC1234567890/application/submit", INTERNAL_SERVER_ERROR, "500 response returned from DES")
        controller.onSubmit(fakeRequest)
      }}

      "call the case registration details once" in {
        result()
        verify(getRequestedFor(urlEqualTo("/iht/AA123456A/home/caseDetails/ABC1234567890")))
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/home/caseDetails/ABC1234567890")).build()).getCount shouldBe 1
      }

      "retrieve the application details once" in {
        result()
        verify(getRequestedFor(urlEqualTo("/iht/AA123456A/application/get/ABC1234567890/AAABBBCCC")))
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/application/get/ABC1234567890/AAABBBCCC")).build()).getCount shouldBe 1
      }

      "save the application details once" in {
        result()
        verify(postRequestedFor(urlEqualTo("/iht/AA123456A/application/save/AAABBBCCC"))
          .withRequestBody(equalToJson(Json.toJson(testSaveApplicationDetails).toString())))
        wireMockServer.countRequestsMatching(postRequestedFor(urlEqualTo("/iht/AA123456A/application/save/AAABBBCCC")).build()).getCount shouldBe 1
      }

      "attempt to submit the application details once" in {
        result()
        verify(postRequestedFor(urlEqualTo("/iht/AA123456A/ABC1234567890/application/submit"))
          .withRequestBody(equalToJson(Json.toJson(testSaveApplicationDetails).toString())))
        wireMockServer.countRequestsMatching(postRequestedFor(urlEqualTo("/iht/AA123456A/ABC1234567890/application/submit")).build()).getCount shouldBe 1
      }

      "not attempt to delete the old application details" in {
        result()
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/application/delete/ABC1234567890")).build()).getCount shouldBe 0
      }

      "not attempt to retrieve the probate details" in {
        result()
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/application/probateDetails/ABC1234567890/XX123456789X")).build()).getCount shouldBe 0
      }

      "throw correct error to trigger correct onError response" in {
        val error = result().failed.get
        error shouldBe an[UpstreamErrorResponse]
        error.getMessage should include("500 response returned from DES")
        error.asInstanceOf[UpstreamErrorResponse].statusCode shouldBe 500
      }
    }

    "a failed application submission occurs on submission due to failed save request for application details" should {

      def result() = Try { await {
        WireMock.configureFor(wiremockHost, wiremockPort)
        stubGet("/iht/AA123456A/home/caseDetails/ABC1234567890", OK, Json.toJson(testRegistrationDetails).toString())
        stubGet("/iht/AA123456A/application/get/ABC1234567890/AAABBBCCC", OK, Json.toJson(testApplicationDetails).toString())
        stubPost("/iht/AA123456A/application/save/AAABBBCCC", INTERNAL_SERVER_ERROR, "")
        controller.onSubmit(fakeRequest)
      }}

      "call the case registration details once" in {
        result()
        verify(getRequestedFor(urlEqualTo("/iht/AA123456A/home/caseDetails/ABC1234567890")))
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/home/caseDetails/ABC1234567890")).build()).getCount shouldBe 1
      }

      "retrieve the application details once" in {
        result()
        verify(getRequestedFor(urlEqualTo("/iht/AA123456A/application/get/ABC1234567890/AAABBBCCC")))
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/application/get/ABC1234567890/AAABBBCCC")).build()).getCount shouldBe 1
      }

      "attempt to save the application details once" in {
        result()
        verify(postRequestedFor(urlEqualTo("/iht/AA123456A/application/save/AAABBBCCC"))
          .withRequestBody(equalToJson(Json.toJson(testSaveApplicationDetails).toString())))
        wireMockServer.countRequestsMatching(postRequestedFor(urlEqualTo("/iht/AA123456A/application/save/AAABBBCCC")).build()).getCount shouldBe 1
      }

      "not attempt to submit the application details" in {
        result()
        wireMockServer.countRequestsMatching(postRequestedFor(urlEqualTo("/iht/AA123456A/ABC1234567890/application/submit")).build()).getCount shouldBe 0
      }

      "not attempt to delete the old application details" in {
        result()
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/application/delete/ABC1234567890")).build()).getCount shouldBe 0
      }

      "not attempt to retrieve the probate details" in {
        result()
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/application/probateDetails/ABC1234567890/XX123456789X")).build()).getCount shouldBe 0
      }

      "throw error to not trigger des custom response" in {
        val error = result().failed.get
        error shouldBe an[Exception]
        error.getMessage should include("Problem saving application details")
      }
    }

    "a failed application submission occurs on submission due to failed get request for application details" should {

      def result() = Try { await {
        WireMock.configureFor(wiremockHost, wiremockPort)
        stubGet("/iht/AA123456A/home/caseDetails/ABC1234567890", OK, Json.toJson(testRegistrationDetails).toString())
        stubGet("/iht/AA123456A/application/get/ABC1234567890/AAABBBCCC", INTERNAL_SERVER_ERROR, "error message")
        controller.onSubmit(fakeRequest)
      }}

      "call the case registration details once" in {
        result()
        verify(getRequestedFor(urlEqualTo("/iht/AA123456A/home/caseDetails/ABC1234567890")))
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/home/caseDetails/ABC1234567890")).build()).getCount shouldBe 1
      }

      "attempt to retrieve the application details once" in {
        result()
        verify(getRequestedFor(urlEqualTo("/iht/AA123456A/application/get/ABC1234567890/AAABBBCCC")))
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/application/get/ABC1234567890/AAABBBCCC")).build()).getCount shouldBe 1
      }

      "not attempt to save the application details" in {
        result()
        wireMockServer.countRequestsMatching(postRequestedFor(urlEqualTo("/iht/AA123456A/application/save/AAABBBCCC")).build()).getCount shouldBe 0
      }

      "not attempt to submit the application details" in {
        result()
        wireMockServer.countRequestsMatching(postRequestedFor(urlEqualTo("/iht/AA123456A/ABC1234567890/application/submit")).build()).getCount shouldBe 0
      }

      "not attempt to delete the old application details" in {
        result()
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/application/delete/ABC1234567890")).build()).getCount shouldBe 0
      }

      "not attempt to retrieve the probate details" in {
        result()
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/application/probateDetails/ABC1234567890/XX123456789X")).build()).getCount shouldBe 0
      }

      "return the correct result" in {
        val error = result().failed.get
        error shouldBe an[Exception]
        error.getMessage should include("Problem retrieving application details")
      }
    }

    "a failed application submission occurs on submission due to failed get request for registration details" should {

      def result() = Try { await {
        WireMock.configureFor(wiremockHost, wiremockPort)
        stubGet("/iht/AA123456A/home/caseDetails/ABC1234567890", INTERNAL_SERVER_ERROR, "error message")
        controller.onSubmit(fakeRequest)
      }}

      "call the case registration details once" in {
        result()
        verify(getRequestedFor(urlEqualTo("/iht/AA123456A/home/caseDetails/ABC1234567890")))
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/home/caseDetails/ABC1234567890")).build()).getCount shouldBe 1
      }

      "not attempt to retrieve the application details" in {
        result()
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/application/get/ABC1234567890/AAABBBCCC")).build()).getCount shouldBe 0
      }

      "not attempt to save the application details" in {
        result()
        wireMockServer.countRequestsMatching(postRequestedFor(urlEqualTo("/iht/AA123456A/application/save/AAABBBCCC")).build()).getCount shouldBe 0
      }

      "not attempt to submit the application details" in {
        result()
        wireMockServer.countRequestsMatching(postRequestedFor(urlEqualTo("/iht/AA123456A/ABC1234567890/application/submit")).build()).getCount shouldBe 0
      }

      "not attempt to delete the old application details" in {
        result()
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/application/delete/ABC1234567890")).build()).getCount shouldBe 0
      }

      "not attempt to retrieve the probate details" in {
        result()
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/application/probateDetails/ABC1234567890/XX123456789X")).build()).getCount shouldBe 0
      }

      "return the correct result" in {
        val error = result().failed.get
        error shouldBe an[Exception]
        error.getMessage should include("Problem retrieving Case Details")
      }
    }
  }
}
