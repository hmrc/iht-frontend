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

package iht.controllers.registration.executor

import iht.config.AppConfig
import iht.connector.CachingConnector
import iht.controllers.registration.{RegistrationControllerTest, routes => registrationRoutes}
import iht.forms.registration.CoExecutorForms
import iht.models.{DeceasedDateOfDeath, RegistrationDetails}
import iht.testhelpers.CommonBuilder
import iht.views.html.registration.executor.others_applying_for_probate
import org.joda.time.LocalDate
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

class OthersApplyingForProbateControllerTest extends RegistrationControllerTest with CoExecutorForms {
  val appConfig = mockAppConfig

  protected abstract class TestController extends FrontendController(mockControllerComponents) with OthersApplyingForProbateController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val othersApplyingForProbateView: others_applying_for_probate = app.injector.instanceOf[others_applying_for_probate]
  }

  //Create controller object and pass in mock.
  def othersApplyingForProbateController = new TestController {
    override def cachingConnector: CachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector

    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def othersApplyingForProbateControllerNotAuthorised = new TestController {
    override def cachingConnector: CachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector

    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  "OthersApplyingForProbateController" must {
    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = othersApplyingForProbateControllerNotAuthorised.onPageLoad()(createFakeRequest(false))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(loginUrl)
    }

    "redirect to GG login page on Submit if the user is not logged in" in {
      val result = othersApplyingForProbateControllerNotAuthorised.onSubmit()(createFakeRequest(false))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(loginUrl)
    }

    "redirect to GG login page on PageLoad in edit mode if the user is not logged in" in {
      val result = othersApplyingForProbateControllerNotAuthorised.onEditPageLoad()(createFakeRequest(false))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(loginUrl)
    }

    "redirect to GG login page on Submit in edit mode if the user is not logged in" in {
      val result = othersApplyingForProbateControllerNotAuthorised.onEditSubmit()(createFakeRequest(false))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(loginUrl)
    }

    "redirect to GG login page on PageLoad if the user is not logged in and arrived from overview" in {
      val result = othersApplyingForProbateControllerNotAuthorised.onPageLoadFromOverview()(createFakeRequest(false))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(loginUrl)
    }

    "redirect to GG login page on Submit if the user is not logged in and arrived from overview" in {
      val result = othersApplyingForProbateControllerNotAuthorised.onSubmitFromOverview()(createFakeRequest(false))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(loginUrl)
    }

    "respond appropriately to a submit with a value of Yes" in  {
      val registrationDetails = RegistrationDetails(None, None, None)
      val probateForm = othersApplyingForProbateForm.fill(Some(true))
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=probateForm.data.toSeq, authRetrieveNino = false)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = othersApplyingForProbateController.onSubmit()(request)
      status(result) mustBe(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.CoExecutorPersonalDetailsController.onPageLoad(None).url))
    }

    "respond appropriately to a submit with a value of No" in  {
      val registrationDetails = RegistrationDetails(None, None, None)
      val probateForm = othersApplyingForProbateForm.fill(Some(false))
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=probateForm.data.toSeq, authRetrieveNino = false)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = othersApplyingForProbateController.onSubmitFromOverview()(request)
      status(result) mustBe(SEE_OTHER)
      redirectLocation(result) must be(Some(registrationRoutes.RegistrationSummaryController.onPageLoad.url))
    }

    "When submitting a yes - the areOthersApplyingForProbate must be set to false and any coexcutors must be removed from registration details" in  {
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      val probateForm = othersApplyingForProbateForm.fill(Some(true))
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=probateForm.data.toSeq, authRetrieveNino = false)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = othersApplyingForProbateController.onSubmitFromOverview()(request)
      status(result) mustBe(SEE_OTHER)
      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      capturedValue.coExecutors.length mustBe 1
      capturedValue.areOthersApplyingForProbate mustBe Some(true)
    }

    "When submitting a no - the areOthersApplyingForProbate must be set to false and any coexcutors must be removed from registration details" in  {
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      val probateForm = othersApplyingForProbateForm.fill(Some(false))
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=probateForm.data.toSeq, authRetrieveNino = false)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = othersApplyingForProbateController.onSubmitFromOverview()(request)
      status(result) mustBe(SEE_OTHER)
      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      capturedValue.coExecutors.length mustBe 0
      capturedValue.areOthersApplyingForProbate mustBe Some(false)

    }

    "respond appropriately to an invalid submit: Missing mandatory fields" in {
      val registrationDetails = RegistrationDetails(None, None, None)
      val probateForm = othersApplyingForProbateForm.fill(None)
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=probateForm.data.toSeq, authRetrieveNino = false)

      createMockToGetRegDetailsFromCache(mockCachingConnector, None)
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = await(othersApplyingForProbateController.onSubmit()(request))
      status(result) mustBe(BAD_REQUEST)
    }

    "return true if the guard conditions are true" in {
      val rd = CommonBuilder.buildRegistrationDetails copy (deceasedDateOfDeath =
        Some(DeceasedDateOfDeath(LocalDate.now)), applicantDetails = Some(CommonBuilder.buildApplicantDetails))
      othersApplyingForProbateController.checkGuardCondition(rd, "") mustBe true
    }

    "raise an error when the submit has a value of Yes but the storage fails" in  {
      val registrationDetails = RegistrationDetails(None, None, None)
      val probateForm = othersApplyingForProbateForm.fill(Some(true))
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=probateForm.data.toSeq, authRetrieveNino = false)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCacheWithFailure(mockCachingConnector, Some(registrationDetails))

      val result = othersApplyingForProbateController.onSubmit()(request)
      status(result) mustBe(INTERNAL_SERVER_ERROR)
    }
  }
}
