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
 * Created by jennygj on 01/08/16.
 */
class ExemptionPartnerNameControllerTest extends ApplicationControllerTest {

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def partnerNameController = new ExemptionPartnerNameController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised=true)
    override val ihtConnector = mockIhtConnector
    override val isWhiteListEnabled = false
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def partnerNameControllerNotAuthorised = new ExemptionPartnerNameController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised=false)
    override val ihtConnector = mockIhtConnector
    override val isWhiteListEnabled = false
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  val registrationDetailsWithNoIhtRef = CommonBuilder.buildRegistrationDetails copy (
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    ihtReference = None)

  "ExemptionPartnerNameController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = partnerNameControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = partnerNameControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "return OK on page load" in {

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(CommonBuilder.buildApplicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = partnerNameController.onPageLoad(createFakeRequest(isAuthorised = true))
      status(result) should be(OK)
      contentAsString(result) should include(messagesApi("page.iht.application.exemptions.partner.name.title"))
    }

    "respond with error when ApplicationDetails could not be retrieved on page load" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = None,
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = partnerNameController.onPageLoad(createFakeRequest(isAuthorised = true))
      status(result) should be(INTERNAL_SERVER_ERROR)
    }

    "save and redirect to spouse or civil partner exemptions overview page on successful page submit" in {
      val partnerExemptionValues = CommonBuilder.buildPartnerExemption

      val partnerForm = partnerExemptionNameForm.fill(partnerExemptionValues)
      implicit val request = createFakeRequest(isAuthorised = true).withFormUrlEncodedBody(partnerForm.data.toSeq: _*)

      val applicationDetails = Some(CommonBuilder.buildApplicationDetails.copy(allExemptions = Some
      (CommonBuilder.buildAllExemptions.copy(partner = Some(CommonBuilder.buildPartnerExemption)))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = applicationDetails,
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = partnerNameController.onSubmit(request)
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(addFragmentIdentifierToUrl(routes.PartnerOverviewController.onPageLoad().url, ExemptionsPartnerNameID)))
    }

    "show relevant error message when page fails in validation while submission" in {
      val partnerExemptionValues = CommonBuilder.buildPartnerExemption.copy(firstName = Some(""), lastName = Some(""))

      val partnerForm = partnerExemptionNameForm.fill(partnerExemptionValues)
      implicit val request = createFakeRequest(isAuthorised = true).withFormUrlEncodedBody(partnerForm.data.toSeq: _*)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)

      val result = partnerNameController.onSubmit(request)
      status(result) should be(BAD_REQUEST)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      partnerNameController.onPageLoad(createFakeRequest()))
  }
}
