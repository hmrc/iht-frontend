/*
 * Copyright 2019 HM Revenue & Customs
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

import iht.constants.IhtProperties._
import iht.controllers.application.ApplicationControllerTest
import iht.forms.TnrbForms._
import iht.testhelpers.MockObjectBuilder._
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import org.joda.time.LocalDate
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

/**
 *
 * Created by Vineet Tyagi on 14/01/16.
 *l
 */
class DateOfMarriageControllerTest  extends ApplicationControllerTest{



  def dateOfMarriageController = new DateOfMarriageController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def dateOfMarriageControllerNotAuthorised = new DateOfMarriageController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  "DateOfMarriageController" must {

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = dateOfMarriageController.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to ida login page on Submit if the user is not logged in" in {
      val result = dateOfMarriageController.onSubmit(createFakeRequest(isAuthorised = false))
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

      val result = dateOfMarriageController.onPageLoad (createFakeRequest())
      status(result) mustBe OK
    }

    "respond with Bad Request when input is not valid" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(widowCheck = Some(CommonBuilder.buildWidowedCheck))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val dateOfMarriageValue = CommonBuilder.buildTnrbEligibility.copy(dateOfMarriage = None)

      val filledDateOfMarriageValueForm = dateOfMarriageForm.fill(dateOfMarriageValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledDateOfMarriageValueForm.data.toSeq: _*)

      val result = dateOfMarriageController.onSubmit (request)
      status(result) mustBe BAD_REQUEST
    }

    "respond with Bad Request when date of marriage is after predeceased date of death" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        widowCheck = Some(CommonBuilder.buildWidowedCheck.copy(dateOfPreDeceased = Some(new LocalDate(1987,12,12)))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val dateOfMarriageValue = CommonBuilder.buildTnrbEligibility.copy(dateOfMarriage = Some(new LocalDate(1988,12,12)))

      val filledDateOfMarriageValueForm = dateOfMarriageForm.fill(dateOfMarriageValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledDateOfMarriageValueForm.data.toSeq: _*)

      val result = dateOfMarriageController.onSubmit (request)
      status(result) mustBe BAD_REQUEST
    }

    "save application and go to Tnrb Overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(widowCheck = Some(CommonBuilder.buildWidowedCheck))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val dateOfMarriageValue = CommonBuilder.buildTnrbEligibility.copy(dateOfMarriage = Some(new LocalDate(1986,12,12)))

      val filleDateOfMarriageValueForm = dateOfMarriageForm.fill(dateOfMarriageValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filleDateOfMarriageValueForm.data.toSeq: _*)

      val result = dateOfMarriageController.onSubmit (request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be(Some(routes.TnrbOverviewController.onPageLoad().url + "#" + TnrbSpouseDateOfMarriageID))
    }

    "go to successful Tnrb page on submit when its satisfies happy path" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(increaseIhtThreshold =
        Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(CommonBuilder.firstNameGenerator),
          lastName = Some(CommonBuilder.surnameGenerator))),
        widowCheck = Some(CommonBuilder.buildWidowedCheck))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val dateOfMarriageValue = CommonBuilder.buildTnrbEligibility.copy(dateOfMarriage = Some(new LocalDate(1986,12,12)))

      val filleDateOfMarriageValueForm = dateOfMarriageForm.fill(dateOfMarriageValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filleDateOfMarriageValueForm.data.toSeq: _*)

      val result = dateOfMarriageController.onSubmit (request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be(Some(routes.TnrbSuccessController.onPageLoad().url))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      dateOfMarriageController.onPageLoad(createFakeRequest()))

  }
}
