/*
 * Copyright 2022 HM Revenue & Customs
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

package iht.controllers.registration.executor

import iht.config.AppConfig
import iht.connector.CachingConnector
import iht.controllers.registration.RegistrationControllerTest
import iht.forms.registration.CoExecutorForms
import iht.models.{CoExecutor, RegistrationDetails}
import iht.testhelpers.CommonBuilder
import iht.views.html.registration.executor.coexecutor_personal_details
import org.scalatest.BeforeAndAfter
import play.api.data.format.Formatter
import play.api.data.{FieldMapping, Form, FormError, Forms}
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

class CoExecutorPersonalDetailsControllerTest extends RegistrationControllerTest with BeforeAndAfter {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with CoExecutorPersonalDetailsController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val coexecutorPersonalDetailsView: coexecutor_personal_details = app.injector.instanceOf[coexecutor_personal_details]

  }

  // Create controller object and pass in mock.
  def controller(coExecutorForms2:CoExecutorForms): TestController = new TestController {
    override val cachingConnector: CachingConnector = mockCachingConnector
    override val authConnector: PlayAuthConnector = mockAuthConnector

    override def ninoForCoExecutor(blankMessageKey: String, lengthMessageKey: String, formatMessageKey: String,
                                   coExecutorIDKey: String, oRegDetails: Option[RegistrationDetails], loginNino: String)
                                  (implicit appConfig: AppConfig): FieldMapping[String] = {
      coExecutorForms2.ninoForCoExecutor(blankMessageKey, lengthMessageKey, formatMessageKey, coExecutorIDKey, oRegDetails, loginNino)(appConfig)
    }

  }

  def controllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector

    }

  def formWithMockedNinoValidation(coExecutor: CoExecutor, mockCachingConnector: CachingConnector): CoExecutorForms = {
    def coExecutorForms: CoExecutorForms = {
      new CoExecutorForms {
        override implicit lazy val appConfig: AppConfig = mockAppConfig
        override def ninoForCoExecutor(blankMessageKey: String, lengthMessageKey: String, formatMessageKey: String,
                                       coExecutorIDKey:String, oRegDetails: Option[RegistrationDetails], loginNino: String)
                                      (implicit appConfig: AppConfig): FieldMapping[String] = {
          val formatter = new Formatter[String] {
            override val format: Option[(String, Seq[Any])] = None
            override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = Right(coExecutor.nino)
            override def unbind(key: String, value: String): Map[String, String] = Map(key -> value)
          }
          val fieldMapping: FieldMapping[String] = Forms.of(formatter)
          fieldMapping
        }
      }
    }
    coExecutorForms
  }

  def formWithMockedNinoValidationNoCoExecutor(mockCachingConnector: CachingConnector): CoExecutorForms = {
    def coExecutorForms: CoExecutorForms = {
      new CoExecutorForms {
        override implicit lazy val appConfig: AppConfig = mockAppConfig
        override def ninoForCoExecutor(blankMessageKey: String, lengthMessageKey: String, formatMessageKey: String,
                                       coExecutorIDKey: String, oRegDetails: Option[RegistrationDetails], loginNino: String)
                                      (implicit appConfig: AppConfig): FieldMapping[String] = {
          val formatter = new Formatter[String] {
            override val format: Option[(String, Seq[Any])] = None
            override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = Right("")
            override def unbind(key: String, value: String): Map[String, String] = Map(key -> value)
          }
          val fieldMapping: FieldMapping[String] = Forms.of(formatter)
          fieldMapping
        }
      }
    }
    coExecutorForms
  }

  val fakedFormNino = "SR000009C"

  "CoExecutorPersonalDetailsController" must {

    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = controllerNotAuthorised.onPageLoad(None)(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to GG login page on Submit if the user is not logged in" in {
      val result = controllerNotAuthorised.onSubmit(None)(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to GG login page on PageLoad in edit mode if the user is not logged in" in {
      val result = controllerNotAuthorised.onEditPageLoad("1")(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to GG login page on Submit in edit mode if the user is not logged in" in {
      val result = controllerNotAuthorised.onEditSubmit("1")(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "load when no prior co-executors are saved" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate))

      val coExecutorForms: CoExecutorForms = formWithMockedNinoValidationNoCoExecutor(mockCachingConnector)

      val result = controller(coExecutorForms).onPageLoad(None)(createFakeRequest())

      status(result) must be(OK)
      contentAsString(result) must include(messagesApi("page.iht.registration.co-executor-personal-details.title"))
    }

    "contain Continue button when Page is loaded in normal mode" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate))

      val coExecutorForms: CoExecutorForms = formWithMockedNinoValidationNoCoExecutor(mockCachingConnector)

      val result = controller(coExecutorForms).onPageLoad(None)(createFakeRequest())
      status(result) mustBe OK

      contentAsString(result) must include(messagesApi("iht.continue"))
      contentAsString(result) must not include messagesApi("site.link.cancel")
    }

    "contain Continue and Cancel buttons when Page is loaded in edit mode" in {
      val coExec1 = CommonBuilder.buildCoExecutor copy (id = Some("1"), firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate copy(
        coExecutors = Seq(coExec1))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val coExecutorForms: CoExecutorForms = formWithMockedNinoValidationNoCoExecutor(mockCachingConnector)

      val result = controller(coExecutorForms).onEditPageLoad("1")(createFakeRequest())
      status(result) mustBe OK

      contentAsString(result) must include(messagesApi("iht.continue"))
      contentAsString(result) must include(messagesApi("site.link.cancel"))
    }

    "not contain the 'Do you live in the UK' question when loaded in edit mode" in {
      val coExec1 = CommonBuilder.buildCoExecutor copy (id = Some("1"), firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate copy(
        coExecutors = Seq(coExec1))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val coExecutorForms: CoExecutorForms = formWithMockedNinoValidationNoCoExecutor(mockCachingConnector)

      val result = controller(coExecutorForms).onEditPageLoad("1")(createFakeRequestWithReferrer(referrerURL=referrerURL,host=host))
      status(result) mustBe OK

      contentAsString(result) must not include messagesApi("page.iht.registration.co-executor-personal-details.isAddressInUk")
    }


    "load an existing co-executor" in {
      val firstName = CommonBuilder.firstNameGenerator
      val surname = CommonBuilder.surnameGenerator
      val coExec1 = CommonBuilder.buildCoExecutor copy (id = Some("1"), firstName=firstName, lastName=surname)
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate copy(
        coExecutors = Seq(coExec1))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val coExecutorForms: CoExecutorForms = formWithMockedNinoValidationNoCoExecutor(mockCachingConnector)

      val result = controller(coExecutorForms).onPageLoad(Some("1"))(createFakeRequest())

      status(result) must be(OK)
      contentAsString(result) must include(messagesApi("page.iht.registration.co-executor-personal-details.title"))
      contentAsString(result) must include(firstName)
      contentAsString(result) must include(surname)
    }

    "load when creating a new co-executor and another already exists" in {
      val firstName = CommonBuilder.firstNameGenerator
      val surname = CommonBuilder.surnameGenerator
      val coExec1 = CommonBuilder.buildCoExecutor copy (id = Some("1"), firstName=firstName, lastName=surname)
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate copy(
        coExecutors = Seq(coExec1))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val coExecutorForms: CoExecutorForms = formWithMockedNinoValidationNoCoExecutor(mockCachingConnector)
      val result = controller(coExecutorForms).onPageLoad(None)(createFakeRequest())

      status(result) must be(OK)
      contentAsString(result) must include(messagesApi("page.iht.registration.co-executor-personal-details.title"))
      contentAsString(result) must not include firstName
      contentAsString(result) must not include surname
    }

    "raise an error when accessed for a non-existant co-executor" in {
      val coExec1 = CommonBuilder.buildCoExecutor copy (id = Some("1"), firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate copy(
        coExecutors = Seq(coExec1))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

        val coExecutorForms: CoExecutorForms = formWithMockedNinoValidationNoCoExecutor(mockCachingConnector)

      intercept[Exception] {
        val result = controller(coExecutorForms).onPageLoad(Some("2"))(createFakeRequest())
        status(result)
      }
    }

    "raise an error when trying to add more co-executors than the maximum allowed" in {
      val coExec1 = CommonBuilder.buildCoExecutor copy (id = Some("1"), firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val coExec2 = CommonBuilder.buildCoExecutor copy (id = Some("2"), firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val coExec3 = CommonBuilder.buildCoExecutor copy (id = Some("3"), firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate copy(
        coExecutors = Seq(coExec1, coExec2, coExec3))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      intercept[Exception] {
        val coExecutorForms: CoExecutorForms = formWithMockedNinoValidationNoCoExecutor(mockCachingConnector)

        val result = controller(coExecutorForms).onPageLoad(None)(createFakeRequest())
        status(result)
      }
    }

    "redirect estate report if trying to add a co-executor but others applying for probate is unanswered" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails))

        val coExecutorForms: CoExecutorForms = formWithMockedNinoValidationNoCoExecutor(mockCachingConnector)

        val result = controller(coExecutorForms).onPageLoad(None)(createFakeRequest())
        status(result) mustBe SEE_OTHER
    }

    "redirect estate report if trying to add a co-executor but others applying for probate is answered false" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails
        copy (areOthersApplyingForProbate = Some(false))))

        val coExecutorForms: CoExecutorForms = formWithMockedNinoValidationNoCoExecutor(mockCachingConnector)

        val result = controller(coExecutorForms).onPageLoad(None)(createFakeRequest())
        status(result) mustBe SEE_OTHER
    }

    "save a valid new co-executor located in the uk" in {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails),
        Some(deceasedDetails), areOthersApplyingForProbate = Some(true))

      val coExecutor = CommonBuilder.buildCoExecutorPersonalDetails(None).copy(nino = fakedFormNino)

      val coExecutorForms: CoExecutorForms = formWithMockedNinoValidation(coExecutor, mockCachingConnector)

      val form: Form[CoExecutor] = coExecutorForms.coExecutorPersonalDetailsForm(loginNino = CommonBuilder.DefaultNino).fill(coExecutor)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = form.data.toSeq).withMethod("POST")

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate))

      val result = controller(coExecutorForms).onSubmit(None)(request)

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(
        Some(iht.controllers.registration.executor.routes.OtherPersonsAddressController.onPageLoadUK("1").url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)

      capturedValue.coExecutors.length mustBe 1
      capturedValue.coExecutors.head mustBe CommonBuilder.buildCoExecutorPersonalDetails(Some("1")).copy(nino = fakedFormNino)
      capturedValue.deceasedDetails mustBe Some(deceasedDetails)
      capturedValue.applicantDetails mustBe Some(applicantDetails)
    }

    "fail to save a new co-executor if entered NINO is the same as the login NINO" in {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails),
        Some(deceasedDetails), areOthersApplyingForProbate = Some(true))

      val coExecutor = CommonBuilder.buildCoExecutorPersonalDetails(None).copy(nino = CommonBuilder.DefaultNino)

      val coExecutorForms: CoExecutorForms = formWithMockedNinoValidation(coExecutor, mockCachingConnector)

      val form: Form[CoExecutor] = coExecutorForms.coExecutorPersonalDetailsForm(loginNino = CommonBuilder.DefaultNino).fill(coExecutor)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = form.data.toSeq).withMethod("POST")

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate))

      val result = controller(coExecutorForms).onSubmit(None)(request)

      status(result) must be(BAD_REQUEST)
    }

    "save a valid new co-executor located outside of the uk" in {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails), Some(deceasedDetails),
        areOthersApplyingForProbate = Some(true))

      val coExecutor = CommonBuilder.buildCoExecutorPersonalDetails().copy(isAddressInUk = Some(false), nino = fakedFormNino)
      val coExecutorForms: CoExecutorForms = formWithMockedNinoValidation(coExecutor, mockCachingConnector)

      val form: Form[CoExecutor] = coExecutorForms.coExecutorPersonalDetailsForm(loginNino = CommonBuilder.DefaultNino).fill(coExecutor)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = form.data.toSeq).withMethod("POST")

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(
        CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate))

      val result = controller(coExecutorForms).onSubmit(None)(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(
        Some(iht.controllers.registration.executor.routes.OtherPersonsAddressController.onPageLoadAbroad("1").url))
    }

    "save a valid co-executor when another already exists" in {
      val coExec1 = CommonBuilder.buildCoExecutorPersonalDetails(Some("1")) copy (firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate copy(
        coExecutors = Seq(coExec1))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val coExecutor = CommonBuilder.buildCoExecutorPersonalDetails(None).copy(nino = fakedFormNino)
      val coExecutorForms: CoExecutorForms = formWithMockedNinoValidation(coExecutor, mockCachingConnector)

      val form: Form[CoExecutor] = coExecutorForms.coExecutorPersonalDetailsForm(loginNino = CommonBuilder.DefaultNino).fill(coExecutor)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host,
        data = form.data.toSeq).withMethod("POST")

      createMockToStoreRegDetailsInCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate))

      val result = controller(coExecutorForms).onSubmit(None)(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(
        Some(iht.controllers.registration.executor.routes.OtherPersonsAddressController.onPageLoadUK("2").url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val expectedCoExecutor = CommonBuilder.buildCoExecutorPersonalDetails(Some("2"))

      capturedValue.coExecutors.length mustBe 2
      capturedValue.coExecutors.head mustBe coExec1
      capturedValue.coExecutors(1) mustBe expectedCoExecutor.copy(nino = fakedFormNino)
    }

    "update an existing co-executor with valid data" in {
      val coExec1 = CommonBuilder.buildCoExecutor copy (id = Some("1"), firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate copy(
        coExecutors = Seq(coExec1))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val firstName = CommonBuilder.firstNameGenerator
      val surname = CommonBuilder.surnameGenerator
      val coExecutor = CommonBuilder.buildCoExecutorWithId(Some("1")) copy (firstName = firstName,
        lastName = surname, nino = fakedFormNino)

      val coExecutorForms: CoExecutorForms = formWithMockedNinoValidation(coExecutor, mockCachingConnector)

      val form: Form[CoExecutor] = coExecutorForms.coExecutorPersonalDetailsForm(loginNino = CommonBuilder.DefaultNino).fill(coExecutor)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host,
        data = form.data.toSeq).withMethod("POST")

      createMockToStoreRegDetailsInCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate))

      val result = controller(coExecutorForms).onSubmit(Some("1"))(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(
        Some(iht.controllers.registration.executor.routes.OtherPersonsAddressController.onPageLoadUK("1").url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val expectedCoExecutor = coExec1 copy (firstName = firstName, lastName = surname)
      capturedValue.coExecutors.length mustBe 1
      capturedValue.coExecutors.head mustBe expectedCoExecutor.copy(nino = fakedFormNino)
    }

    "update an existing co-executor in edit mode with valid data" in {
      val coExec1 = CommonBuilder.buildCoExecutor copy (id = Some("1"), firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator
        , isAddressInUk = Some(true))
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate copy(
        coExecutors = Seq(coExec1))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val firstName = CommonBuilder.firstNameGenerator
      val surname = CommonBuilder.surnameGenerator
      val coExecutor = CommonBuilder.buildCoExecutorWithId(Some("1")) copy (firstName = firstName,
        lastName = surname, isAddressInUk = Some(false), nino = fakedFormNino)

      val coExecutorForms: CoExecutorForms = formWithMockedNinoValidation(coExecutor, mockCachingConnector)

      val form: Form[CoExecutor] = coExecutorForms.coExecutorPersonalDetailsForm(loginNino = CommonBuilder.DefaultNino).fill(coExecutor)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host,
        data = form.data.toSeq).withMethod("POST")

      createMockToStoreRegDetailsInCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate))

      val result = controller(coExecutorForms).onEditSubmit("1")(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(
        Some(iht.controllers.registration.routes.RegistrationSummaryController.onPageLoad.url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val expectedCoExecutor = coExec1 copy (firstName = firstName, lastName = surname, isAddressInUk = Some(true))
      capturedValue.coExecutors.length mustBe 1
      capturedValue.coExecutors.head mustBe expectedCoExecutor.copy(nino = fakedFormNino)
    }

    "raise an error when trying to save for a non-existant co-executor" in {
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val coExecutor = CommonBuilder.buildCoExecutor.copy(nino = fakedFormNino)
      val coExecutorForms: CoExecutorForms = formWithMockedNinoValidation(coExecutor, mockCachingConnector)

      val form: Form[CoExecutor] = coExecutorForms.coExecutorPersonalDetailsForm(loginNino = CommonBuilder.DefaultNino).fill(coExecutor)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = form.data.toSeq).withMethod("POST")

      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

        val result = controller(coExecutorForms).onSubmit(Some("1"))(request)
        redirectLocation(result) must be(
        Some(iht.controllers.registration.executor.routes.ExecutorOverviewController.onPageLoad.url))
    }

    "raise an error when trying to save a new co-executor and the maximum number already exist" in {
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate copy(
        coExecutors = Seq(CommonBuilder.DefaultCoExecutor1, CommonBuilder.DefaultCoExecutor2, CommonBuilder.DefaultCoExecutor3))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val coExecutor = CommonBuilder.buildCoExecutor.copy(nino = fakedFormNino)

      val coExecutorForms: CoExecutorForms = formWithMockedNinoValidation(coExecutor, mockCachingConnector)

      val form: Form[CoExecutor] = coExecutorForms.coExecutorPersonalDetailsForm(loginNino = CommonBuilder.DefaultNino).fill(coExecutor)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host,
        data = form.data.toSeq).withMethod("POST")

      createMockToStoreRegDetailsInCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate))

      intercept[Exception] {
        val result = controller(coExecutorForms).onSubmit(None)(request)
        status(result)
      }
    }
//
    "raise an error when trying to submit a co-executor but others applying for probate is unanswered" in {
      val registrationDetails = CommonBuilder.buildRegistrationDetails
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val coExecutor = CommonBuilder.buildCoExecutor
      val coExecutorForms: CoExecutorForms = formWithMockedNinoValidation(coExecutor, mockCachingConnector)

      val form: Form[CoExecutor] = coExecutorForms.coExecutorPersonalDetailsForm(loginNino = CommonBuilder.DefaultNino).fill(coExecutor)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = form.data.toSeq).withMethod("POST")

      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

        val result = controller(coExecutorForms).onSubmit(None)(request)
      status(result) mustBe SEE_OTHER
    }

    "raise an error when trying to submit a co-executor but others are not applying for probate" in {
      val registrationDetails = CommonBuilder.buildRegistrationDetails copy(
        areOthersApplyingForProbate = Some(false))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val coExecutor = CommonBuilder.buildCoExecutor
      val coExecutorForms: CoExecutorForms = formWithMockedNinoValidation(coExecutor, mockCachingConnector)

      val form: Form[CoExecutor] = coExecutorForms.coExecutorPersonalDetailsForm(loginNino = CommonBuilder.DefaultNino).fill(coExecutor)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = form.data.toSeq).withMethod("POST")

      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

        val result = controller(coExecutorForms).onSubmit(None)(request)
      status(result) mustBe SEE_OTHER
    }

    "show an error when some data is invalid" in {
      val coExecutor = CommonBuilder.buildCoExecutor copy (firstName = "")
      checkForErrorOnSubmissionOfModel(coExecutor, "error.firstName.give")
    }

    "save a valid new co-executor located in the uk will return an internal server error of the storage fails" in {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails),
        Some(deceasedDetails), areOthersApplyingForProbate = Some(true))

      val coExecutor = CommonBuilder.buildCoExecutorPersonalDetails(None).copy(nino = fakedFormNino)
      val coExecutorForms: CoExecutorForms = formWithMockedNinoValidation(coExecutor, mockCachingConnector)

      val form: Form[CoExecutor] = coExecutorForms.coExecutorPersonalDetailsForm(loginNino = CommonBuilder.DefaultNino).fill(coExecutor)

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCacheWithFailure(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate))

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = form.data.toSeq).withMethod("POST")

      val result = controller(coExecutorForms).onSubmit(None)(request)
      status(result) must be(INTERNAL_SERVER_ERROR)
    }
//
    "when update an existing co-executor with valid data return an internal server error if the storage fails" in {
      val coExec1 = CommonBuilder.buildCoExecutor copy (id = Some("1"), firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate copy(
        coExecutors = Seq(coExec1))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val coExecutor = CommonBuilder.buildCoExecutorWithId(Some("1")) copy (firstName = CommonBuilder.firstNameGenerator,
        lastName = CommonBuilder.surnameGenerator, nino = fakedFormNino)

      val coExecutorForms: CoExecutorForms = formWithMockedNinoValidation(coExecutor, mockCachingConnector)

      val form: Form[CoExecutor] = coExecutorForms.coExecutorPersonalDetailsForm(loginNino = CommonBuilder.DefaultNino).fill(coExecutor)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host,
        data = form.data.toSeq).withMethod("POST")

      createMockToStoreRegDetailsInCacheWithFailure(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate))

      val result = controller(coExecutorForms).onSubmit(Some("1"))(request)
      status(result) must be(INTERNAL_SERVER_ERROR)
    }

    def checkForErrorOnSubmissionOfModel(coExecutor: CoExecutor,expectedError: String): Unit = {
      val result = submitCoExecutorPersonalDetailsModel(
        CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate, coExecutor, None)
      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messagesApi(expectedError))
    }

    def prepareForm(coExecutor: CoExecutor): Form[CoExecutor] = {
      formWithMockedNinoValidation(coExecutor, mockCachingConnector).coExecutorPersonalDetailsForm(loginNino = CommonBuilder.DefaultNino).fill(coExecutor)
    }

    def submitCoExecutorPersonalDetailsModel(rd: RegistrationDetails, detailsToSubmit: CoExecutor,
                                             submissionId: Option[String]): Future[Result] = {
      val form = prepareForm(detailsToSubmit)
      submitCoExecutorPersonalDetails(rd, form.data.toSeq, submissionId)
    }

    def submitCoExecutorPersonalDetails(rd: RegistrationDetails, detailsToSubmit: Seq[(String, String)],
                                        submissionId: Option[String]): Future[Result] = {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rd))

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = detailsToSubmit).withMethod("POST")
      val coExecutorForms: CoExecutorForms = formWithMockedNinoValidationNoCoExecutor(mockCachingConnector)

      controller(coExecutorForms).onSubmit(submissionId)(request)
    }
  }
}
