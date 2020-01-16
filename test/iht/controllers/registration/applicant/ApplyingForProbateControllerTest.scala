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

package iht.controllers.registration.applicant

import iht.config.AppConfig
import iht.forms.registration.ApplicantForms._
import iht.models.ApplicantDetails

import iht.testhelpers.{CommonBuilder, ContentChecker, MockFormPartialRetriever}
import iht.utils.{DeceasedInfoHelper, RegistrationKickOutHelper}
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class ApplyingForProbateControllerTest
  extends RegistrationApplicantControllerWithEditModeBehaviour[ApplyingForProbateController] with RegistrationKickOutHelper {

  implicit val messages: Messages = mockControllerComponents.messagesApi.preferred(Seq(Lang.defaultLang)).messages
  protected abstract class TestController extends FrontendController(mockControllerComponents) with ApplyingForProbateController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  val appConfig = mockAppConfig

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

  "ApplyingForProbateController" must {

    behave like securedRegistrationApplicantController()

    "load when visited for the first time and show a Continue link" in {
      val regdDetailsWithDeceasedDetails = CommonBuilder.buildRegistrationDetailsWithDeceasedDetails
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(regdDetailsWithDeceasedDetails))

      val result = controller.onPageLoad(createFakeRequest())

      status(result) must be(OK)

      val contentResult = ContentChecker.stripLineBreaks(contentAsString(result))
      contentResult must include(messagesApi("page.iht.registration.applicant.applyingForProbate",
        DeceasedInfoHelper.getDeceasedNameOrDefaultString(regdDetailsWithDeceasedDetails)))
      contentResult must include(messagesApi("iht.continue"))
      contentResult must not include(messagesApi("site.link.cancel"))
    }

    "load when revisited after answering Yes" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy (applicantDetails =
          Some(new ApplicantDetails(isApplyingForProbate = Some(true), role = Some(mockAppConfig.roleLeadExecutor))))))

      val result: Future[Result] = controller.onPageLoad(createFakeRequest())

      status(result) must be(OK)
    }

    "load when revisited after answering No" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy (applicantDetails =
          Some(new ApplicantDetails(isApplyingForProbate = Some(false), role = Some(mockAppConfig.roleLeadExecutor))))))

      val result = controller.onPageLoad(createFakeRequest())

      status(result) must be(OK)
    }

    "load in edit mode and show Continue and Cancel links" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy (applicantDetails =
          Some(new ApplicantDetails(isApplyingForProbate = Some(true), role = Some(mockAppConfig.roleLeadExecutor))))))

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

      val form = applyingForProbateForm.fill(ApplicantDetails(isApplyingForProbate = Some(true), role = Some(mockAppConfig.roleLeadExecutor)))
      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = form.data.toSeq, authRetrieveNino = false)

      val result = controller.onSubmit(request)
      status(result) mustBe SEE_OTHER
    }

    "show an error message on submit when the question is not answered" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails))

      val form = applyingForProbateForm.fill(ApplicantDetails(role = Some(mockAppConfig.roleLeadExecutor))).data.toSeq
      val seq = form filter { case (key: String, value: String) => key != "isApplyingForProbate"}
      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = seq, authRetrieveNino = false)

      val result = controller.onSubmit(request)
      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messagesApi("error.applicantIsApplyingForProbate.select"))
    }

    "save and redirect correctly on submit when answering Yes" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy(
          applicantDetails = Some(new ApplicantDetails(role = Some(mockAppConfig.roleLeadExecutor))))))
      createMockToStoreRegDetailsInCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails))

      val form = applyingForProbateForm.fill(ApplicantDetails(isApplyingForProbate = Some(true), role = Some(mockAppConfig.roleLeadExecutor)))

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host, data = form.data.toSeq, authRetrieveNino = false)

      val result = controller.onSubmit(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(
        Some(iht.controllers.registration.applicant.routes.ExecutorOfEstateController.onPageLoad().url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val applicant = capturedValue.applicantDetails.get
      applicant.isApplyingForProbate mustBe Some(true)
    }

    "save and redirect correctly on submit when answering No" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy(
          applicantDetails = Some(new ApplicantDetails(role = Some(mockAppConfig.roleLeadExecutor))))))
      createMockToStoreRegDetailsInCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails))
      createMockToGetSingleValueFromCache(mockCachingConnector,
        singleValueReturn = Some(KickoutNotApplyingForProbate))
      createMockToStoreSingleValueInCache(mockCachingConnector,
        singleValueReturn = Some(KickoutNotApplyingForProbate))

      val form = applyingForProbateForm.fill(ApplicantDetails(isApplyingForProbate = Some(false), role = Some(mockAppConfig.roleLeadExecutor)))

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = form.data.toSeq, authRetrieveNino = false)

      val result = controller.onSubmit(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(iht.controllers.registration.routes.KickoutRegController.onPageLoad().url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val applicant = capturedValue.applicantDetails.get
      applicant.isApplyingForProbate mustBe Some(false)

      val storeResult = verifyAndReturnStoredSingleValue(mockCachingConnector)
      storeResult._1 mustBe RegistrationKickoutReasonCachingKey
      storeResult._2 mustBe KickoutNotApplyingForProbate
    }

    "save and redirect correctly on submit in edit mode when answering Yes" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy(applicantDetails = Some(new ApplicantDetails(role = Some(mockAppConfig.roleLeadExecutor))))))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails))

      val form = applyingForProbateForm.fill(ApplicantDetails(isApplyingForProbate = Some(true), role = Some(mockAppConfig.roleLeadExecutor)))

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host, data = form.data.toSeq, authRetrieveNino = false)

      val result = controller.onEditSubmit(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(iht.controllers.registration.routes.RegistrationSummaryController.onPageLoad().url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val applicant = capturedValue.applicantDetails.get
      applicant.isApplyingForProbate mustBe Some(true)
    }

    "show bad request when errors" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy(applicantDetails = Some(new ApplicantDetails(role = Some(mockAppConfig.roleLeadExecutor))))))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails))

      val form = applyingForProbateForm.fill(ApplicantDetails(isApplyingForProbate = Some(true), role = Some(mockAppConfig.roleLeadExecutor)))

      implicit val request = createFakeRequest(authRetrieveNino = false).withFormUrlEncodedBody(("isApplyingForProbate", ""))
      val result = controller.onEditSubmit(request)
      status(result) must be(BAD_REQUEST)
    }

    "save and redirect correctly on submit in edit mode when answering No" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy(applicantDetails = Some(new ApplicantDetails(role = Some(mockAppConfig.roleLeadExecutor))))))
      createMockToStoreRegDetailsInCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails))
      createMockToGetSingleValueFromCache(mockCachingConnector,
        singleValueReturn = Some(KickoutNotApplyingForProbate))
      createMockToStoreSingleValueInCache(mockCachingConnector,
        singleValueReturn = Some(KickoutNotApplyingForProbate))

      val form = applyingForProbateForm.fill(ApplicantDetails(isApplyingForProbate = Some(false), role = Some(mockAppConfig.roleLeadExecutor)))

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host, data = form.data.toSeq, authRetrieveNino = false)

      val result = controller.onEditSubmit(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(iht.controllers.registration.routes.KickoutRegController.onPageLoad().url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val applicant = capturedValue.applicantDetails.get
      applicant.isApplyingForProbate mustBe Some(false)

      val storeResult = verifyAndReturnStoredSingleValue(mockCachingConnector)
      storeResult._1 mustBe RegistrationKickoutReasonCachingKey
      storeResult._2 mustBe KickoutNotApplyingForProbate
    }
  }
}
