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

package iht.controllers.registration.executor

import iht.connector.CachingConnector
import iht.controllers.registration.{RegistrationControllerTest, routes => registrationRoutes}
import iht.forms.registration.CoExecutorForms._
import iht.metrics.Metrics
import iht.testhelpers.CommonBuilder
import iht.testhelpers.CommonBuilder._
import iht.testhelpers.MockObjectBuilder._
import org.scalatest.BeforeAndAfter
import play.api.test.Helpers._
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

class ExecutorOverviewControllerTest extends RegistrationControllerTest with BeforeAndAfter {

  before {
    mockCachingConnector = mock[CachingConnector]
  }

  //Create controller object and pass in mock.
  def executorOverviewController = new ExecutorOverviewController {
    override def metrics: Metrics = Metrics
    override def cachingConnector: CachingConnector = mockCachingConnector
    override protected def authConnector: AuthConnector = createFakeAuthConnector(true)
    override val isWhiteListEnabled = false
  }

  def executorOverviewControllerNotAuthorised = new ExecutorOverviewController {
    override def metrics: Metrics = Metrics
    override def cachingConnector: CachingConnector = mockCachingConnector
    override protected def authConnector: AuthConnector = createFakeAuthConnector(false)
    override val isWhiteListEnabled = false
  }

  "ExecutorOverviewController" must {
    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = executorOverviewControllerNotAuthorised.onPageLoad(createFakeRequest(false))
      status(result) shouldBe (SEE_OTHER)
      redirectLocation(result) shouldBe (Some(loginUrl))
    }

    "redirect to GG login page on Submit if the user is not logged in" in {
      val result = executorOverviewControllerNotAuthorised.onSubmit(createFakeRequest(false))
      status(result) shouldBe (SEE_OTHER)
      redirectLocation(result) shouldBe (Some(loginUrl))
    }

    "The page can only be seen if others are applying for probate" in {
      val host="localhost:9070"

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails))

      intercept[Exception] {
        val result = executorOverviewController.onPageLoad()(createFakeRequestWithReferrer(referrerURL = referrerURL, host = host))
        status(result) shouldBe (OK)
      }
    }

    "the displayed page should contain the title 'Other people applying for probate'" in {
      val rd = CommonBuilder.buildRegistrationDetails copy (areOthersApplyingForProbate = Some(true), coExecutors = Seq())

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = executorOverviewController.onPageLoad()(createFakeRequestWithReferrer(referrerURL=referrerURL,host="localhost:9070"))

      status(result) shouldBe(OK)
      contentAsString(result) should include(messagesApi("iht.registration.othersApplyingForProbate"))
    }

   "load the existing coexecutors when they exist" in {
      val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      val existingCoExec0 = CommonBuilder.buildCoExecutor
      val rdWithCoExecs = rd copy (coExecutors = Seq(existingCoExec0, CommonBuilder.DefaultCoExecutor1))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, rdWithCoExecs)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithCoExecs))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rdWithCoExecs))

      val result = executorOverviewController.onPageLoad()(createFakeRequestWithReferrer(referrerURL=referrerURL,host="localhost:9070"))

      status(result) should be(OK)
      contentAsString(result) should include(CommonBuilder.DefaultName)
      contentAsString(result) should include(CommonBuilder.DefaultCoExecutor1.name)
    }

    "if there are three coExecutors already the radio buttons to add more should not exist but the continue button should" in {
      val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      val existingCoExec0 = CommonBuilder.buildCoExecutor
      val rdWithCoExecs = rd copy (coExecutors = Seq(existingCoExec0, CommonBuilder.DefaultCoExecutor1, CommonBuilder.DefaultCoExecutor2))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, rdWithCoExecs)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithCoExecs))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rdWithCoExecs))


      val result = executorOverviewController.onPageLoad()(createFakeRequestWithReferrer(referrerURL=referrerURL,host="localhost:9070"))

      status(result) should be(OK)
      contentAsString(result) should include(CommonBuilder.DefaultName)
      contentAsString(result) should include(CommonBuilder.DefaultCoExecutor1.name)
      contentAsString(result) should include(CommonBuilder.DefaultCoExecutor2.name)
      contentAsString(result) should include(messagesApi("iht.continue"))
      contentAsString(result) should not include messagesApi("page.iht.registration.executor-overview.yesnoQuestion")
      contentAsString(result) should not include "radio" // There are some radio buttons
    }
  }

  "when the summary page is displayed with fewer than three names, clicking on yes redirects to the uk address entry page" in {
    val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
    val existingCoExec0 = CommonBuilder.buildCoExecutor
    val rdWithCoExecs = rd copy (coExecutors = Seq(existingCoExec0, CommonBuilder.DefaultCoExecutor1))
    val summaryForm = executorOverviewForm.fill(Some(true))

    createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, rdWithCoExecs)
    createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithCoExecs))
    createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rdWithCoExecs))

    val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = "localhost:9070", data = summaryForm.data.toSeq)

    val result = executorOverviewController.onSubmit()(request)
    status(result) shouldBe SEE_OTHER
  }

  "when the summary page is displayed with fewer than three names, clicking on no redirects to the next page in the sequence" in {
    val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
    val existingCoExec0 = CommonBuilder.buildCoExecutor
    val rdWithCoExecs = rd copy (coExecutors = Seq(existingCoExec0, CommonBuilder.DefaultCoExecutor1))
    val summaryForm = executorOverviewForm.fill(Some(false))

    createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, rdWithCoExecs)
    createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithCoExecs))
    createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rdWithCoExecs))


    val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = "localhost:9070", data = summaryForm.data.toSeq)

    val result = executorOverviewController.onSubmit()(request)
    status(result) shouldBe SEE_OTHER
    redirectLocation(result) shouldBe Some(registrationRoutes.RegistrationSummaryController.onPageLoad().url)
  }

  "when the summary page is displayed with fewer than three names, clicking on continue without selecting yes of no is an error" in {
    val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
    val existingCoExec0 = CommonBuilder.buildCoExecutor
    val rdWithCoExecs = rd copy (coExecutors = Seq(existingCoExec0, CommonBuilder.DefaultCoExecutor1))
    val summaryForm = executorOverviewForm.fill(None)

    createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, rdWithCoExecs)
    createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithCoExecs))
    createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rdWithCoExecs))


    val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = "localhost:9070", data = summaryForm.data.toSeq)

    val result = executorOverviewController.onSubmit()(request)
    status(result)
    status(result) shouldBe(BAD_REQUEST)
  }

  "when  registration details has no coexecutors but areOthersAplyingForProbate is set, submission must return an error message" in {
    val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
    val rdWithNoCoExecs = rd copy (coExecutors = Seq())
    val summaryForm = executorOverviewForm.fill(Some(false))

    createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, rdWithNoCoExecs)
    createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithNoCoExecs))
    createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rdWithNoCoExecs))

    val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = "localhost:9070", data = summaryForm.data.toSeq)
    val result = executorOverviewController.onSubmit()(request)

    status(result) shouldBe BAD_REQUEST
    contentAsString(result) should include(escapeApostrophes(messagesApi("error.applicant.insufficientCoExecutors")))
    contentAsString(result) should include(messagesApi("error.applicant.insufficientCoExecutors"))
  }
}
