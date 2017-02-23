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

package iht.controllers.registration.applicant

import iht.connector.CachingConnector
import iht.constants.IhtProperties
import iht.controllers.registration.{routes => registrationRoutes}
import iht.forms.registration.ApplicantForms._
import iht.metrics.Metrics
import iht.models.{ApplicantDetails, DeceasedDateOfDeath, RegistrationDetails}
import iht.testhelpers.MockObjectBuilder._
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils.RegistrationKickOutHelper._
import org.joda.time.LocalDate
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._

class ProbateLocationControllerTest
  extends RegistrationApplicantControllerWithEditModeBehaviour[ProbateLocationController] {

 //Create controller object and pass in mock.
 def controller = new ProbateLocationController {
   override val cachingConnector = mockCachingConnector
   override val authConnector = createFakeAuthConnector(isAuthorised=true)
   override val metrics:Metrics = mock[Metrics]
   override val isWhiteListEnabled = false
 }

  def controllerNotAuthorised = new ProbateLocationController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val metrics:Metrics = mock[Metrics]
    override val isWhiteListEnabled = false
  }

  before {
    mockCachingConnector = mock[CachingConnector]
  }

  "ProbateLocationController" must {

    behave like securedRegistrationApplicantController()

    "contain the right title and Continue link when page loads" in {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val registrationDetails= CommonBuilder.buildRegistrationDetails copy (deceasedDateOfDeath = Some(DeceasedDateOfDeath(LocalDate.now)),
        deceasedDetails=Some(CommonBuilder.buildDeceasedDetails), applicantDetails = Some(applicantDetails))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onPageLoad()(createFakeRequest())
      status(result) shouldBe OK

      contentAsString(result) should include(messagesApi("page.iht.registration.applicant.probateLocation.title"))
      contentAsString(result) should include(messagesApi("iht.continue"))
      contentAsString(result) should not include(messagesApi("site.link.cancel"))
    }

    "contain Continue and Cancel buttons when page is loaded in edit mode" in {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val registrationDetails= CommonBuilder.buildRegistrationDetails copy (
        deceasedDateOfDeath = Some(DeceasedDateOfDeath(LocalDate.now)),
        deceasedDetails=Some(CommonBuilder.buildDeceasedDetails), applicantDetails = Some(applicantDetails))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onEditPageLoad()(createFakeRequest())
      status(result) shouldBe OK

      contentAsString(result) should include(messagesApi("page.iht.registration.applicant.probateLocation.title"))
      contentAsString(result) should include(messagesApi("iht.continue"))
      contentAsString(result) should include(messagesApi("site.link.cancel"))
    }

    "respond appropriately to a submit with valid values in all fields" in  {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails), None)
      val filledForm = probateLocationForm.fill(applicantDetails)
      val request = createFakeRequest().withFormUrlEncodedBody(filledForm.data.toSeq: _*)

      createMockToGetExistingRegDetailsFromCache(mockCachingConnector, registrationDetails)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onSubmit()(request)
      redirectLocation(result) should be(Some(routes.ApplicantTellUsAboutYourselfController.onPageLoad().url))
    }

    "respond appropriately to a submit in edit mode with valid values in all fields" in  {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails), None)
      val filledForm = probateLocationForm.fill(applicantDetails)
      val request = createFakeRequest().withFormUrlEncodedBody(filledForm.data.toSeq: _*)

      createMockToGetExistingRegDetailsFromCache(mockCachingConnector, registrationDetails)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onEditSubmit()(request)
      redirectLocation(result) should be(Some(registrationRoutes.RegistrationSummaryController.onPageLoad().url))
    }

    "respond appropriately to an invalid submit: Missing mandatory fields" in {
      val applicantDetails = ApplicantDetails(isApplyingForProbate = Some(true), country = None)
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy (
        applicantDetails = Some(applicantDetails))
      val filledForm = probateLocationForm.fill(applicantDetails)
      val request = createFakeRequest().withFormUrlEncodedBody(filledForm.data.toSeq: _*)

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = await(controller.onSubmit()(request))
      status(result) shouldBe BAD_REQUEST
    }

    "save valid data correctly when coming to this screen for the first time" in {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails), None)
      val filledForm = probateLocationForm.fill(applicantDetails)

      createMockToGetExistingRegDetailsFromCache(mockCachingConnector, registrationDetails)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val form = probateLocationForm.fill(applicantDetails)

      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=form.data.toSeq)

      val result = controller.onSubmit()(request)
      status(result) should be (SEE_OTHER)

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      capturedValue.applicantDetails shouldBe Some(applicantDetails)
    }

    "save valid data correctly when returning to this screen" in {
      val existingApplicantDetails = CommonBuilder.buildApplicantDetails copy (country =
        Some(IhtProperties.applicantCountryNorthernIreland))
      val existingDeceasedDetails = CommonBuilder.buildDeceasedDetails
      val existingDod = DeceasedDateOfDeath(new LocalDate(1980, 1, 1))

      val existingRegistrationDetails = RegistrationDetails(Some(existingDod),
        Some(existingApplicantDetails), Some(existingDeceasedDetails))
      val applicantDetails = ApplicantDetails(country = Some(IhtProperties.applicantCountryEnglandOrWales))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(existingRegistrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(existingRegistrationDetails))

      val form = probateLocationForm.fill(applicantDetails)

      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=form.data.toSeq)

      val result = controller.onEditSubmit()(request)
      status(result) should be (SEE_OTHER)

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      capturedValue.deceasedDetails shouldBe Some(existingDeceasedDetails)
      capturedValue.deceasedDateOfDeath shouldBe Some(existingDod)
      capturedValue.applicantDetails shouldBe Some(existingApplicantDetails copy (country = applicantDetails.country))
    }

    "return true if the guard conditions are true" in {
      val rd = CommonBuilder.buildRegistrationDetails copy (applicantDetails =
        Some(ApplicantDetails(isApplyingForProbate = Some(true))),
        deceasedDetails = Some(CommonBuilder.buildDeceasedDetails))
      controller.checkGuardCondition(rd, "") shouldBe true
    }

    "return false if the guard conditions are false" in {
      val rd = CommonBuilder.buildRegistrationDetails copy (applicantDetails = None)
      controller.checkGuardCondition(rd, "") shouldBe false
    }

    def ensureRedirectOnKickout(country: String, kickoutReasonKey: String) = {
      val applicantDetails = CommonBuilder.buildApplicantDetails copy (country = Some(country))
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails), None)
      val applicantDetailsForm1 = probateLocationForm.fill(applicantDetails)
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=applicantDetailsForm1.data.toSeq)

      createMockToGetExistingRegDetailsFromCache(mockCachingConnector, registrationDetails)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreSingleValueInCache(
        cachingConnector=mockCachingConnector,
        singleValueReturn=Some(kickoutReasonKey))

      val result = await(controller.onSubmit()(request))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (
        Some(iht.controllers.registration.routes.KickoutController.onPageLoad().url))
      verifyAndReturnStoredSingleValue(mockCachingConnector) match {
        case (cachedKey, cachedValue) =>
          cachedKey shouldBe RegistrationKickoutReasonCachingKey
          cachedValue shouldBe kickoutReasonKey
      }
    }

    "redirect to kickout page if probate location is Scotland" in {
      ensureRedirectOnKickout(TestHelper.ApplicantCountryScotland, KickoutApplicantDetailsProbateScotland)
    }

    "redirect to kickout page if probate location is Northern Ireland" in {
      ensureRedirectOnKickout(TestHelper.ApplicantCountryNorthernIreland, KickoutApplicantDetailsProbateNi)
    }

    "respond with an internal server error to a submit with valid values in all fields but the storage operation fail" in  {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails), None)
      val filledForm = probateLocationForm.fill(applicantDetails)
      val request = createFakeRequest().withFormUrlEncodedBody(filledForm.data.toSeq: _*)

      createMockToGetExistingRegDetailsFromCache(mockCachingConnector, registrationDetails)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCacheWithFailure(mockCachingConnector, Some(registrationDetails))


      val result = controller.onSubmit()(request)
      status(result) should be(INTERNAL_SERVER_ERROR)
    }
  }
}
