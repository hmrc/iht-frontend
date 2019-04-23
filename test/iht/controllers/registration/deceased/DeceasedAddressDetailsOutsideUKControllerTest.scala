/*
 * Copyright 2019 HM Revenue & Customs
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

package iht.controllers.registration.deceased

import iht.config.AppConfig
import iht.controllers.registration.applicant.{routes => applicantRoutes}
import iht.controllers.registration.{routes => registrationRoutes}
import iht.forms.registration.DeceasedForms._
import iht.models.{DeceasedDateOfDeath, DeceasedDetails, RegistrationDetails, UkAddress}
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import org.joda.time.LocalDate
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class DeceasedAddressDetailsOutsideUKControllerTest
  extends RegistrationDeceasedControllerWithEditModeBehaviour[DeceasedAddressDetailsOutsideUKController]{

  protected abstract class TestController extends FrontendController(mockControllerComponents) with DeceasedAddressDetailsOutsideUKController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def controller = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def controllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  "DeceasedAddressDetailsOutsideUKController" must {

    behave like securedRegistrationDeceasedController()
    
    "forget address details when changing from UK to abroad" in {
      val existingAddress = UkAddress("New Line 1", "New Line 2", None, None, "AA1 1AA", "GB")
      val existingApplicantDetails = CommonBuilder.buildApplicantDetails
      val existingDeceasedDetails = CommonBuilder.buildDeceasedDetails copy(ukAddress = Some(existingAddress),
        isAddressInUK = Some(true))
      val existingDod = DeceasedDateOfDeath(new LocalDate(1980, 1, 1))

      val existingRegistrationDetails = RegistrationDetails(Some(existingDod),
        Some(existingApplicantDetails), Some(existingDeceasedDetails))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(existingRegistrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(existingRegistrationDetails))

      val result = controller.onPageLoad()(createFakeRequestWithReferrer(referrerURL = referrerURL, host = host))
      status(result) mustBe OK
      contentAsString(result) must not include("New Line 1")
      contentAsString(result) must not include("New Line 2")
      contentAsString(result) must not include("AA1 1AA")
    }

    "respond appropriately to a submit with valid values in all fields" in {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(None, None, Some(deceasedDetails))
      val deceasedDetailsForm1 = deceasedAddressDetailsOutsideUKForm.fill(deceasedDetails)
      val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host, data = deceasedDetailsForm1.data.toSeq, authRetrieveNino = false)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onSubmit()(request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be(Some(applicantRoutes.ApplyingForProbateController.onPageLoad().url))
    }

    "respond appropriately to a edit submit with valid values in all fields" in {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(None, None, Some(deceasedDetails))
      val deceasedDetailsForm1 = deceasedAddressDetailsOutsideUKForm.fill(deceasedDetails)
      val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host, data = deceasedDetailsForm1.data.toSeq, authRetrieveNino = false)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onEditSubmit()(request)
      status(result) mustBe SEE_OTHER
    }

    "respond appropriately to an invalid submit: Missing mandatory fields" in {
      var existingDeceasedDetails = CommonBuilder.buildDeceasedDetails
      val deceasedDetails = DeceasedDetails(None, None, None, None, None, None, None, None, None)
      val registrationDetails = RegistrationDetails(None, None, Some(existingDeceasedDetails))
      val deceasedDetailsForm1 = deceasedAddressDetailsOutsideUKForm.fill(deceasedDetails)
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=deceasedDetailsForm1.data.toSeq, authRetrieveNino = false)

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = await(controller.onSubmit()(request))
      status(result) mustBe BAD_REQUEST
    }

    "respond appropriately to a submit in edit mode with valid values in all fields" in {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(None, None, Some(deceasedDetails))
      val deceasedDetailsForm1 = deceasedAddressDetailsOutsideUKForm.fill(deceasedDetails)
      val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host, data = deceasedDetailsForm1.data.toSeq, authRetrieveNino = false)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onEditSubmit()(request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be (Some(registrationRoutes.RegistrationSummaryController.onPageLoad().url))
    }

    "respond appropriately to a submit in edit mode with invalid values in some fields" in {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(None, None, Some(deceasedDetails))
      val deceasedDetailsForm1 = deceasedAddressDetailsOutsideUKForm.fill(deceasedDetails)
      implicit val request = createFakeRequest(authRetrieveNino = false).withFormUrlEncodedBody(("ukAddress.ukAddressLine1", "addr1"))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onEditSubmit()(request)
      status(result) mustBe BAD_REQUEST
    }

    "save valid data correctly when coming to this screen for the first time" in {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(None, None, Some(deceasedDetails))

      val newDeceasedDetails = DeceasedDetails(None, None, None, None,
        Some(UkAddress("New Line 1", "New Line 2", None, None, "", "US")), None, None, None, None)

      val form = deceasedAddressDetailsOutsideUKForm.fill(newDeceasedDetails)

      val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host,
        data = form.data.toSeq, authRetrieveNino = false)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = await(controller.onSubmit()(request))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val expectedDeceasedDetails = deceasedDetails copy(ukAddress = newDeceasedDetails.ukAddress, isAddressInUK = Some(false))
      capturedValue.deceasedDetails mustBe Some(expectedDeceasedDetails)
    }

    "save valid data correctly when returning to this screen" in {
      val existingApplicantDetails = CommonBuilder.buildApplicantDetails
      val existingDeceasedDetails = CommonBuilder.buildDeceasedDetails
      val existingDod = DeceasedDateOfDeath(new LocalDate(1980, 1, 1))

      val existingRegistrationDetails = RegistrationDetails(Some(existingDod),
        Some(existingApplicantDetails), Some(existingDeceasedDetails))

      val newDeceasedDetails = DeceasedDetails(None, None, None, None,
        Some(UkAddress("New Line 1", "New Line 2", None, None, "", "US")), None, None, None, None)

      val form = deceasedAddressDetailsOutsideUKForm.fill(newDeceasedDetails)

      val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host, data = form.data.toSeq, authRetrieveNino = false)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(existingRegistrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(existingRegistrationDetails))

      val result = await(controller.onEditSubmit()(request))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val expectedDeceasedDetails = existingDeceasedDetails copy(
        ukAddress = newDeceasedDetails.ukAddress, isAddressInUK = Some(false))
      capturedValue.deceasedDetails mustBe Some(expectedDeceasedDetails)
    }

    "return true if the guard condition is met" in {
      val rd = CommonBuilder.buildRegistrationDetails copy (deceasedDetails =
        Some(DeceasedDetails(None, None, None, None, None, None, None, None, Some(false))))
      controller.checkGuardCondition(rd, "") mustBe true
    }

    "return false if the guard condition is not met" in {
      val rd = CommonBuilder.buildRegistrationDetails copy (deceasedDetails =
        Some(DeceasedDetails(None, None, None, None, None, None, None, None, None)))
      controller.checkGuardCondition(rd, "") mustBe false
    }

    "respond with an internal server error to a submit with valid values in all fields when teh storage fails" in  {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(None, None, Some(deceasedDetails))
      val deceasedDetailsForm1 = deceasedAddressDetailsOutsideUKForm.fill(deceasedDetails)
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=deceasedDetailsForm1.data.toSeq, authRetrieveNino = false)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCacheWithFailure(mockCachingConnector, Some(registrationDetails))

      val result = controller.onSubmit()(request)
      status(result) mustBe(INTERNAL_SERVER_ERROR)

    }

  }
}
