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
import iht.connector.CitizenDetailsConnector
import iht.controllers.application.assets.trusts.TrustsMoreThanOneQuestionController
import iht.controllers.registration.{routes => registrationRoutes}
import iht.forms.registration.ApplicantForms._
import iht.metrics.IhtMetrics
import iht.models.{ApplicantDetails, RegistrationDetails, _}

import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever, NinoBuilder}
import org.joda.time.LocalDate
import org.mockito.Mockito._
import play.api.i18n.{Lang, Messages}
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.domain.TaxIds
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class ApplicantTellUsAboutYourselfControllerTest
  extends RegistrationApplicantControllerWithEditModeBehaviour[ApplicantTellUsAboutYourselfController] {

  val mockCitizenDetailsConnector = mock[CitizenDetailsConnector]

  val firstName = CommonBuilder.firstNameGenerator
  val surname = CommonBuilder.surnameGenerator
  val userDetails = CidPerson(Some(CidNames(Some(CidName(Some(firstName), Some(surname))), None)),
    TaxIds(NinoBuilder.randomNino), Some("01011950"))

  implicit val messages: Messages = mockControllerComponents.messagesApi.preferred(Seq(Lang.defaultLang)).messages
  protected abstract class TestController extends FrontendController(mockControllerComponents) with ApplicantTellUsAboutYourselfController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

 def controller = new TestController {
   override val cachingConnector = mockCachingConnector
   override val authConnector = mockAuthConnector
   override val metrics: IhtMetrics = mock[IhtMetrics]

   override def citizenDetailsConnector = mockCitizenDetailsConnector
   override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
 }

  def controllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val metrics: IhtMetrics = mock[IhtMetrics]

    override def guardConditions: Set[Predicate] = Set((_, _) => true)
    override def citizenDetailsConnector = mockCitizenDetailsConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  override def beforeEach {
    reset(mockCitizenDetailsConnector)
    super.beforeEach
  }

  // Perform tests.
  "ApplicantTellUsAboutYourselfController" must {

    behave like securedRegistrationApplicantController()

    "contain Continue button when Page is loaded in normal mode" in {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val registrationDetails=CommonBuilder.buildRegistrationDetails copy (applicantDetails = Some(applicantDetails))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onPageLoad()(createFakeRequestWithReferrer(referrerURL=referrerURL,host=host))
      status(result) mustBe OK

      contentAsString(result) must include(messagesApi("iht.continue"))
      contentAsString(result) must not include(messagesApi("site.link.cancel"))
    }

    "contain Continue and Cancel buttons when Page is loaded in edit mode" in {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val registrationDetails=CommonBuilder.buildRegistrationDetails copy (applicantDetails = Some(applicantDetails))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onEditPageLoad()(createFakeRequestWithReferrer(referrerURL=referrerURL,host=host))
      status(result) mustBe OK

      contentAsString(result) must include(messagesApi("iht.continue"))
      contentAsString(result) must include(messagesApi("site.link.cancel"))
    }

    "not contain the 'Do you live in the UK' question when loaded in edit mode" in {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val registrationDetails=CommonBuilder.buildRegistrationDetails copy (applicantDetails = Some(applicantDetails))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onEditPageLoad()(createFakeRequestWithReferrer(referrerURL=referrerURL,host=host))
      status(result) mustBe OK

      contentAsString(result) must not include messagesApi("page.iht.registration.applicantTellUsAboutYourself.question.label")
    }

    "respond appropriately to a submit with valid values in all fields and living in UK" in  {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails), None)
      val form = applicantTellUsAboutYourselfForm.fill(applicantDetails)
      val request =
        createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=form.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))
      createMockToGetCitizenDetails(mockCitizenDetailsConnector, userDetails)

      val result = controller.onSubmit()(request)
      status(result) mustBe (SEE_OTHER)
      redirectLocation(result) must be(Some(routes.ApplicantAddressController.onPageLoadUk().url))
    }

    "respond appropriately to a submit with valid values in all fields and living in UK but no applicant details" in  {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val registrationDetails = RegistrationDetails(None, None, None)
      val form = applicantTellUsAboutYourselfForm.fill(applicantDetails)
      val request =
        createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=form.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))
      createMockToGetCitizenDetails(mockCitizenDetailsConnector, userDetails)

      val result = controller.onSubmit()(request)
      status(result) mustBe (SEE_OTHER)
      redirectLocation(result) must be(Some(routes.ApplicantAddressController.onPageLoadUk().url))
    }

    "respond appropriately to a submit with valid values in all fields and living abroad" in  {
      val applicantDetails = CommonBuilder.buildApplicantDetails copy (doesLiveInUK = Some(false))
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails), None)
      val form = applicantTellUsAboutYourselfForm.fill(applicantDetails)
      val request =
        createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=form.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))
      createMockToGetCitizenDetails(mockCitizenDetailsConnector, userDetails)

      val result = controller.onSubmit()(request)
      status(result) mustBe (SEE_OTHER)
      redirectLocation(result) must be(Some(routes.ApplicantAddressController.onPageLoadAbroad().url))
    }

    "respond appropriately to a submit in edit mode with valid values in all fields and living in UK" in  {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails), None)
      val applicantDetailsForm1 = applicantTellUsAboutYourselfForm.fill(applicantDetails)
      val request =
        createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
          data=applicantDetailsForm1.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))
      createMockToGetCitizenDetails(mockCitizenDetailsConnector, userDetails)

      val result = controller.onEditSubmit()(request)
      status(result) mustBe (SEE_OTHER)
      redirectLocation(result) must be(Some(registrationRoutes.RegistrationSummaryController.onPageLoad().url))
    }

    "respond appropriately to a submit in edit mode with valid values in all fields and living in UK and no applicant details" in  {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val registrationDetails = RegistrationDetails(None, None, None)
      val applicantDetailsForm1 = applicantTellUsAboutYourselfForm.fill(applicantDetails)
      val request =
        createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
          data=applicantDetailsForm1.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))
      createMockToGetCitizenDetails(mockCitizenDetailsConnector, userDetails)

      val result = controller.onEditSubmit()(request)
      status(result) mustBe (SEE_OTHER)
      redirectLocation(result) must be(Some(registrationRoutes.RegistrationSummaryController.onPageLoad().url))
    }

    "respond appropriately to a submit in edit mode with valid values in all fields and living abroad" in  {
      val applicantDetails = CommonBuilder.buildApplicantDetails copy (doesLiveInUK = Some(false))
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails), None)
      val form = applicantTellUsAboutYourselfForm.fill(applicantDetails)
      val request =
        createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=form.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))
      createMockToGetCitizenDetails(mockCitizenDetailsConnector, userDetails)

      val result = controller.onEditSubmit()(request)
      status(result) mustBe (SEE_OTHER)
      redirectLocation(result) must be(Some(registrationRoutes.RegistrationSummaryController.onPageLoad().url))
    }

    "respond appropriately to a submit in edit mode with invalid values" in  {
      val applicantDetails = CommonBuilder.buildApplicantDetails copy (doesLiveInUK = Some(false))
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails), None)
      val form = applicantTellUsAboutYourselfForm.fill(applicantDetails)

      implicit val request = createFakeRequest().withFormUrlEncodedBody(("phoneNo", CommonBuilder.emptyString))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))
      createMockToGetCitizenDetails(mockCitizenDetailsConnector, userDetails)

      val result = controller.onEditSubmit()(request)
      status(result) mustBe BAD_REQUEST
    }

    "respond appropriately to an invalid submit: Missing mandatory fields" in {
      val applicantDetails = ApplicantDetails(None, None, None, None, None, None, None, None, None, None, None)
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails), None)
      val form = applicantTellUsAboutYourselfForm.fill(applicantDetails)
      val request =
        createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=form.data.toSeq)

      createMockToGetRegDetailsFromCache(mockCachingConnector, None)
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))
      createMockToGetCitizenDetails(mockCitizenDetailsConnector, userDetails)

      val result = await(controller.onSubmit()(request))
      status(result) mustBe BAD_REQUEST
    }

    "raise an error when the citizen details service is unavailable" in {
      createMockToThrowExceptionWhenGettingCitizenDetails(mockCitizenDetailsConnector)

      val applicantDetails = CommonBuilder.buildApplicantDetails
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails), None)
      val form = applicantTellUsAboutYourselfForm.fill(applicantDetails)
      val request =
        createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=form.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      intercept[RuntimeException] {
        await(controller.onSubmit()(request))
      }
    }

    "raise a not found error when the citizen details service can't find nino" in {
      createMockToThrowNotFoundExceptionWhenGettingCitizenDetails(mockCitizenDetailsConnector)

      val applicantDetails = CommonBuilder.buildApplicantDetails
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails), None)
      val form = applicantTellUsAboutYourselfForm.fill(applicantDetails)
      val request =
        createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=form.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = await(controller.onSubmit()(request))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) must include(Messages("page.iht.registration.applicantDetails.citizenDetailsNotFound.guidance"))
    }

    "save valid data correctly including citizen details when coming to this screen for the first time" in {
      val applicantDetails = CommonBuilder.buildApplicantDetails copy (doesLiveInUK = Some(false),
        phoneNo = Some("SomePhoneNumber"))
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails), None)
      val form = applicantTellUsAboutYourselfForm.fill(applicantDetails)
      val request =
        createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=form.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))
      createMockToGetCitizenDetails(mockCitizenDetailsConnector, userDetails)

      val result = await(controller.onSubmit()(request))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)

      val ad = capturedValue.applicantDetails.get
      ad.firstName mustBe Some(firstName)
      ad.lastName mustBe Some(surname)
      ad.dateOfBirth mustBe Some(new LocalDate(1950, 1, 1))
      ad.doesLiveInUK mustBe Some(false)
      ad.phoneNo mustBe Some("SOMEPHONENUMBER")
      ad.nino mustBe Some(CommonBuilder.DefaultNino)
    }

    "save valid data correctly including citizen details when returning to this screen" in {
      val existingApplicantDetails = CommonBuilder.buildApplicantDetails copy (phoneNo = Some("SomePhoneNumber"))
      val newApplicantDetails = ApplicantDetails(None, None, None, None, None, None, Some("SomeOtherPhoneNumber"), None, None, Some(false), None)
      val existingDeceasedDetails = CommonBuilder.buildDeceasedDetails
      val existingDod = DeceasedDateOfDeath(new LocalDate(1980, 1, 1))
      val registrationDetails = RegistrationDetails(Some(existingDod), Some(existingApplicantDetails), Some(existingDeceasedDetails))
      val form = applicantTellUsAboutYourselfEditForm.fill(newApplicantDetails)
      val request =
        createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=form.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))
      createMockToGetCitizenDetails(mockCitizenDetailsConnector, userDetails)

      val result = await(controller.onEditSubmit()(request))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      capturedValue.deceasedDateOfDeath mustBe Some(existingDod)
      capturedValue.deceasedDetails mustBe Some(existingDeceasedDetails)

      val ad = capturedValue.applicantDetails.get
      ad.doesLiveInUK mustBe Some(true) // Because we shouldn't overwrite the existing details
      ad.phoneNo mustBe Some("SOMEOTHERPHONENUMBER")
    }

    "return true if the guard conditions are true" in {
      val rd = CommonBuilder.buildRegistrationDetails copy (applicantDetails = Some(
        ApplicantDetails(country=Some(CommonBuilder.DefaultCountry), role=Some(mockAppConfig.roleLeadExecutor))))
      controller.checkGuardCondition(rd, "") mustBe true
    }

    "return false if the guard conditions are false" in {
      val rd = CommonBuilder.buildRegistrationDetails copy (applicantDetails = Some(
        ApplicantDetails(country=None, role=Some(mockAppConfig.roleLeadExecutor))))
      controller.checkGuardCondition(rd, "") mustBe false
    }
  }

  "respond with a server error to a submit with valid values in all fields and living in UK when the storage of registration details fails" in  {
    val applicantDetails = CommonBuilder.buildApplicantDetails
    val registrationDetails = RegistrationDetails(None, Some(applicantDetails), None)
    val applicantDetailsForm1 = applicantTellUsAboutYourselfForm.fill(applicantDetails)
    val request =
      createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=applicantDetailsForm1.data.toSeq)

    createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
    createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
    createMockToStoreRegDetailsInCacheWithFailure(mockCachingConnector, Some(registrationDetails))
    createMockToGetCitizenDetails(mockCitizenDetailsConnector, userDetails)

    val result = controller.onSubmit()(request)
    status(result) mustBe (INTERNAL_SERVER_ERROR)
  }
}
