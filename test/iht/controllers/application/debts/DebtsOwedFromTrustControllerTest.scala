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

package iht.controllers.application.debts

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.debts.BasicEstateElementLiabilities
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class DebtsOwedFromTrustControllerTest extends ApplicationControllerTest {

  implicit val hc = new HeaderCarrier()

  protected abstract class TestController extends FrontendController(mockControllerComponents) with DebtsOwedFromATrustController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def debtsOwedFromTrustController = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  val registrationDetails = CommonBuilder.buildRegistrationDetails copy(
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    ihtReference = Some("ABC1234567890")
    )

  "DebtsOwedFromTrust" must {
    "return OK on page load" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(CommonBuilder.buildApplicationDetails),
        getAppDetails = true)

      val result = debtsOwedFromTrustController.onPageLoad()(createFakeRequest(isAuthorised = true))

      status(result) must be(OK)
    }

    "save on submit" in {
      val testValue = BasicEstateElementLiabilities(isOwned = Some(true), value = Some(BigDecimal(33)))
      val filledForm = debtsTrustForm.fill(testValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledForm.data.toSeq: _*)
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allLiabilities = Some(CommonBuilder
        .buildAllLiabilities.copy(trust = Some(testValue))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val result = debtsOwedFromTrustController.onSubmit()(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.DebtsOverviewController.onPageLoad().url + "#" + appConfig.DebtsOwedFromTrustID))
    }

    "respond with bad request on submit when request is malformed" in {
      val testValue = CommonBuilder.buildBasicEstateElementLiabilities.copy(isOwned = None)
      val filledForm = debtsTrustForm.fill(testValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledForm.data.toSeq: _*)
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allLiabilities = Some(CommonBuilder
        .buildAllLiabilities.copy(trust = Some(testValue))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val result = debtsOwedFromTrustController.onSubmit()(request)
      status(result) must be(BAD_REQUEST)
    }

    "take you to internal server error on failure" in {
      val testValue = BasicEstateElementLiabilities(isOwned = Some(true), value = Some(BigDecimal(33)))
      val filledForm = debtsTrustForm.fill(testValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledForm.data.toSeq: _*)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = None,
        getAppDetails = true,
        saveAppDetails = true)

      val result = debtsOwedFromTrustController.onSubmit()(request)
      status(result) must be(INTERNAL_SERVER_ERROR)
    }

    "save application, wipe out the value and go to Debts overview page on submit when users selects No" in {
      val debtForTrust = BasicEstateElementLiabilities(isOwned = Some(false), value = Some(BigDecimal(33)))

      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allLiabilities = Some(CommonBuilder
                                              .buildAllLiabilities.copy(trust = Some(debtForTrust))))

      val filledForm = debtsTrustForm.fill(debtForTrust)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledForm.data.toSeq: _*)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val result = debtsOwedFromTrustController.onSubmit()(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result).get must be(routes.DebtsOverviewController.onPageLoad().url + "#" + appConfig.DebtsOwedFromTrustID)

      val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      val expectedAppDetails = applicationDetails.copy(allLiabilities = applicationDetails.allLiabilities.map(_.copy(
        trust = Some(CommonBuilder.buildBasicEstateElementLiabilities.copy(value = None, isOwned = Some(false))))))

      capturedValue mustBe expectedAppDetails
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      debtsOwedFromTrustController.onPageLoad(createFakeRequest()))
  }
}
