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

package iht.controllers.application

import java.util.UUID

import iht.connector.{CachingConnector, IhtConnector}
import iht.metrics.Metrics
import iht.testhelpers.MockObjectBuilder._
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder, MockObjectBuilder, TestHelper}
import iht.utils.{DeceasedInfoHelper, KickOutReason, ApplicationStatus => AppStatus}
import org.mockito.ArgumentMatchers._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever
import uk.gov.hmrc.http.SessionKeys

class KickoutControllerTest extends ApplicationControllerTest {

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def kickoutController = new KickoutController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val ihtConnector = mockIhtConnector

    override lazy val metrics:Metrics = mock[Metrics]
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def kickoutControllerNotAuthorised = new KickoutController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val ihtConnector = mockIhtConnector

    override lazy val metrics:Metrics = mock[Metrics]
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  val uuid = s"session-${UUID.randomUUID}"

  "Kickout Controller" must {
    "load ihtkickout in flight page " in {

        val applicationDetails = CommonBuilder.buildApplicationDetails
          .copy(kickoutReason = Some(KickOutReason.ForeignAssetsValueMoreThanMax),
            status = AppStatus.KickOut)
        val regDetails = MockObjectBuilder.buildRegistrationDetailsWithDeceasedAndIhtRefDetails
        createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, regDetails)
        createMockToGetApplicationDetails(mockIhtConnector, Some(applicationDetails))
        createMockToDoNothingWhenDeleteSingleValueSyncFromCache(mockCachingConnector)
        createMockToGetSingleValueFromCache(mockCachingConnector, singleValueReturn = None)

        val result = kickoutController.onPageLoad(createFakeRequest(isAuthorised = true))
        status(result) should be(OK)
        contentAsString(result) should include(messagesApi("page.iht.application.assets.kickout.foreignAssetsValueMoreThanMax.summary",
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
        createMockToDoNothingWhenDeleteSingleValueSyncFromCache(mockCachingConnector)
        createMockToGetSingleValueFromCache(mockCachingConnector, singleValueReturn = None)

        val result = kickoutController.onPageLoad(createFakeRequest(isAuthorised = true))
        status(result) should be(OK)
        contentAsString(result) should include(messagesApi("page.iht.application.tnrb.kickout.estateValueNotInLimit.summary"))
    }
  }

  "onPageLoad method" must {
    "redirect to ida login page on ApplicationPageLoad if the user is not logged in" in {
        val result = kickoutControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
        status(result) should be(SEE_OTHER)
        redirectLocation(result) should be(Some(loginUrl))
    }

    "redirect to ida login page on Submit if the user is not logged in" in {
        val result = kickoutControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
        status(result) should be(SEE_OTHER)
        redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load when there is a valid kickout Reason data in Keystore" in {
        implicit val request = FakeRequest().withSession(SessionKeys.sessionId -> uuid)
        val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
          kickoutReason = Some(TestHelper.KickOutAnnuitiesOnInsurance),
          status = AppStatus.KickOut)

        createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)
        createMockToGetApplicationDetails(mockIhtConnector, Some(applicationDetails))
        createMockToGetApplicationDetailsFromCache(mockCachingConnector, Some(applicationDetails))
        createMockToDoNothingWhenDeleteSingleValueSyncFromCache(mockCachingConnector)
        createMockToGetSingleValueFromCache(mockCachingConnector, singleValueReturn = None)

        val result = kickoutController.onPageLoad(createFakeRequest())
        status(result) shouldBe OK
    }

    "intercept RuntimeException on page load when there is no application details data in Keystore" in {
        createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)
        createMockToDoNothingWhenDeleteSingleValueSyncFromCache(mockCachingConnector)
        createMockToGetSingleValueFromCache(mockCachingConnector, singleValueReturn = None)
        createMockToGetApplicationDetails(mockIhtConnector, None)

        implicit val request = FakeRequest().withSession(SessionKeys.sessionId -> uuid)
        intercept[RuntimeException] {
          val result = kickoutController.onPageLoad(createFakeRequest())
          status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "intercept RuntimeException on page load when there is NOT a valid kickoutReason data in Keystore" in {
        createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)
        createMockToGetApplicationDetails(mockIhtConnector)
        createMockToDoNothingWhenDeleteSingleValueSyncFromCache(mockCachingConnector)
        createMockToGetSingleValueFromCache(mockCachingConnector, singleValueReturn = None)

        implicit val request = FakeRequest().withSession(SessionKeys.sessionId -> uuid)

        intercept[RuntimeException] {
          val result = kickoutController.onPageLoad(createFakeRequest())
          status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "respond with redirect to deleting estate report page on page submission" in {
        createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)
        createMockToDoNothingWhenDeleteSingleValueSyncFromCache(mockCachingConnector)
        createMockToDoNothingWhenDeleteApplication(mockIhtConnector)
        createMockToStoreSingleValueInCache(mockCachingConnector, any(), Some("true"))
        createMockToGetSingleValueFromCache(mockCachingConnector, any(), None)
        implicit val request = FakeRequest().withSession(SessionKeys.sessionId -> uuid)
        val result = kickoutController.onSubmit(createFakeRequest())
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) should be (Some(
          iht.controllers.application.routes.KickoutController.onPageLoadDeleting().url))
    }

    "respond with redirect to iht400 page on page submission when first page already seen" in {
        createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)
        createMockToDoNothingWhenDeleteSingleValueSyncFromCache(mockCachingConnector)
        createMockToDoNothingWhenDeleteApplication(mockIhtConnector)
        createMockToGetSingleValueFromCache(mockCachingConnector, any(), Some("true"))
        implicit val request = FakeRequest().withSession(SessionKeys.sessionId -> uuid)
        val result = kickoutController.onSubmit(createFakeRequest())
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) should be (Some(iht.controllers.routes.DeadlinesController.onPageLoadApplication().url))
    }

    "respond with INTERNAL_SERVER_ERROR on page submission when unable to store value in cache" in {
        createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)
        createMockToDoNothingWhenDeleteSingleValueSyncFromCache(mockCachingConnector)
        createMockToDoNothingWhenDeleteApplication(mockIhtConnector)
        createMockToStoreSingleValueInCache(mockCachingConnector, any(), None)
        createMockToGetSingleValueFromCache(mockCachingConnector, any(), None)
        implicit val request = FakeRequest().withSession(SessionKeys.sessionId -> uuid)
        val result = kickoutController.onSubmit(createFakeRequest())
        status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "respond with redirect to application overview when no registration details found in cache" in {
        createMockToGetRegDetailsFromCache(mockCachingConnector, None)
        val result = kickoutController.onPageLoad(createFakeRequest())
        status(result) should be(SEE_OTHER)
        redirectLocation(result) shouldBe Some(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url)
    }
  }
}
