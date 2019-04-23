/*
 * Copyright 2019 HM Revenue & Customs
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

/*
 * Copyright 2019 HM Revenue & Customs
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
import iht.controllers.registration.RegistrationControllerTest
import iht.forms.registration.CoExecutorForms
import iht.models.{RegistrationDetails, UkAddress}
import iht.testhelpers.CommonBuilder._

import iht.testhelpers.{CommonBuilder, ContentChecker, MockFormPartialRetriever, NinoBuilder}
import iht.utils.StringHelper
import org.joda.time._
import org.scalatest.BeforeAndAfter
import play.api.data.Form
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class OtherPersonsAddressControllerTest extends RegistrationControllerTest with BeforeAndAfter with StringHelper with CoExecutorForms {

  val inProgressCoExecutor = CommonBuilder.buildCoExecutorPersonalDetails(Some("2")) copy
    (firstName = CommonBuilder.firstNameGenerator, lastName = CommonBuilder.surnameGenerator, nino = NinoBuilder.defaultNino,
      dateOfBirth = new LocalDate(new org.joda.time.DateTime(1980, 5, 1, 0, 0)))

  implicit val messages: Messages = mockControllerComponents.messagesApi.preferred(Seq(Lang.defaultLang)).messages
  protected abstract class TestController extends FrontendController(mockControllerComponents) with OtherPersonsAddressController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  lazy val appConfig = mockAppConfig

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

  "OtherPersonsAddressController" must {
    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = controllerNotAuthorised.onPageLoadUK("1")(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(loginUrl))
    }

    "redirect to GG login page on Submit if the user is not logged in" in {
      val result = controllerNotAuthorised.onSubmitUK("1")(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(loginUrl))
    }

    "load UK page when the coExecutors address has not been entered" in {
      val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = controller.onPageLoadUK("1")(
        createFakeRequestWithUri("http://localhost:9070/inheritance-tax/registration/other-persons-address-in-uk/1", authRetrieveNino = false))

      status(result) must be(OK)
      ContentChecker.stripLineBreaks(contentAsString(result)) must include(
        messagesApi("page.iht.registration.others-applying-for-probate-address.sectionTitlePostfix",
          addApostrophe(rd.coExecutors(0).name)))
      val msg = escapeApostrophes(
        messagesApi("iht.registration.changeAddressToAbroad"))
      contentAsString(result) must include(msg)
      contentAsString(result) must include(messagesApi("page.iht.registration.others-applying-for-probate-address.address.guidance"))
    }

    "load international page when the coExecutors address has not been entered" in {
      val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = controller.onPageLoadAbroad("1")(
        createFakeRequestWithUri("http://localhost:9070/inheritance-tax/registration/other-persons-address-abroad/1", authRetrieveNino = false))

      status(result) must be(OK)
      ContentChecker.stripLineBreaks(contentAsString(result)) must include(
        messagesApi("page.iht.registration.others-applying-for-probate-address.sectionTitlePostfix",
          addApostrophe(rd.coExecutors(0).name)))
      val msg = escapeApostrophes(
        messagesApi("iht.registration.changeAddressToUK"))
      contentAsString(result) must include(msg)
      contentAsString(result) must include(messagesApi("page.iht.registration.others-applying-for-probate-address.address.guidance"))
    }

    "raise an error when trying to load the UK view when trying to add a co-executor but " +
      "the id is not the id of a previously entered address" in {
      val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      val rdWithOthersApplyingForProbateAndOneOther = rd copy(areOthersApplyingForProbate = Some(true),
        coExecutors = Seq(CommonBuilder.buildCoExecutor))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithOthersApplyingForProbateAndOneOther))
      val result = controller.onPageLoadUK("1")(
        createFakeRequestWithUri("http://localhost:9070/inheritance-tax/registration/other-persons-address-in-uk/2", authRetrieveNino = false))
      status(result) mustBe SEE_OTHER
    }

    "raise an error when trying to load the international  view when trying to add a co-executor but" +
      " the id is not the id of a previously entered address" in {
      val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      val rdWithOthersApplyingForProbateAndOneOther = rd copy(areOthersApplyingForProbate = Some(true),
        coExecutors = Seq(CommonBuilder.buildCoExecutor))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithOthersApplyingForProbateAndOneOther))
      val result = controller.onPageLoadAbroad("1")(
        createFakeRequestWithUri("http://localhost:9070/inheritance-tax/registration/other-persons-address-abroad/2", authRetrieveNino = false))
      status(result) mustBe SEE_OTHER
    }

    "load the UK display when the coExecutors address has been entered" in {
      val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      val existingCoExec = CommonBuilder.buildCoExecutor
      val rdWithCoExecs = rd copy (coExecutors = Seq(existingCoExec))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithCoExecs))

      val result = controller.onPageLoadUK("1")(
        createFakeRequestWithUri("http://localhost:9070/inheritance-tax/registration/other-persons-address-in-uk/1", authRetrieveNino = false))

      status(result) must be(OK)
      contentAsString(result) must include("addr1")
      contentAsString(result) must include("addr2")
      contentAsString(result) must include("addr3")
      contentAsString(result) must include("addr4")
      contentAsString(result) must include(CommonBuilder.DefaultUkAddress.postCode)
    }

    "load the international display when the coExecutors address has been entered" in {
      val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      val existingCoExec = CommonBuilder.buildCoExecutor copy(
        ukAddress = Some(UkAddress("addr1", "addr2", Some("addr3"), Some("addr4"), "", "AU")),
        isAddressInUk = Some(false))
      val rdWithCoExecs = rd copy (coExecutors = Seq(existingCoExec))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithCoExecs))

      val result = controller.onPageLoadAbroad("1")(
        createFakeRequestWithUri("http://localhost:9070/inheritance-tax/registration/other-persons-address-abroad/1", authRetrieveNino = false))

      status(result) must be(OK)
      contentAsString(result) must include("addr1")
      contentAsString(result) must include("addr2")
      contentAsString(result) must include("addr3")
      contentAsString(result) must include("addr4")
      contentAsString(result) must include("AU")
    }


    "load the uk address when the uk page load is routed to" in {
      val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = controller.onPageLoadUK("1")(
        createFakeRequestWithUri("http://localhost:9070/inheritance-tax/registration/other-persons-address-in-uk/1", authRetrieveNino = false))

      status(result) must be(OK)
      contentAsString(result) must include(messagesApi("iht.postcode"))
      val msg = escapeApostrophes(
        messagesApi("iht.registration.changeAddressToAbroad"))
      contentAsString(result) must include(msg)
    }


    "load the international address when the international page load is routed to" in {
      val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))

      val result = controller.onPageLoadAbroad("1")(
        createFakeRequestWithUri("http://localhost:9070/inheritance-tax/registration/other-persons-address-abroad/1", authRetrieveNino = false))

      status(result) must be(OK)
      contentAsString(result) must include(
        messagesApi("iht.country"))
      val msg = escapeApostrophes(
        messagesApi("iht.registration.changeAddressToUK"))
      contentAsString(result) must include(msg)
    }

    "load the UK view when creating a new co-executor and another already exists" in {
      val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      val existingCoExec = CommonBuilder.buildCoExecutor
      val inProgressCoExecutor = CommonBuilder.buildCoExecutorPersonalDetails(Some("2")) copy
        (firstName = CommonBuilder.firstNameGenerator, lastName = CommonBuilder.surnameGenerator, nino = NinoBuilder.defaultNino,
          dateOfBirth = new LocalDate(new org.joda.time.DateTime(1980, 5, 1, 0, 0)))
      val rdWithCoExecs = rd copy (coExecutors = Seq(existingCoExec, inProgressCoExecutor))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithCoExecs))

      val result = controller.onPageLoadUK("2")(
        createFakeRequestWithUri("http://localhost:9070/inheritance-tax/registration/other-persons-address-in-uk/2", authRetrieveNino = false))

      status(result) must be(OK)
      ContentChecker.stripLineBreaks(contentAsString(result)) must include(
        messagesApi("page.iht.registration.others-applying-for-probate-address.sectionTitlePostfix",
          addApostrophe(rdWithCoExecs.coExecutors(1).name)))
      val msg = escapeApostrophes(
        messagesApi("iht.registration.changeAddressToAbroad"))
      contentAsString(result) must include(msg)
    }

    "load the international view when creating a new co-executor and another already exists" in {
      val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      val existingCoExec = CommonBuilder.buildCoExecutor
      val rdWithCoExecs = rd copy (coExecutors = Seq(existingCoExec, inProgressCoExecutor))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithCoExecs))

      val result = controller.onPageLoadAbroad("2")(
        createFakeRequestWithUri("http://localhost:9070/inheritance-tax/registration/other-persons-address-abroad/2", authRetrieveNino = false))

      status(result) must be(OK)
      ContentChecker.stripLineBreaks(contentAsString(result)) must include(
        messagesApi("page.iht.registration.others-applying-for-probate-address.sectionTitlePostfix",
          addApostrophe(rdWithCoExecs.coExecutors(1).name)))
      val msg = escapeApostrophes(
        messagesApi("iht.registration.changeAddressToUK"))
      contentAsString(result) must include(msg)
    }


    "raise an error when UK view accessed for a non-existent co-executor" in {
      val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      val existingCoExec = CommonBuilder.buildCoExecutor
      val rdWithCoExecs = rd copy (coExecutors = Seq(existingCoExec))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithCoExecs))

      val result = controller.onPageLoadUK("2")(
        createFakeRequestWithUri("http://localhost:9070/inheritance-tax/registration/other-persons-address-uk/2", authRetrieveNino = false))
      status(result) mustBe SEE_OTHER
    }

    "raise an error when international view accessed for a non-existent co-executor" in {
      val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      val existingCoExec = CommonBuilder.buildCoExecutor
      val rdWithCoExecs = rd copy (coExecutors = Seq(existingCoExec))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithCoExecs))

      val result = controller.onPageLoadAbroad("2")(
        createFakeRequestWithUri("http://localhost:9070/inheritance-tax/registration/other-persons-address-abroad/2", authRetrieveNino = false))
      status(result) mustBe SEE_OTHER
    }

    "raise an error when trying to add a co-executor but no first name or last name," +
      " or NINO, or DOB has has been entered for the co executor" in {
      val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      val rdWithNoAnswer = rd copy (coExecutors = Seq(CommonBuilder.buildCoExecutor))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithNoAnswer))

      val result = controller.onPageLoadAbroad("1")(createFakeRequest(authRetrieveNino = false))
      status(result) mustBe SEE_OTHER
    }

    "raise an error when first name has not been entered from the previous page is not present" in {
      val rd = CommonBuilder.buildRegistrationDetailsWithCoExecutors
      val invalidCoExcutor = inProgressCoExecutor copy (firstName = "")
      val rdWithPartialCoExec = rd copy (coExecutors = invalidCoExcutor +: rd.coExecutors)
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rdWithPartialCoExec))


      val result = controller.onPageLoadUK("2")(
        createFakeRequestWithUri("http://localhost:9070/inheritance-tax/registration/other-persons-address-uk/2", authRetrieveNino = false))
      status(result) mustBe SEE_OTHER
    }

    "respond appropriately to a submit in the UK with a plausible address" in {
      val registrationDetails = CommonBuilder.buildRegistrationDetails copy (
        coExecutors = Seq(inProgressCoExecutor copy (id = Some("1"))))
      val addressForm = coExecutorAddressUkForm.fill(CommonBuilder.DefaultUkAddress)
      val request = createFakeRequestWithReferrerWithBody(
        referrerURL = "http://localhost:9070/inheritance-tax/registration/other-persons-address-uk/1",
        host = host, data = addressForm.data.toSeq, authRetrieveNino = false)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onSubmitUK("1")(request)
      status(result) mustBe (SEE_OTHER)
    }
  }

  "respond appropriately to a submit abroad with a plausible address" in {
    val registrationDetails = CommonBuilder.buildRegistrationDetails copy (
      coExecutors = Seq(inProgressCoExecutor copy(id = Some("1"), isAddressInUk = Some(false))))
    val addressForm = coExecutorAddressAbroadForm.fill(
      CommonBuilder.DefaultUkAddress copy(postCode = "", countryCode = "AU"))
    val request = createFakeRequestWithReferrerWithBody(
      referrerURL = "http://localhost:9070/inheritance-tax/registration/other-persons-address-abroad/1",
      host = host, data = addressForm.data.toSeq, authRetrieveNino = false)

    createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
    createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
    createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

    val result = controller.onSubmitAbroad("1")(request)

    status(result) mustBe (SEE_OTHER)
  }

  "store plausible data when submitted" in {
    val form: Form[UkAddress] = coExecutorAddressUkForm.fill(CommonBuilder.DefaultUkAddress)
    val registrationDetails = Some(CommonBuilder.buildRegistrationDetails copy (
      coExecutors = Seq(inProgressCoExecutor copy (id = Some("1")))))
    createMockToGetRegDetailsFromCache(mockCachingConnector, registrationDetails)

    implicit val request = createFakeRequestWithReferrerWithBody(
      referrerURL = "http://localhost:9070/inheritance-tax/registration/other-persons-address-abroad/1",
      host = host, data = form.data.toSeq, authRetrieveNino = false)

    createMockToStoreRegDetailsInCache(mockCachingConnector, registrationDetails)

    val result = controller.onSubmitUK("1")(request)
    status(result)

    val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
    capturedValue.coExecutors.head.ukAddress mustBe Some(CommonBuilder.DefaultUkAddress)
  }

  "show an error when the first address line is blank when submitted to the UK route" in {
    val address = CommonBuilder.DefaultUkAddress copy (ukAddressLine1 = "")
    checkForErrorOnSubmissionOfModelToUKRoute(address, "error.address.give")
  }

  "show an error when the second address line is blank when submitted to the UK route" in {
    val address = CommonBuilder.DefaultUkAddress copy (ukAddressLine2 = "")
    checkForErrorOnSubmissionOfModelToUKRoute(address, "error.address.give")
  }

  "show an error when the postcode is blank when submitted to the UK route" in {
    val address = CommonBuilder.DefaultUkAddress copy (postCode = "")
    checkForErrorOnSubmissionOfModelToUKRoute(address, "error.address.givePostcode")
  }

  "show an error when the first address line is blank when submitted to the international route" in {
    val address = CommonBuilder.DefaultUkAddress copy (ukAddressLine1 = "")
    checkForErrorOnSubmissionOfModelToInternationalRoute(address, "error.address.give")
  }

  "show an error when the second address line is blank when submitted to the international route" in {
    val address = CommonBuilder.DefaultUkAddress copy (ukAddressLine2 = "")
    checkForErrorOnSubmissionOfModelToInternationalRoute(address, "error.address.give")
  }

  "show an error when the country code is blank when submitted to the international route" in {
    val address = CommonBuilder.DefaultUkAddress copy(postCode = "", countryCode = "")
    checkForErrorOnSubmissionOfModelToInternationalRoute(address, "error.country.select")
  }

  "If you load the uk page and the coexecutors address is set up as an international address " +
    "(postcode is blank but countrycode is not uk) show an empty address" in {
    val internationalAddress = UkAddress("addr1", "addr2", Some("addr3"), Some("addr4"), "", "AU")
    val internationalCoExecutor = CommonBuilder.buildCoExecutor copy(
      ukAddress = Some(internationalAddress), isAddressInUk = Some(false))
    val registrationDetails = CommonBuilder.buildRegistrationDetails copy (
      coExecutors = Seq(internationalCoExecutor copy (id = Some("1"))))

    createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
    createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
    createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

    val result = controller.onPageLoadUK("1")(
      createFakeRequestWithUri("http://localhost:9070/inheritance-tax/registration/other-persons-address-uk/1", authRetrieveNino = false))

    status(result) must be(OK)
    contentAsString(result) must not include ("addr1")
    contentAsString(result) must not include ("addr2")
    contentAsString(result) must not include ("addr3")
    contentAsString(result) must not include ("addr4")
    contentAsString(result) must not include ("AU")
  }

  "If you load the international page and the coexecutors address is set up as an uk address " +
    "(postcode is given but countrycode is uk) show an empty address" in {
    val ukAddress = UkAddress("addr1", "addr2", Some("addr3"), Some("addr4"), CommonBuilder.DefaultPostCode)
    val ukCoExecutor = CommonBuilder.buildCoExecutor copy(ukAddress = Some(ukAddress), isAddressInUk = Some(true))
    val registrationDetails = CommonBuilder.buildRegistrationDetails copy (
      coExecutors = Seq(ukCoExecutor copy (id = Some("1"))))

    createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
    createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
    createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

    val result = controller.onPageLoadAbroad("1")(
      createFakeRequestWithUri("http://localhost:9070/inheritance-tax/registration/other-persons-address-abroad/1", authRetrieveNino = false))

    status(result) must be(OK)
    contentAsString(result) must not include ("addr1")
    contentAsString(result) must not include ("addr2")
    contentAsString(result) must not include ("addr3")
    contentAsString(result) must not include ("addr4")
    contentAsString(result) must not include (CommonBuilder.DefaultPostCode)
  }

  "if you submit a page with errors to the UK view it displays errors on the uk view" in {
    val ukAddress = UkAddress("addr1", "addr2", Some("addr3"), Some("addr4"), "")
    val ukCoExecutor = CommonBuilder.buildCoExecutor copy (ukAddress = Some(ukAddress))
    val registrationDetails = CommonBuilder.buildRegistrationDetails copy (
      coExecutors = Seq(ukCoExecutor copy (id = Some("1"))))

    createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
    createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
    createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

    val result = controller.onPageLoadUK("1")(
      createFakeRequestWithUri("http://localhost:9070/inheritance-tax/registration/other-persons-address-uk/1", authRetrieveNino = false))

    status(result) must be(OK)
    contentAsString(result) must include(escapeApostrophes(
      messagesApi("iht.registration.changeAddressToAbroad")))

  }

  "if you submit a page with errors to the international view and it has errors," +
    " it displays the errors in the international view" in {
    val internationalAddress = UkAddress("addr1", "addr2", Some("addr3"), Some("addr4"), "", "")
    val internationalCoExecutor = CommonBuilder.buildCoExecutor copy (ukAddress = Some(internationalAddress))
    val registrationDetails = CommonBuilder.buildRegistrationDetails copy (
      coExecutors = Seq(internationalCoExecutor copy (id = Some("1"))))

    createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
    createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
    createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

    val result = controller.onPageLoadAbroad("1")(
      createFakeRequestWithUri("http://localhost:9070/inheritance-tax/registration/other-persons-address-Abroad/1", authRetrieveNino = false))

    status(result) must be(OK)
    contentAsString(result) must include(escapeApostrophes(
      messagesApi("iht.registration.changeAddressToUK")))
  }

  "raise an error when a submitting a plausible UK address but the storage fails" in {
    val registrationDetails = CommonBuilder.buildRegistrationDetails copy (
      coExecutors = Seq(inProgressCoExecutor copy (id = Some("1"))))
    val addressForm = coExecutorAddressUkForm.fill(CommonBuilder.DefaultUkAddress)
    val request = createFakeRequestWithReferrerWithBody(
      referrerURL = "http://localhost:9070/inheritance-tax/registration/other-persons-address-uk/1",
      host = host, data = addressForm.data.toSeq, authRetrieveNino = false)

    createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
    createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
    createMockToStoreRegDetailsInCacheWithFailure(mockCachingConnector, Some(registrationDetails))

    val result = controller.onSubmitUK("1")(request)
    status(result) mustBe (INTERNAL_SERVER_ERROR)
  }

  type SubmissionFunc = String => Action[AnyContent]

  def checkForErrorOnSubmissionOfModelToUKRoute(address: UkAddress, expectedError: String): Unit = {
    val result = submitCoExecutorPersonalDetailsModel(
      CommonBuilder.buildRegistrationDetailsWithCoExecutors, address, "1", controller.onSubmitUK)
    status(result) must be(BAD_REQUEST)
    contentAsString(result) must include(messagesApi(expectedError))
  }

  def checkForErrorOnSubmissionOfModelToInternationalRoute(address: UkAddress, expectedError: String): Unit = {
    val result = submitCoExecutorPersonalDetailsModel(
      CommonBuilder.buildRegistrationDetailsWithCoExecutors, address, "1", controller.onSubmitAbroad)
    status(result) must be(BAD_REQUEST)
    contentAsString(result) must include(messagesApi(expectedError))
  }

  def prepareForm(address: UkAddress): Form[UkAddress] = {
    coExecutorAddressUkForm.fill(address)
  }

  def submitCoExecutorPersonalDetailsModel(rd: RegistrationDetails, address: UkAddress, submissionId: String,
                                           submissionFunc: SubmissionFunc): Future[Result] = {
    val form = prepareForm(address)
    submitCoExecutorAddress(rd, form.data.toSeq, submissionId, submissionFunc)
  }

  def submitCoExecutorAddress(rd: RegistrationDetails, detailsToSubmit: Seq[(String, String)], submissionId: String,
                              submissionFunc: SubmissionFunc): Future[Result] = {
    createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))
    createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rd))

    implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
      host = host, data = detailsToSubmit, authRetrieveNino = false)

    submissionFunc(submissionId)(request)
  }
}
