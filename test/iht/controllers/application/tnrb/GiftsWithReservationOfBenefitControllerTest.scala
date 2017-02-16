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

package iht.controllers.application.tnrb

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.TnrbForms._
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import org.joda.time.LocalDate
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._

/**
 *
 * Created by Vineet Tyagi on 14/01/16.
 *l
 */
class GiftsWithReservationOfBenefitControllerTest  extends ApplicationControllerTest{

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def giftsWithReservationOfBenefitController = new GiftsWithReservationOfBenefitController {
    override val authConnector = createFakeAuthConnector(isAuthorised=true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def giftsWithReservationOfBenefitControllerNotAuthorised = new GiftsWithReservationOfBenefitController {
    override val authConnector = createFakeAuthConnector(isAuthorised=false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  "GiftsWithReservationOfBenefitController" must {

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = giftsWithReservationOfBenefitController.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to ida login page on Submit if the user is not logged in" in {
      val result = giftsWithReservationOfBenefitController.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(widowCheck= Some(CommonBuilder.buildWidowedCheck))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val result = giftsWithReservationOfBenefitController.onPageLoad (createFakeRequest())
      status(result) shouldBe OK
    }

    "show predeceased name on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(increaseIhtThreshold =
        Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(CommonBuilder.firstNameGenerator),
          lastName = Some(CommonBuilder.surnameGenerator))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val result = giftsWithReservationOfBenefitController.onPageLoad (createFakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include(messagesApi("iht.estateReport.tnrb.giftsWithReservationOfBenefit.question",
        CommonBuilder.DefaultFirstName + " " +CommonBuilder.DefaultLastName))
    }

    "save application and go to Tnrb Overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(increaseIhtThreshold =
        Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(CommonBuilder.firstNameGenerator),
          lastName = Some(CommonBuilder.surnameGenerator))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val withGiftsWithReservationOfBenefitValue = CommonBuilder.buildTnrbEligibility

      val filledGiftsWithReservationOfBenefitForm = partnerGiftWithResToOtherForm.fill(withGiftsWithReservationOfBenefitValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledGiftsWithReservationOfBenefitForm.data.toSeq: _*)

      val result = giftsWithReservationOfBenefitController.onSubmit (request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should be(Some(routes.TnrbOverviewController.onPageLoad().url))
    }

    "go to KickOut page if gifts with reservation of benefit given other than spouse" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(widowCheck= Some(CommonBuilder.buildWidowedCheck))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val withGiftsWithReservationOfBenefitValue = CommonBuilder.buildTnrbEligibility.copy(isPartnerGiftWithResToOther = Some(true))

      val filledGiftsWithReservationOfBenefitForm = partnerGiftWithResToOtherForm.fill(withGiftsWithReservationOfBenefitValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledGiftsWithReservationOfBenefitForm.data.toSeq: _*)

      val result = giftsWithReservationOfBenefitController.onSubmit (request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should be(Some(iht.controllers.application.routes.KickoutController.onPageLoad.url))
    }

    "go to successful Tnrb page on submit when its satisfies happy path" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(increaseIhtThreshold =
        Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(CommonBuilder.firstNameGenerator),
          lastName = Some(CommonBuilder.surnameGenerator),
          dateOfMarriage= Some(new LocalDate(1984, 12, 11)))),
        widowCheck = Some(CommonBuilder.buildWidowedCheck))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val withGiftsWithReservationOfBenefitValue = CommonBuilder.buildTnrbEligibility.copy(isPartnerGiftWithResToOther = Some(false))

      val filledGiftsWithReservationOfBenefitForm = partnerGiftWithResToOtherForm.fill(withGiftsWithReservationOfBenefitValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledGiftsWithReservationOfBenefitForm.data.toSeq: _*)

      val result = giftsWithReservationOfBenefitController.onSubmit (request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should be(Some(routes.TnrbSuccessController.onPageLoad().url))
    }
  }
}
