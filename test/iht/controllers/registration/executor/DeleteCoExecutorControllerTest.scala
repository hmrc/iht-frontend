/*
 * Copyright 2018 HM Revenue & Customs
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

import iht.connector.CachingConnector
import iht.constants.IhtProperties
import iht.controllers.registration.RegistrationControllerTest
import iht.forms.registration.CoExecutorForms._
import iht.models.UkAddress
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder}
import iht.testhelpers.MockObjectBuilder._
import org.scalatest.BeforeAndAfter
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._
import iht.utils._
import uk.gov.hmrc.play.partials.FormPartialRetriever

class DeleteCoExecutorControllerTest extends RegistrationControllerTest with BeforeAndAfter {

  before {
    mockCachingConnector = mock[CachingConnector]
  }

  def deleteCoExecutorController = new DeleteCoExecutorController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised=true)

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def deleteCoExecutorControllerNotAuthorised = new DeleteCoExecutorController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = false)

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  val coExecutor=CommonBuilder.buildCoExecutor
  val registrationDetails = CommonBuilder.buildRegistrationDetails copy (applicantDetails = Some(CommonBuilder.buildApplicantDetails),
    coExecutors = Seq(coExecutor))



  "DeleteCoExecutor controller" must {

    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = deleteCoExecutorControllerNotAuthorised.onPageLoad(coExecutor.id.getOrElse(""))(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(loginUrl))
    }

    "redirect to GG login page on Submit if the user is not logged in" in {
      val result = deleteCoExecutorControllerNotAuthorised.onSubmit(coExecutor.id.getOrElse(""))(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(loginUrl))
    }

    "if the registration details does not have areOthersApplying set then respond with an error" in {
      val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors copy (areOthersApplyingForProbate = Some(false))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, rd)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rd))

      val result = deleteCoExecutorController.onPageLoad("1")(createFakeRequest())
      status(result) shouldBe SEE_OTHER
    }

    "if the registration details does not have more than one coExecutor set then respond with an error" in {
      val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors copy (areOthersApplyingForProbate = Some(true), coExecutors = Seq())

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, rd)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rd))

      val result = deleteCoExecutorController.onPageLoad("1")(createFakeRequest())
      status(result) shouldBe SEE_OTHER
    }

    "if the coexecutor with given id does not exist - respond with a server error" in {
      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, CommonBuilder.buildRegistrationDetailsWithCoExecutors)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithCoExecutors))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithCoExecutors))

      val result = deleteCoExecutorController.onPageLoad("2")(createFakeRequestWithReferrer(referrerURL=referrerURL, host="localhost:9070"))

      status(result) shouldBe (INTERNAL_SERVER_ERROR)
    }

    "if the coexecutor with given id exists - respond with an OK" in {
      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, CommonBuilder.buildRegistrationDetailsWithCoExecutors)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithCoExecutors))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithCoExecutors))

      val result = deleteCoExecutorController.onPageLoad("1")(createFakeRequestWithReferrer(referrerURL=referrerURL, host="localhost:9070"))

      status(result) shouldBe(OK)
    }

    "if the coexecutor with given id exists - the instruction must be visible" in {
      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, CommonBuilder.buildRegistrationDetailsWithCoExecutors)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithCoExecutors))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithCoExecutors))

      val result = deleteCoExecutorController.onPageLoad("1")(createFakeRequestWithReferrer(referrerURL=referrerURL, host="localhost:9070"))

      status(result) shouldBe(OK)
      contentAsString(result) should include("Confirm that you want to delete this person")
    }

    "if the coexecutor with given id exists - the confirm or delete button must be visible" in {
      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, CommonBuilder.buildRegistrationDetailsWithCoExecutors)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithCoExecutors))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithCoExecutors))

      val result = deleteCoExecutorController.onPageLoad("1")(createFakeRequestWithReferrer(referrerURL=referrerURL, host="localhost:9070"))

      status(result) shouldBe(OK)
      contentAsString(result) should include(messagesApi("site.button.confirmDelete"))
    }

    "if the coexecutor with given id exists - a cancel link must be visible" in {
      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, CommonBuilder.buildRegistrationDetailsWithCoExecutors)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithCoExecutors))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithCoExecutors))

      val result = deleteCoExecutorController.onPageLoad("1")(createFakeRequestWithReferrer(referrerURL = referrerURL, host = "localhost:9070"))

      status(result) shouldBe (OK)
      contentAsString(result) should include(messagesApi("site.link.cancel"))
      contentAsString(result) should include(messagesApi(routes.ExecutorOverviewController.onPageLoad().url))
    }

    "if the coexecutor with given id exists - the name of the coexecutor must be visible" in {
      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, CommonBuilder.buildRegistrationDetailsWithCoExecutors)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithCoExecutors))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithCoExecutors))

      val result = deleteCoExecutorController.onPageLoad("1")(createFakeRequestWithReferrer(referrerURL=referrerURL, host="localhost:9070"))

      status(result) shouldBe(OK)
      contentAsString(result) should include(CommonBuilder.buildCoExecutor.name)
    }

    "if the coexecutor with given id exists - the first line of the address of the coexecutor must be visible" in {
      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, CommonBuilder.buildRegistrationDetailsWithCoExecutors)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithCoExecutors))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithCoExecutors))

      val result = deleteCoExecutorController.onPageLoad("1")(createFakeRequestWithReferrer(referrerURL=referrerURL, host="localhost:9070"))

      status(result) shouldBe(OK)
      contentAsString(result) should include(CommonBuilder.buildCoExecutor.ukAddress.get.ukAddressLine1)
    }

    "if the coexecutor with given id exists - the second line of the address of the coexecutor must be visible" in {
      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, CommonBuilder.buildRegistrationDetailsWithCoExecutors)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithCoExecutors))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithCoExecutors))

      val result = deleteCoExecutorController.onPageLoad("1")(createFakeRequestWithReferrer(referrerURL=referrerURL, host="localhost:9070"))

      status(result) shouldBe(OK)
      contentAsString(result) should include(CommonBuilder.buildCoExecutor.ukAddress.get.ukAddressLine2)
    }

    "if the coexecutor with given id exists - and the third line of the address exists the third line of the address of the coexecutor must be visible" in {
      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, CommonBuilder.buildRegistrationDetailsWithCoExecutors)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithCoExecutors))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithCoExecutors))

      val result = deleteCoExecutorController.onPageLoad("1")(createFakeRequestWithReferrer(referrerURL=referrerURL, host="localhost:9070"))

      status(result) shouldBe(OK)
      contentAsString(result) should include(CommonBuilder.buildCoExecutor.ukAddress.get.ukAddressLine3.get)
    }

    "if the coexecutor with given id exists - and the third line of the address does not exist - " +
      "the third line of the address of the coexecutor must be not visible" in {
      val ukAddressWithNoThirdLine = UkAddress("addr1", "addr2", None, None, "AA11AA", "GB")
      val coExecutor = CommonBuilder.buildCoExecutor copy (ukAddress = Some(ukAddressWithNoThirdLine))
      val rd = CommonBuilder.buildRegistrationDetails copy (areOthersApplyingForProbate = Some(true), coExecutors = Seq(coExecutor))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, rd)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rd))

      val result = deleteCoExecutorController.onPageLoad("1")(createFakeRequestWithReferrer(referrerURL=referrerURL, host="localhost:9070"))

      status(result) shouldBe(OK)
    }

    "if the coexecutor with given id exists - and the fourth line of the address exists the fourth line of the address of the coexecutor must be visible" in {
      val ukAddressWithNoFourthLine = UkAddress("addr1", "addr2", None, Some("addr4"), "AA11AA", "GB")
      val coExecutor = CommonBuilder.buildCoExecutor copy (ukAddress = Some(ukAddressWithNoFourthLine))
      val rd = CommonBuilder.buildRegistrationDetails copy (areOthersApplyingForProbate = Some(true), coExecutors = Seq(coExecutor))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, rd)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rd))

      val result = deleteCoExecutorController.onPageLoad("1")(createFakeRequestWithReferrer(referrerURL=referrerURL, host="localhost:9070"))

      status(result) shouldBe(OK)
      contentAsString(result) should include(CommonBuilder.buildCoExecutor.ukAddress.get.ukAddressLine4.get)
    }

    "if the coexecutor with given id exists - and the fourth line of the address does not exist - " +
      "the fourth line of the address of the coexecutor must be not visible" in {
      val ukAddressWithNoFourthLine = UkAddress("addr1", "addr2", None, None, "AA11AA", "GB")
      val coExecutor = CommonBuilder.buildCoExecutor copy (ukAddress = Some(ukAddressWithNoFourthLine))
      val rd = CommonBuilder.buildRegistrationDetails copy (areOthersApplyingForProbate = Some(true), coExecutors = Seq(coExecutor))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, rd)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rd))

      val result = deleteCoExecutorController.onPageLoad("1")(createFakeRequestWithReferrer(referrerURL=referrerURL, host="localhost:9070"))

      status(result) shouldBe(OK)
    }

    "if the coexecutor with given id exists - and the postcode line of the address does not exist - " +
      "the postcode line of the address of the coexecutor must be not visible" in {
      val ukAddressWithNoFourthLine = UkAddress("addr1", "addr2", None, None, "", "GB")
      val coExecutor = CommonBuilder.buildCoExecutor copy (ukAddress = Some(ukAddressWithNoFourthLine))
      val rd = CommonBuilder.buildRegistrationDetails copy (areOthersApplyingForProbate = Some(true), coExecutors = Seq(coExecutor))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, rd)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rd))

      val result = deleteCoExecutorController.onPageLoad("1")(createFakeRequestWithReferrer(referrerURL=referrerURL, host="localhost:9070"))

      status(result) shouldBe(OK)
    }

    "if the coexecutor with given id exists - and the postcode line of the address does exist - " +
      "the postcode line of the address of the coexecutor must be visible" in {
      val ukAddressWithNoFourthLine = UkAddress("addr1", "addr2", None, None, CommonBuilder.DefaultPostCode, "GB")
      val coExecutor = CommonBuilder.buildCoExecutor copy (ukAddress = Some(ukAddressWithNoFourthLine))
      val rd = CommonBuilder.buildRegistrationDetails copy (areOthersApplyingForProbate = Some(true), coExecutors = Seq(coExecutor))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, rd)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rd))

      val result = deleteCoExecutorController.onPageLoad("1")(createFakeRequestWithReferrer(referrerURL=referrerURL, host="localhost:9070"))

      status(result) shouldBe(OK)
      contentAsString(result) should include(CommonBuilder.DefaultPostCode)
    }

    "if the coexecutor with given id exists - and the countrycode line of the address is GB - " +
      "do not display it" in {
      val ukAddressWithGBCountryCode = UkAddress("addr1", "addr2", None, None, "", IhtProperties.ukIsoCountryCode)
      val coExecutor = CommonBuilder.buildCoExecutor copy (ukAddress = Some(ukAddressWithGBCountryCode))
      val rd = CommonBuilder.buildRegistrationDetails copy (areOthersApplyingForProbate = Some(true), coExecutors = Seq(coExecutor))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, rd)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rd))

      val result = deleteCoExecutorController.onPageLoad("1")(createFakeRequestWithReferrer(referrerURL=referrerURL, host="localhost:9070"))

      status(result) shouldBe(OK)
      contentAsString(result) should not include coExecutor.ukAddress.get.countryCode
    }

    "if the coexecutor with given id exists - and the countrycode line of the address is not GB - " +
      "it must be displayed" in {
      val ukAddressWithInternationalCountryCode = UkAddress("addr1", "addr2", None, None, "", "AU")
      val coExecutor = CommonBuilder.buildCoExecutor copy (ukAddress = Some(ukAddressWithInternationalCountryCode))
      val rd = CommonBuilder.buildRegistrationDetails copy (areOthersApplyingForProbate = Some(true), coExecutors = Seq(coExecutor))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, rd)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rd))

      val result = deleteCoExecutorController.onPageLoad("1")(createFakeRequestWithReferrer(referrerURL=referrerURL, host="localhost:9070"))

      status(result) shouldBe OK
      contentAsString(result) should include(countryName(coExecutor.ukAddress.get.countryCode))
    }

    "After a submit, when the coexecutor with the given id does not exist, should result in a server error" in {
      val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      val confirmForm = deleteConfirmationForm.fill(None)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, rd)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rd))


      val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = "localhost:9070", data = confirmForm.data.toSeq)

      val result = deleteCoExecutorController.onSubmit("2")(request)

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "After a submit, when the coexecutor with the given id exists, the registration details should have been properly modified" in {
      val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      val existingCoExec0 = CommonBuilder.buildCoExecutor
      val existingCoExec1 = CommonBuilder.buildCoExecutor copy (id = Some("2"), firstName = CommonBuilder.firstNameGenerator,
        lastName = CommonBuilder.surnameGenerator)
      val rdWithCoExecs = rd copy (coExecutors = Seq(existingCoExec0, existingCoExec1))
      val confirmForm = deleteConfirmationForm.fill(None)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, rdWithCoExecs)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithCoExecs))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rdWithCoExecs))


      val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = "localhost:9070", data = confirmForm.data.toSeq)

      val result = deleteCoExecutorController.onSubmit("1")(request)

      status(result) shouldBe SEE_OTHER
      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      capturedValue.coExecutors.length shouldBe 1
      capturedValue.coExecutors.head shouldBe existingCoExec1
    }

    "After a submit, when the coexecutor with the given id exists, if the storage operation fails the result must be a server error" in {
      val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      val existingCoExec0 = CommonBuilder.buildCoExecutor
      val existingCoExec1 = CommonBuilder.buildCoExecutor copy (id = Some("2"), firstName = CommonBuilder.firstNameGenerator,
        lastName = CommonBuilder.surnameGenerator)
      val rdWithCoExecs = rd copy (coExecutors = Seq(existingCoExec0, existingCoExec1))
      val confirmForm = deleteConfirmationForm.fill(None)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, rdWithCoExecs)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithCoExecs))
      createMockToStoreRegDetailsInCacheWithFailure(mockCachingConnector, Some(rdWithCoExecs))


      val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = "localhost:9070", data = confirmForm.data.toSeq)
      val result = deleteCoExecutorController.onSubmit("1")(request)

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
