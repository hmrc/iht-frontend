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
import iht.controllers.registration.{RegistrationControllerTest, routes => registrationRoutes}
import iht.forms.registration.ApplicantForms._
import iht.models.{ApplicantDetails, UkAddress}
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._

class ApplicantAddressControllerTest extends RegistrationControllerTest  {

  var maxLength = 0

  before {
    mockCachingConnector = mock[CachingConnector]
    maxLength = IhtProperties.validationMaxLengthAddresslines.toInt
  }

  // Create controller object and pass in mock.
  def controller = new ApplicantAddressController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised=true)
    override val isWhiteListEnabled = false
  }

  def controllerNotAuthorised = new ApplicantAddressController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val isWhiteListEnabled = false
  }

  def registrationDetailsWithUkApplicant =
    CommonBuilder.buildRegistrationDetails copy (applicantDetails = Some(ApplicantDetails(doesLiveInUK = Some(true),
      phoneNo = Some("SomeText"))))

  def registrationDetailsWithApplicantAbroad =
    CommonBuilder.buildRegistrationDetails copy (applicantDetails = Some(ApplicantDetails(doesLiveInUK = Some(false),
      phoneNo = Some("SomeText"))))

  def ukAddress = UkAddress("UK Line 1", "UK Line 2", None, None, "AA1 1AA")
  def addressAbroad = UkAddress("Abroad Line 1", "Abroad Line 2", Some("Abroad Line 3"),
    Some("Abroad Line 4"), "", "US")

  def applicantWithUkAddress = ApplicantDetails(doesLiveInUK = Some(true), ukAddress = Some(ukAddress),
    phoneNo = Some("SomeText"))
  def applicantWithAddressAbroad = ApplicantDetails(doesLiveInUK = Some(false), ukAddress = Some(addressAbroad),
    phoneNo = Some("SomeText"))

  def registrationDetailsWithUkApplicantPopulated =
    CommonBuilder.buildRegistrationDetails copy (applicantDetails = Some(applicantWithUkAddress))

  def registrationDetailsWithApplicantAbroadPopulated =
    CommonBuilder.buildRegistrationDetails copy (applicantDetails = Some(applicantWithAddressAbroad))

  "ApplicantAddressController" must {

    "redirect to GG login page on PageLoad of a Uk address if the user is not logged in" in {
      val result = controllerNotAuthorised.onPageLoadUk(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to GG login page on Submit of a Uk address if the user is not logged in" in {
      val result = controllerNotAuthorised.onSubmitUk(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to GG login page on PageLoad of an address abroad if the user is not logged in" in {
      val result = controllerNotAuthorised.onPageLoadAbroad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to GG login page on Submit of an address abroad if the user is not logged in" in {
      val result = controllerNotAuthorised.onSubmitAbroad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to GG login page on PageLoad of a Uk address in edit mode if the user is not logged in" in {
      val result = controllerNotAuthorised.onEditPageLoadUk(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to GG login page on Submit of a Uk address if in edit mode the user is not logged in" in {
      val result = controllerNotAuthorised.onEditSubmitUk(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to GG login page on PageLoad of an address abroad in edit mode if the user is not logged in" in {
      val result = controllerNotAuthorised.onEditPageLoadAbroad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to GG login page on Submit of an address abroad in edit mode if the user is not logged in" in {
      val result = controllerNotAuthorised.onEditSubmitAbroad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "load when visited for the first time and applicant lives in the UK" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithUkApplicant))

      val result = controller.onPageLoadUk(createFakeRequest())

      status(result) should be(OK)
      contentAsString(result) should include(messagesApi("page.iht.registration.applicantAddress.title"))
      contentAsString(result) should include(messagesApi("page.iht.registration.applicantAddress.hint"))
      contentAsString(result) should include(messagesApi("iht.address.line1"))
      contentAsString(result) should include(messagesApi("iht.address.line2"))
      contentAsString(result) should include(messagesApi("iht.address.line3"))
      contentAsString(result) should include(messagesApi("iht.address.line4"))
      contentAsString(result) should include(messagesApi("iht.postcode"))
      contentAsString(result) should include(messagesApi("iht.registration.changeAddressToAbroad"))
      contentAsString(result) should include(routes.ApplicantAddressController.onPageLoadAbroad().url)
    }

    "load when revisited and applicant lives in the UK" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithUkApplicantPopulated))

      val result = controller.onPageLoadUk(createFakeRequest())

      status(result) should be(OK)
      contentAsString(result) should include("UK Line 1")
      contentAsString(result) should include("UK Line 2")
      contentAsString(result) should include("AA1 1AA")
    }

    "load when revisited in edit mode and applicant lives in the UK" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithUkApplicantPopulated))

      val result = controller.onEditPageLoadUk(createFakeRequest())

      status(result) should be(OK)
      contentAsString(result) should include("UK Line 1")
      contentAsString(result) should include("UK Line 2")
      contentAsString(result) should include("AA1 1AA")
    }

    "load when visited for the first time and applicant lives abroad" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroad))

      val result = controller.onPageLoadAbroad(createFakeRequest())

      status(result) should be(OK)
      contentAsString(result) should include(messagesApi("page.iht.registration.applicantAddress.title"))
      contentAsString(result) should include(messagesApi("page.iht.registration.applicantAddress.hint"))
      contentAsString(result) should include(messagesApi("iht.address.line1"))
      contentAsString(result) should include(messagesApi("iht.address.line2"))
      contentAsString(result) should include(messagesApi("iht.address.line3"))
      contentAsString(result) should include(messagesApi("iht.address.line4"))
      contentAsString(result) should include(messagesApi("iht.country"))
      contentAsString(result) should include(messagesApi("iht.registration.changeAddressToUK"))
      contentAsString(result) should include(routes.ApplicantAddressController.onPageLoadUk().url)
    }

    "load when revisited and applicant lives abroad" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroadPopulated))

      val result = controller.onPageLoadAbroad(createFakeRequest())

      status(result) should be(OK)
      contentAsString(result) should include("Abroad Line 1")
      contentAsString(result) should include("Abroad Line 2")
      contentAsString(result) should include("Abroad Line 3")
      contentAsString(result) should include("Abroad Line 4")
      contentAsString(result) should include("US")
    }

    "load when revisited in edit mode and applicant lives abroad" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroadPopulated))

      val result = controller.onEditPageLoadAbroad(createFakeRequest())

      status(result) should be(OK)
      contentAsString(result) should include("Abroad Line 1")
      contentAsString(result) should include("Abroad Line 2")
      contentAsString(result) should include("Abroad Line 3")
      contentAsString(result) should include("Abroad Line 4")
      contentAsString(result) should include("US")
    }

    "forget address details when changing from UK to abroad" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithUkApplicantPopulated))

      val result = controller.onPageLoadAbroad(createFakeRequest())

      status(result) should be(OK)
      contentAsString(result) shouldNot include("UK Line 1")
      contentAsString(result) shouldNot include("UK Line 2")
      contentAsString(result) shouldNot include("AA1 1AA")
    }

    "forget address details when changing from abroad to UK" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroadPopulated))

      val result = controller.onPageLoadUk(createFakeRequest())

      status(result) should be(OK)
      contentAsString(result) shouldNot include("Abroad Line 1")
      contentAsString(result) shouldNot include("Abroad Line 2")
      contentAsString(result) shouldNot include("Abroad Line 3")
      contentAsString(result) shouldNot include("Abroad Line 4")
      contentAsString(result) shouldNot include("GB")
    }

    "forget address details when changing from UK to abroad in edit mode" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithUkApplicantPopulated))

      val result = controller.onEditPageLoadAbroad(createFakeRequest())

      status(result) should be(OK)
      contentAsString(result) shouldNot include("UK Line 1")
      contentAsString(result) shouldNot include("UK Line 2")
      contentAsString(result) shouldNot include("AA1 1AA")
    }

    "forget address details when changing from abroad to UK in edit mode" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroadPopulated))

      val result = controller.onEditPageLoadUk(createFakeRequest())

      status(result) should be(OK)
      contentAsString(result) shouldNot include("Abroad Line 1")
      contentAsString(result) shouldNot include("Abroad Line 2")
      contentAsString(result) shouldNot include("Abroad Line 3")
      contentAsString(result) shouldNot include("Abroad Line 4")
      contentAsString(result) shouldNot include("GB")
    }

    "save and redirect correctly on submit when saving a UK address" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroad))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroad))

      val form = applicantAddressUkForm.fill(ukAddress)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host,
        data = form.data.toSeq)

      val result = controller.onSubmitUk(request)
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(
        Some(iht.controllers.registration.executor.routes.OthersApplyingForProbateController.onPageLoad.url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val applicant = capturedValue.applicantDetails.get
      applicant.ukAddress shouldBe Some(ukAddress copy (countryCode = IhtProperties.ukIsoCountryCode))
      applicant.doesLiveInUK shouldBe Some(true)
    }

    "save and redirect correctly on submit when saving an address abroad" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroad))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroad))

      val form = applicantAddressAbroadForm.fill(addressAbroad)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host,
        data = form.data.toSeq)

      val result = controller.onSubmitAbroad(request)
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(
        Some(iht.controllers.registration.executor.routes.OthersApplyingForProbateController.onPageLoad.url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val applicant = capturedValue.applicantDetails.get
      applicant.ukAddress shouldBe Some(addressAbroad)
      applicant.doesLiveInUK shouldBe Some(false)
    }

    "save and redirect correctly on submit in edit mode when saving a UK address" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroad))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroad))

      val form = applicantAddressUkForm.fill(ukAddress)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host, data = form.data.toSeq)

      val result = controller.onEditSubmitUk(request)
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(registrationRoutes.RegistrationSummaryController.onPageLoad().url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val applicant = capturedValue.applicantDetails.get
      applicant.ukAddress shouldBe Some(ukAddress copy (countryCode = IhtProperties.ukIsoCountryCode))
      applicant.doesLiveInUK shouldBe Some(true)
    }

    "save and redirect correctly on submit in edit mode when saving an address abroad" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroad))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroad))

      val form = applicantAddressAbroadForm.fill(addressAbroad)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host, data = form.data.toSeq)

      val result = controller.onEditSubmitAbroad(request)
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(registrationRoutes.RegistrationSummaryController.onPageLoad().url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val applicant = capturedValue.applicantDetails.get
      applicant.ukAddress shouldBe Some(addressAbroad)
      applicant.doesLiveInUK shouldBe Some(false)
    }

    "raise an error when accessing the screen for a UK address without first entering applicant details" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails))

      intercept[Exception] {
        await(controller.onPageLoadUk(createFakeRequest()))
      }
    }

    "raise an error when accessing the screen for an address abroad without first entering applicant details" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails))

      intercept[Exception] {
        await(controller.onPageLoadAbroad(createFakeRequest()))
      }
    }

    "raise an error when submitting the screen for a UK address without first entering applicant details" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails))

      val form = applicantAddressUkForm.fill(ukAddress)
      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host,
        data = form.data.toSeq)

      intercept[Exception] {
        await(controller.onSubmitUk(request))
      }
    }

    "raise an error when submitting the screen for an address abroad without first entering applicant details" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails))

      val form = applicantAddressAbroadForm.fill(addressAbroad)
      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host,
        data = form.data.toSeq)

      intercept[Exception] {
        await(controller.onSubmitAbroad(createFakeRequest()))
      }
    }

    "show an error when submitting a UK address when address line 1 is blank" in {
      val address = ukAddress copy (ukAddressLine1 = "")
      checkForErrorOnSubmissionOfModel(address, isInternational = false, "error.address.give")
    }

    "show an error when submitting a UK address when address line 1 is too long" in {
      val address = ukAddress copy (ukAddressLine1 = "X" * (maxLength + 1))
      checkForErrorOnSubmissionOfModel(address, isInternational = false, "error.address.giveUsing35CharsOrLess")
    }

    "show an error when submitting a UK address when address line 2 is blank" in {
      val address = ukAddress copy (ukAddressLine2 = "")
      checkForErrorOnSubmissionOfModel(address, isInternational = false, "error.address.give")
    }

    "show an error when submitting a UK address when address line 2 is too long" in {
      val address = ukAddress copy (ukAddressLine2 = "X" * (maxLength + 1))
      checkForErrorOnSubmissionOfModel(address, isInternational = false, "error.address.giveUsing35CharsOrLess")
    }

    "show an error when submitting a UK address when address line 3 is too long" in {
      val address = ukAddress copy (ukAddressLine3 = Some("X" * (maxLength + 1)))
      checkForErrorOnSubmissionOfModel(address, isInternational = false, "error.address.giveUsing35CharsOrLess")
    }

    "show an error when submitting a UK address when address line 4 is too long" in {
      val address = ukAddress copy (ukAddressLine4 = Some("X" * (maxLength + 1)))
      checkForErrorOnSubmissionOfModel(address, isInternational = false, "error.address.giveUsing35CharsOrLess")
    }

    "show an error when submitting a UK address when postcode is blank" in {
      val address = ukAddress copy (postCode = "")
      checkForErrorOnSubmissionOfModel(address, isInternational = false, "error.address.givePostcode")
    }

    "show an error when submitting a UK address when postcode is invalid" in {
      val address = ukAddress copy (postCode = "INVALID")
      checkForErrorOnSubmissionOfModel(address, isInternational = false, "error.address.givePostcodeUsingNumbersAndLetters")
    }

    "show an error when submitting an address abroad when address line 1 is blank" in {
      val address = addressAbroad copy (ukAddressLine1 = "")
      checkForErrorOnSubmissionOfModel(address, isInternational = true, "error.address.give")
    }

    "show an error when submitting an address abroad when address line 1 is too long" in {
      val address = addressAbroad copy (ukAddressLine1 = "X" * (maxLength + 1))
      checkForErrorOnSubmissionOfModel(address, isInternational = true, "error.address.giveUsing35CharsOrLess")
    }

    "show an error when submitting an address abroad when address line 2 is blank" in {
      val address = addressAbroad copy (ukAddressLine2 = "")
      checkForErrorOnSubmissionOfModel(address, isInternational = true, "error.address.give")
    }

    "show an error when submitting an address abroad when address line 2 is too long" in {
      val address = addressAbroad copy (ukAddressLine2 = "X" * (maxLength + 1))
      checkForErrorOnSubmissionOfModel(address, isInternational = true, "error.address.giveUsing35CharsOrLess")
    }

    "show an error when submitting an address abroad when address line 3 is too long" in {
      val address = addressAbroad copy (ukAddressLine3 = Some("X" * (maxLength + 1)))
      checkForErrorOnSubmissionOfModel(address, isInternational = true, "error.address.giveUsing35CharsOrLess")
    }

    "show an error when submitting an address abroad when address line 4 is too long" in {
      val address = addressAbroad copy (ukAddressLine4 = Some("X" * (maxLength + 1)))
      checkForErrorOnSubmissionOfModel(address, isInternational = true, "error.address.giveUsing35CharsOrLess")
    }

    "show an error when submitting an address abroad when country code is blank" in {
      val address = addressAbroad copy (countryCode = "")
      checkForErrorOnSubmissionOfModel(address, isInternational = true, "error.country.select")
    }

    "show an error when submitting an address abroad when country code is invalid" in {
      val address = addressAbroad copy (countryCode = "INVALID")
      checkForErrorOnSubmissionOfModel(address, isInternational = true, "error.country.select")
    }

    def checkForErrorOnSubmissionOfModel(address: UkAddress, isInternational: Boolean, expectedError: String)
    = {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithUkApplicant))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetailsWithUkApplicant))

      val form = if (isInternational) applicantAddressAbroadForm.fill(address) else applicantAddressUkForm.fill(address)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host,
        data = form.data.toSeq)

      val result = if (isInternational) controller.onSubmitAbroad(request) else controller.onSubmitUk(request)
      status(result) should be(BAD_REQUEST)
      contentAsString(result) should include(messagesApi(expectedError))
    }

    "After a submit, when submitting an address, if the storage operation fails the result must be a server error" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroad))
      createMockToStoreRegDetailsInCacheWithFailure(mockCachingConnector, Some(registrationDetailsWithApplicantAbroad))

      val form = applicantAddressUkForm.fill(ukAddress)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host,
        data = form.data.toSeq)

      val result = controller.onSubmitUk(request)
      status(result) should be(INTERNAL_SERVER_ERROR)
    }
  }
}
