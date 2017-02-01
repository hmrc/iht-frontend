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
import iht.controllers.registration.RegistrationControllerTest
import iht.forms.registration.DeceasedForms._
import iht.models._
import iht.models.application.debts._
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import iht.utils.RegistrationKickOutHelper
import iht.utils.RegistrationKickOutHelper._
import org.joda.time.LocalDate
import org.mockito.Matchers._
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.{Action, AnyContent}
import play.api.test.Helpers._
import play.api.test.{FakeApplication, FakeRequest, WithApplication}

class DeceasedDateOfDeathTest extends RegistrationControllerTest {

  lazy val defaultReferrerURL="http://localhost:9070/inheritance-tax"
  lazy val defaultHost="localhost:9070"

  before {
    mockCachingConnector = mock[CachingConnector]
  }

  def setupMocks = {
    val deceasedDateOfDeath = DeceasedDateOfDeath(new LocalDate(2001,11, 11))
    val deceasedDateOfDeathChanged = DeceasedDateOfDeath(new LocalDate(2011,11, 11))
    val deceasedDetails = CommonBuilder.buildDeceasedDetails
    val coExec1 = CommonBuilder.buildCoExecutor
    val registrationDetails = new RegistrationDetails(
      Some(deceasedDateOfDeath),
      None,
      Some(deceasedDetails),
      Seq(coExec1))

    val newRegistrationDetails = new RegistrationDetails(
      Some(deceasedDateOfDeathChanged),
      None,
      Some(deceasedDetails),
      Seq(coExec1))

    createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
    createMockToStoreRegDetailsInCache(mockCachingConnector, Some(newRegistrationDetails))
  }

  def controller = new DeceasedDateOfDeathController {
    override lazy val cachingConnector = mockCachingConnector
    override lazy val authConnector = createFakeAuthConnector(isAuthorised=true)
    override val isWhiteListEnabled = false
  }

  def controllerNotAuthorised = new DeceasedDateOfDeathController {
    override lazy val cachingConnector = mockCachingConnector
    override lazy val authConnector = createFakeAuthConnector(isAuthorised=false)
    override val isWhiteListEnabled = false
  }

  "dateOfDeath controller" must {

    "redirect to GG login page on PageLoad if the user is not logged in" in{
      val result = controllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to GG login page on EditPageLoad if the user is not logged in" in{
      val result = controllerNotAuthorised.onEditPageLoad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to GG login page on Submit if the user is not logged in" in{
      val result = controllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to GG login page on EditSubmit if the user is not logged in" in{
      val result = controllerNotAuthorised.onEditSubmit(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "persist CoExecutor details on change of date of death" in {
      val deceasedDateOfDeath = DeceasedDateOfDeath(new LocalDate(2001,11, 11))
      val deceasedDateOfDeathChanged = DeceasedDateOfDeath(new LocalDate(2011,11, 11))
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val coExec1 = CommonBuilder.buildCoExecutor

      val registrationDetails = new RegistrationDetails(
        Some(deceasedDateOfDeath),
        None,
        Some(deceasedDetails),
        Seq(coExec1))

      val newRegistrationDetails = new RegistrationDetails(
        Some(deceasedDateOfDeathChanged),
        None,
        Some(deceasedDetails),
        Seq(coExec1))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(newRegistrationDetails))

      val form = deceasedDateOfDeathForm.fill(deceasedDateOfDeathChanged)
      val request = createFakeRequest(isAuthorised = true).withFormUrlEncodedBody(form.data.toSeq: _*)

      val result = controller.onEditSubmit()(request)

      status(result) shouldBe(SEE_OTHER)

      redirectLocation(result) should be (
        Some(iht.controllers.registration.routes.RegistrationSummaryController.onPageLoad().url))

    }

    "Load the DeceasedDateOfDeath page without sessionId" in {
      setupMocks
      val result = controller.onPageLoad()(createFakeRequest(true))
      status(result) should be(OK)
      contentAsString(result) should include(Messages("page.iht.registration.deceasedDateOfDeath.title"))
    }

    "Load the DeceasedDateOfDeath page when there is Id" in {

      val deceasedDateOfDeath = new DeceasedDateOfDeath(new LocalDate(2001,11, 11))
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails

      val registrationDetails = RegistrationDetails(Some(deceasedDateOfDeath), Some(applicantDetails),
        Some(deceasedDetails))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onPageLoad()(
        createFakeRequestWithReferrer(referrerURL=defaultReferrerURL,host=defaultHost))
      status(result) should be(OK)
      contentAsString(result) should include(Messages("page.iht.registration.deceasedDateOfDeath.title"))
    }

    "contain Continue button when Page is loaded in normal mode" in {
      setupMocks
      val referrerURL="http://localhost:9070/inheritance-tax"
      val host="localhost:9070"
      val result =controller.onPageLoad()(createFakeRequestWithReferrer(referrerURL=referrerURL,
        host=host))

      status(result) should be(OK)
      contentAsString(result) should include(Messages("page.iht.registration.deceasedDateOfDeath.title"))
      contentAsString(result) should include(Messages("iht.continue"))
      contentAsString(result) should not include(Messages("site.link.cancel"))
    }

    "contain Continue and Cancel button when Page is loaded in edit mode" in {
      val referrerURL = "http://localhost:9070/inheritance-tax/registration/editDeceasedDateOfDeath"
      val host = "localhost:9070"

      val deceasedDateOfDeath=CommonBuilder.buildDeceasedDateOfDeath
      val applicantDetails=CommonBuilder.buildApplicantDetails
      val deceasedDetails=CommonBuilder.buildDeceasedDetails

      val registrationDetails=CommonBuilder.buildRegistrationDetails copy (deceasedDateOfDeath=Some
        (deceasedDateOfDeath),applicantDetails=Some(applicantDetails),deceasedDetails=Some(deceasedDetails))

      implicit val requestWithHeaders = FakeRequest().withHeaders(("referer",referrerURL),("host",host))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result =controller.onEditPageLoad(createFakeRequestWithReferrer(referrerURL=referrerURL,
        host=host))

      status(result) should be(OK)
      contentAsString(result) should include(Messages("page.iht.registration.deceasedDateOfDeath.title"))

      contentAsString(result) should include(Messages("iht.continue"))
      contentAsString(result) should include(Messages("site.link.cancel"))
    }

    "respond with OK" in new WithApplication(FakeApplication()) {
      val deceasedDateOfDeath = new DeceasedDateOfDeath(new LocalDate(2011,11, 11))
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(Some(deceasedDateOfDeath), Some(applicantDetails),
        Some(deceasedDetails))

      var form = deceasedDateOfDeathForm.fill(deceasedDateOfDeath)
      implicit val request = FakeRequest().withFormUrlEncodedBody(form.data.toSeq: _*)

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onSubmit()(request)
      status(result) shouldBe(303)
    }

    "return to Reg Summary Page after submit when ApplicantDetails are edited from " +
      "Reg Summary Page" in new WithApplication(FakeApplication()){
      val referrerURL="http://localhost:9070/inheritance-tax/registration/registrationSummary/focusElementId?"
      val host="localhost:9070"

      val deceasedDateOfDeath= CommonBuilder.buildDeceasedDateOfDeath copy (
        dateOfDeath = new LocalDate(2012, 4, 6)
      )
      val applicantDetails=CommonBuilder.buildApplicantDetails
      val deceasedDetails=CommonBuilder.buildDeceasedDetails

      val registrationDetails=CommonBuilder.buildRegistrationDetails copy (deceasedDateOfDeath=Some
        (deceasedDateOfDeath),applicantDetails=Some(applicantDetails),deceasedDetails=Some(deceasedDetails))

      val form = deceasedDateOfDeathForm.fill(deceasedDateOfDeath)
      val requestWithHeadersWithBody = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,
        host=host, data=form.data.toSeq )

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onEditSubmit()(requestWithHeadersWithBody)
      status(result) shouldBe(SEE_OTHER)
      redirectLocation(result) should be (
        Some(iht.controllers.registration.routes.RegistrationSummaryController.onPageLoad().url))

    }

    "respond with bad request on incorrect form data" in new WithApplication(FakeApplication()) {
      val deceasedDateOfDeath = new DeceasedDateOfDeath(new LocalDate(2011,11, 11))
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails copy (maritalStatus=Some("Single"))
      val registrationDetails = RegistrationDetails(Some(deceasedDateOfDeath), Some(applicantDetails),
        Some(deceasedDetails))

      var form = deceasedDateOfDeathForm.fill(new DeceasedDateOfDeath(new LocalDate(20011,11, 11)))
      implicit val request = createFakeRequestWithReferrer(referrerURL=defaultReferrerURL,
        host=defaultHost).withFormUrlEncodedBody(form.data.toSeq: _*)

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onSubmit()(request)
      status(result) shouldBe(400)
    }

    "redirect when store registration return empty elements" in new WithApplication(FakeApplication()) {
      val deceasedDateOfDeath = new DeceasedDateOfDeath(new LocalDate(2001,11, 11))
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(Some(deceasedDateOfDeath), Some(applicantDetails),
        Some(deceasedDetails))

      var form = deceasedDateOfDeathForm.fill(deceasedDateOfDeath)
      implicit val request = FakeRequest().withFormUrlEncodedBody(form.data.toSeq: _*)

      createMockToGetRegDetailsFromCache(mockCachingConnector, None)
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onSubmit()(request)
      status(result) shouldBe SEE_OTHER
    }

    "redirect to DeceasedDetals when there is no prefilled data" in new WithApplication(FakeApplication()) {
      val deceasedDateOfDeath = new DeceasedDateOfDeath(new LocalDate(2001,11, 11))
      val registrationDetails = RegistrationDetails(Some(deceasedDateOfDeath), None, None)

      var form = deceasedDateOfDeathForm.fill(deceasedDateOfDeath)
      implicit val request = FakeRequest().withFormUrlEncodedBody(form.data.toSeq: _*)

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onSubmit()(request)
      status(result) shouldBe SEE_OTHER
    }

    "respond when incorrect date" in new WithApplication(FakeApplication()) {
      val deceasedDateOfDeath = new DeceasedDateOfDeath(new LocalDate(1985,11, 11))
      val registrationDetails = RegistrationDetails(Some(deceasedDateOfDeath), None, None)
      val form = deceasedDateOfDeathForm.fill(deceasedDateOfDeath)

      val request = createFakeRequest(isAuthorised = true).withFormUrlEncodedBody(
        form.data.toSeq: _*)

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreSingleValueInCache(mockCachingConnector, any(),
        Some(RegistrationKickOutHelper.KickoutDeceasedDateOfDeathDateCapitalTax))
      createMockToGetSingleValueFromCache(mockCachingConnector, any(),
        Some(RegistrationKickOutHelper.KickoutDeceasedDateOfDeathDateCapitalTax))

      val result = controller.onSubmit()(request)
      status(result) shouldBe(SEE_OTHER)
    }

    "onEditSubmit" in new WithApplication(FakeApplication()) {
      val deceasedDateOfDeath = DeceasedDateOfDeath(new LocalDate(2001,11, 11))
      val deceasedDateOfDeathChanged = DeceasedDateOfDeath(new LocalDate(2011,11, 11))
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val coExec1 = CommonBuilder.buildCoExecutor

      val registrationDetails = new RegistrationDetails(
        Some(deceasedDateOfDeath),
        None,
        Some(deceasedDetails),
        Seq(coExec1))

      val newRegistrationDetails = new RegistrationDetails(
        Some(deceasedDateOfDeathChanged),
        None,
        Some(deceasedDetails),
        Seq(coExec1))

      createMockToGetExistingRegDetailsFromCache(mockCachingConnector, registrationDetails)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(newRegistrationDetails))

      val form = deceasedDateOfDeathForm.fill(
        deceasedDateOfDeathChanged)
      val request = createFakeRequest(isAuthorised = true).withFormUrlEncodedBody(
        form.data.toSeq: _*)
      val result = controller.onEditSubmit()(request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get should be (iht.controllers.registration.routes.RegistrationSummaryController.onPageLoad().url)
    }

    "onEditSubmit where date of birth comes after date of death " +
      "produces validation error" in new WithApplication(FakeApplication()) {
      val deceasedDateOfDeath = DeceasedDateOfDeath(new LocalDate(2001,11, 11))
      val deceasedDateOfDeathChanged = DeceasedDateOfDeath(new LocalDate(2011,11, 11))
      val deceasedDetails = CommonBuilder.buildDeceasedDetails copy (dateOfBirth = Some(new LocalDate(2012,12,12)))
      val coExec1 = CommonBuilder.buildCoExecutor

      val registrationDetails = new RegistrationDetails(
        Some(deceasedDateOfDeath),
        None,
        Some(deceasedDetails),
        Seq(coExec1))

      val newRegistrationDetails = new RegistrationDetails(
        Some(deceasedDateOfDeathChanged),
        None,
        Some(deceasedDetails),
        Seq(coExec1))

      createMockToGetExistingRegDetailsFromCache(mockCachingConnector, registrationDetails)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(newRegistrationDetails))

      val form = deceasedDateOfDeathForm.fill(deceasedDateOfDeathChanged)
      val request = createFakeRequest(isAuthorised = true).withFormUrlEncodedBody(form.data.toSeq: _*)
      val result = controller.onEditSubmit()(request)

      status(result) shouldBe BAD_REQUEST
    }

    "compareDateOfBirthToDateOfDeath compares correctly" in new WithApplication(FakeApplication()) {
      val deceasedDateOfDeathChanged = DeceasedDateOfDeath(new LocalDate(2011,4, 1))
      val form = deceasedDateOfDeathForm.fill(deceasedDateOfDeathChanged)
      val result = controller.compareDateOfBirthToDateOfDeath(form,
        new LocalDate( 2012,2,2 ))
      val formError = result.error("dateOfDeath")
      formError shouldBe Some(FormError("dateOfDeath", "error.dateOfDeath.giveAfterDateOfBirth"))
    }

    def ensureRedirectOnKickout(dateOfDeath: LocalDate, kickoutReasonKey: String, action: Action[AnyContent]) = {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails copy (dateOfBirth = Some(new LocalDate(1980, 1, 1)))
      val deceasedDateOfDeath = CommonBuilder.buildDeceasedDateOfDeath copy (dateOfDeath = dateOfDeath)
      val registrationDetails = RegistrationDetails(Some(deceasedDateOfDeath), None, Some(deceasedDetails))
      val deceasedDateOfDeathForm1 = deceasedDateOfDeathForm.fill(deceasedDateOfDeath)
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=deceasedDateOfDeathForm1.data.toSeq)

      createMockToGetExistingRegDetailsFromCache(mockCachingConnector, registrationDetails)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreSingleValueInCache(
        cachingConnector=mockCachingConnector,
        singleValueReturn=Some(kickoutReasonKey))

      val result = await(action(request))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (
        Some(iht.controllers.registration.routes.KickoutController.onPageLoad().url))
      verifyAndReturnStoredSingleValue(mockCachingConnector) match {
        case (cachedKey, cachedValue) =>
          cachedKey shouldBe RegistrationKickoutReasonCachingKey
          cachedValue shouldBe kickoutReasonKey
      }
    }

    "redirect to kickout page if KickoutDeceasedDateOfDeathDateOther conditions apply to onEditSubmit" in
      new WithApplication(FakeApplication()) {
      ensureRedirectOnKickout(new LocalDate(2010, 1, 1), KickoutDeceasedDateOfDeathDateOther,
        controller.onEditSubmit)
    }

    "redirect to kickout page if KickoutDeceasedDateOfDeathDateCapitalTax conditions apply to onEditSubmit" in
      new WithApplication(FakeApplication()) {
      ensureRedirectOnKickout(new LocalDate(1985, 1, 1), KickoutDeceasedDateOfDeathDateCapitalTax,
        controller.onEditSubmit)
    }

    "redirect to kickout page if KickoutDeceasedDateOfDeathDateOther conditions apply to onSubmit" in
      new WithApplication(FakeApplication()) {
      ensureRedirectOnKickout(new LocalDate(2010, 1, 1), KickoutDeceasedDateOfDeathDateOther,
        controller.onSubmit)
    }

    "redirect to kickout page if KickoutDeceasedDateOfDeathDateCapitalTax conditions apply to onSubmit" in
      new WithApplication(FakeApplication()) {
      ensureRedirectOnKickout(new LocalDate(1985, 1, 1), KickoutDeceasedDateOfDeathDateCapitalTax,
        controller.onSubmit)
    }
  }
}
