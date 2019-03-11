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

package iht.controllers.registration.applicant

import iht.forms.registration.ApplicantForms._
import iht.models.{ApplicantDetails, DeceasedDateOfDeath}
import iht.testhelpers.MockObjectBuilder._
import iht.testhelpers.{CommonBuilder, ContentChecker, MockFormPartialRetriever}
import iht.utils.{DeceasedInfoHelper, RegistrationKickOutHelper}
import org.joda.time.LocalDate
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class executorOfEstateControllerTest
  extends RegistrationApplicantControllerWithEditModeBehaviour[executorOfEstateController] {

  // Create controller object and pass in mock.
  def controller = new executorOfEstateController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def controllerNotAuthorised = new executorOfEstateController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  "executorOfEstateController" must {

    behave like securedRegistrationApplicantController()

    val applicantDetails = CommonBuilder.buildApplicantDetails

    "load when visited for the first time and show a Continue link" in {
      val regdDetailsWithDeceasedDetails = CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy (applicantDetails = Some(applicantDetails))
      createMockToGetRegDetailsFromCache(mockCachingConnector,
      Some(regdDetailsWithDeceasedDetails))

//      val registrationDetails= CommonBuilder.buildRegistrationDetails copy (deceasedDateOfDeath = Some(DeceasedDateOfDeath(LocalDate.now)),
//        deceasedDetails=Some(CommonBuilder.buildDeceasedDetails), applicantDetails = Some(applicantDetails))

      val result = controller.onPageLoad(createFakeRequest())

      status(result) must be(OK)

      val contentResult = ContentChecker.stripLineBreaks(contentAsString(result))
      contentResult must include(messagesApi("page.iht.registration.applicant.executorOfEstate",
        DeceasedInfoHelper.getDeceasedNameOrDefaultString(regdDetailsWithDeceasedDetails)))
      contentResult must include(messagesApi("page.iht.registration.applicant.executorOfEstate.p1"))
      contentResult must include(messagesApi("iht.continue"))
      contentResult must not include(messagesApi("site.link.cancel"))
    }

    "load when revisited after answering Yes" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy (applicantDetails =
          Some(new ApplicantDetails(isApplyingForProbate = Some(true))))))

      val result: Future[Result] = controller.onPageLoad(createFakeRequest())

      status(result) must be(OK)
    }

    "load when revisited after answering No" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy (applicantDetails =
          Some(new ApplicantDetails(isApplyingForProbate = Some(false))))))

      val result = controller.onPageLoad(createFakeRequest())

      status(result) must be(OK)
    }

    "load in edit mode and show Continue and Cancel links" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy (applicantDetails =
          Some(new ApplicantDetails(isApplyingForProbate = Some(true))))))

      val result = controller.onEditPageLoad(createFakeRequest())

      status(result) must be(OK)

      contentAsString(result) must include(messagesApi("iht.continue"))
      contentAsString(result) must include(messagesApi("site.link.cancel"))
    }

    "redirect on load to the estate report page if the RegistrationDetails does not contain deceased details" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails))

      val result = controller.onPageLoad(createFakeRequest())
      status(result) mustBe SEE_OTHER
    }

    "redirect on submit to the estate report page if the RegistrationDetails does not contain deceased details" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails))

      val form = executorOfEstateForm.fill(ApplicantDetails(executorOfEstate = Some(true)))
      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = form.data.toSeq, authRetrieveNino = false)

      val result = controller.onSubmit(request)
      status(result) mustBe SEE_OTHER
    }

    "show an error message on submit when the question is not answered" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy (applicantDetails = Some(applicantDetails))))
      createMockToStoreRegDetailsInCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails))

      val form = executorOfEstateForm.fill(ApplicantDetails()).data.toSeq
      val seq = form filter { case (key: String, value: String) => key != "executorOfEstate"}
      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = seq, authRetrieveNino = false)

      val result = controller.onSubmit(request)
      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messagesApi("error.applicantExecutorOfEstate.select"))
    }

    "save and redirect correctly on submit when answering Yes" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy(
          applicantDetails = Some(applicantDetails))))
      createMockToStoreRegDetailsInCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails))

      val form = executorOfEstateForm.fill(ApplicantDetails(executorOfEstate = Some(true)))

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = form.data.toSeq, authRetrieveNino = false)

      val result = controller.onSubmit(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(
        Some(iht.controllers.registration.applicant.routes.ProbateLocationController.onPageLoad().url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val applicant = capturedValue.applicantDetails.get
      applicant.executorOfEstate mustBe Some(true)
    }

    "save and redirect correctly on submit when answering No" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy(
          applicantDetails = Some(applicantDetails))))
      createMockToStoreRegDetailsInCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails))
      createMockToGetSingleValueFromCache(mockCachingConnector,
        singleValueReturn = Some(RegistrationKickOutHelper.KickoutNotAnExecutor))
      createMockToStoreSingleValueInCache(mockCachingConnector,
        singleValueReturn = Some(RegistrationKickOutHelper.KickoutNotAnExecutor))

      val form = executorOfEstateForm.fill(ApplicantDetails(executorOfEstate = Some(false)))

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = form.data.toSeq, authRetrieveNino = false)

      val result = controller.onSubmit(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(iht.controllers.registration.routes.KickoutRegController.onPageLoad().url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val applicant = capturedValue.applicantDetails.get
      applicant.executorOfEstate mustBe Some(false)

      val storeResult = verifyAndReturnStoredSingleValue(mockCachingConnector)
      storeResult._1 mustBe RegistrationKickOutHelper.RegistrationKickoutReasonCachingKey
      storeResult._2 mustBe RegistrationKickOutHelper.KickoutNotAnExecutor
    }

    "save and redirect correctly on submit in edit mode when answering Yes" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy(applicantDetails = Some(applicantDetails))))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails))

      val form = executorOfEstateForm.fill(ApplicantDetails(executorOfEstate = Some(true)))

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host, data = form.data.toSeq, authRetrieveNino = false)

      val result = controller.onEditSubmit(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(iht.controllers.registration.routes.RegistrationSummaryController.onPageLoad().url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val applicant = capturedValue.applicantDetails.get
      applicant.executorOfEstate mustBe Some(true)
    }

    "show bad request when errors" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy(applicantDetails = Some(applicantDetails))))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails))

      val form = executorOfEstateForm.fill(ApplicantDetails(executorOfEstate = Some(true)))

      implicit val request = createFakeRequest(authRetrieveNino = false).withFormUrlEncodedBody(("executorOfEstate", ""))
      val result = controller.onEditSubmit(request)
      status(result) must be(BAD_REQUEST)
    }

    "save and redirect correctly on submit in edit mode when answering No" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy(applicantDetails = Some(applicantDetails))))
      createMockToStoreRegDetailsInCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails))
      createMockToGetSingleValueFromCache(mockCachingConnector,
        singleValueReturn = Some(RegistrationKickOutHelper.KickoutNotAnExecutor))
      createMockToStoreSingleValueInCache(mockCachingConnector,
        singleValueReturn = Some(RegistrationKickOutHelper.KickoutNotAnExecutor))

      val form = executorOfEstateForm.fill(ApplicantDetails(executorOfEstate = Some(false)))

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host, data = form.data.toSeq, authRetrieveNino = false)

      val result = controller.onEditSubmit(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(iht.controllers.registration.routes.KickoutRegController.onPageLoad().url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val applicant = capturedValue.applicantDetails.get
      applicant.executorOfEstate mustBe Some(false)

      val storeResult = verifyAndReturnStoredSingleValue(mockCachingConnector)
      storeResult._1 mustBe RegistrationKickOutHelper.RegistrationKickoutReasonCachingKey
      storeResult._2 mustBe RegistrationKickOutHelper.KickoutNotAnExecutor
    }
  }
}
