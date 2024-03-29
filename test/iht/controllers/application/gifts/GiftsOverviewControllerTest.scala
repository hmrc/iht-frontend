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

package iht.controllers.application.gifts

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.testhelpers._
import iht.utils._
import iht.views.html.application.gift.gifts_overview
import org.mockito.ArgumentMatchers._
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future


class GiftsOverviewControllerTest extends ApplicationControllerTest {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with GiftsOverviewController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val giftsOverviewView: gifts_overview = app.injector.instanceOf[gifts_overview]
  }

  def giftsOverviewController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def giftsOverviewControllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def createMocksForRegistrationAndApplication(rd: RegistrationDetails, ad: ApplicationDetails) = {
    createMockToGetCaseDetails(mockIhtConnector, Future.successful(rd))
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

  val finalDestinationURL = "url"
  val ref = "A1912233"




  "GiftsOverviewController" must {

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = giftsOverviewControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(loginUrl))
    }

    val allGiftsQ1Yes = CommonBuilder.buildAllGifts copy (isGivenAway = Some(true))

    "respond with OK on page load" in {
      val allGifts = allGiftsQ1Yes
      val applicationDetails = CommonBuilder.buildApplicationDetails copy (allGifts = Some(allGifts))
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = giftsOverviewController.onPageLoad(createFakeRequest())

      status(result) must be(OK)
    }

    "respond with error on page load when there are no ApplicationDetails" in {

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = None,
        getAppDetails = true)

      giftsOverviewController.onPageLoad(createFakeRequest())
    }

    "redirect when first question, is given away, is not answered" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails copy (allGifts = Some(CommonBuilder.buildAllGifts))
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = giftsOverviewController.onPageLoad(createFakeRequest())

      status(result) must be(SEE_OTHER)
      redirectLocation(result) mustBe Some(iht.controllers.application.gifts.routes.GivenAwayController.onPageLoad.url)
    }

    "display NO on page when there is no reservation of benefit" in {
      val allGifts = allGiftsQ1Yes copy (isReservation = Some(false))
      val applicationDetails = CommonBuilder.buildApplicationDetails copy (allGifts = Some(allGifts))
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = giftsOverviewController.onPageLoad(createFakeRequest())

      status(result) must be(OK)
      contentAsString(result) must include(messagesApi("iht.no"))
    }

    "display YES on page when there is no reservation of benefit" in {
      val allGifts = allGiftsQ1Yes copy (isReservation = Some(true))
      val applicationDetails = CommonBuilder.buildApplicationDetails copy (allGifts = Some(allGifts))
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = giftsOverviewController.onPageLoad(createFakeRequest())

      status(result) must be(OK)
      contentAsString(result) must include(messagesApi("iht.yes"))
    }

    "display return to estate overview link on page" in {
      val allGifts = allGiftsQ1Yes
      val applicationDetails = CommonBuilder.buildApplicationDetails copy (allGifts = Some(allGifts))
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = giftsOverviewController.onPageLoad(createFakeRequest())

      status(result) must be(OK)
      contentAsString(result) must include(messagesApi("iht.estateReport.returnToEstateOverview"))
    }

    "display gift guidance on page" in {
      val allGifts = allGiftsQ1Yes
      val applicationDetails = CommonBuilder.buildApplicationDetails copy (allGifts = Some(allGifts))
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      val regDetails = buildRegistrationDetailsWithDeceasedAndIhtRefDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = regDetails,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = giftsOverviewController.onPageLoad(createFakeRequest())
      status(result) must be(OK)
      ContentChecker.stripLineBreaks(contentAsString(result)) must include(messagesApi("page.iht.application.gifts.overview.guidance1",
                                                       DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails),
                                                       DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)))
    }

    "display gift value on page when value entered" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allGifts = Some(allGiftsQ1Yes),
        giftsList = Some(Seq(CommonBuilder.buildPreviousYearsGifts)))
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = giftsOverviewController.onPageLoad(createFakeRequest())
      status(result) must be(OK)
      contentAsString(result) must include(messagesApi("page.iht.application.gifts.overview.value.question1"))
      contentAsString(result) must include("£0")
    }

    "not display any gift value on page when value is not entered" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allGifts = Some(allGiftsQ1Yes),
        giftsList = Some(Seq(CommonBuilder.buildPreviousYearsGifts
          .copy(value = None, exemptions = None))))
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = giftsOverviewController.onPageLoad(createFakeRequest())
      status(result) must be(OK)
      contentAsString(result) must not include messagesApi("page.iht.application.gifts.overview.value.question1")
    }

    "not display 'value of gifts given away' question when initial question answered 'No'" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allGifts = Some(CommonBuilder.buildAllGifts
        .copy(isGivenAway = Some(false))))
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = giftsOverviewController.onPageLoad(createFakeRequest())
      status(result) must be(OK)
      contentAsString(result) must not include messagesApi("iht.estateReport.gifts.valueOfGiftsGivenAway")
    }

    "display 'value of gifts given away' question when initial question answered 'Yes'" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allGifts = Some(CommonBuilder.buildAllGifts
        .copy(isGivenAway = Some(true))))
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = giftsOverviewController.onPageLoad(createFakeRequest())
      status(result) must be(OK)
      contentAsString(result) must include(messagesApi("iht.estateReport.gifts.valueOfGiftsGivenAway"))
    }


    "display second 'gifts given to trust' question when all other questions answered 'No'" in {
      val regDetails = CommonBuilder.buildRegistrationDetails copy (
                            deceasedDetails = Some(CommonBuilder.buildDeceasedDetails), ihtReference = Some("AbC123"))
      val  deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allGifts = Some(CommonBuilder.buildAllGifts
        .copy(isGivenAway = Some(false), isReservation = Some(false), isGivenInLast7Years = Some(false))))
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = giftsOverviewController.onPageLoad(createFakeRequest())
      status(result) must be (OK)
      ContentChecker.stripLineBreaks(contentAsString(result)) must include (messagesApi("page.iht.application.gifts.trust.question",
        deceasedName))
    }

    "not display second 'gifts given to trust' question when 7 year question answered 'Yes'" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allGifts = Some(CommonBuilder.buildAllGifts
        .copy(isGivenAway = Some(false), isReservation = Some(false), isGivenInLast7Years = Some(true))))
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = giftsOverviewController.onPageLoad(createFakeRequest())
      status(result) must be (OK)
      contentAsString(result) mustNot include (messagesApi("page.iht.application.gifts.trust.question"))
    }


    "respond with REDIRECT when the estate exceeds the TNRB threashold" in {
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      createMocksForRegistrationAndApplication(
        CommonBuilder.buildRegistrationDetails1,
        CommonBuilder.buildApplicationDetailsOverLowerThresholdAndGuidanceSeenFlagNotSet(ref) copy (
          allGifts = Some(allGiftsQ1Yes)
        )
      )

      val result = giftsOverviewController.onPageLoad(createFakeRequest())
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be(Some(
        iht.controllers.application.exemptions.routes.ExemptionsGuidanceIncreasingThresholdController.onPageLoad(ref).url))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      giftsOverviewController.onPageLoad(createFakeRequest()))
  }
}
