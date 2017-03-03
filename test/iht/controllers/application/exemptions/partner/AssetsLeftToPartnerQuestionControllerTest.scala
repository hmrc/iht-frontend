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

package iht.controllers.application.exemptions.partner

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.exemptions.PartnerExemption
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import iht.utils.CommonHelper
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._

/**
 * Created by Vineet Tyagi on 29/07/16.
 */

class AssetsLeftToPartnerQuestionControllerTest extends ApplicationControllerTest{

  val mockCachingConnector = mock[CachingConnector]
  var mockIhtConnector = mock[IhtConnector]

  def assetsLeftToPartnerQuestionController = new AssetsLeftToPartnerQuestionController {
    override val authConnector = createFakeAuthConnector(isAuthorised=true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def assetsLeftToPartnerQuestionControllerNotAuthorised = new AssetsLeftToPartnerQuestionController {
    override val authConnector = createFakeAuthConnector(isAuthorised=false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  before {
    mockIhtConnector = mock[IhtConnector]
  }


  "AssetsLeftToPartnerQuestionController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = assetsLeftToPartnerQuestionControllerNotAuthorised.onPageLoad(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = assetsLeftToPartnerQuestionControllerNotAuthorised.onSubmit(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      val regDetails = CommonBuilder.buildRegistrationDetails1
      val deceasedName = regDetails.deceasedDetails.map(_.name).fold("")(identity)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = regDetails,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)



      val result = assetsLeftToPartnerQuestionController.onPageLoad (createFakeRequest())
      status(result) shouldBe (OK)
      contentAsString(result) should include (messagesApi("iht.estateReport.exemptions.spouse.assetLeftToSpouse.question", deceasedName))
    }

    "save application and go to Exemptions Overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(partner = Some(PartnerExemption(
          Some(true), Some(true), None, None, None, None, Some(1000))))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val assetLeftToSpouse = CommonBuilder.buildPartnerExemption.copy(isAssetForDeceasedPartner = Some(true))

      val filledAssetLeftToSpouseQuestionForm = assetsLeftToSpouseQuestionForm.fill(assetLeftToSpouse)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledAssetLeftToSpouseQuestionForm.data
        .toSeq: _*)

      val result = assetsLeftToPartnerQuestionController.onSubmit(request)
      status(result) shouldBe (SEE_OTHER)
    }

    "wipe out all the partner exemption data if user selects the assets left to partner question as No, " +
      "save application, and go to Exemptions Overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(partner = Some(PartnerExemption(
          Some(false), Some(true), None, None, None, None, Some(1000))))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val assetLeftToSpouse = CommonBuilder.buildPartnerExemption.copy(isAssetForDeceasedPartner = Some(false))

      val filledAssetLeftToSpouseQuestionForm = assetsLeftToSpouseQuestionForm.fill(assetLeftToSpouse)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledAssetLeftToSpouseQuestionForm.data
        .toSeq: _*)

      val result = assetsLeftToPartnerQuestionController.onSubmit(request)
      status(result) shouldBe (SEE_OTHER)

      val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)

      val expectedAppDetails = applicationDetails.copy(
        allExemptions = applicationDetails.allExemptions.map(_.copy(partner =
                              Some(CommonBuilder.buildPartnerExemption.copy(
                                Some(false), None, None, None, None, None,None)))))

      capturedValue shouldBe expectedAppDetails
    }

    "save application and go to PartnerPermanentHome when user select yes and submit  " in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(partner = Some(PartnerExemption(
          Some(true), None, None, None, None, None, Some(1000))))))

      val filledAssetLeftToSpouseQuestionForm = assetsLeftToSpouseQuestionForm.fill(CommonBuilder.buildPartnerExemption.
        copy(isAssetForDeceasedPartner = Some(true)))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledAssetLeftToSpouseQuestionForm.data.toSeq: _*)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = assetsLeftToPartnerQuestionController.onSubmit()(request)
      status(result) shouldBe (SEE_OTHER)
      redirectLocation(result) should be (Some(routes.PartnerPermanentHomeQuestionController.onPageLoad.url))

    }

    "display validation message when incomplete form is submitted" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(partner = Some(PartnerExemption(
          None, Some(true), None, None, None, None, Some(1000))))))

      val filledAssetLeftToSpouseQuestionForm = assetsLeftToSpouseQuestionForm.fill(CommonBuilder.buildPartnerExemption.
        copy(isAssetForDeceasedPartner = None))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledAssetLeftToSpouseQuestionForm.data.toSeq: _*)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = assetsLeftToPartnerQuestionController.onSubmit()(request)
      status(result) should be (BAD_REQUEST)
      contentAsString(result) should include (messagesApi("error.problem"))
    }
  }
}
