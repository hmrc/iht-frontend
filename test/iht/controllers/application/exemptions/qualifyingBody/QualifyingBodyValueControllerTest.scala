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

package iht.controllers.application.exemptions.qualifyingBody

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.models.application.exemptions.QualifyingBody
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder}
import iht.testhelpers.MockObjectBuilder._
import org.scalatest.BeforeAndAfter
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{AnyContentAsFormUrlEncoded, Request}
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

/**
 * Created by jennygj on 26/08/16.
 */

class QualifyingBodyValueControllerTest extends ApplicationControllerTest with BeforeAndAfter {
  val mockCachingConnector = mock[CachingConnector]
  var mockIhtConnector = mock[IhtConnector]
  val qualifyingBody1 = QualifyingBody(Some("1"), Some("Qualifying Body 1"), Some(BigDecimal(324)))
  val qualifyingBody2 = QualifyingBody(Some("2"), Some("Qualifying Body 2"), Some(BigDecimal(65454)))
  val referrerURL = "localhost:9070"

  before {
    mockIhtConnector = mock[IhtConnector]
  }

  def qualifyingBodyValueController = new QualifyingBodyValueController {
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def qualifyingBodyValueControllerNotAuthorised = new QualifyingBodyValueController {
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def createMocksForQualifyingBodyValue = {
    val ad = CommonBuilder.buildApplicationDetails copy (qualifyingBodies = Seq(qualifyingBody1))
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(ad),
      getAppDetails = true,
      saveAppDetails = true,
      storeAppDetailsInCache = true)
  }

  def createMocksForQualifyingBodyValueWithTwoItems = {
    val ad = CommonBuilder.buildApplicationDetails copy (qualifyingBodies = Seq(qualifyingBody1, qualifyingBody2))
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(ad),
      getAppDetails = true,
      saveAppDetails = true,
      storeAppDetailsInCache = true)
  }

  lazy val resultOnPageLoadNotAuthorised =
    qualifyingBodyValueControllerNotAuthorised.onPageLoad(createFakeRequest())
  def resultOnEditPageLoadNotAuthorised(id: String) =
    qualifyingBodyValueControllerNotAuthorised.onEditPageLoad(id)(createFakeRequest())
  def resultOnSubmitNotAuthorised(request: Request[AnyContentAsFormUrlEncoded])=
    qualifyingBodyValueControllerNotAuthorised.onSubmit(request)
  def resultOnEditSubmitNotAuthorised(id: String) =
    qualifyingBodyValueControllerNotAuthorised.onEditSubmit(id)(createFakeRequest())

  lazy val resultOnPageLoad =
    qualifyingBodyValueController.onPageLoad(createFakeRequest())
  def resultOnEditPageLoad(id: String) =
    qualifyingBodyValueController.onEditPageLoad(id)(createFakeRequest())
  def resultOnSubmit(request: Request[AnyContentAsFormUrlEncoded]) =
    qualifyingBodyValueController.onSubmit(request)
  def resultOnEditSubmit(id: String)(request: Request[AnyContentAsFormUrlEncoded]) =
    qualifyingBodyValueController.onEditSubmit(id)(request)


  def request(form: Form[_]) =
    createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = "localhost:9070", data = form.data.toSeq)


  "QualifyingBodyValueControllerTest" must {

    "redirect to log in page onPageLoad if user is not logged in" in {
      status(resultOnPageLoadNotAuthorised) should be(SEE_OTHER)
      redirectLocation(resultOnPageLoadNotAuthorised) should be(Some(loginUrl))
    }

    "redirect to log in page onEditPageLoad if user is not logged in" in {
      status(resultOnEditPageLoadNotAuthorised("1")) should be(SEE_OTHER)
      redirectLocation(resultOnEditPageLoadNotAuthorised("1")) should be(Some(loginUrl))
    }

    "redirect to log in page onSubmit if user is not logged in" in {
      status(resultOnSubmitNotAuthorised(createFakeRequest().withFormUrlEncodedBody((
        "totalValue", "101.00")))) should be(SEE_OTHER)
      redirectLocation(resultOnSubmitNotAuthorised(createFakeRequest().withFormUrlEncodedBody((
        "totalValue", "101.00")))) should be(Some(loginUrl))
    }

    "redirect to log in page onEditSubmit if user is not logged in" in {
      status(resultOnEditSubmitNotAuthorised("1")) should be(SEE_OTHER)
      redirectLocation(resultOnEditSubmitNotAuthorised("1")) should be(Some(loginUrl))
    }

    "return OK onPageLoad" in {
      createMocksForQualifyingBodyValue
      status(resultOnPageLoad) should be(OK)
    }

    "display an error message when a non-numeric value is entered" in {
      createMocksForQualifyingBodyValue
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("totalValue", "blaaaaah"))
      val result = resultOnSubmit(fakePostRequest)

      status(result) shouldBe BAD_REQUEST
      contentAsString(result) should include("a problem")
    }

    "save new value with new ID to application details onSubmit and redirect to QB detail overview" in {
      createMocksForQualifyingBodyValue
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("totalValue", "100"))
      val result = resultOnSubmit(fakePostRequest)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(iht.controllers.application.exemptions.qualifyingBody.routes.QualifyingBodyDetailsOverviewController.onEditPageLoad("2").url)
      val appDetails = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      appDetails.qualifyingBodies.size shouldBe 2
      appDetails.qualifyingBodies.tail.head shouldBe QualifyingBody(Some("2"), None, Some(100))
    }

    "display previously entered value on EditPageLoad" in {
      createMocksForQualifyingBodyValue
      contentAsString(resultOnEditPageLoad("1")) should include("324")
    }

    "amend existing value with ID 1 in application details onEditSubmit and redirect to QB detail overview" in {
      createMocksForQualifyingBodyValueWithTwoItems
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("totalValue", "777"))
      val result = resultOnEditSubmit("1")(fakePostRequest)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(iht.controllers.application.exemptions.qualifyingBody.routes.QualifyingBodyDetailsOverviewController.onEditPageLoad("1").url)
      val appDetails = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      appDetails.qualifyingBodies.size shouldBe 2
      appDetails.qualifyingBodies.head shouldBe QualifyingBody(Some("1"), Some("Qualifying Body 1"), Some(777))
    }

    "amend existing value with ID 2 in application details onEditSubmit and redirect to QB detail overview" in {
      createMocksForQualifyingBodyValueWithTwoItems
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("totalValue", "888"))
      val result = resultOnEditSubmit("2")(fakePostRequest)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(iht.controllers.application.exemptions.qualifyingBody.routes.QualifyingBodyDetailsOverviewController.onEditPageLoad("2").url)
      val appDetails = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      appDetails.qualifyingBodies.size shouldBe 2
      appDetails.qualifyingBodies.tail.head shouldBe QualifyingBody(Some("2"), Some("Qualifying Body 2"), Some(888))
    }

    "return an internal server error if onPageLoad for invalid ID is entered" in {
      createMocksForQualifyingBodyValue
      a[RuntimeException] shouldBe thrownBy {
       await(resultOnEditPageLoad("10"))
      }
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      qualifyingBodyValueController.onPageLoad(createFakeRequest()))
  }
}
