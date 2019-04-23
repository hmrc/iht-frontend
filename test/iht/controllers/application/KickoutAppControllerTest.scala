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

package iht.controllers.application

import java.util.UUID

import iht.config.AppConfig
import iht.metrics.IhtMetrics

import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever, MockObjectBuilder, TestHelper}
import iht.utils.{DeceasedInfoHelper, KickOutReason, ApplicationStatus => AppStatus}
import org.mockito.ArgumentMatchers._
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.{status => playStatus, _}
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class KickoutAppControllerTest extends ApplicationControllerTest {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with KickoutAppController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def kickoutController = new TestController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = mockAuthConnector

    override lazy val metrics:IhtMetrics = mock[IhtMetrics]
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def kickoutControllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = mockAuthConnector

    override lazy val metrics:IhtMetrics = mock[IhtMetrics]
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  val uuid = s"session-${UUID.randomUUID}"

  "Kickout Controller" must {
    "load ihtkickout in flight page " in {

        val applicationDetails = CommonBuilder.buildApplicationDetails
          .copy(kickoutReason = Some(KickOutReason.ForeignAssetsValueMoreThanMax),
            status = AppStatus.KickOut)
        val regDetails = buildRegistrationDetailsWithDeceasedAndIhtRefDetails
        createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(regDetails)))
        createMockToGetApplicationDetails(mockIhtConnector, Some(applicationDetails))
        createMockToDoNothingWhenDeleteSingleValueFromCache(mockCachingConnector)
        createMockToGetSingleValueFromCache(mockCachingConnector, singleValueReturn = None)

        val result = kickoutController.onPageLoad(createFakeRequest(isAuthorised = true))
        playStatus(result) must be(OK)
        contentAsString(result) must include(messagesApi("page.iht.application.assets.kickout.foreignAssetsValueMoreThanMax.summary",
                                                DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)))
    }

    "load ihtkickout on im done page " in {
        val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
          increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility),
          widowCheck = Some(CommonBuilder.buildWidowedCheck),
          kickoutReason = Some(KickOutReason.ExemptionEstateValueIsMoreThanMaximum),
          status = AppStatus.KickOut)

        createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)
        createMockToGetApplicationDetails(mockIhtConnector, Some(applicationDetails))
        createMockToDoNothingWhenDeleteSingleValueFromCache(mockCachingConnector)
        createMockToGetSingleValueFromCache(mockCachingConnector, singleValueReturn = None)

        val result = kickoutController.onPageLoad(createFakeRequest(isAuthorised = true))
        playStatus(result) must be(OK)
        contentAsString(result) must include(messagesApi("page.iht.application.tnrb.kickout.estateValueNotInLimit.summary"))
    }
  }

  "onPageLoad method" must {
    "redirect to ida login page on ApplicationPageLoad if the user is not logged in" in {
        val result = kickoutControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
        playStatus(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(loginUrl))
    }

    "redirect to ida login page on Submit if the user is not logged in" in {
        val result = kickoutControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
        playStatus(result) must be(SEE_OTHER)
        redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load when there is a valid kickout Reason data in Keystore" in {
        implicit val request = FakeRequest().withSession(SessionKeys.sessionId -> uuid)
        val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
          kickoutReason = Some(TestHelper.KickOutAnnuitiesOnInsurance),
          status = AppStatus.KickOut)

        createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)
        createMockToGetApplicationDetails(mockIhtConnector, Some(applicationDetails))
        createMockToGetApplicationDetailsFromCache(mockCachingConnector, Some(applicationDetails))
        createMockToDoNothingWhenDeleteSingleValueFromCache(mockCachingConnector)
        createMockToGetSingleValueFromCache(mockCachingConnector, singleValueReturn = None)

        val result = kickoutController.onPageLoad(createFakeRequest())
        playStatus(result) mustBe OK
    }

    "intercept RuntimeException on page load when there is no application details data in Keystore" in {
        createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)
        createMockToDoNothingWhenDeleteSingleValueFromCache(mockCachingConnector)
        createMockToGetSingleValueFromCache(mockCachingConnector, singleValueReturn = None)
        createMockToGetApplicationDetails(mockIhtConnector, None)

        implicit val request = FakeRequest().withSession(SessionKeys.sessionId -> uuid)
        intercept[RuntimeException] {
          val result = kickoutController.onPageLoad(createFakeRequest())
          playStatus(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "intercept RuntimeException on page load when there is NOT a valid kickoutReason data in Keystore" in {
        createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)
        createMockToGetApplicationDetails(mockIhtConnector)
        createMockToDoNothingWhenDeleteSingleValueFromCache(mockCachingConnector)
        createMockToGetSingleValueFromCache(mockCachingConnector, singleValueReturn = None)

        implicit val request = FakeRequest().withSession(SessionKeys.sessionId -> uuid)

        intercept[RuntimeException] {
          val result = kickoutController.onPageLoad(createFakeRequest())
          playStatus(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "respond with redirect to deleting estate report page on page submission" in {
        createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)
        createMockToDoNothingWhenDeleteSingleValueFromCache(mockCachingConnector)
        createMockToDoNothingWhenDeleteApplication(mockIhtConnector)
        createMockToStoreSingleValueInCache(mockCachingConnector, any(), Some("true"))
        createMockToGetSingleValueFromCache(mockCachingConnector, any(), None)
        implicit val request = FakeRequest().withSession(SessionKeys.sessionId -> uuid)
        val result = kickoutController.onSubmit(createFakeRequest())
        playStatus(result) mustBe SEE_OTHER
        redirectLocation(result) must be (Some(
          iht.controllers.application.routes.KickoutAppController.onPageLoadDeleting().url))
    }

    "respond with redirect to iht400 page on page submission when first page already seen" in {
        createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)
        createMockToDoNothingWhenDeleteSingleValueFromCache(mockCachingConnector)
        createMockToDoNothingWhenDeleteApplication(mockIhtConnector)
        createMockToGetSingleValueFromCache(mockCachingConnector, any(), Some("true"))
        createMockToGetApplicationDetails(mockIhtConnector)
        implicit val request = FakeRequest().withSession(SessionKeys.sessionId -> uuid)
        val result = kickoutController.onSubmit(createFakeRequest())
        playStatus(result) mustBe SEE_OTHER
        redirectLocation(result) must be (Some(iht.controllers.routes.DeadlinesController.onPageLoadApplication().url))
    }

    "respond with INTERNAL_SERVER_ERROR on page submission when unable to store value in cache" in {
        createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)
        createMockToDoNothingWhenDeleteSingleValueFromCache(mockCachingConnector)
        createMockToDoNothingWhenDeleteApplication(mockIhtConnector)
        createMockToStoreSingleValueInCache(mockCachingConnector, any(), None)
        createMockToGetSingleValueFromCache(mockCachingConnector, any(), None)
        implicit val request = FakeRequest().withSession(SessionKeys.sessionId -> uuid)
        val result = kickoutController.onSubmit(createFakeRequest())
        playStatus(result) mustBe INTERNAL_SERVER_ERROR
    }

    "respond with redirect to application overview when no registration details found in cache" in {
        createMockToGetRegDetailsFromCache(mockCachingConnector, None)
        val result = kickoutController.onPageLoad(createFakeRequest())
        playStatus(result) must be(SEE_OTHER)
        redirectLocation(result) mustBe Some(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url)
    }
  }
}
