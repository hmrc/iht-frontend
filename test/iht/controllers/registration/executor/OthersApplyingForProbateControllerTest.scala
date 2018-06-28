/*
 * Copyright 2018 HM Revenue & Customs
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

import iht.connector.CachingConnector
import iht.controllers.registration.{RegistrationControllerTest, routes => registrationRoutes}
import iht.forms.registration.CoExecutorForms._
import iht.metrics.Metrics
import iht.models.{DeceasedDateOfDeath, RegistrationDetails}
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import iht.testhelpers.MockObjectBuilder._
import org.joda.time.LocalDate
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class OthersApplyingForProbateControllerTest extends RegistrationControllerTest {

  before {
    mockCachingConnector = mock[CachingConnector]
  }

  //Create controller object and pass in mock.
  def othersApplyingForProbateController = new OthersApplyingForProbateController {
    override def metrics: Metrics = Metrics
    override def cachingConnector: CachingConnector = mockCachingConnector
    override protected def authConnector: AuthConnector = createFakeAuthConnector(true)

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def othersApplyingForProbateControllerNotAuthorised = new OthersApplyingForProbateController {
    override def metrics: Metrics = Metrics
    override def cachingConnector: CachingConnector = mockCachingConnector
    override protected def authConnector: AuthConnector = createFakeAuthConnector(false)

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  "OthersApplyingForProbateController" must {
    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = othersApplyingForProbateControllerNotAuthorised.onPageLoad()(createFakeRequest(false))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(loginUrl)
    }

    "redirect to GG login page on Submit if the user is not logged in" in {
      val result = othersApplyingForProbateControllerNotAuthorised.onSubmit()(createFakeRequest(false))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(loginUrl)
    }

    "redirect to GG login page on PageLoad in edit mode if the user is not logged in" in {
      val result = othersApplyingForProbateControllerNotAuthorised.onEditPageLoad()(createFakeRequest(false))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(loginUrl)
    }

    "redirect to GG login page on Submit in edit mode if the user is not logged in" in {
      val result = othersApplyingForProbateControllerNotAuthorised.onEditSubmit()(createFakeRequest(false))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(loginUrl)
    }

    "redirect to GG login page on PageLoad if the user is not logged in and arrived from overview" in {
      val result = othersApplyingForProbateControllerNotAuthorised.onPageLoadFromOverview()(createFakeRequest(false))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(loginUrl)
    }

    "redirect to GG login page on Submit if the user is not logged in and arrived from overview" in {
      val result = othersApplyingForProbateControllerNotAuthorised.onSubmitFromOverview()(createFakeRequest(false))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(loginUrl)
    }

    "respond appropriately to a submit with a value of Yes" in  {
      val registrationDetails = RegistrationDetails(None, None, None)
      val probateForm = othersApplyingForProbateForm.fill(Some(true))
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=probateForm.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = othersApplyingForProbateController.onSubmit()(request)
      status(result) shouldBe(SEE_OTHER)
      redirectLocation(result) should be(Some(routes.CoExecutorPersonalDetailsController.onPageLoad(None).url))
    }

    "respond appropriately to a submit with a value of No" in  {
      val registrationDetails = RegistrationDetails(None, None, None)
      val probateForm = othersApplyingForProbateForm.fill(Some(false))
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=probateForm.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = othersApplyingForProbateController.onSubmitFromOverview()(request)
      status(result) shouldBe(SEE_OTHER)
      redirectLocation(result) should be(Some(registrationRoutes.RegistrationSummaryController.onPageLoad.url))
    }

    "When submitting a yes - the areOthersApplyingForProbate must be set to false and any coexcutors must be removed from registration details" in  {
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      val probateForm = othersApplyingForProbateForm.fill(Some(true))
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=probateForm.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = othersApplyingForProbateController.onSubmitFromOverview()(request)
      status(result) shouldBe(SEE_OTHER)
      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      capturedValue.coExecutors.length shouldBe 1
      capturedValue.areOthersApplyingForProbate shouldBe Some(true)
    }

    "When submitting a no - the areOthersApplyingForProbate must be set to false and any coexcutors must be removed from registration details" in  {
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      val probateForm = othersApplyingForProbateForm.fill(Some(false))
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=probateForm.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = othersApplyingForProbateController.onSubmitFromOverview()(request)
      status(result) shouldBe(SEE_OTHER)
      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      capturedValue.coExecutors.length shouldBe 0
      capturedValue.areOthersApplyingForProbate shouldBe Some(false)

    }

    "respond appropriately to an invalid submit: Missing mandatory fields" in {
      val registrationDetails = RegistrationDetails(None, None, None)
      val probateForm = othersApplyingForProbateForm.fill(None)
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=probateForm.data.toSeq)

      createMockToGetRegDetailsFromCache(mockCachingConnector, None)
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = await(othersApplyingForProbateController.onSubmit()(request))
      status(result) shouldBe(BAD_REQUEST)
    }

    "return true if the guard conditions are true" in {
      val rd = CommonBuilder.buildRegistrationDetails copy (deceasedDateOfDeath =
        Some(DeceasedDateOfDeath(LocalDate.now)), applicantDetails = Some(CommonBuilder.buildApplicantDetails))
      othersApplyingForProbateController.checkGuardCondition(rd, "") shouldBe true
    }

    "raise an error when the submit has a value of Yes but the storage fails" in  {
      val registrationDetails = RegistrationDetails(None, None, None)
      val probateForm = othersApplyingForProbateForm.fill(Some(true))
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=probateForm.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCacheWithFailure(mockCachingConnector, Some(registrationDetails))

      val result = othersApplyingForProbateController.onSubmit()(request)
      status(result) shouldBe(INTERNAL_SERVER_ERROR)
    }
  }
}
