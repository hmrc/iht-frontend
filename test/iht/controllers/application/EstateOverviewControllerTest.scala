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

package iht.controllers.application

import iht.config.AppConfig
import iht.constants.Constants
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.models.application.exemptions.BasicExemptionElement
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever, TestHelper}
import iht.views.HtmlSpec
import org.mockito.ArgumentMatchers._
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers.{status => playStatus, _}
import play.api.test.{FakeHeaders, FakeRequest}
import uk.gov.hmrc.http.{HeaderCarrier, Upstream5xxResponse}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EstateOverviewControllerTest extends ApplicationControllerTest with HtmlSpec {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with EstateOverviewController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  implicit val headerCarrier = FakeHeaders()
  implicit val request = FakeRequest()
  implicit val hc = new HeaderCarrier

  val ref = "A1912233"
  val finalDestinationURL = "url"

  val registrationDetails = CommonBuilder.buildRegistrationDetails copy(
    deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath),
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    ihtReference = Some(ref))

  val regDetailsDeceasedSingle = CommonBuilder.buildRegistrationDetails.copy(
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(maritalStatus = Some(TestHelper.MaritalStatusSingle))),
    ihtReference = Some(ref))


  def controller = new TestController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
//    override val authConnector = mockAuthConnector
    override val authConnector = mockAuthConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def controllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = mockAuthConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def createMocksForRegistrationAndApplication(rd: Future[Option[RegistrationDetails]], ad: ApplicationDetails) = {
    createMockToGetCaseDetails(mockIhtConnector, rd.map(_.get))
    createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, rd)
    createMockToStoreRegDetailsInCache(mockCachingConnector, rd)
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

  def createMocksForExemptionsGuidance(finalDestinationURL: String) = {
    createMockToGetSingleValueFromCache(mockCachingConnector,
      same(Constants.ExemptionsGuidanceContinueUrlKey), None)

    createMockToStoreSingleValueInCache(mockCachingConnector,
      same(Constants.ExemptionsGuidanceContinueUrlKey),
      Some(finalDestinationURL))
    createMockToDeleteKeyFromCache(mockCachingConnector, Constants.ExemptionsGuidanceContinueUrlKey)
  }

  "EstateOverviewController" must {
    "redirect to GG login page on PageLoadWithIhtRef if the user is not logged in" in {
      val result = controllerNotAuthorised.onPageLoadWithIhtRef(ref)(createFakeRequest(isAuthorised = false))
      playStatus(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(loginUrl))
    }

    "respond with OK on page load" in {
      createMocksForRegistrationAndApplication(
        Future.successful(Some(CommonBuilder.buildRegistrationDetails1)),
        CommonBuilder.buildApplicationDetails copy (ihtRef = Some(ref)))
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      val result = controller.onPageLoadWithIhtRef(ref)(createFakeRequest())
      playStatus(result) mustBe OK
      val content = contentAsString(result)
      val doc = asDocument(content)

      assertEqualsValue(doc, "h1",
        messagesApi("page.iht.application.overview.title2", registrationDetails.deceasedDetails.get.name))
      assertEqualsValue(doc, "title",
        messagesApi("page.iht.application.overview.browserTitle") + " " + messagesApi("site.title.govuk"))

      assertNotRenderedById(doc, "continue-to-declaration")

      assertEqualsValue(doc, "p#all-sections-not-complete-declaration-guidance-text1 strong",
        messagesApi("page.iht.application.estateOverview.declaration.allSectionsNotComplete.guidance.text1"))

      assertEqualsValue(doc, "p#all-sections-not-complete-declaration-guidance-text2",
        messagesApi("page.iht.application.estateOverview.declaration.allSectionsNotComplete.guidance.text2"))
    }

    "redirect to List of Cases page if the case status is other than Awaiting Return" in {
      createMocksForRegistrationAndApplication(
        Future.successful(Some(CommonBuilder.buildRegistrationDetails1.copy(status = "In Review"))),
        CommonBuilder.buildApplicationDetails copy (ihtRef = Some(ref)))
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      val result = controller.onPageLoadWithIhtRef(ref)(createFakeRequest())
      playStatus(result) mustBe SEE_OTHER
      redirectLocation(result) must be(
        Some(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url))
    }

    "respond with INTERNAL_SERVER_ERROR when exception contains 'JSON validation against schema failed'" in {
      createMocksForRegistrationAndApplication(
        Future.failed(Upstream5xxResponse("JSON validation against schema failed", 500, 502)),
        CommonBuilder.buildApplicationDetails copy (ihtRef = Some(ref)))
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      val result = controller.onPageLoadWithIhtRef(ref)(createFakeRequest())
      playStatus(result) mustBe INTERNAL_SERVER_ERROR
    }


    "respond with REDIRECT when the estate exceeds the TNRB threashold" in {
      createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      createMocksForRegistrationAndApplication(
        Future.successful(Some(CommonBuilder.buildRegistrationDetails1)),
        CommonBuilder.buildApplicationDetailsOverLowerThresholdAndGuidanceSeenFlagNotSet(ref))

      val result = controller.onPageLoadWithIhtRef(ref)(createFakeRequest())
      playStatus(result) mustBe SEE_OTHER
      redirectLocation(result) must be (
        Some(
          iht.controllers.application.exemptions.routes.ExemptionsGuidanceIncreasingThresholdController.onPageLoad(ref).url))
    }

  }

  "EstateOverviewController onContinueOrDeclarationRedirect method" must {
    "have Redirect Url as Exemptions Guidance page" when {
      "Assets, Gifts, Debts are completed and Estate value is above the threshold " +
        "with no exemptions and tnrb and no kickout" in {
        val completeAppDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts.copy(
          allAssets = Some(CommonBuilder.buildAllAssetsWithAllSectionsFilled.copy(
            money = Some(ShareableBasicEstateElement(Some(200000), Some(150000), Some(true), Some(true)))
          )),
          ihtRef = Some(ref)
        )

        createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)
        createMocksForRegistrationAndApplication(Future.successful(Some(registrationDetails)), completeAppDetails)

        val result = controller.onContinueOrDeclarationRedirect(ref)(createFakeRequest())
        playStatus(result) mustBe SEE_OTHER
        redirectLocation(result) must be(
          Some(
            iht.controllers.application.exemptions.routes.ExemptionsGuidanceIncreasingThresholdController.onPageLoad(ref).url)
        )

      }
    }

    "have Redirect Url as Kickout page" when {
      "Assets, Gifts, Debts are completed and Estate value is above the threshold with tnrb and no kickout" in {
        val completeAppDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts.copy(
          allAssets = Some(CommonBuilder.buildAllAssetsWithAllSectionsFilled.copy(
            money = Some(ShareableBasicEstateElement(Some(400000),Some(450000), Some(true), Some(true)))
          )),
          increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility),
          widowCheck = Some(CommonBuilder.buildWidowedCheck)
        )

        createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)
        createMocksForRegistrationAndApplication(Future.successful(Some(registrationDetails)), completeAppDetails)

        val result = controller.onContinueOrDeclarationRedirect(ref)(createFakeRequest())
        playStatus(result) mustBe SEE_OTHER
        redirectLocation(result) must be(
          Some(iht.controllers.application.routes.KickoutAppController.onPageLoad().url))

      }
    }

    "have Redirect Url as Kickout page" when {
      "Assets, Gifts, Debts are completed and Estate value is above the threshold with exemptions, tnrb and no kickout" in {
        val completeAppDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts.copy(
          allAssets = Some(CommonBuilder.buildAllAssetsWithAllSectionsFilled.copy(
            money = Some(ShareableBasicEstateElement(Some(400000),Some(450000), Some(true), Some(true)))
          )),
          allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
            partner = Some(CommonBuilder.buildPartnerExemption),
            charity = Some(BasicExemptionElement(Some(true))),
            qualifyingBody = Some(BasicExemptionElement(Some(false))))),
          charities = Seq(CommonBuilder.buildCharity.copy(
            Some("1"),Some("testCharity"),Some("123456"), Some(BigDecimal(8000)))),
          increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility),
          widowCheck = Some(CommonBuilder.buildWidowedCheck)
        )

        createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)
        createMocksForRegistrationAndApplication(Future.successful(Some(registrationDetails)), completeAppDetails)

        val result = controller.onContinueOrDeclarationRedirect(ref)(createFakeRequest())
        playStatus(result) mustBe SEE_OTHER
        redirectLocation(result) must be(
          Some(iht.controllers.application.routes.KickoutAppController.onPageLoad().url))

      }
    }

    "have Redirect Url as Kickout page" when {
      "Assets, Gifts, Debts are completed and Estate value is more than £1 M with no exemptions and tnrb " in {
        val completeAppDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts.copy(
          allAssets = Some(CommonBuilder.buildAllAssetsWithAllSectionsFilled.copy(
            money = Some(ShareableBasicEstateElement(Some(800000),Some(450000), Some(true), Some(true)))
          )))

        createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)
        createMocksForRegistrationAndApplication(Future.successful(Some(registrationDetails)), completeAppDetails)

        val result = controller.onContinueOrDeclarationRedirect(ref)(createFakeRequest())
        playStatus(result) mustBe SEE_OTHER
        redirectLocation(result) must be(
          Some(iht.controllers.application.routes.KickoutAppController.onPageLoad().url))
      }
    }

    "have Redirect Url as Kickout page" when {
      "Assets, Gifts, Debts are completed and Estate value is more than £325 K after exemptions " +
        "and Deceased is single " in {
        val completeAppDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts.copy(
          allAssets = Some(CommonBuilder.buildAllAssetsWithAllSectionsFilled.copy(
            money = Some(ShareableBasicEstateElement(Some(40000),Some(450000), Some(true), Some(true)))
          )),
          allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
            partner = Some(CommonBuilder.buildPartnerExemption.copy(isPartnerHomeInUK = Some(false))),
            charity = Some(BasicExemptionElement(Some(true))),
            qualifyingBody = Some(BasicExemptionElement(Some(false))))),
          charities = Seq(CommonBuilder.buildCharity.copy(
            Some("1"),Some("testCharity"),Some("123456"), Some(BigDecimal(80000)))))

        createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)
        createMocksForRegistrationAndApplication(Future.successful(Some(regDetailsDeceasedSingle)), completeAppDetails)

        val result = controller.onContinueOrDeclarationRedirect(ref)(createFakeRequest())
        playStatus(result) mustBe SEE_OTHER
        redirectLocation(result) must be(
          Some(iht.controllers.application.routes.KickoutAppController.onPageLoad().url))

      }
    }

    "have Redirect Url as KickOut Page url" when {
      "Assets, Gifts, Debts are completed, Estate value is between 325K and 650K with no kickout " +
        "and user has selected No to widowed check question" in {
        val completeAppDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts.copy(
          allAssets = Some(CommonBuilder.buildAllAssets.copy(
            money = Some(CommonBuilder.buildShareableBasicElementExtended.copy(value = Some(BigDecimal(200000)),
              shareValue = Some(BigDecimal(200000)),
              isOwned = Some(true),
              isOwnedShare = Some(true))))),
          widowCheck = Some(CommonBuilder.buildWidowedCheck.copy(Some(false), None))
        )

        createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)
        createMocksForRegistrationAndApplication(Future.successful(Some(registrationDetails)), completeAppDetails)

        val result = controller.onContinueOrDeclarationRedirect(ref)(createFakeRequest())
        playStatus(result) mustBe SEE_OTHER
        redirectLocation(result) must be(
          Some(iht.controllers.application.routes.KickoutAppController.onPageLoad().url))

      }
    }

    "have Redirect Url as Tnrb guidance page" when {
      "Assets, Gifts, Debts are completed and Estate value is more than £325 K but less than £650K " +
        "after exemptions, no Tnrb and Deceased is Married " in {
        val completeAppDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts.copy(
          allAssets = Some(CommonBuilder.buildAllAssetsWithAllSectionsFilled.copy(
            money = Some(ShareableBasicEstateElement(Some(400000),Some(200000), Some(true), Some(true)))
          )),
          allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
            partner = Some(CommonBuilder.buildPartnerExemption.copy(isPartnerHomeInUK = Some(true))),
            charity = Some(BasicExemptionElement(Some(true))),
            qualifyingBody = Some(BasicExemptionElement(Some(false))))),
          charities = Seq(CommonBuilder.buildCharity.copy(
            Some("1"),Some("testCharity"),Some("123456"), Some(BigDecimal(40000)))))

        createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)
        createMocksForRegistrationAndApplication(Future.successful(Some(registrationDetails)), completeAppDetails)

        val result = controller.onContinueOrDeclarationRedirect(ref)(createFakeRequest())
        playStatus(result) mustBe SEE_OTHER
        redirectLocation(result) must be(
          Some(iht.controllers.application.tnrb.routes.TnrbGuidanceController.onSystemPageLoad().url))

      }
    }

    "have Redirect Url as Declaration Page url" when {
      "Assets, Gifts, Debts are completed and Estate value is below the threshold with no kick out" in {
        val completeAppDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts


        createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)
        createMocksForRegistrationAndApplication(Future.successful(Some(registrationDetails)), completeAppDetails)

        val result = controller.onContinueOrDeclarationRedirect(ref)(createFakeRequest())
        playStatus(result) mustBe SEE_OTHER
        redirectLocation(result) must be(
          Some(iht.controllers.application.declaration.routes.CheckedEverythingQuestionController.onPageLoad().url))

      }
    }

    "have Redirect Url as Declaration Page url" when {
      "all the required application sections are complete and total estate value is " +
        "below threshold after exemptions and tnrb" in {

        val propertyList = List(CommonBuilder.buildProperty.copy(Some("1"), Some(CommonBuilder.DefaultUkAddress),
          TestHelper.PropertyTypeDeceasedHome, TestHelper.TypesOfOwnershipDeceasedOnly,
          TestHelper.TenureFreehold, Some(230000)),
          CommonBuilder.buildProperty.copy(Some("2"), Some(CommonBuilder.DefaultUkAddress),
            TestHelper.PropertyTypeDeceasedHome, TestHelper.TypesOfOwnershipDeceasedOnly,
            TestHelper.TenureFreehold, Some(230000)))

        val completeAppDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts.copy(
          propertyList = propertyList,
          allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
            partner = Some(CommonBuilder.buildPartnerExemption),
            charity = Some(BasicExemptionElement(Some(true))),
            qualifyingBody = Some(BasicExemptionElement(Some(false))))),
          charities = Seq(CommonBuilder.buildCharity.copy(
            Some("1"),Some("testCharity"),Some("123456"), Some(BigDecimal(80000)))),
          increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility),
          widowCheck = Some(CommonBuilder.buildWidowedCheck))


        createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)
        createMocksForRegistrationAndApplication(Future.successful(Some(registrationDetails)), completeAppDetails)

        val result = controller.onContinueOrDeclarationRedirect(ref)(createFakeRequest())
        playStatus(result) mustBe SEE_OTHER
        redirectLocation(result) must be(
          Some(iht.controllers.application.declaration.routes.CheckedEverythingQuestionController.onPageLoad().url))

      }
    }

  }
}
