package controllers

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.declaration.DeclarationController
import iht.forms.ApplicationForms.declarationForm
import iht.metrics.Metrics
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.WiremockHelper.{wiremockHost, wiremockPort}
import utils.{IntegrationBaseSpec, TestDataUtil}
import play.api.test.Helpers._
import uk.gov.hmrc.http.Upstream5xxResponse

import scala.concurrent.Future
import scala.util.Try

class DeclarationControllerSpec extends IntegrationBaseSpec with MockitoSugar with TestDataUtil {

  lazy val applicantDetailsForm: Form[Boolean] = declarationForm.fill(true)
  lazy val fakeRequest: FakeRequest[AnyContentAsFormUrlEncoded] = createFakeRequest().withFormUrlEncodedBody(applicantDetailsForm.data.toSeq: _*)

  "Calling onSubmit" when {
    val mockCachingConnector = mock[CachingConnector]
    val mockAuthConnector = createFakeAuthConnector()

    lazy val controller = new DeclarationController {
      override def cachingConnector: CachingConnector = mockCachingConnector

      override def ihtConnector: IhtConnector = IhtConnector

      override val metrics: Metrics = mock[Metrics]

      override protected def authConnector: AuthConnector = mockAuthConnector
    }

    when(mockCachingConnector.getRegistrationDetails(any(), any()))
      .thenReturn(Future.successful(Some(testRegistrationDetails)))

    when(mockCachingConnector.storeProbateDetails(any())(any(), any()))
      .thenReturn(Future.successful(Some(testProbateDetails)))

    "a successful application submission occurs" should {

      def result() = Try { await {
        WireMock.configureFor(wiremockHost, wiremockPort)
        stubGet("/iht/AA123456A/home/caseDetails/ABC1234567890", OK, Json.toJson(testRegistrationDetails).toString())
        stubGet("/iht/AA123456A/application/get/ABC1234567890/AAABBBCCC", OK, Json.toJson(testApplicationDetails).toString())
        stubPost("/iht/AA123456A/application/save/AAABBBCCC", OK, Json.toJson(testApplicationDetails).toString())
        stubPost("/iht/AA123456A/ABC1234567890/application/submit", OK, "XX123456789X")
        stubGet("/iht/AA123456A/application/delete/ABC1234567890", OK, "")
        stubGet("/iht/AA123456A/application/probateDetails/ABC1234567890/XX123456789X", OK, Json.toJson(testProbateDetails).toString())
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

      "retrieve the probate details once" in {
        result()
        verify(getRequestedFor(urlEqualTo("/iht/AA123456A/application/probateDetails/ABC1234567890/XX123456789X")))
        wireMockServer.countRequestsMatching(getRequestedFor(urlEqualTo("/iht/AA123456A/application/probateDetails/ABC1234567890/XX123456789X")).build()).getCount shouldBe 1
      }

      "return the correct result" in {
        status(result().get) shouldBe SEE_OTHER
        redirectLocation(result().get) shouldBe Some(iht.controllers.application.declaration.routes.DeclarationReceivedController.onPageLoad().url)
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
        stubPost("/iht/AA123456A/ABC1234567890/application/submit", INTERNAL_SERVER_ERROR, "error message")
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

      "return the correct result" in {
        status(result().get) shouldBe INTERNAL_SERVER_ERROR
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

      "return the correct result" in {
        status(result().get) shouldBe INTERNAL_SERVER_ERROR
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
        intercept[Upstream5xxResponse](result().get).message shouldBe "GET of 'http://localhost:11111/iht/AA123456A/application/get/ABC1234567890/AAABBBCCC' returned 500. Response body: 'error message'"
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
        intercept[Upstream5xxResponse](result().get).message shouldBe "GET of 'http://localhost:11111/iht/AA123456A/home/caseDetails/ABC1234567890' returned 500. Response body: 'error message'"
      }
    }
  }
}
