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

package iht.controllers.application.gifts

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models.application.gifts.PreviousYearsGifts
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import play.api.http.Status._
import play.api.i18n.Messages
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation}
import play.api.test.{FakeHeaders, FakeRequest}
import uk.gov.hmrc.play.http.HeaderCarrier

/**
 * Created by jamestuttle on 09/10/15.
 */

class GiftsDetailsControllerTests extends ApplicationControllerTest {

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  val registrationDetails = CommonBuilder.buildRegistrationDetails copy (
    deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath),
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    ihtReference = Some("ABC1234567890")
    )

  // Create gift details controller object and pass in mock.
  def giftsDetailsController = new GiftsDetailsController {
    def metrics : Metrics = Metrics
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val ihtConnector = mockIhtConnector
    override val isWhiteListEnabled = false
  }

  def giftsDetailsControllerNotAuthorised = new GiftsDetailsController {
    def metrics : Metrics = Metrics
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val ihtConnector = mockIhtConnector
    override val isWhiteListEnabled = false
  }

  implicit val headerCarrier = FakeHeaders()
  implicit val request = FakeRequest()
  implicit val hc = new HeaderCarrier

  // Perform tests.
  "Gifts Details Controller" must {

    "on PageLoad if user is not logged in then redirect to login page" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        registrationDetails,
        Some(applicationDetails),
        getAppDetails = true)

      val result = giftsDetailsControllerNotAuthorised.onPageLoad("1")(createFakeRequest())

      redirectLocation(result) should be (Some(loginUrl))
    }

    "On PageLoad if Gift found then display gift details" in {
      val applicationModel = new ApplicationDetails(giftsList = Some(Seq(PreviousYearsGifts(Some("1"),
        Some(BigDecimal(1000000000)), Some(BigDecimal(1000)), Some("23"), Some("232")))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        registrationDetails,
        Some(applicationModel),
        getAppDetails = true)

      val result = giftsDetailsController.onPageLoad("1")(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(BigDecimal(1000000000).toString())
    }

    "On PageLoad if no Gift found then displays no gift details" in {

      val applicationModel = new ApplicationDetails(giftsList = Some(Seq(PreviousYearsGifts(Some("1"),
        Some(BigDecimal(1000000000)), Some(BigDecimal(1000)), Some("23"), Some("232")))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        registrationDetails,
        Some(applicationModel),
        getAppDetails = true)

      val result = giftsDetailsController.onPageLoad("0")(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should not include(BigDecimal(1000000000).toString)
    }

    "On PageLoad if no Application Details found then displays no gift details" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        registrationDetails,
        None)

      val result = giftsDetailsController.onPageLoad("1")(createFakeRequest())
      status(result) should be(OK)
    }

    "On processSubmit if Gift found then redirect" in {
      val applicationModel = new ApplicationDetails(giftsList = Some(Seq(PreviousYearsGifts(Some("1"),
        Some(BigDecimal(1000)), Some(BigDecimal(1000)), Some("23"), Some("232")))))

      val filledPreviousYearGiftsForm = previousYearsGiftsForm.fill(CommonBuilder.buildPreviousYearsGifts.copy(Some("1"),
        Some(BigDecimal(1000)), Some(BigDecimal(1000)), Some("23"), Some("232")))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPreviousYearGiftsForm.data.toSeq: _*)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        registrationDetails,
        Some(applicationModel),
        getAppDetails = true,

        saveAppDetails = true)

      val result = giftsDetailsController.onSubmit()(request)
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(routes.SevenYearsGiftsValuesController.onPageLoad().url))
    }

    "On processSubmit if no Gift found then redirect" in {
      val applicationModel = new ApplicationDetails(giftsList = Some(Seq(PreviousYearsGifts(Some("3"),
        Some(BigDecimal(1234)), Some(BigDecimal(1234)), Some("76"), Some("123")))))

      val filledPreviousYearGiftsForm = previousYearsGiftsForm.fill(CommonBuilder.buildPreviousYearsGifts.copy(Some("1"),
        Some(BigDecimal(1000)), Some(BigDecimal(1000)), Some("23"), Some("232")))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPreviousYearGiftsForm.data.toSeq:_*)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        registrationDetails,
        Some(applicationModel),
        getAppDetails = true)

      val result = giftsDetailsController.onSubmit()(request)
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(routes.SevenYearsGiftsValuesController.onPageLoad().url))
    }

    "On processSubmit display correct error message if exemptions more than value" in {
      val applicationModel = new ApplicationDetails(giftsList = Some(Seq(PreviousYearsGifts(Some("3"),
        Some(BigDecimal(1234)), Some(BigDecimal(1234)), Some("76"), Some("123")))))

      val filledPreviousYearGiftsForm = previousYearsGiftsForm.fill(CommonBuilder.buildPreviousYearsGifts.copy(Some("1"),
        Some(BigDecimal(100)), Some(BigDecimal(1000)), Some("23"), Some("232")))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPreviousYearGiftsForm.data.toSeq: _*)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        registrationDetails,
        Some(applicationModel),
        Some("site.link.cancel"),
        getAppDetails = true,

        getSingleValueFromCache = true)

      val result = giftsDetailsController.onSubmit()(request)
      status(result) should be(BAD_REQUEST)
      contentAsString(result) should include(Messages("error.giftsDetails.exceedsGivenAway"))
    }

    "On processSubmit display correct error message if exemptions entered but not value" in {
      val applicationModel = new ApplicationDetails(giftsList = Some(Seq(PreviousYearsGifts(Some("3"),
        Some(BigDecimal(1234)), Some(BigDecimal(1234)), Some("76"), Some("123")))))

      val filledPreviousYearGiftsForm = previousYearsGiftsForm.fill(CommonBuilder.buildPreviousYearsGifts.copy(Some("1"),
        None, Some(BigDecimal(1000)), Some("23"), Some("232")))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPreviousYearGiftsForm.data.toSeq: _*)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        registrationDetails,
        Some(applicationModel),
        Some("site.link.cancel"),
        getAppDetails = true,

        getSingleValueFromCache = true)

      val result = giftsDetailsController.onSubmit()(request)
      status(result) should be(BAD_REQUEST)
      contentAsString(result) should include(Messages("error.giftsDetails.noValue"))
    }
  }
}
