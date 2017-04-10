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

package iht.controllers.application

import iht.connector.{CachingConnector, IhtConnector}
import iht.constants.Constants
import iht.models.application.ApplicationDetails
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.models.application.exemptions.BasicExemptionElement
import iht.models.RegistrationDetails
import iht.testhelpers.MockObjectBuilder._
import iht.testhelpers.{CommonBuilder, MockObjectBuilder, TestHelper}
import iht.views.HtmlSpec
import org.mockito.Matchers._
import play.api.i18n.MessagesApi
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}
import uk.gov.hmrc.play.http.HeaderCarrier

class EstateOverviewControllerTest extends ApplicationControllerTest with HtmlSpec {

  override implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val headerCarrier = FakeHeaders()
  implicit val request = FakeRequest()
  implicit val hc = new HeaderCarrier
  var mockCachingConnector = mock[CachingConnector]
  var mockIhtConnector = mock[IhtConnector]

  val ref = "A1912233"
  val finalDestinationURL = "url"

  val registrationDetails = CommonBuilder.buildRegistrationDetails copy(
    deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath),
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    ihtReference = Some(ref))

  val regDetailsDeceasedSingle = CommonBuilder.buildRegistrationDetails.copy(
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(maritalStatus = Some(TestHelper.MaritalStatusSingle))),
    ihtReference = Some(ref))


  def controller = new EstateOverviewController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val isWhiteListEnabled = false
  }

  def controllerNotAuthorised = new EstateOverviewController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val isWhiteListEnabled = false
  }

  def createMocksForRegistrationAndApplication(rd: RegistrationDetails, ad: ApplicationDetails) = {
    createMockToGetCaseDetails(mockIhtConnector, rd)
    createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, rd)
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

  def createMocksForExemptionsGuidance(finalDestinationURL: String) = {
    MockObjectBuilder.createMockToGetSingleValueFromCache(mockCachingConnector,
      same(Constants.ExemptionsGuidanceContinueUrlKey), None)

    MockObjectBuilder.createMockToStoreSingleValueInCache(mockCachingConnector,
      same(Constants.ExemptionsGuidanceContinueUrlKey),
      Some(finalDestinationURL))
    MockObjectBuilder.createMockToDeleteKeyFromCache(mockCachingConnector, Constants.ExemptionsGuidanceContinueUrlKey)
  }

  before {
    mockCachingConnector = mock[CachingConnector]
    mockIhtConnector = mock[IhtConnector]
  }

  "EstateOverviewController" must {
    "redirect to GG login page on PageLoadWithIhtRef if the user is not logged in" in {
      val result = controllerNotAuthorised.onPageLoadWithIhtRef(ref)(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(loginUrl))
    }

    "respond with OK on page load" in {
      createMocksForRegistrationAndApplication(
        CommonBuilder.buildRegistrationDetails1,
        CommonBuilder.buildApplicationDetails copy (ihtRef = Some(ref)))
      MockObjectBuilder.createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      val result = controller.onPageLoadWithIhtRef(ref)(createFakeRequest())
      status(result) shouldBe OK
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


    "respond with REDIRECT when the estate exceeds the TNRB threashold" in {
      MockObjectBuilder.createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)

      createMocksForRegistrationAndApplication(
        CommonBuilder.buildRegistrationDetails1,
        CommonBuilder.buildApplicationDetailsOverLowerThresholdAndGuidanceSeenFlagNotSet(ref))

      val result = controller.onPageLoadWithIhtRef(ref)(createFakeRequest())
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should be (
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

        MockObjectBuilder.createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)
        createMocksForRegistrationAndApplication(registrationDetails, completeAppDetails)

        val result = controller.onContinueOrDeclarationRedirect(ref)(createFakeRequest())
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) should be(
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

        MockObjectBuilder.createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)
        createMocksForRegistrationAndApplication(registrationDetails, completeAppDetails)

        val result = controller.onContinueOrDeclarationRedirect(ref)(createFakeRequest())
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) should be(
          Some(iht.controllers.application.routes.KickoutController.onPageLoad().url))

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

        MockObjectBuilder.createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)
        createMocksForRegistrationAndApplication(registrationDetails, completeAppDetails)

        val result = controller.onContinueOrDeclarationRedirect(ref)(createFakeRequest())
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) should be(
          Some(iht.controllers.application.routes.KickoutController.onPageLoad().url))

      }
    }

    "have Redirect Url as Kickout page" when {
      "Assets, Gifts, Debts are completed and Estate value is more than £1 M with no exemptions and tnrb " in {
        val completeAppDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts.copy(
          allAssets = Some(CommonBuilder.buildAllAssetsWithAllSectionsFilled.copy(
            money = Some(ShareableBasicEstateElement(Some(800000),Some(450000), Some(true), Some(true)))
          )))

        MockObjectBuilder.createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)
        createMocksForRegistrationAndApplication(registrationDetails, completeAppDetails)

        val result = controller.onContinueOrDeclarationRedirect(ref)(createFakeRequest())
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) should be(
          Some(iht.controllers.application.routes.KickoutController.onPageLoad().url))
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

        MockObjectBuilder.createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)
        createMocksForRegistrationAndApplication(regDetailsDeceasedSingle, completeAppDetails)

        val result = controller.onContinueOrDeclarationRedirect(ref)(createFakeRequest())
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) should be(
          Some(iht.controllers.application.routes.KickoutController.onPageLoad().url))

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

        MockObjectBuilder.createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)
        createMocksForRegistrationAndApplication(registrationDetails, completeAppDetails)

        val result = controller.onContinueOrDeclarationRedirect(ref)(createFakeRequest())
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) should be(
          Some(iht.controllers.application.routes.KickoutController.onPageLoad().url))

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

        MockObjectBuilder.createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)
        createMocksForRegistrationAndApplication(registrationDetails, completeAppDetails)

        val result = controller.onContinueOrDeclarationRedirect(ref)(createFakeRequest())
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) should be(
          Some(iht.controllers.application.tnrb.routes.TnrbGuidanceController.onPageLoad().url))

      }
    }

    "have Redirect Url as Declaration Page url" when {
      "Assets, Gifts, Debts are completed and Estate value is below the threshold with no kick out" in {
        val completeAppDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts


        MockObjectBuilder.createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)
        createMocksForRegistrationAndApplication(registrationDetails, completeAppDetails)

        val result = controller.onContinueOrDeclarationRedirect(ref)(createFakeRequest())
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) should be(
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


        MockObjectBuilder.createMocksForExemptionsGuidanceSingleValue(mockCachingConnector, finalDestinationURL)
        createMocksForRegistrationAndApplication(registrationDetails, completeAppDetails)

        val result = controller.onContinueOrDeclarationRedirect(ref)(createFakeRequest())
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) should be(
          Some(iht.controllers.application.declaration.routes.CheckedEverythingQuestionController.onPageLoad().url))

      }
    }

  }
}
