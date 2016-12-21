/*
 * Copyright 2016 HM Revenue & Customs
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
import iht.models.{DeceasedDateOfDeath, DeceasedDetails, RegistrationDetails}
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import org.joda.time.LocalDate
import org.scalatest.BeforeAndAfter
import play.api.i18n.Messages
import play.api.test.Helpers._

class DeceasedAddressQuestionControllerTest extends RegistrationControllerTest with BeforeAndAfter {

  //Create controller object and pass in mock.
  def deceasedAddressQuestionController = new DeceasedAddressQuestionController {
   override val cachingConnector = mockCachingConnector
   override val authConnector = createFakeAuthConnector(isAuthorised=true)
   override val isWhiteListEnabled = false
  }

  def deceasedAddressQuestionControllerNotAuthorised = new DeceasedAddressQuestionController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val isWhiteListEnabled = false
  }

  before {
    mockCachingConnector = mock[CachingConnector]
  }

  "DeceasedAddressQuestionController" must {
    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = deceasedAddressQuestionControllerNotAuthorised.onPageLoad(
        createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to GG login page on Submit if the user is not logged in" in {
      val result = deceasedAddressQuestionControllerNotAuthorised.onSubmit(
        createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "contain Continue button when Page is loaded in normal mode" in {
      val host="localhost:9070"

      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails=CommonBuilder.buildRegistrationDetails copy (deceasedDateOfDeath = Some(DeceasedDateOfDeath(LocalDate.now)),
        deceasedDetails=Some(deceasedDetails))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = deceasedAddressQuestionController.onPageLoad()(createFakeRequestWithReferrer(referrerURL=referrerURL,host=host))
      status(result) shouldBe(OK)

      contentAsString(result) should include(Messages("iht.continue"))
    }

    "redirect to UK address page when question is answered yes" in  {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails copy (isAddressInUK = Some(true))
      val registrationDetails = RegistrationDetails(None, None, Some(deceasedDetails))
      val deceasedDetailsForm1 = deceasedAddressQuestionForm.fill(deceasedDetails)
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=deceasedDetailsForm1.data.toSeq)

      createMockToGetExistingRegDetailsFromCache(mockCachingConnector, registrationDetails)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = deceasedAddressQuestionController.onSubmit()(request)
      status(result) shouldBe(SEE_OTHER)
      redirectLocation(result) should be (
        Some(iht.controllers.registration.deceased.routes.DeceasedAddressDetailsUKController.onPageLoad().url))

    }

    "redirect to non-UK address page when question is answered no" in  {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails copy (isAddressInUK = Some(false))
      val registrationDetails = RegistrationDetails(None, None, Some(deceasedDetails))
      val deceasedDetailsForm1 = deceasedAddressQuestionForm.fill(deceasedDetails)
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=deceasedDetailsForm1.data.toSeq)

      createMockToGetExistingRegDetailsFromCache(mockCachingConnector, registrationDetails)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = deceasedAddressQuestionController.onSubmit()(request)
      status(result) shouldBe(SEE_OTHER)
      redirectLocation(result) should be (
        Some(iht.controllers.registration.deceased.routes.DeceasedAddressDetailsOutsideUKController.onPageLoad().url))
    }

    "respond appropriately to an invalid submit: Missing mandatory fields" in {
      val existingDeceasedDetails = CommonBuilder.buildDeceasedDetails
      val deceasedDetails = DeceasedDetails(None, None, None, None, None, None, None, None, None)
      val registrationDetails = RegistrationDetails(None, None, Some(existingDeceasedDetails))
      val deceasedDetailsForm1 = deceasedAddressQuestionForm.fill(deceasedDetails)
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=deceasedDetailsForm1.data.toSeq)

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = await(deceasedAddressQuestionController.onSubmit()(request))
      status(result) shouldBe(BAD_REQUEST)
    }

    "save valid data correctly" in {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(None, None, Some(deceasedDetails))

      val newDeceasedDetails = DeceasedDetails(None, None, None, None, None, None, None, None, None, Some(true))

      val form = deceasedAddressQuestionForm.fill(newDeceasedDetails)

      val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host,
        data = form.data.toSeq)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = await(deceasedAddressQuestionController.onSubmit()(request))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val expectedDeceasedDetails = deceasedDetails copy (isAddressInUK = newDeceasedDetails.isAddressInUK)
      capturedValue.deceasedDetails shouldBe Some(expectedDeceasedDetails)
    }

// TODO:
//    "return true if the guard conditions are true" in {
//      val rd = CommonBuilder.buildRegistrationDetails copy (deceasedDateOfDeath = Some(DeceasedDateOfDeath(LocalDate.now)))
//      deceasedAddressQuestionController.checkGuardCondition(rd) shouldBe true
//    }
//
//    "return false if the guard conditions are false" in {
//      val rd = CommonBuilder.buildRegistrationDetails copy (deceasedDateOfDeath = None)
//      deceasedAddressQuestionController.checkGuardCondition(rd) shouldBe false
//    }
  }

  "return a server error to UK address page when question is answered yes but the storage fails" in  {
    val deceasedDetails = CommonBuilder.buildDeceasedDetails copy (isAddressInUK = Some(true))
    val registrationDetails = RegistrationDetails(None, None, Some(deceasedDetails))
    val deceasedDetailsForm1 = deceasedAddressQuestionForm.fill(deceasedDetails)
    val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=deceasedDetailsForm1.data.toSeq)

    createMockToGetExistingRegDetailsFromCache(mockCachingConnector, registrationDetails)
    createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
    createMockToStoreRegDetailsInCacheWithFailure(mockCachingConnector, Some(registrationDetails))

    val result = deceasedAddressQuestionController.onSubmit()(request)
    status(result) shouldBe(INTERNAL_SERVER_ERROR)
  }

  "return a server error to non-UK address page when question is answered no but the storage fails" in  {
    val deceasedDetails = CommonBuilder.buildDeceasedDetails copy (isAddressInUK = Some(false))
    val registrationDetails = RegistrationDetails(None, None, Some(deceasedDetails))
    val deceasedDetailsForm1 = deceasedAddressQuestionForm.fill(deceasedDetails)
    val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
      data=deceasedDetailsForm1.data.toSeq)

    createMockToGetExistingRegDetailsFromCache(mockCachingConnector, registrationDetails)
    createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
    createMockToStoreRegDetailsInCacheWithFailure(mockCachingConnector, Some(registrationDetails))

    val result = deceasedAddressQuestionController.onSubmit()(request)
    status(result) shouldBe(INTERNAL_SERVER_ERROR)
  }
}
