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

package iht.controllers.registration.deceased

import iht.config.AppConfig
import iht.connector.CachingConnector
import iht.controllers.registration.RegistrationControllerTest
import iht.forms.registration.DeceasedForms
import iht.models.{DeceasedDateOfDeath, DeceasedDetails, RegistrationDetails}
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import org.joda.time.LocalDate
import org.scalatest.BeforeAndAfter
import play.api.data.format.Formatter
import play.api.data.{FieldMapping, Form, FormError, Forms}
import play.api.mvc.{MessagesControllerComponents, Request}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}


class AboutDeceasedControllerTest extends RegistrationControllerTest with BeforeAndAfter {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with AboutDeceasedController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def controller(deceasedForms2:DeceasedForms) = new TestController {
   override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector

   override def deceasedForms = deceasedForms2
   override def checkGuardCondition(registrationDetails: RegistrationDetails, id: String): Boolean = true
   override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def controllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector

    override def deceasedForms = DeceasedForms
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def formWithMockedNinoValidation(deceased: DeceasedDetails, mockCachingConnector: CachingConnector): DeceasedForms = {
    def deceasedForms: DeceasedForms = {
      new DeceasedForms {
      }
    }
    deceasedForms
  }

  def formWithMockedNinoValidationNoDeceased(mockCachingConnector: CachingConnector): DeceasedForms = {
    def deceasedForms: DeceasedForms = {
      new DeceasedForms {
        def ninoForDeceased(blankMessageKey: String, lengthMessageKey: String,
                            formatMessageKey: String, oRegDetails: Option[RegistrationDetails])(
                             implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): FieldMapping[String] = {
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
    implicit val request = createFakeRequestWithReferrer(referrerURL = referrerURL, host = host)
    implicit val hc = new HeaderCarrier()
    deceasedForms
  }

  val fakedFormNino = "SR000009C"

  "AboutDeceasedController" must {

    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = controllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to GG login page on Submit if the user is not logged in" in {
      val result = controllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to GG login page on PageLoad in edit mode if the user is not logged in" in {
      val result = controllerNotAuthorised.onEditPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to GG login page on Submit in edit mode if the user is not logged in" in {
      val result = controllerNotAuthorised.onEditSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails), Some(deceasedDetails))
      val deceasedForms = formWithMockedNinoValidationNoDeceased(mockCachingConnector)

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller(deceasedForms).onPageLoad()(createFakeRequestWithReferrer(referrerURL=referrerURL,host=host))
      status(result) mustBe OK
    }

    "ensure date field is blank when page is first loaded" in {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(Some(CommonBuilder.buildDeceasedDateOfDeath), Some(applicantDetails),
        Some(DeceasedDetails(None, None, None, None, None, None, Some(CommonBuilder.DefaultDomicile), None, None)))
      val deceasedForms = formWithMockedNinoValidationNoDeceased(mockCachingConnector)

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller(deceasedForms).onPageLoad()(
        createFakeRequestWithReferrer(referrerURL=referrerURL,host=host))
      status(result) mustBe OK
      contentAsString(result) must not include "value=\"1\""
      contentAsString(result) must not include "value=\"1\""
      contentAsString(result) must not include "value=\"1970\""
    }

    "contain Continue button when Page is loaded in normal mode" in {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails=CommonBuilder.buildRegistrationDetails copy (deceasedDetails=Some
        (deceasedDetails))
      val deceasedForms = formWithMockedNinoValidationNoDeceased(mockCachingConnector)

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller(deceasedForms).onPageLoad()(
        createFakeRequestWithReferrer(referrerURL=referrerURL,host=host))
      status(result) mustBe OK

      contentAsString(result) must include(messagesApi("iht.continue"))
      contentAsString(result) must not include(messagesApi("site.link.cancel"))
    }

    "contain Continue and Cancel buttons when page is loaded in edit mode" in {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails=CommonBuilder.buildRegistrationDetails copy (deceasedDetails=Some
      (deceasedDetails))
      val deceasedForms = formWithMockedNinoValidation(deceasedDetails,mockCachingConnector)

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller(deceasedForms).onEditPageLoad()(createFakeRequestWithReferrer(referrerURL=referrerURL,host=host))
      status(result) mustBe OK

      contentAsString(result) must include(messagesApi("iht.continue"))
      contentAsString(result) must include(messagesApi("site.link.cancel"))
    }

    "load the page with the fields filled from DB" in {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails), Some(deceasedDetails))
      val deceasedForms = formWithMockedNinoValidation(deceasedDetails,mockCachingConnector)

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller(deceasedForms).onPageLoad()(
        createFakeRequestWithReferrer(referrerURL=referrerURL,host=host))
      status(result) mustBe OK
    }

    "respond appropriately to a submit with valid values in all fields" in {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails.copy(nino = Some(fakedFormNino))
      val registrationDetails = RegistrationDetails(None, None, Some(deceasedDetails))
      val deceasedDetailsForm1 = formWithMockedNinoValidation(deceasedDetails, mockCachingConnector)

      implicit val req = createFakeRequestWithReferrer(referrerURL = referrerURL, host = host)
      implicit val hc = new HeaderCarrier()
      val form: Form[DeceasedDetails] = deceasedDetailsForm1.aboutDeceasedForm(loginNino = CommonBuilder.DefaultNino).fill(deceasedDetails)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = controller(deceasedDetailsForm1).onSubmit()(createFakeRequestWithReferrerWithBody(
        referrerURL = referrerURL, host = host, data = form.data.toSeq))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be(
        Some(iht.controllers.registration.deceased.routes.DeceasedAddressQuestionController.onPageLoad().url))
    }

    "respond appropriately to a submit with valid values in all fields but the NINO is the same as the login NINO" in  {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails.copy(nino = Some(CommonBuilder.DefaultNino))
      val registrationDetails = RegistrationDetails(None, None, Some(deceasedDetails))
      val deceasedDetailsForm1 = formWithMockedNinoValidation(deceasedDetails,mockCachingConnector)

      implicit val req = createFakeRequestWithReferrer(referrerURL = referrerURL, host = host)
      implicit val hc = new HeaderCarrier()
      val form: Form[DeceasedDetails] = deceasedDetailsForm1.aboutDeceasedForm(loginNino = CommonBuilder.DefaultNino).fill(deceasedDetails)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = controller(deceasedDetailsForm1).onSubmit()(createFakeRequestWithReferrerWithBody(
        referrerURL=referrerURL,host=host,data=form.data.toSeq))
      status(result) mustBe BAD_REQUEST
    }

    "load the page with pre-populated data" in {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails.copy(nino = Some(fakedFormNino))
      val registrationDetails = RegistrationDetails(Some(CommonBuilder.buildDeceasedDateOfDeath), None,
        Some(deceasedDetails))
      val deceasedName = CommonBuilder.DefaultFirstName
      val deceasedForms = formWithMockedNinoValidation(deceasedDetails,mockCachingConnector)

      implicit val req = createFakeRequestWithReferrer(referrerURL = referrerURL, host = host)
      implicit val hc = new HeaderCarrier()
      val form: Form[DeceasedDetails] = deceasedForms.aboutDeceasedForm(loginNino = CommonBuilder.DefaultNino).fill(deceasedDetails)

      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=form.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(RegistrationDetails(None,None,
        Some(deceasedDetails))))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = controller(deceasedForms).onPageLoad()(request)
      status(result) mustBe OK
      contentAsString(result) must include (deceasedName)
    }

    "respond with an internal server error to a submit with valid values in all fields but the storage fails" in  {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails.copy(nino = Some(fakedFormNino))
      val registrationDetails = RegistrationDetails(None, None, Some(deceasedDetails))
      val deceasedForms = formWithMockedNinoValidation(deceasedDetails,mockCachingConnector)

      implicit val req = createFakeRequestWithReferrer(referrerURL = referrerURL, host = host)
      implicit val hc = new HeaderCarrier()
      val form: Form[DeceasedDetails] = deceasedForms.aboutDeceasedForm(loginNino = CommonBuilder.DefaultNino).fill(deceasedDetails)

      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=form.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCacheWithFailure(mockCachingConnector, Some(registrationDetails))

      val result = controller(deceasedForms).onSubmit()(request)
      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "respond appropriately to a submit in edit mode with valid values in all fields" in  {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails.copy(nino = Some(fakedFormNino))
      val registrationDetails = RegistrationDetails(None, None, Some(deceasedDetails))
      val deceasedForms = formWithMockedNinoValidation(deceasedDetails,mockCachingConnector)

      implicit val req = createFakeRequestWithReferrer(referrerURL = referrerURL, host = host)
      implicit val hc = new HeaderCarrier()
      val form: Form[DeceasedDetails] = deceasedForms.aboutDeceasedForm(loginNino = CommonBuilder.DefaultNino).fill(deceasedDetails)

      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=form.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = controller(deceasedForms).onEditSubmit()(request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be (Some(iht.controllers.registration.routes.RegistrationSummaryController.onPageLoad().url))
    }

    "respond appropriately to an invalid submit in edit mode: Missing mandatory fields" in {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails copy (firstName=None, lastName=None)
      val registrationDetails = RegistrationDetails(Some(CommonBuilder.buildDeceasedDateOfDeath), None, Some(deceasedDetails))
      val deceasedForms = formWithMockedNinoValidation(deceasedDetails,mockCachingConnector)

      implicit val req = createFakeRequestWithReferrer(referrerURL = referrerURL, host = host)
      implicit val hc = new HeaderCarrier()
      val form: Form[DeceasedDetails] = deceasedForms.aboutDeceasedForm(loginNino = CommonBuilder.DefaultNino).fill(deceasedDetails)

      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=form.data.toSeq)

      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))
      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, None)

      val result = await(controller(deceasedForms).onEditSubmit()(request))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) must include(messagesApi("error.firstName.give"))
    }

    "save valid data correctly when coming to this screen for the first time" in {
      val existingDod = DeceasedDateOfDeath(new LocalDate(2014, 1, 1))
      val existingDeceasedDetails = DeceasedDetails(domicile = Some(mockAppConfig.domicileEnglandOrWales))
      val existingRegistrationDetails = RegistrationDetails(Some(existingDod), None, Some(existingDeceasedDetails))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(existingRegistrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(existingRegistrationDetails))

      val newDetails = DeceasedDetails(Some(CommonBuilder.firstNameGenerator), None, Some(CommonBuilder.surnameGenerator),
        Some(fakedFormNino), None, Some(new LocalDate(1980, 2, 2)), None, Some(mockAppConfig.statusMarried))

      val deceasedForms = formWithMockedNinoValidation(newDetails, mockCachingConnector)

      implicit val req = createFakeRequestWithReferrer(referrerURL = referrerURL, host = host)
      implicit val hc = new HeaderCarrier()
      val form: Form[DeceasedDetails] = deceasedForms.aboutDeceasedForm(loginNino = CommonBuilder.DefaultNino).fill(newDetails)

      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=form.data.toSeq)

      val result = controller(deceasedForms).onSubmit()(request)
      status(result) mustBe SEE_OTHER
      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      capturedValue.deceasedDateOfDeath mustBe Some(existingDod)
      capturedValue.deceasedDetails mustBe Some(newDetails copy (domicile = existingDeceasedDetails.domicile))
    }


    "save valid data correctly when returning to this screen" in {
      val existingDod = DeceasedDateOfDeath(new LocalDate(2014, 1, 1))
      val existingDeceasedDetails = DeceasedDetails(Some(CommonBuilder.firstNameGenerator), None,
        Some(CommonBuilder.surnameGenerator), Some(CommonBuilder.DefaultNino), None,
        Some(new LocalDate(1980, 2, 2)), Some(mockAppConfig.domicileEnglandOrWales), Some(mockAppConfig.statusMarried))
      val existingRegistrationDetails = RegistrationDetails(Some(existingDod), None, Some(existingDeceasedDetails))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(existingRegistrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(existingRegistrationDetails))

      val newDetails = DeceasedDetails(Some(CommonBuilder.firstNameGenerator), None, Some(CommonBuilder.surnameGenerator),
        Some(fakedFormNino), None, Some(new LocalDate(1990, 3, 3)), None, Some(mockAppConfig.statusMarried))

      val deceasedForms: DeceasedForms = formWithMockedNinoValidation(newDetails, mockCachingConnector)

      implicit val req = createFakeRequestWithReferrer(referrerURL = referrerURL, host = host)
      implicit val hc = new HeaderCarrier()
      val form: Form[DeceasedDetails] = deceasedForms.aboutDeceasedForm(loginNino = CommonBuilder.DefaultNino).fill(newDetails)

      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=form.data.toSeq)

      val result = controller(deceasedForms).onEditSubmit()(request)
      status(result) mustBe SEE_OTHER
      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      capturedValue.deceasedDateOfDeath mustBe Some(existingDod)
      capturedValue.deceasedDetails mustBe Some(newDetails copy (domicile = existingDeceasedDetails.domicile))
    }

  }
}
