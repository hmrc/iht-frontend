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
import iht.metrics.IhtMetrics
import iht.testhelpers.CommonBuilder
import iht.testhelpers.CommonBuilder._
import iht.views.html.registration.executor.executor_overview
import org.scalatest.BeforeAndAfter
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

class ExecutorOverviewControllerTest extends RegistrationControllerTest with BeforeAndAfter with CoExecutorForms {

  lazy val appConfig = mockAppConfig

  protected abstract class TestController extends FrontendController(mockControllerComponents) with ExecutorOverviewController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val executorOverviewView: executor_overview = app.injector.instanceOf[executor_overview]
  }

  def executorOverviewController = new TestController {
    override def cachingConnector: CachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override def metrics: IhtMetrics = mockIhtMetrics
  }

  def executorOverviewControllerNotAuthorised = new TestController {
    override def cachingConnector: CachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override def metrics: IhtMetrics = mockIhtMetrics
  }

  "ExecutorOverviewController" must {
    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = executorOverviewControllerNotAuthorised.onPageLoad(createFakeRequest(false))
      status(result) mustBe (SEE_OTHER)
      redirectLocation(result) mustBe (Some(loginUrl))
    }

    "redirect to GG login page on Submit if the user is not logged in" in {
      val result = executorOverviewControllerNotAuthorised.onSubmit(createFakeRequest(false))
      status(result) mustBe (SEE_OTHER)
      redirectLocation(result) mustBe (Some(loginUrl))
    }

    "The page can only be seen if others are applying for probate" in {
      val host="localhost:9070"

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails))

      intercept[Exception] {
        val result = executorOverviewController.onPageLoad(createFakeRequestWithReferrer(referrerURL = referrerURL, host = host, authRetrieveNino = false))
        status(result) mustBe (OK)
      }
    }

    "the displayed page should contain the title 'Other people applying for probate'" in {
      val rd = CommonBuilder.buildRegistrationDetails copy (areOthersApplyingForProbate = Some(true), coExecutors = Seq())

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = executorOverviewController.onPageLoad(createFakeRequestWithReferrer(referrerURL=referrerURL,host="localhost:9070", authRetrieveNino = false))

      status(result) mustBe(OK)
      contentAsString(result) must include(messagesApi("iht.registration.othersApplyingForProbate"))
    }

   "load the existing coexecutors when they exist" in {
      val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      val existingCoExec0 = CommonBuilder.buildCoExecutor
      val rdWithCoExecs = rd copy (coExecutors = Seq(existingCoExec0, CommonBuilder.DefaultCoExecutor1))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(rdWithCoExecs)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithCoExecs))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rdWithCoExecs))

      val result = executorOverviewController.onPageLoad(createFakeRequestWithReferrer(referrerURL=referrerURL,host="localhost:9070", authRetrieveNino = false))

      status(result) must be(OK)
      contentAsString(result) must include(CommonBuilder.DefaultName)
      contentAsString(result) must include(CommonBuilder.DefaultCoExecutor1.name)
    }

    "if there are three coExecutors already the radio buttons to add more must not exist but the continue button should" in {
      val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      val existingCoExec0 = CommonBuilder.buildCoExecutor
      val rdWithCoExecs = rd copy (coExecutors = Seq(existingCoExec0, CommonBuilder.DefaultCoExecutor1, CommonBuilder.DefaultCoExecutor2))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(rdWithCoExecs)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithCoExecs))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rdWithCoExecs))


      val result = executorOverviewController.onPageLoad(createFakeRequestWithReferrer(referrerURL=referrerURL,host="localhost:9070", authRetrieveNino = false))

      status(result) must be(OK)
      contentAsString(result) must include(CommonBuilder.DefaultName)
      contentAsString(result) must include(CommonBuilder.DefaultCoExecutor1.name)
      contentAsString(result) must include(CommonBuilder.DefaultCoExecutor2.name)
      contentAsString(result) must include(messagesApi("iht.continue"))
      contentAsString(result) must not include messagesApi("page.iht.registration.executor-overview.yesnoQuestion")
      contentAsString(result) must not include "radio" // There are some radio buttons
    }
  }

  "when the summary page is displayed with fewer than three names, clicking on yes redirects to the uk address entry page" in {
    val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
    val existingCoExec0 = CommonBuilder.buildCoExecutor
    val rdWithCoExecs = rd copy (coExecutors = Seq(existingCoExec0, CommonBuilder.DefaultCoExecutor1))
    val summaryForm = executorOverviewForm.fill(Some(true))

    createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(rdWithCoExecs)))
    createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithCoExecs))
    createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rdWithCoExecs))

    val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = "localhost:9070", data = summaryForm.data.toSeq, authRetrieveNino = false)
      .withMethod("POST")

    val result = executorOverviewController.onSubmit()(request)
    status(result) mustBe SEE_OTHER
  }

  "when the summary page is displayed with fewer than three names, clicking on no redirects to the next page in the sequence" in {
    val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
    val existingCoExec0 = CommonBuilder.buildCoExecutor
    val rdWithCoExecs = rd copy (coExecutors = Seq(existingCoExec0, CommonBuilder.DefaultCoExecutor1))
    val summaryForm = executorOverviewForm.fill(Some(false))

    createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(rdWithCoExecs)))
    createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithCoExecs))
    createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rdWithCoExecs))


    val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = "localhost:9070", data = summaryForm.data.toSeq, authRetrieveNino = false)
      .withMethod("POST")

    val result = executorOverviewController.onSubmit()(request)
    status(result) mustBe SEE_OTHER
    redirectLocation(result) mustBe Some(registrationRoutes.RegistrationSummaryController.onPageLoad.url)
  }

  "when the summary page is displayed with fewer than three names, clicking on continue without selecting yes of no is an error" in {
    val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
    val existingCoExec0 = CommonBuilder.buildCoExecutor
    val rdWithCoExecs = rd copy (coExecutors = Seq(existingCoExec0, CommonBuilder.DefaultCoExecutor1))
    val summaryForm = executorOverviewForm.fill(None)

    createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(rdWithCoExecs)))
    createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithCoExecs))
    createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rdWithCoExecs))


    val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = "localhost:9070", data = summaryForm.data.toSeq, authRetrieveNino = false)
      .withMethod("POST")

    val result = executorOverviewController.onSubmit()(request)
    status(result)
    status(result) mustBe(BAD_REQUEST)
  }

  "when  registration details has no coexecutors but areOthersAplyingForProbate is set, submission must return an error message" in {
    val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
    val rdWithNoCoExecs = rd copy (coExecutors = Seq())
    val summaryForm = executorOverviewForm.fill(Some(false))

    createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(rdWithNoCoExecs)))
    createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithNoCoExecs))
    createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rdWithNoCoExecs))

    val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = "localhost:9070", data = summaryForm.data.toSeq, authRetrieveNino = false)
      .withMethod("POST")
    val result = executorOverviewController.onSubmit()(request)

    status(result) mustBe BAD_REQUEST
    contentAsString(result) must include(escapeApostrophes(messagesApi("error.applicant.insufficientCoExecutors")))
    contentAsString(result) must include(messagesApi("error.applicant.insufficientCoExecutors"))
  }
}
