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
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder}
import iht.testhelpers.MockObjectBuilder._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._
import iht.testhelpers.TestHelper._
import iht.utils.CommonHelper._
import uk.gov.hmrc.play.partials.FormPartialRetriever

/**
 * Created by Vineet Tyagi on 29/07/16.
 */

class PartnerPermanentHomeQuestionControllerTest extends ApplicationControllerTest{

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def partnerPermanentHomeQuestionController = new PartnerPermanentHomeQuestionController {
    override val authConnector = createFakeAuthConnector(isAuthorised=true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def partnerPermanentHomeQuestionControllerNotAuthorised = new PartnerPermanentHomeQuestionController {
    override val authConnector = createFakeAuthConnector(isAuthorised=false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  "PartnerPermanentHomeQuestionController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = partnerPermanentHomeQuestionControllerNotAuthorised.onPageLoad(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = partnerPermanentHomeQuestionControllerNotAuthorised.onSubmit(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load, page contains Return link and Save button" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = partnerPermanentHomeQuestionController.onPageLoad (createFakeRequest())
      status(result) shouldBe (OK)
      contentAsString(result) should include (messagesApi("iht.saveAndContinue"))

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

      val partnerPermanentHomeQuestion = CommonBuilder.buildPartnerExemption.copy(isAssetForDeceasedPartner = Some(true))

      val filledPartnerPermanentHomeQuestionForm = partnerPermanentHomeQuestionForm.fill(partnerPermanentHomeQuestion)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPartnerPermanentHomeQuestionForm.data
        .toSeq: _*)

      val result = partnerPermanentHomeQuestionController.onSubmit(request)
      status(result) shouldBe (SEE_OTHER)
      redirectLocation(result) should be(Some(addFragmentIdentifierToUrl(routes.PartnerOverviewController.onPageLoad().url, ExemptionsPartnerHomeID)))
    }

    "display validation message when incomplete form is submitted" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(partner = Some(PartnerExemption(
          None, Some(true), None, None, None, None, Some(1000))))))

      val filledPartnerPermanentHomeQuestionForm = partnerPermanentHomeQuestionForm.fill(CommonBuilder.buildPartnerExemption.
        copy(isPartnerHomeInUK = None))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPartnerPermanentHomeQuestionForm.data.toSeq: _*)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = partnerPermanentHomeQuestionController.onSubmit()(request)
      status(result) should be (BAD_REQUEST)
      contentAsString(result) should include (messagesApi("error.problem"))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      partnerPermanentHomeQuestionController.onPageLoad(createFakeRequest()))
  }
}
