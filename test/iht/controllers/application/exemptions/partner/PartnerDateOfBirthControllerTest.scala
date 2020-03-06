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

package iht.controllers.application.exemptions.partner

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.testhelpers.TestHelper._
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import iht.utils.CommonHelper._
import org.joda.time.LocalDate
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

/**
 * Created by james on 01/08/16.
 */
class PartnerDateOfBirthControllerTest extends ApplicationControllerTest {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with PartnerDateOfBirthController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def partnerDateOfBirthController = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def partnerDateOfBirthControllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  val registrationDetailsWithNoIhtRef = CommonBuilder.buildRegistrationDetails copy (
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    ihtReference = None)

  "PartnerDateOfBirthController" must {

    "redirect to login page on Page load if the user is not logged in" in {
      val result = partnerDateOfBirthControllerNotAuthorised.onPageLoad()(createFakeRequest(isAuthorised = false))
      status(result) must be (SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to login page on submit if the user is not logged in" in {
      val result = partnerDateOfBirthControllerNotAuthorised.onSubmit()(createFakeRequest(isAuthorised = false))
      status(result) must be (SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "Return OK on Page Load" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(CommonBuilder.buildApplicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = partnerDateOfBirthController.onPageLoad()(createFakeRequest())
      status(result) must be (OK)
    }

    "display the correct stored date on page load" in {

      val applicationDetails = CommonBuilder.buildApplicationDetails
        .copy(allExemptions = Some(CommonBuilder.buildAllExemptions
        .copy(Some(CommonBuilder.buildPartnerExemption))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = partnerDateOfBirthController.onPageLoad()(createFakeRequest())
      status(result) must be (OK)
      contentAsString(result) must include ("12")
      contentAsString(result) must include ("1998")
    }

    "respond with error when ApplicationDetails could not be retrieved on page load" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = None,
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = partnerDateOfBirthController.onPageLoad()(createFakeRequest())
      status(result) must be (INTERNAL_SERVER_ERROR)
    }

    "save and redirect to Exemptions overview page on successful page submit" in {
      val partnerExemptionValues = CommonBuilder.buildPartnerExemption.copy(dateOfBirth = Some(new LocalDate(1990,8,20)))

      val dateOfBirthForm = spouseDateOfBirthForm.fill(partnerExemptionValues)
      implicit val request = createFakeRequest(isAuthorised = true).withFormUrlEncodedBody(dateOfBirthForm.data.toSeq: _*)

      val applicationDetails = Some(CommonBuilder.buildApplicationDetails.copy(allExemptions = Some
        (CommonBuilder.buildAllExemptions.copy(partner = Some(CommonBuilder.buildPartnerExemption)))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = applicationDetails,
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = partnerDateOfBirthController.onSubmit(request)
      status(result) must be (SEE_OTHER)
      redirectLocation(result) must be (Some(addFragmentIdentifierToUrl(routes.PartnerOverviewController.onPageLoad().url, ExemptionsPartnerDobID)))
    }

    "redirect to Partner Date of Birth Exemption page and show relevant error message when page fails in validation while submission" in {
      val partnerExemptionValues = CommonBuilder.buildPartnerExemption.copy(dateOfBirth = Some(new LocalDate(2050,10,10)))

      val dateOfBirthForm = spouseDateOfBirthForm.fill(partnerExemptionValues)
      implicit val request = createFakeRequest(isAuthorised = true).withFormUrlEncodedBody(dateOfBirthForm.data.toSeq: _*)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)

      val result = partnerDateOfBirthController.onSubmit(request)
      status(result) must be(OK)
    }

    "on page load throws exception when no iht ref" in {
      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetailsWithNoIhtRef)))

      a [RuntimeException] mustBe thrownBy {
        await(partnerDateOfBirthController.onPageLoad(createFakeRequest()))
      }
    }

    "save and redirect to partner exemption overview page on successful page submit where no exemptions" in {
      val ad = CommonBuilder.buildApplicationDetails.copy(allExemptions = None)
      val partnerExemptionValues = CommonBuilder.buildPartnerExemption

      val dateOfBirth = spouseDateOfBirthForm.fill(partnerExemptionValues)
      implicit val request = createFakeRequest(isAuthorised = true).withFormUrlEncodedBody(dateOfBirth.data.toSeq: _*)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(ad),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = partnerDateOfBirthController.onSubmit(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result).get must be(addFragmentIdentifierToUrl(routes.PartnerOverviewController.onPageLoad().url, ExemptionsPartnerDobID))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      partnerDateOfBirthController.onPageLoad(createFakeRequest()))
  }
}
