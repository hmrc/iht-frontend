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

package iht.controllers.application.assets

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever, TestHelper}
import org.mockito.ArgumentMatchers._
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future


class AssetsOverviewControllerTest extends ApplicationControllerTest {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with AssetsOverviewController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def assetsOverviewController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  val allAssets=CommonBuilder.buildAllAssets
  val applicationDetails=CommonBuilder.buildApplicationDetails copy (allAssets = Some(allAssets))
  val finalDestinationURL = "url"
  val ref = "A1912233"

  def createMocksForRegistrationAndApplication(rd: RegistrationDetails, ad: ApplicationDetails) = {
    createMockToGetCaseDetails(mockIhtConnector, rd)
    createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(rd)))
    createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rd))
    createMockToGetApplicationDetails(mockIhtConnector, Some(ad))
    createMockToGetProbateDetails(mockIhtConnector)
    createMockToGetProbateDetailsFromCache(mockCachingConnector)
    createMockToSaveApplicationDetails(mockIhtConnector, Some(ad))
    createMockToGetCaseList(mockIhtConnector, Seq(CommonBuilder.buildIhtApplication.copy(ihtRefNo = ref)))
    createMockToGetSingleValueFromCache(
      cachingConnector = mockCachingConnector,
      singleValueFormKey = same(TestHelper.ExemptionsGuidanceSeen),
      singleValueReturn = Some("true"))
  }

  "AssetsOverviewController" must {

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = assetsOverviewController.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load where assets exist" in {
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      createMocksForApplication(cachingConnector= mockCachingConnector,
        ihtConnector = mockIhtConnector ,
        appDetails = Some(applicationDetails),
        getAppDetails = true)

      val result = assetsOverviewController.onPageLoad (createFakeRequest())
      status(result) mustBe (OK)
    }

    "respond with OK on page load where no assets" in {
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      val ad=CommonBuilder.buildApplicationDetails copy (allAssets = None)
      createMocksForApplication(cachingConnector= mockCachingConnector,
        ihtConnector = mockIhtConnector ,
        appDetails = Some(ad),
        getAppDetails = true)

      val result = assetsOverviewController.onPageLoad (createFakeRequest())
      status(result) mustBe (OK)
    }

    "respond with error on page load when there is no ApplicationDetails" in {

      createMocksForApplication(cachingConnector= mockCachingConnector,
        ihtConnector = mockIhtConnector ,
        appDetails= None,
        getAppDetails = true)

      a[RuntimeException] mustBe thrownBy {
        await(assetsOverviewController.onPageLoad (createFakeRequest()))
      }
    }

    "respond with REDIRECT when the estate exceeds the TNRB threashold" in {
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      createMocksForRegistrationAndApplication(
        CommonBuilder.buildRegistrationDetails1,
        CommonBuilder.buildApplicationDetailsOverLowerThresholdAndGuidanceSeenFlagNotSet(ref))

      val result = assetsOverviewController.onPageLoad(createFakeRequest())
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be (Some(iht.controllers.application.exemptions.routes.ExemptionsGuidanceIncreasingThresholdController.onPageLoad(ref).url))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      assetsOverviewController.onPageLoad(createFakeRequest()))

  }
}
