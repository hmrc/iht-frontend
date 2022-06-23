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

package iht.controllers.application.gifts


import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.models.application.gifts.PreviousYearsGifts
import iht.testhelpers.CommonBuilder
import iht.views.html.application.gift.gifts_details
import play.api.http.Status._
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers.{await, contentAsString, redirectLocation, status => playStatus}
import play.api.test.{FakeHeaders, FakeRequest}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class GiftsDetailsControllerTests extends ApplicationControllerTest {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with GiftsDetailsController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val giftsDetailsView: gifts_details = app.injector.instanceOf[gifts_details]
  }

  val registrationDetails = CommonBuilder.buildRegistrationDetails copy (
    deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath),
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    ihtReference = Some("ABC1234567890")
    )

  // Create gift details controller object and pass in mock.
  def giftsDetailsController = new TestController {

    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector
  }

  def giftsDetailsControllerNotAuthorised = new TestController {

    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector

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

      val result = giftsDetailsControllerNotAuthorised.onPageLoad("1")(createFakeRequest(isAuthorised = false))

      redirectLocation(result) must be (Some(loginUrl))
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
      playStatus(result) must be(OK)
      contentAsString(result) must include(BigDecimal(1000000000).toString())
    }

    "On PageLoad if no Gift found then displays no gift details" in {

      val applicationModel = new ApplicationDetails(giftsList = Some(Seq(PreviousYearsGifts(Some("1"),
        Some(BigDecimal(1000000000)), Some(BigDecimal(1000)), Some("2008-4-4"), Some("2009-3-4")))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        registrationDetails,
        Some(applicationModel),
        getAppDetails = true)

      val result = giftsDetailsController.onPageLoad("0")(createFakeRequest())
      playStatus(result) must be(OK)
      contentAsString(result) must not include(BigDecimal(1000000000).toString)
    }

    "On PageLoad if no Application Details found then throw a RuntimeException" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        registrationDetails,
        None,
        getAppDetails = true)

      intercept[RuntimeException] {
        await(giftsDetailsController.onPageLoad("1")(createFakeRequest()))
      }
    }

    "On processSubmit if Gift found then redirect" in {
      val applicationModel = new ApplicationDetails(giftsList = Some(Seq(PreviousYearsGifts(Some("1"),
        Some(BigDecimal(1000)), Some(BigDecimal(1000)), Some("23"), Some("232")))))

      val filledPreviousYearGiftsForm = previousYearsGiftsForm.fill(CommonBuilder.buildPreviousYearsGifts.copy(Some("1"),
        Some(BigDecimal(1000)), Some(BigDecimal(1000)), Some("23"), Some("232")))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPreviousYearGiftsForm.data.toSeq: _*).withMethod("POST")

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        registrationDetails,
        Some(applicationModel),
        getAppDetails = true,

        saveAppDetails = true)

      val result = giftsDetailsController.onSubmit()(request)
      playStatus(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.SevenYearsGiftsValuesController.onPageLoad.url + "#" + appConfig.GiftsValueDetailID + "1"))
    }

    "On processSubmit if no Gift found then redirect" in {
      val applicationModel = new ApplicationDetails(giftsList = Some(Seq(PreviousYearsGifts(Some("3"),
        Some(BigDecimal(1234)), Some(BigDecimal(1234)), Some("76"), Some("123")))))

      val filledPreviousYearGiftsForm = previousYearsGiftsForm.fill(CommonBuilder.buildPreviousYearsGifts.copy(Some("1"),
        Some(BigDecimal(1000)), Some(BigDecimal(1000)), Some("23"), Some("232")))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPreviousYearGiftsForm.data.toSeq:_*).withMethod("POST")

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        registrationDetails,
        Some(applicationModel),
        getAppDetails = true)

      val result = giftsDetailsController.onSubmit()(request)
      playStatus(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.SevenYearsGiftsValuesController.onPageLoad.url))
    }

    "On processSubmit display correct error message if exemptions more than value" in {
      val applicationModel = new ApplicationDetails(giftsList = Some(Seq(PreviousYearsGifts(Some("3"),
        Some(BigDecimal(1234)), Some(BigDecimal(1234)), Some("76"), Some("123")))))

      val filledPreviousYearGiftsForm = previousYearsGiftsForm.fill(CommonBuilder.buildPreviousYearsGifts.copy(Some("1"),
        Some(BigDecimal(100)), Some(BigDecimal(1000)), Some("23"), Some("232")))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPreviousYearGiftsForm.data.toSeq: _*).withMethod("POST")

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        registrationDetails,
        Some(applicationModel),
        Some("site.link.cancel"),
        getAppDetails = true,

        getSingleValueFromCache = true)

      val result = giftsDetailsController.onSubmit()(request)
      playStatus(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messagesApi("error.giftsDetails.exceedsGivenAway"))
    }

    "On processSubmit display correct error message if exemptions entered but not value" in {
      val applicationModel = new ApplicationDetails(giftsList = Some(Seq(PreviousYearsGifts(Some("3"),
        Some(BigDecimal(1234)), Some(BigDecimal(1234)), Some("76"), Some("123")))))

      val filledPreviousYearGiftsForm = previousYearsGiftsForm.fill(CommonBuilder.buildPreviousYearsGifts.copy(Some("1"),
        None, Some(BigDecimal(1000)), Some("23"), Some("232")))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPreviousYearGiftsForm.data.toSeq: _*).withMethod("POST")

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        registrationDetails,
        Some(applicationModel),
        Some("site.link.cancel"),
        getAppDetails = true,

        getSingleValueFromCache = true)

      val result = giftsDetailsController.onSubmit()(request)
      playStatus(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messagesApi("error.giftsDetails.noValue"))
    }
  }
}
