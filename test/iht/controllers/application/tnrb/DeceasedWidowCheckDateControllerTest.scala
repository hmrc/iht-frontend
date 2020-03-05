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

package iht.controllers.application.tnrb


import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.forms.TnrbForms._
import iht.models.application.ApplicationDetails
import iht.models.application.tnrb.WidowCheck

import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever, TestHelper}
import iht.utils.CommonHelper
import iht.utils.tnrb.TnrbHelper
import iht.views.HtmlSpec
import org.joda.time.LocalDate
import org.scalatest.BeforeAndAfter
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class DeceasedWidowCheckDateControllerTest  extends ApplicationControllerTest with HtmlSpec with BeforeAndAfter with TnrbHelper {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with DeceasedWidowCheckDateController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def deceasedWidowCheckDateController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def deceasedWidowCheckDateControllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  "DeceasedWidowCheckDateController" must {

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = deceasedWidowCheckDateController.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to ida login page on Submit if the user is not logged in" in {
      val result = deceasedWidowCheckDateController.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(widowCheck= Some(CommonBuilder.buildWidowedCheck))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val result = deceasedWidowCheckDateController.onPageLoad (createFakeRequest())
      status(result) mustBe OK
    }

    "save application and go to Tnrb Overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(widowCheck= Some(CommonBuilder.buildWidowedCheck))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val withWidowedValue = CommonBuilder.buildWidowedCheck

      val filledDeceasedWidowCheckDateForm = deceasedWidowCheckDateForm.fill(withWidowedValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledDeceasedWidowCheckDateForm.data.toSeq: _*)

      val result = deceasedWidowCheckDateController.onSubmit (request)
      redirectLocation(result) must be(Some(routes.TnrbOverviewController.onPageLoad().url + "#" + mockAppConfig.TnrbSpouseDateOfDeathID))
    }

    "when saving application must set the widowed field of the widowed check to Some(true)" in {
      val widowCheckWithNoWidowField = WidowCheck(widowed = None, dateOfPreDeceased = Some(CommonBuilder.DefaultDateOfPreDeceased))
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(widowCheck = Some(widowCheckWithNoWidowField))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val filledDeceasedWidowCheckDateForm = deceasedWidowCheckDateForm.fill(widowCheckWithNoWidowField)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledDeceasedWidowCheckDateForm.data.toSeq: _*)

      val result = deceasedWidowCheckDateController.onSubmit (request)
      status(result) mustBe(SEE_OTHER)
      val capturedValue: ApplicationDetails = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      val capturedWidowCheck = capturedValue.widowCheck.getOrElse(WidowCheck(None, None))
      capturedWidowCheck.widowed mustBe(Some(true))
    }

    "go to KickOut page when predeceased date of death is before minimum date" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        widowCheck= Some(CommonBuilder.buildWidowedCheck))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val withWidowedValue = CommonBuilder.buildWidowedCheck.copy(
        dateOfPreDeceased = Some(TestHelper.dateOfPredeceasedForTnrbEligibility minusDays(1)))

      val filledDeceasedWidowCheckDateForm = deceasedWidowCheckDateForm.fill(withWidowedValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledDeceasedWidowCheckDateForm.data.toSeq: _*)

      val result = deceasedWidowCheckDateController.onSubmit (request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be(Some(iht.controllers.application.routes.KickoutAppController.onPageLoad.url))
    }

    "go to successful Tnrb page on submit when its satisfies happy path" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(increaseIhtThreshold =
        Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(CommonBuilder.firstNameGenerator),
          lastName = Some(CommonBuilder.surnameGenerator),
          dateOfMarriage= Some(new LocalDate(1984, 12, 11)))),
        widowCheck = Some(CommonBuilder.buildWidowedCheck))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val withWidowedValue = CommonBuilder.buildWidowedCheck.copy(dateOfPreDeceased = Some(new LocalDate(1986, 12, 11)))

      val filledDeceasedWidowCheckDateForm = deceasedWidowCheckDateForm.fill(withWidowedValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledDeceasedWidowCheckDateForm.data.toSeq: _*)

      val result = deceasedWidowCheckDateController.onSubmit (request)
      redirectLocation(result) must be(Some(routes.TnrbSuccessController.onPageLoad().url))
    }

    "show errors when incorrect submit is made" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(increaseIhtThreshold =
        Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(CommonBuilder.firstNameGenerator),
          lastName = Some(CommonBuilder.surnameGenerator),
          dateOfMarriage= Some(new LocalDate(1984, 12, 11)))),
        widowCheck = Some(CommonBuilder.buildWidowedCheck))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      implicit val request = createFakeRequest().withFormUrlEncodedBody(
        ("dateOfPreDeceased.day", "44"),
        ("dateOfPreDeceased.month", "12"),
        ("dateOfPreDeceased.year", "2000")
      )

      val result = deceasedWidowCheckDateController.onSubmit (request)
      status(result) mustBe BAD_REQUEST
    }

    "Give internal server error when no application details and onsubmit called" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(increaseIhtThreshold =
        Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(CommonBuilder.firstNameGenerator),
          lastName = Some(CommonBuilder.surnameGenerator),
          dateOfMarriage= Some(new LocalDate(1984, 12, 11)))),
        widowCheck = Some(CommonBuilder.buildWidowedCheck))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = None,
        getAppDetails = true,
        saveAppDetails = true)

      val withWidowedValue = CommonBuilder.buildWidowedCheck.copy(dateOfPreDeceased = Some(new LocalDate(1986, 12, 11)))

      val filledDeceasedWidowCheckDateForm = deceasedWidowCheckDateForm.fill(withWidowedValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledDeceasedWidowCheckDateForm.data.toSeq: _*)

      val result = deceasedWidowCheckDateController.onSubmit (request)
      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "have error when date of marriage is >= predeceased date of death" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(increaseIhtThreshold =
        Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(CommonBuilder.firstNameGenerator),
          lastName = Some(CommonBuilder.surnameGenerator),
          dateOfMarriage= Some(new LocalDate(1992, 12, 11)))),
        widowCheck = Some(CommonBuilder.buildWidowedCheck))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val withWidowedValue = CommonBuilder.buildWidowedCheck.copy(dateOfPreDeceased = Some(new LocalDate(1986, 12, 11)))

      val filledDeceasedWidowCheckDateForm = deceasedWidowCheckDateForm.fill(withWidowedValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledDeceasedWidowCheckDateForm.data.toSeq: _*)

      val result = deceasedWidowCheckDateController.onSubmit (request)
      status(result) mustBe BAD_REQUEST
    }

    "go to successful Tnrb page on submit when its satisfies happy path when widow check is empty" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(increaseIhtThreshold =
        Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(CommonBuilder.firstNameGenerator),
          lastName = Some(CommonBuilder.surnameGenerator),
          dateOfMarriage= Some(new LocalDate(1984, 12, 11)))),
        widowCheck = None)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val withWidowedValue = CommonBuilder.buildWidowedCheck.copy(dateOfPreDeceased = Some(new LocalDate(1986, 12, 11)))

      val filledDeceasedWidowCheckDateForm = deceasedWidowCheckDateForm.fill(withWidowedValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledDeceasedWidowCheckDateForm.data.toSeq: _*)

      val result = deceasedWidowCheckDateController.onSubmit (request)
      redirectLocation(result) must be(Some(routes.TnrbSuccessController.onPageLoad().url))
    }

    "return internal server error when save of app details fails" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(increaseIhtThreshold =
        Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(CommonBuilder.firstNameGenerator),
          lastName = Some(CommonBuilder.surnameGenerator),
          dateOfMarriage= Some(new LocalDate(1984, 12, 11)))),
        widowCheck = Some(CommonBuilder.buildWidowedCheck))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        saveAppDetailsObject = None,
        getAppDetails = true,
        saveAppDetails = true)

      val withWidowedValue = CommonBuilder.buildWidowedCheck.copy(dateOfPreDeceased = Some(new LocalDate(1986, 12, 11)))

      val filledDeceasedWidowCheckDateForm = deceasedWidowCheckDateForm.fill(withWidowedValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledDeceasedWidowCheckDateForm.data.toSeq: _*)

      val result = deceasedWidowCheckDateController.onSubmit (request)
      status(result) mustBe INTERNAL_SERVER_ERROR
    }
  }

  "onPageLoad" should {
    def setupMocksForTitleTests = {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(widowCheck = Some(CommonBuilder.buildWidowedCheck),
        increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(CommonBuilder.firstNameGenerator),
          lastName = Some(CommonBuilder.surnameGenerator))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      applicationDetails
    }

    "show the correct title" in {
      val ad = setupMocksForTitleTests

      val result = deceasedWidowCheckDateController.onPageLoad (createFakeRequest())
      val doc = asDocument(contentAsString(result))
      val headers = doc.getElementsByTag("h1")
      headers.size() mustBe 1

      val expectedTitle = messagesApi("page.iht.application.tnrbEligibilty.overview.partner.dod.question",
        spouseOrCivilPartnerLabelGenitive(
          CommonHelper.getOrException(ad.increaseIhtThreshold),
          CommonHelper.getOrException(ad.widowCheck),
          messagesApi("page.iht.application.tnrbEligibilty.partner.additional.label.the.deceased")
        )
      )

      headers.first().text() mustBe expectedTitle
    }

    "show the correct browser title" in {
      setupMocksForTitleTests
      val result = deceasedWidowCheckDateController.onPageLoad (createFakeRequest())
      val doc = asDocument(contentAsString(result))
      assertEqualsValue(doc, "title",
        messagesApi("iht.estateReport.tnrb.increasingIHTThreshold") + " " + messagesApi("site.title.govuk"))
    }

    "return html containing link which points to estate overview when widow check date is empty" in {
      val ihtRef = "ihtRef"
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(ihtRef=Some(ihtRef),
        widowCheck= Some(CommonBuilder.buildWidowedCheck copy(dateOfPreDeceased=None)),
        increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(CommonBuilder.firstNameGenerator),
          lastName = Some(CommonBuilder.surnameGenerator))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val expectedUrl = iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(ihtRef).url

      val result = deceasedWidowCheckDateController.onPageLoad (createFakeRequest())
      status(result) mustBe OK
      val doc = asDocument(contentAsString(result))
      assertRenderedById(doc, "cancel-button")

      val link = doc.getElementById("cancel-button")
      link.text() mustBe messagesApi("iht.estateReport.returnToEstateOverview")
      link.attr("href") mustBe expectedUrl
    }

    "return html containing link which points to tnrb overview when widow check date is not empty" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(widowCheck= Some(CommonBuilder.buildWidowedCheck),
        increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(CommonBuilder.firstNameGenerator),
          lastName = Some(CommonBuilder.surnameGenerator))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val expectedUrl = iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad.url + "#" + mockAppConfig.TnrbSpouseDateOfDeathID

      val result = deceasedWidowCheckDateController.onPageLoad (createFakeRequest())
      status(result) mustBe OK
      val doc = asDocument(contentAsString(result))
      assertRenderedById(doc, "cancel-button")

      val link = doc.getElementById("cancel-button")
      link.text() mustBe messagesApi("page.iht.application.tnrb.returnToIncreasingThreshold")
      link.attr("href") mustBe expectedUrl
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      deceasedWidowCheckDateController.onPageLoad(createFakeRequest()))
  }
}
