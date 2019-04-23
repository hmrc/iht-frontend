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


import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.forms.TnrbForms._
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import org.joda.time.LocalDate
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever


class EstateClaimControllerTest  extends ApplicationControllerTest {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with EstateClaimController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def estateClaimController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def estateClaimControllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  "EstateClaimController" must {

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = estateClaimController.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to ida login page on Submit if the user is not logged in" in {
      val result = estateClaimController.onSubmit(createFakeRequest(isAuthorised = false))
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

      val result = estateClaimController.onPageLoad (createFakeRequest())
      status(result) mustBe OK
    }

    "show predeceased name on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(increaseIhtThreshold =
        Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(CommonBuilder.firstNameGenerator),
          lastName = Some(CommonBuilder.surnameGenerator))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val result = estateClaimController.onPageLoad (createFakeRequest())
      status(result) mustBe OK
      contentAsString(result) must include(messagesApi("iht.estateReport.tnrb.stateClaim.question"))
    }

    "save application and go to Tnrb Overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(increaseIhtThreshold =
        Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(CommonBuilder.firstNameGenerator),
          lastName = Some(CommonBuilder.surnameGenerator))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val withEstateClaimValue = CommonBuilder.buildTnrbEligibility

      val filledEstateClaimForm = estateClaimAnyBusinessForm.fill(withEstateClaimValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledEstateClaimForm.data.toSeq: _*)

      val result = estateClaimController.onSubmit (request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be(Some(routes.TnrbOverviewController.onPageLoad().url + "#" + mockAppConfig.TnrbEstateReliefID))
    }

    "go to KickOut page if estate claimed any business" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(widowCheck= Some(CommonBuilder.buildWidowedCheck))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val withEstateClaimValue = CommonBuilder.buildTnrbEligibility.copy(isStateClaimAnyBusiness = Some(true))

      val filledEstateClaimForm = estateClaimAnyBusinessForm.fill(withEstateClaimValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledEstateClaimForm.data.toSeq: _*)

      val result = estateClaimController.onSubmit (request)
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

      val withEstateClaimValue = CommonBuilder.buildTnrbEligibility.copy(isStateClaimAnyBusiness = Some(false))

      val filledEstateClaimForm = estateClaimAnyBusinessForm.fill(withEstateClaimValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledEstateClaimForm.data.toSeq: _*)

      val result = estateClaimController.onSubmit (request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be(Some(routes.TnrbSuccessController.onPageLoad().url))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      estateClaimController.onPageLoad(createFakeRequest()))
  }
}
