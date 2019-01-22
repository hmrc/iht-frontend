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

package iht.controllers.registration.deceased

import iht.connector.CachingConnector
import iht.constants.IhtProperties
import iht.controllers.ControllerHelper.Mode
import iht.forms.registration.DeceasedForms._
import iht.models.{DeceasedDateOfDeath, DeceasedDetails, RegistrationDetails}
import iht.testhelpers.MockObjectBuilder._
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever, TestHelper}
import iht.utils.RegistrationKickOutHelper._
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class DeceasedPermanentHomeControllerTest
  extends RegistrationDeceasedControllerWithEditModeBehaviour[DeceasedPermanentHomeController]{

  val defaultDod = Some(DeceasedDateOfDeath(new LocalDate(2014, 1, 1)))

  override implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]


 //Create controller object and pass in mock.
 def controller = new DeceasedPermanentHomeController {
   override val cachingConnector = mockCachingConnector
   override val authConnector = mockAuthConnector

   override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
 }

  def controllerNotAuthorised = new DeceasedPermanentHomeController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  // Perform tests.
  "DeceasedPermanentHomeController" must {

    behave like securedRegistrationDeceasedController()

    "create the new form when there is no deceased details present" in {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails), None)
     // val messages = messagesApi.preferred(request)

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result: Form[DeceasedDetails] = controller.fillForm(registrationDetails)
      result mustBe a[Form[_]]
    }

    "contain Continue button when Page is loaded in normal mode" in {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails=CommonBuilder.buildRegistrationDetails copy (
        deceasedDateOfDeath = Some(DeceasedDateOfDeath(LocalDate.now)),
        deceasedDetails=Some(deceasedDetails))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onPageLoad()(createFakeRequestWithReferrer(
        referrerURL=referrerURL,host=host))
      status(result) mustBe(OK)

      contentAsString(result) must include(messagesApi("iht.continue"))
      contentAsString(result) must not include(messagesApi("site.link.cancel"))
    }

    "contain Continue and Cancel buttons when page is loaded in edit mode" in {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails=CommonBuilder.buildRegistrationDetails copy (deceasedDateOfDeath = Some(DeceasedDateOfDeath(LocalDate.now)),
        deceasedDetails=Some(deceasedDetails))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onEditPageLoad()(createFakeRequestWithReferrer(referrerURL=referrerURL,host=host))
      status(result) mustBe(OK)

      contentAsString(result) must include(messagesApi("iht.continue"))
      contentAsString(result) must include(messagesApi("site.link.cancel"))
    }

    "respond appropriately to a submit with valid values in all fields" in  {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(defaultDod, None, Some(deceasedDetails))
      val deceasedDetailsForm1 = deceasedPermanentHomeForm.fill(deceasedDetails)
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=deceasedDetailsForm1.data.toSeq, authRetrieveNino = false)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onSubmit()(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (
        Some(iht.controllers.registration.deceased.routes.AboutDeceasedController.onPageLoad().url))
    }

    "respond appropriately to an invalid submit: Missing mandatory fields" in {
      val deceasedDetails = DeceasedDetails(None, None, None, None, None, None, None, None, None)
      val registrationDetails = RegistrationDetails(defaultDod, None, Some(deceasedDetails))
      val deceasedDetailsForm1 = deceasedPermanentHomeForm.fill(deceasedDetails)
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=deceasedDetailsForm1.data.toSeq, authRetrieveNino = false)

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = await(controller.onSubmit()(request))
      status(result) mustBe(BAD_REQUEST)
    }

    "respond appropriately to a submit in edit mode with valid values in all fields" in {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(defaultDod, None, Some(deceasedDetails))
      val deceasedDetailsForm1 = deceasedPermanentHomeForm.fill(deceasedDetails)
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=deceasedDetailsForm1.data.toSeq, authRetrieveNino = false)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onEditSubmit()(request)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(iht.controllers.registration.routes.RegistrationSummaryController.onPageLoad().url))
    }

    "respond appropriately to an invalid submit in edit mode: Missing mandatory fields" in {
      val deceasedDetails = DeceasedDetails(None, None, None, None, None, None, None, None, None)
      val registrationDetails = RegistrationDetails(defaultDod, None, Some(deceasedDetails))
      val deceasedDetailsForm1 = deceasedPermanentHomeForm.fill(deceasedDetails)
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=deceasedDetailsForm1.data.toSeq, authRetrieveNino = false)

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      val result = await(controller.onEditSubmit()(request))
      status(result) mustBe(BAD_REQUEST)
    }

    "save valid data correctly when coming to this screen for the first time" in {
      val existingRegistrationDetails = RegistrationDetails(Some(DeceasedDateOfDeath(new LocalDate(1980, 1, 1))), None, None)
      val deceasedDetails = DeceasedDetails(domicile = Some(IhtProperties.domicileEnglandOrWales))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(existingRegistrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(existingRegistrationDetails))

      val form = deceasedPermanentHomeForm.fill(deceasedDetails)

      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host, data=form.data.toSeq, authRetrieveNino = false)

      val result = controller.onSubmit()(request)
      status(result) must be (SEE_OTHER)

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      capturedValue.deceasedDetails mustBe Some(deceasedDetails)
    }

    "return true if the guard conditions are true" in {
      val rd = CommonBuilder.buildRegistrationDetails copy (
        deceasedDateOfDeath = Some(DeceasedDateOfDeath(LocalDate.now)))
      controller.checkGuardCondition(rd, "") mustBe true
    }

    "return false if the guard conditions are false" in {
      val rd = CommonBuilder.buildRegistrationDetails copy (deceasedDateOfDeath = None)
      controller.checkGuardCondition(rd, "") mustBe false
    }

    def ensureRedirectOnKickout(domicile: String, kickoutReasonKey: String, mode: Mode.Value) = {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails copy (domicile = Some(domicile))
      val registrationDetails = RegistrationDetails(defaultDod, None, Some(deceasedDetails))
      val deceasedDetailsForm1 = deceasedPermanentHomeForm.fill(deceasedDetails)
      val request = createFakeRequestWithReferrerWithBody(referrerURL=referrerURL,host=host,
        data=deceasedDetailsForm1.data.toSeq, authRetrieveNino = false)

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreSingleValueInCache(
        cachingConnector=mockCachingConnector,
        singleValueReturn=Some(kickoutReasonKey))

      val result =
        if (mode == Mode.Standard) await(controller.onSubmit()(request))
        else await(controller.onEditSubmit()(request))

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (
        Some(iht.controllers.registration.routes.KickoutRegController.onPageLoad().url))
      verifyAndReturnStoredSingleValue(mockCachingConnector) match {
        case (cachedKey, cachedValue) =>
          cachedKey mustBe RegistrationKickoutReasonCachingKey
          cachedValue mustBe kickoutReasonKey
      }
    }

    "redirect to kickout page if domicile is Scotland" in {
      ensureRedirectOnKickout(TestHelper.domicileScotland, KickoutDeceasedDetailsLocationScotland, Mode.Standard)
    }

    "redirect to kickout page if domicile is Northern Ireland" in {
      ensureRedirectOnKickout(TestHelper.domicileNI, KickoutDeceasedDetailsLocationNI, Mode.Standard)
    }

    "redirect to kickout page if domicile is outside the UK" in {
      ensureRedirectOnKickout(TestHelper.domicileOther, KickoutDeceasedDetailsLocationOther, Mode.Standard)
    }

    "redirect to kickout page in edit mode if domicile is Scotland" in {
      ensureRedirectOnKickout(TestHelper.domicileScotland, KickoutDeceasedDetailsLocationScotland, Mode.Edit)
    }

    "redirect to kickout page in edit mode if domicile is Northern Ireland" in {
      ensureRedirectOnKickout(TestHelper.domicileNI, KickoutDeceasedDetailsLocationNI, Mode.Edit)
    }

    "redirect to kickout page in edit mode if domicile is outside the UK" in {
      ensureRedirectOnKickout(TestHelper.domicileOther, KickoutDeceasedDetailsLocationOther, Mode.Edit)
    }
  }
}
