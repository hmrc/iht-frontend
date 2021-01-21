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

package iht.controllers.application.tnrb


import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.forms.TnrbForms._
import iht.models.application.tnrb.WidowCheck
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import iht.views.HtmlSpec
import play.api.i18n.MessagesApi
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class DeceasedWidowCheckQuestionControllerTest extends ApplicationControllerTest with HtmlSpec {

  override implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  protected abstract class TestController extends FrontendController(mockControllerComponents) with DeceasedWidowCheckQuestionController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }


  def deceasedWidowCheckQuestionController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def deceasedWidowCheckQuestionControllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }



  "DeceasedWidowCheckQuestionController" must {

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = deceasedWidowCheckQuestionController.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(loginUrl))
    }

    "redirect to ida login page on Submit if the user is not logged in" in {
      val result = deceasedWidowCheckQuestionController.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(loginUrl))
    }
  }

  "onSubmit" must {
    "save application and go to date when the deceased's partner died page on submit if the deceased is widowed" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(widowCheck =
        Some(CommonBuilder.buildWidowedCheck.copy(dateOfPreDeceased = None)))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val withWidowedValue = CommonBuilder.buildWidowedCheck

      val filledDeceasedWidowCheckQuestionForm = deceasedWidowCheckQuestionForm.fill(withWidowedValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledDeceasedWidowCheckQuestionForm.data.toSeq: _*)

      val result = deceasedWidowCheckQuestionController.onSubmit(request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be(Some(routes.DeceasedWidowCheckDateController.onPageLoad().url))
    }

    "show errors if invalid fields" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(widowCheck =
        Some(CommonBuilder.buildWidowedCheck.copy(dateOfPreDeceased = None)),
        ihtRef = Some(CommonBuilder.DefaultString)
      )

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)


      implicit val request = createFakeRequest().withFormUrlEncodedBody(("widowed", ""), ("shareValue", "233"))

      val result = deceasedWidowCheckQuestionController.onSubmit(request)
      status(result) mustBe BAD_REQUEST
    }

    "give internal server error if no app details" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = None,
        getAppDetails = true,
        saveAppDetails = true)

      implicit val request = createFakeRequest().withFormUrlEncodedBody(("widowed", ""), ("shareValue", "233"))

      val result = deceasedWidowCheckQuestionController.onSubmit(request)
      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "save application and go to Widow check date page on submit if WidowCheck section is not completed" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        widowCheck = Some(CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = None)))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val withWidowedValue = CommonBuilder.buildWidowedCheck

      val filledDeceasedWidowCheckQuestionForm = deceasedWidowCheckQuestionForm.fill(withWidowedValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledDeceasedWidowCheckQuestionForm.data.toSeq: _*)

      val result = deceasedWidowCheckQuestionController.onSubmit(request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be(Some(routes.DeceasedWidowCheckDateController.onPageLoad().url))
    }

    "go to estate overview page on submit when the deceased is not widowed" in {
      val ihtRef = "IhtRef"
      val applicationDetails = CommonBuilder.buildApplicationDetails copy (ihtRef = Some(ihtRef))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val withWidowedValue = WidowCheck(Some(false), None)

      val filledDeceasedWidowCheckQuestionForm = deceasedWidowCheckQuestionForm.fill(withWidowedValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledDeceasedWidowCheckQuestionForm.data.toSeq: _*)

      val result = deceasedWidowCheckQuestionController.onSubmit(request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be(Some(iht.controllers.application.routes.EstateOverviewController
        .onPageLoadWithIhtRef(ihtRef).url))
    }

    "go to Tnrb overview page on submit when the deceased selects Yes in Edit mode" in {
      val ihtRef = "IhtRef"
      lazy val tnrbModel = CommonBuilder.buildTnrbEligibility.copy(None, None,None,None,None,None,None,None,None,None,None)
      val applicationDetails = CommonBuilder.buildApplicationDetails copy (ihtRef = Some(ihtRef),
        widowCheck = Some(CommonBuilder.buildWidowedCheck),
        increaseIhtThreshold = Some(tnrbModel))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val widowedCheckComplete = CommonBuilder.buildWidowedCheck

      val filledDeceasedWidowCheckQuestionForm = deceasedWidowCheckQuestionForm.fill(widowedCheckComplete)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledDeceasedWidowCheckQuestionForm.data.toSeq: _*)

      val result = deceasedWidowCheckQuestionController.onSubmit(request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be(Some(iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad().url + "#" + mockAppConfig.TnrbSpouseMartialStatusID))
    }

    "wipe out the WidowCheck date and tnrb eligibility data, go to estate overview page on submit " +
      "when user selects no to Deceased widowed question" in {

      val ihtRef = "IhtRef"
      val withWidowedValue = CommonBuilder.buildWidowedCheck.copy(widowed = Some(false),
        dateOfPreDeceased = Some(CommonBuilder.DefaultDateOfPreDeceased))
      val tnrbEligibilty = CommonBuilder.buildTnrbEligibility.copy(dateOfPreDeceased = Some(CommonBuilder.DefaultDateOfPreDeceased))
      val applicationDetails = CommonBuilder.buildApplicationDetails copy(ihtRef = Some(ihtRef),
        widowCheck = Some(withWidowedValue),
        increaseIhtThreshold = Some(tnrbEligibilty))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val filledDeceasedWidowCheckQuestionForm = deceasedWidowCheckQuestionForm.fill(withWidowedValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledDeceasedWidowCheckQuestionForm.data.toSeq: _*)

      val result = deceasedWidowCheckQuestionController.onSubmit(request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be(Some(iht.controllers.application.routes.EstateOverviewController
        .onPageLoadWithIhtRef(ihtRef).url))

      val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)

      val expectedAppDetails = applicationDetails.copy(
        widowCheck = Some(withWidowedValue.copy(dateOfPreDeceased = None)),
        increaseIhtThreshold = None
      )

      capturedValue mustBe expectedAppDetails
    }
  }

  "onPageLoad" must {

    "respond with OK" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(widowCheck = Some(CommonBuilder.buildWidowedCheck))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val result = deceasedWidowCheckQuestionController.onPageLoad(createFakeRequest())
      status(result) mustBe OK
    }

    "respond with OK when widow check is None" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
        .copy(ihtRef = Some(CommonBuilder.DefaultString), widowCheck = None)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val result = deceasedWidowCheckQuestionController.onPageLoad(createFakeRequest())
      status(result) mustBe OK
    }

    "respond with internal server error when no app details" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = None,
        getAppDetails = true,
        saveAppDetails = true)

      val result = deceasedWidowCheckQuestionController.onPageLoad(createFakeRequest())
      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "return html containing link which points to estate overview when widow check date is empty" in {
      val ihtRef = "ihtRef"
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(ihtRef = Some(ihtRef),
        widowCheck = Some(CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = None)),
        increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(CommonBuilder.firstNameGenerator),
          lastName = Some(CommonBuilder.surnameGenerator))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val expectedUrl = iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(ihtRef).url

      val result = deceasedWidowCheckQuestionController.onPageLoad(createFakeRequest())
      status(result) mustBe OK
      val doc = asDocument(contentAsString(result))
      assertRenderedById(doc, "cancel-button")

      val link = doc.getElementById("cancel-button")
      link.text() mustBe messagesApi("iht.estateReport.returnToEstateOverview")
      link.attr("href") mustBe expectedUrl
    }

    "return html containing link which points to tnrb overview when widow check date is not empty" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(widowCheck = Some(CommonBuilder.buildWidowedCheck),
        increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(CommonBuilder.firstNameGenerator),
          lastName = Some(CommonBuilder.surnameGenerator))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val expectedUrl = iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad.url + "#" + mockAppConfig.TnrbSpouseMartialStatusID

      val result = deceasedWidowCheckQuestionController.onPageLoad(createFakeRequest())
      status(result) mustBe OK
      val doc = asDocument(contentAsString(result))
      assertRenderedById(doc, "cancel-button")

      val link = doc.getElementById("cancel-button")
      link.text() mustBe messagesApi("page.iht.application.tnrb.returnToIncreasingThreshold")
      link.attr("href") mustBe expectedUrl
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      deceasedWidowCheckQuestionController.onPageLoad(createFakeRequest()))
  }
}
