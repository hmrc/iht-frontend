/*
 * Copyright 2020 HM Revenue & Customs
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

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.models.application.exemptions.QualifyingBody
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import org.scalatest.BeforeAndAfter
import play.api.data.Form
import play.api.mvc.{AnyContentAsFormUrlEncoded, MessagesControllerComponents, Request}
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

/**
 * Copied by yasar and grant from jennygj on 01/09/16.
 */

class QualifyingBodyNameControllerTest extends ApplicationControllerTest with BeforeAndAfter {

  val QualifyingBody1Name = "Qualifying Body 1"
  val QualifyingBody2Name = "Qualifying Body 2"
  val QualifyingBodyNameAtLengthBoundary = "a" * 35
  val QualifyingBody1Value = BigDecimal(324)
  val QualifyingBody2Value = BigDecimal(65454)
  val qualifyingBody1 = QualifyingBody(Some("1"), Some(QualifyingBody1Name), Some(QualifyingBody1Value))
  val qualifyingBody2 = QualifyingBody(Some("2"), Some(QualifyingBody2Name), Some(QualifyingBody2Value))
  val referrerURL = "localhost:9070"

  protected abstract class TestController extends FrontendController(mockControllerComponents) with QualifyingBodyNameController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def qualifyingBodyNameController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def qualifyingBodyNameControllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def createMocksForQualifyingBodyName = {
    val ad = CommonBuilder.buildApplicationDetails copy (qualifyingBodies = Seq(qualifyingBody1))
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(ad),
      getAppDetails = true,
      saveAppDetails = true,
      storeAppDetailsInCache = true)
  }

  def createMocksForQualifyingBodyNameWithTwoItems = {
    val ad = CommonBuilder.buildApplicationDetails copy (qualifyingBodies = Seq(qualifyingBody1, qualifyingBody2))
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(ad),
      getAppDetails = true,
      saveAppDetails = true,
      storeAppDetailsInCache = true)
  }

  lazy val resultOnPageLoadNotAuthorised =
    qualifyingBodyNameControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
  def resultOnEditPageLoadNotAuthorised(id: String) =
    qualifyingBodyNameControllerNotAuthorised.onEditPageLoad(id)(createFakeRequest(isAuthorised = false))
  def resultOnSubmitNotAuthorised(request: Request[AnyContentAsFormUrlEncoded])=
    qualifyingBodyNameControllerNotAuthorised.onSubmit(request)
  def resultOnEditSubmitNotAuthorised(id: String) =
    qualifyingBodyNameControllerNotAuthorised.onEditSubmit(id)(createFakeRequest(isAuthorised = false))

  lazy val resultOnPageLoad =
    qualifyingBodyNameController.onPageLoad(createFakeRequest(authRetrieveNino = false))
  def resultOnEditPageLoad(id: String) =
    qualifyingBodyNameController.onEditPageLoad(id)(createFakeRequest())
  def resultOnSubmit(request: Request[AnyContentAsFormUrlEncoded]) =
    qualifyingBodyNameController.onSubmit(request)
  def resultOnEditSubmit(id: String)(request: Request[AnyContentAsFormUrlEncoded]) =
    qualifyingBodyNameController.onEditSubmit(id)(request)

  def request(form: Form[_]) =
    createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = "localhost:9070", data = form.data.toSeq)

  "QualifyingBodyNameControllerTest" must {

    "redirect to log in page onPageLoad if user is not logged in" in {
      status(resultOnPageLoadNotAuthorised) must be(SEE_OTHER)
      redirectLocation(resultOnPageLoadNotAuthorised) must be(Some(loginUrl))
    }

    "redirect to log in page onEditPageLoad if user is not logged in" in {
      status(resultOnEditPageLoadNotAuthorised("1")) must be(SEE_OTHER)
      redirectLocation(resultOnEditPageLoadNotAuthorised("1")) must be(Some(loginUrl))
    }

    "redirect to log in page onSubmit if user is not logged in" in {
      status(resultOnSubmitNotAuthorised(createFakeRequest(isAuthorised = false).withFormUrlEncodedBody((
        "totalValue", "101.00")))) must be(SEE_OTHER)
      redirectLocation(resultOnSubmitNotAuthorised(createFakeRequest(isAuthorised = false).withFormUrlEncodedBody((
        "totalValue", "101.00")))) must be(Some(loginUrl))
    }

    "redirect to log in page onEditSubmit if user is not logged in" in {
      status(resultOnEditSubmitNotAuthorised("1")) must be(SEE_OTHER)
      redirectLocation(resultOnEditSubmitNotAuthorised("1")) must be(Some(loginUrl))
    }

    "return OK onPageLoad" in {
      createMocksForQualifyingBodyName
      status(resultOnPageLoad) must be(OK)
    }

    "display errors when a blank value is submitted" in {
      createMocksForQualifyingBodyName
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("name", ""))
      val result = resultOnSubmit(fakePostRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) must include("a problem")
    }

    "save new value with new ID to application details onSubmit where exactly 36 characters in length and redirect to QB detail overview" in {
      createMocksForQualifyingBodyName
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("name", QualifyingBodyNameAtLengthBoundary))
      val result = resultOnSubmit(fakePostRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(iht.controllers.application.exemptions.qualifyingBody.routes.QualifyingBodyDetailsOverviewController.onEditPageLoad("2").url)
      val appDetails = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      appDetails.qualifyingBodies.size mustBe 2
      appDetails.qualifyingBodies.tail.head mustBe QualifyingBody(Some("2"), Some(QualifyingBodyNameAtLengthBoundary), None)
    }

    "save new value with new ID to application details onSubmit and redirect to QB detail overview" in {
      createMocksForQualifyingBodyName
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("name", QualifyingBody1Name))
      val result = resultOnSubmit(fakePostRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(iht.controllers.application.exemptions.qualifyingBody.routes.QualifyingBodyDetailsOverviewController.onEditPageLoad("2").url)
      val appDetails = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      appDetails.qualifyingBodies.size mustBe 2
      appDetails.qualifyingBodies.tail.head mustBe QualifyingBody(Some("2"), Some(QualifyingBody1Name), None)
    }

    "display previously entered value on EditPageLoad" in {
      createMocksForQualifyingBodyName
      contentAsString(resultOnEditPageLoad("1")) must include(QualifyingBody1Name)
    }

    "amend existing value with ID 1 in application details onEditSubmit and redirect to QB detail overview" in {
      createMocksForQualifyingBodyNameWithTwoItems
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("name", QualifyingBody2Name))
      val result = resultOnEditSubmit("1")(fakePostRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(iht.controllers.application.exemptions.qualifyingBody.routes.QualifyingBodyDetailsOverviewController.onEditPageLoad("1").url)
      val appDetails = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      appDetails.qualifyingBodies.size mustBe 2
      appDetails.qualifyingBodies.head mustBe QualifyingBody(Some("1"), Some(QualifyingBody2Name), Some(QualifyingBody1Value))
    }

    "amend existing value with ID 2 in application details onEditSubmit and redirect to QB detail overview" in {
      createMocksForQualifyingBodyNameWithTwoItems
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("name", QualifyingBody1Name))
      val result = resultOnEditSubmit("2")(fakePostRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(iht.controllers.application.exemptions.qualifyingBody.routes.QualifyingBodyDetailsOverviewController.onEditPageLoad("2").url)
      val appDetails = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      appDetails.qualifyingBodies.size mustBe 2
      appDetails.qualifyingBodies.tail.head mustBe QualifyingBody(Some("2"), Some(QualifyingBody1Name), Some(QualifyingBody2Value))
    }

    "return an internal server error if onPageLoad for invalid ID is entered" in {
      createMocksForQualifyingBodyName
      a[RuntimeException] mustBe thrownBy {
       await(resultOnEditPageLoad("10"))
      }
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      qualifyingBodyNameController.onPageLoad(createFakeRequest(authRetrieveNino = false)))
  }
}
