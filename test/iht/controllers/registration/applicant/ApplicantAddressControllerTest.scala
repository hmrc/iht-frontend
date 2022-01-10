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

package iht.controllers.registration.applicant

import iht.config.AppConfig
import iht.controllers.registration.{RegistrationControllerTest, routes => registrationRoutes}
import iht.forms.registration.ApplicantForms._
import iht.models.{ApplicantDetails, UkAddress}
import iht.testhelpers.CommonBuilder
import iht.views.html.registration.applicant.applicant_address
import play.api.i18n.{Lang, Messages}
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class ApplicantAddressControllerTest extends RegistrationControllerTest  {

  lazy val maxLength = mockAppConfig.validationMaxLengthAddresslines.toInt

  implicit val messages: Messages = mockControllerComponents.messagesApi.preferred(Seq(Lang.defaultLang)).messages
  protected abstract class TestController extends FrontendController(mockControllerComponents) with ApplicantAddressController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val applicantAddressView: applicant_address = app.injector.instanceOf[applicant_address]
  }

  // Create controller object and pass in mock.
  def controller = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
  }

  def controllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
  }

  def registrationDetailsWithUkApplicant =
    CommonBuilder.buildRegistrationDetails copy (applicantDetails = Some(ApplicantDetails(doesLiveInUK = Some(true),
      phoneNo = Some("SomeText"), role = Some(mockAppConfig.roleLeadExecutor))))

  def registrationDetailsWithApplicantAbroad =
    CommonBuilder.buildRegistrationDetails copy (applicantDetails = Some(ApplicantDetails(doesLiveInUK = Some(false),
      phoneNo = Some("SomeText"), role = Some(mockAppConfig.roleLeadExecutor))))

  def ukAddress = UkAddress("UK Line 1", "UK Line 2", None, None, "AA1 1AA")
  def addressAbroad = UkAddress("Abroad Line 1", "Abroad Line 2", Some("Abroad Line 3"),
    Some("Abroad Line 4"), "", "US")

  def applicantWithUkAddress = ApplicantDetails(doesLiveInUK = Some(true), ukAddress = Some(ukAddress),
    phoneNo = Some("SomeText"), role = Some(mockAppConfig.roleLeadExecutor))
  def applicantWithAddressAbroad = ApplicantDetails(doesLiveInUK = Some(false), ukAddress = Some(addressAbroad),
    phoneNo = Some("SomeText"), role = Some(mockAppConfig.roleLeadExecutor))

  def registrationDetailsWithUkApplicantPopulated =
    CommonBuilder.buildRegistrationDetails copy (applicantDetails = Some(applicantWithUkAddress))

  def registrationDetailsWithApplicantAbroadPopulated =
    CommonBuilder.buildRegistrationDetails copy (applicantDetails = Some(applicantWithAddressAbroad))

  "ApplicantAddressController" must {

    "redirect to GG login page on PageLoad of a Uk address if the user is not logged in" in {
      val result = controllerNotAuthorised.onPageLoadUk(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to GG login page on Submit of a Uk address if the user is not logged in" in {
      val result = controllerNotAuthorised.onSubmitUk(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to GG login page on PageLoad of an address abroad if the user is not logged in" in {
      val result = controllerNotAuthorised.onPageLoadAbroad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to GG login page on Submit of an address abroad if the user is not logged in" in {
      val result = controllerNotAuthorised.onSubmitAbroad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to GG login page on PageLoad of a Uk address in edit mode if the user is not logged in" in {
      val result = controllerNotAuthorised.onEditPageLoadUk(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to GG login page on Submit of a Uk address if in edit mode the user is not logged in" in {
      val result = controllerNotAuthorised.onEditSubmitUk(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to GG login page on PageLoad of an address abroad in edit mode if the user is not logged in" in {
      val result = controllerNotAuthorised.onEditPageLoadAbroad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to GG login page on Submit of an address abroad in edit mode if the user is not logged in" in {
      val result = controllerNotAuthorised.onEditSubmitAbroad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "load when visited for the first time and applicant lives in the UK" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithUkApplicant))

      val result = controller.onPageLoadUk(createFakeRequest(authRetrieveNino = false))

      status(result) must be(OK)
      contentAsString(result) must include(messagesApi("page.iht.registration.applicantAddress.title"))
      contentAsString(result) must include(messagesApi("page.iht.registration.applicantAddress.hint"))
      contentAsString(result) must include(messagesApi("iht.address.line1"))
      contentAsString(result) must include(messagesApi("iht.address.line2"))
      contentAsString(result) must include(messagesApi("iht.address.line3"))
      contentAsString(result) must include(messagesApi("iht.address.line4"))
      contentAsString(result) must include(messagesApi("iht.postcode"))
      contentAsString(result) must include(messagesApi("iht.registration.changeAddressToAbroad"))
      contentAsString(result) must include(routes.ApplicantAddressController.onPageLoadAbroad().url)
    }

    "load when revisited and applicant lives in the UK" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithUkApplicantPopulated))

      val result = controller.onPageLoadUk(createFakeRequest(authRetrieveNino = false))

      status(result) must be(OK)
      contentAsString(result) must include("UK Line 1")
      contentAsString(result) must include("UK Line 2")
      contentAsString(result) must include("AA1 1AA")
    }

    "load when revisited in edit mode and applicant lives in the UK" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithUkApplicantPopulated))

      val result = controller.onEditPageLoadUk(createFakeRequest(authRetrieveNino = false))

      status(result) must be(OK)
      contentAsString(result) must include("UK Line 1")
      contentAsString(result) must include("UK Line 2")
      contentAsString(result) must include("AA1 1AA")
    }

    "load when visited for the first time and applicant lives abroad" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroad))

      val result = controller.onPageLoadAbroad(createFakeRequest(authRetrieveNino = false))

      status(result) must be(OK)
      contentAsString(result) must include(messagesApi("page.iht.registration.applicantAddress.title"))
      contentAsString(result) must include(messagesApi("page.iht.registration.applicantAddress.hint"))
      contentAsString(result) must include(messagesApi("iht.address.line1"))
      contentAsString(result) must include(messagesApi("iht.address.line2"))
      contentAsString(result) must include(messagesApi("iht.address.line3"))
      contentAsString(result) must include(messagesApi("iht.address.line4"))
      contentAsString(result) must include(messagesApi("iht.country"))
      contentAsString(result) must include(messagesApi("iht.registration.changeAddressToUK"))
      contentAsString(result) must include(routes.ApplicantAddressController.onPageLoadUk().url)
    }

    "load when revisited and applicant lives abroad" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroadPopulated))

      val result = controller.onPageLoadAbroad(createFakeRequest(authRetrieveNino = false))

      status(result) must be(OK)
      contentAsString(result) must include("Abroad Line 1")
      contentAsString(result) must include("Abroad Line 2")
      contentAsString(result) must include("Abroad Line 3")
      contentAsString(result) must include("Abroad Line 4")
      contentAsString(result) must include("US")
    }

    "load when revisited in edit mode and applicant lives abroad" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroadPopulated))

      val result = controller.onEditPageLoadAbroad(createFakeRequest(authRetrieveNino = false))

      status(result) must be(OK)
      contentAsString(result) must include("Abroad Line 1")
      contentAsString(result) must include("Abroad Line 2")
      contentAsString(result) must include("Abroad Line 3")
      contentAsString(result) must include("Abroad Line 4")
      contentAsString(result) must include("US")
    }

    "forget address details when changing from UK to abroad" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithUkApplicantPopulated))

      val result = controller.onPageLoadAbroad(createFakeRequest(authRetrieveNino = false))

      status(result) must be(OK)
      contentAsString(result) mustNot include("UK Line 1")
      contentAsString(result) mustNot include("UK Line 2")
      contentAsString(result) mustNot include("AA1 1AA")
    }

    "forget address details when changing from abroad to UK" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroadPopulated))

      val result = controller.onPageLoadUk(createFakeRequest(authRetrieveNino = false))

      status(result) must be(OK)
      contentAsString(result) mustNot include("Abroad Line 1")
      contentAsString(result) mustNot include("Abroad Line 2")
      contentAsString(result) mustNot include("Abroad Line 3")
      contentAsString(result) mustNot include("Abroad Line 4")
      contentAsString(result) mustNot include("GB")
    }

    "forget address details when changing from UK to abroad in edit mode" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithUkApplicantPopulated))

      val result = controller.onEditPageLoadAbroad(createFakeRequest(authRetrieveNino = false))

      status(result) must be(OK)
      contentAsString(result) mustNot include("UK Line 1")
      contentAsString(result) mustNot include("UK Line 2")
      contentAsString(result) mustNot include("AA1 1AA")
    }

    "forget address details when changing from abroad to UK in edit mode" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroadPopulated))

      val result = controller.onEditPageLoadUk(createFakeRequest(authRetrieveNino = false))

      status(result) must be(OK)
      contentAsString(result) mustNot include("Abroad Line 1")
      contentAsString(result) mustNot include("Abroad Line 2")
      contentAsString(result) mustNot include("Abroad Line 3")
      contentAsString(result) mustNot include("Abroad Line 4")
      contentAsString(result) mustNot include("GB")
    }

    "save and redirect correctly on submit when saving a UK address" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroad))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroad))

      val form = applicantAddressUkForm.fill(ukAddress)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host,
        data = form.data.toSeq, authRetrieveNino = false)

      val result = controller.onSubmitUk(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(
        Some(iht.controllers.registration.executor.routes.OthersApplyingForProbateController.onPageLoad.url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val applicant = capturedValue.applicantDetails.get
      applicant.ukAddress mustBe Some(ukAddress copy (countryCode = mockAppConfig.ukIsoCountryCode))
      applicant.doesLiveInUK mustBe Some(true)
    }

    "save and redirect correctly on submit when saving an address abroad" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroad))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroad))

      val form = applicantAddressAbroadForm.fill(addressAbroad)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host,
        data = form.data.toSeq, authRetrieveNino = false)

      val result = controller.onSubmitAbroad(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(
        Some(iht.controllers.registration.executor.routes.OthersApplyingForProbateController.onPageLoad.url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val applicant = capturedValue.applicantDetails.get
      applicant.ukAddress mustBe Some(addressAbroad)
      applicant.doesLiveInUK mustBe Some(false)
    }

    "save and redirect correctly on submit in edit mode when saving a UK address" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroad))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroad))

      val form = applicantAddressUkForm.fill(ukAddress)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host, data = form.data.toSeq, authRetrieveNino = false)

      val result = controller.onEditSubmitUk(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(registrationRoutes.RegistrationSummaryController.onPageLoad().url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val applicant = capturedValue.applicantDetails.get
      applicant.ukAddress mustBe Some(ukAddress copy (countryCode = mockAppConfig.ukIsoCountryCode))
      applicant.doesLiveInUK mustBe Some(true)
    }

    "save and redirect correctly on submit in edit mode when saving an address abroad" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroad))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroad))

      val form = applicantAddressAbroadForm.fill(addressAbroad)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host, data = form.data.toSeq, authRetrieveNino = false)

      val result = controller.onEditSubmitAbroad(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(registrationRoutes.RegistrationSummaryController.onPageLoad().url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val applicant = capturedValue.applicantDetails.get
      applicant.ukAddress mustBe Some(addressAbroad)
      applicant.doesLiveInUK mustBe Some(false)
    }

    "redirect UK address to estate report if RegistrationDetails object does not contain applicant details" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails))

      val result = await(controller.onPageLoadUk(createFakeRequest(authRetrieveNino = false)))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url)
    }

    "redirect address to estate report if RegistrationDetails object does not contain applicant details" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails))

      val result = await(controller.onPageLoadAbroad(createFakeRequest(authRetrieveNino = false)))
      status(result) mustBe SEE_OTHER
    }

    "redirect onSubmit UK address to estate report if RegistrationDetails object does not contain applicant details" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails))

      val form = applicantAddressUkForm.fill(ukAddress)
      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host,
        data = form.data.toSeq, authRetrieveNino = false)

      val result = await(controller.onSubmitUk(request))
      status(result) mustBe SEE_OTHER
    }

    "redirect onSubmit address to estate report if RegistrationDetails object does not contain applicant details" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails))

      val result = await(controller.onSubmitAbroad(createFakeRequest(authRetrieveNino = false)))
      status(result) mustBe SEE_OTHER
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
        data = form.data.toSeq, authRetrieveNino = false)

      val result = if (isInternational) controller.onSubmitAbroad(request) else controller.onSubmitUk(request)
      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messagesApi(expectedError))
    }

    "After a submit, when submitting an address, if the storage operation fails the result must be a server error" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetailsWithApplicantAbroad))
      createMockToStoreRegDetailsInCacheWithFailure(mockCachingConnector, Some(registrationDetailsWithApplicantAbroad))

      val form = applicantAddressUkForm.fill(ukAddress)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host,
        data = form.data.toSeq, authRetrieveNino = false)

      val result = controller.onSubmitUk(request)
      status(result) must be(INTERNAL_SERVER_ERROR)
    }
  }
}
