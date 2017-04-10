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

package iht.controllers.registration.deceased

import iht.connector.CachingConnector
import iht.constants.IhtProperties
import iht.forms.registration.DeceasedForms._
import iht.metrics.Metrics
import iht.models.{DeceasedDateOfDeath, DeceasedDetails, RegistrationDetails}
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import org.joda.time.LocalDate
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._

class AboutDeceasedControllerTest
  extends RegistrationDeceasedControllerWithEditModeBehaviour[AboutDeceasedController] {

  def controller = new AboutDeceasedController {
   override val cachingConnector = mockCachingConnector
   override val authConnector = createFakeAuthConnector(isAuthorised=true)
   override val metrics:Metrics = mock[Metrics]
   override val isWhiteListEnabled = false
  }

  def controllerNotAuthorised = new AboutDeceasedController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val metrics:Metrics = mock[Metrics]
    override val isWhiteListEnabled = false
  }

  before {
    mockCachingConnector = mock[CachingConnector]
  }

  "AboutDeceasedController" must {

    behave like securedRegistrationDeceasedController()

    "respond with OK on page load" in {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails), Some(deceasedDetails))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onPageLoad()(
        createFakeRequestWithReferrer(referrerURL=referrerURL,host=host))
      status(result) shouldBe OK
    }

    "ensure date field is blank when page is first loaded" in {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(Some(CommonBuilder.buildDeceasedDateOfDeath), Some(applicantDetails),
        Some(DeceasedDetails(None, None, None, None, None, None, Some(CommonBuilder.DefaultDomicile), None, None)))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onPageLoad()(
        createFakeRequestWithReferrer(referrerURL=referrerURL,host=host))
      status(result) shouldBe OK
      contentAsString(result) should not include "value=\"1\""
      contentAsString(result) should not include "value=\"1\""
      contentAsString(result) should not include "value=\"1970\""
    }

    "contain Continue button when Page is loaded in normal mode" in {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails=CommonBuilder.buildRegistrationDetails copy (deceasedDetails=Some
        (deceasedDetails))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onPageLoad()(
        createFakeRequestWithReferrer(referrerURL=referrerURL,host=host))
      status(result) shouldBe OK

      contentAsString(result) should include(messagesApi("iht.continue"))
      contentAsString(result) should not include(messagesApi("site.link.cancel"))
    }

    "contain Continue and Cancel buttons when page is loaded in edit mode" in {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails=CommonBuilder.buildRegistrationDetails copy (deceasedDetails=Some
      (deceasedDetails))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onEditPageLoad()(createFakeRequestWithReferrer(referrerURL=referrerURL,host=host))
      status(result) shouldBe OK

      contentAsString(result) should include(messagesApi("iht.continue"))
      contentAsString(result) should include(messagesApi("site.link.cancel"))
    }

    "load the page with the fields filled from DB" in {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails), Some(deceasedDetails))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onPageLoad()(
        createFakeRequestWithReferrer(referrerURL=referrerURL,host=host))
      status(result) shouldBe OK
    }

    "respond appropriately to a submit with valid values in all fields" in  {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(None, None, Some(deceasedDetails))
      val deceasedDetailsForm1 = aboutDeceasedForm(LocalDate.now).fill(deceasedDetails)
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=deceasedDetailsForm1.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, registrationDetails)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onSubmit()(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should be (
        Some(iht.controllers.registration.deceased.routes.DeceasedAddressQuestionController.onPageLoad().url))
    }

    "load the page with pre-populated data" in {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(Some(CommonBuilder.buildDeceasedDateOfDeath), None,
        Some(deceasedDetails))
      val deceasedDetailsForm1 = aboutDeceasedForm(LocalDate.now).fill(deceasedDetails)
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=deceasedDetailsForm1.data.toSeq)
      val deceasedName = CommonBuilder.DefaultFirstName

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, registrationDetails)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(RegistrationDetails(None,None,
        Some(deceasedDetails))))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onPageLoad()(request)
      status(result) shouldBe OK
      contentAsString(result) should include (deceasedName)
    }

    "respond appropriately to an invalid submit: Missing mandatory fields" in {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails copy (firstName=None, lastName=None)
      val registrationDetails = RegistrationDetails(Some(CommonBuilder.buildDeceasedDateOfDeath),
        None, Some(deceasedDetails))
      val deceasedDetailsForm1 = aboutDeceasedForm(LocalDate.now).fill(deceasedDetails)
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=deceasedDetailsForm1.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, registrationDetails)
      createMockToGetRegDetailsFromCache(mockCachingConnector, None)
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = await(controller.onSubmit()(request))
      status(result) shouldBe BAD_REQUEST
      contentAsString(result) should include(messagesApi("error.firstName.give"))
    }

    "respond appropriately to an invalid submit: Invalid NINO format" in {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails copy (nino=Some("12345678"))
      val registrationDetails = RegistrationDetails(Some(CommonBuilder.buildDeceasedDateOfDeath),
        None, Some(deceasedDetails))
      val deceasedDetailsForm1 = aboutDeceasedForm(LocalDate.now).fill(deceasedDetails)
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=deceasedDetailsForm1.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, registrationDetails)
      createMockToGetRegDetailsFromCache(mockCachingConnector, None)
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = await(controller.onSubmit()(request))
      status(result) shouldBe BAD_REQUEST
      contentAsString(result) should include(messagesApi("error.nino.giveUsingOnlyLettersAndNumbers"))
    }

    "respond appropriately to an invalid submit: date of birth after date of death" in {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails copy (
        dateOfBirth = Some(new LocalDate(2015, 12, 12)))
      val registrationDetails = RegistrationDetails(Some(CommonBuilder.buildDeceasedDateOfDeath), None, None)
      val deceasedDetailsForm1 = aboutDeceasedForm(
        CommonBuilder.buildDeceasedDateOfDeath.dateOfDeath).fill(deceasedDetails)
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=deceasedDetailsForm1.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, registrationDetails)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onSubmit()(request)
      status(result) shouldBe BAD_REQUEST
      contentAsString(result) should include(messagesApi("error.deceasedDateOfBirth.giveBeforeDateOfDeath"))
    }

    "respond with an internal server error to a submit with valid values in all fields but the storage fails" in  {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(None, None, Some(deceasedDetails))
      val deceasedDetailsForm1 = aboutDeceasedForm(LocalDate.now).fill(deceasedDetails)
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=deceasedDetailsForm1.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, registrationDetails)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCacheWithFailure(mockCachingConnector, Some(registrationDetails))

      val result = controller.onSubmit()(request)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "respond appropriately to a submit in edit mode with valid values in all fields" in  {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(None, None, Some(deceasedDetails))
      val deceasedDetailsForm1 = aboutDeceasedForm(LocalDate.now).fill(deceasedDetails)
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=deceasedDetailsForm1.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, registrationDetails)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onEditSubmit()(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should be (Some(iht.controllers.registration.routes.RegistrationSummaryController.onPageLoad().url))
    }

    "respond appropriately to an invalid submit in edit mode: Missing mandatory fields" in {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails copy (firstName=None, lastName=None)
      val registrationDetails = RegistrationDetails(Some(CommonBuilder.buildDeceasedDateOfDeath), None, Some(deceasedDetails))
      val deceasedDetailsForm1 = aboutDeceasedForm(LocalDate.now).fill(deceasedDetails)
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=deceasedDetailsForm1.data.toSeq)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, registrationDetails)
      createMockToGetRegDetailsFromCache(mockCachingConnector, None)
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = await(controller.onEditSubmit()(request))
      status(result) shouldBe BAD_REQUEST
      contentAsString(result) should include(messagesApi("error.firstName.give"))
    }

    "save valid data correctly when coming to this screen for the first time" in {
      val existingDod = DeceasedDateOfDeath(new LocalDate(2014, 1, 1))
      val existingDeceasedDetails = DeceasedDetails(domicile = Some(IhtProperties.domicileEnglandOrWales))
      val existingRegistrationDetails = RegistrationDetails(Some(existingDod), None, Some(existingDeceasedDetails))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(existingRegistrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(existingRegistrationDetails))

      val newDetails = DeceasedDetails(Some(CommonBuilder.firstNameGenerator), None, Some(CommonBuilder.surnameGenerator),
        Some(CommonBuilder.DefaultNino), None, Some(new LocalDate(1980, 2, 2)), None, Some(IhtProperties.statusMarried))

      val form = aboutDeceasedForm().fill(newDetails)

      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=form.data.toSeq)

      val result = controller.onSubmit()(request)
      status(result) shouldBe SEE_OTHER
      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      capturedValue.deceasedDateOfDeath shouldBe Some(existingDod)
      capturedValue.deceasedDetails shouldBe Some(newDetails copy (domicile = existingDeceasedDetails.domicile))
    }


    "save valid data correctly when returning to this screen" in {
      val existingDod = DeceasedDateOfDeath(new LocalDate(2014, 1, 1))
      val existingDeceasedDetails = DeceasedDetails(Some(CommonBuilder.firstNameGenerator), None,
        Some(CommonBuilder.surnameGenerator), Some(CommonBuilder.DefaultNino), None,
        Some(new LocalDate(1980, 2, 2)), Some(IhtProperties.domicileEnglandOrWales), Some(IhtProperties.statusMarried))
      val existingRegistrationDetails = RegistrationDetails(Some(existingDod), None, Some(existingDeceasedDetails))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(existingRegistrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(existingRegistrationDetails))

      val newDetails = DeceasedDetails(Some(CommonBuilder.firstNameGenerator), None, Some(CommonBuilder.surnameGenerator),
        Some(CommonBuilder.DefaultNino), None, Some(new LocalDate(1990, 3, 3)), None, Some(IhtProperties.statusMarried))

      val form = aboutDeceasedForm().fill(newDetails)

      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=form.data.toSeq)

      val result = controller.onEditSubmit()(request)
      status(result) shouldBe SEE_OTHER
      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      capturedValue.deceasedDateOfDeath shouldBe Some(existingDod)
      capturedValue.deceasedDetails shouldBe Some(newDetails copy (domicile = existingDeceasedDetails.domicile))
    }

  }
}
