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

package iht.controllers.application.exemptions.charity

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.exemptions.BasicExemptionElement
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder}
import iht.testhelpers.MockObjectBuilder._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import iht.utils.CommonHelper._
import iht.testhelpers.TestHelper._
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

class AssetsLeftToCharityQuestionControllerTest extends ApplicationControllerTest {

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def assetsLeftToCharityQuestionController = new AssetsLeftToCharityQuestionController {
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def assetsLeftToCharityQuestionControllerNotAuthorised = new AssetsLeftToCharityQuestionController {
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  "AssetsLeftToCharityQuestionControllerTest" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = assetsLeftToCharityQuestionControllerNotAuthorised.onPageLoad(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = assetsLeftToCharityQuestionControllerNotAuthorised.onSubmit(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = assetsLeftToCharityQuestionController.onPageLoad(createFakeRequest())
      status(result) shouldBe (OK)
      contentAsString(result) should include(messagesApi("page.iht.application.exemptions.assetLeftToCharity.browserTitle"))
      contentAsString(result) should include(messagesApi("iht.saveAndContinue"))
    }

    "save application and go to Exemptions Overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(charity = Some(BasicExemptionElement(isSelected = Some(true))))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val assetLeftToSpouse = BasicExemptionElement(isSelected = Some(true))

      val filledAssetLeftToSpouseQuestionForm = assetsLeftToCharityQuestionForm.fill(assetLeftToSpouse)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledAssetLeftToSpouseQuestionForm.data
        .toSeq: _*)

      val result = assetsLeftToCharityQuestionController.onSubmit(request)
      status(result) shouldBe SEE_OTHER
    }

    "save application and go to Add a charity when user select yes and submit  " in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(charity = Some(BasicExemptionElement(
          Some(true))))))

      val filledAssetLeftToSpouseQuestionForm = assetsLeftToCharityQuestionForm.fill(BasicExemptionElement(isSelected = Some(true)))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledAssetLeftToSpouseQuestionForm.data.toSeq: _*)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = assetsLeftToCharityQuestionController.onSubmit()(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should be (Some(addFragmentIdentifierToUrl(routes.CharityDetailsOverviewController.onPageLoad.url, ExemptionsCharitiesAssetsID)))
    }

    "throw exception when no application details are present" in {
      val filledAssetLeftToSpouseQuestionForm = assetsLeftToCharityQuestionForm.fill(BasicExemptionElement(isSelected = Some(true)))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledAssetLeftToSpouseQuestionForm.data.toSeq: _*)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = None,
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = assetsLeftToCharityQuestionController.onSubmit()(request)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "display validation message when incomplete form is submitted" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(charity = Some(BasicExemptionElement(isSelected = None)))))

      val filledAssetLeftToSpouseQuestionForm = assetsLeftToCharityQuestionForm.fill(BasicExemptionElement(isSelected = None))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledAssetLeftToSpouseQuestionForm.data.toSeq: _*)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = assetsLeftToCharityQuestionController.onSubmit()(request)
      status(result) should be(BAD_REQUEST)
      contentAsString(result) should include(messagesApi("error.problem"))
    }

    "updating application details with No chosen blanks the charities list and sets value to No" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(charities = Seq(CommonBuilder.charity))
      val charity = BasicExemptionElement(Some(false))
      val result = assetsLeftToCharityQuestionController.updateApplicationDetails(applicationDetails, None, charity)
      result._1.charities shouldBe Nil
      result._1.allExemptions.flatMap(_.charity.flatMap(_.isSelected)) shouldBe Some(false)
    }

    "updating application details with Yes chosen keeps the charities list and sets value to Yes" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(charities = Seq(CommonBuilder.charity))
      val charity = BasicExemptionElement(Some(true))
      val result = assetsLeftToCharityQuestionController.updateApplicationDetails(applicationDetails, None, charity)
      result._1.charities shouldBe Seq(CommonBuilder.charity)
      result._1.allExemptions.flatMap(_.charity.flatMap(_.isSelected)) shouldBe Some(true)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      assetsLeftToCharityQuestionController.onPageLoad(createFakeRequest()))
  }
}
