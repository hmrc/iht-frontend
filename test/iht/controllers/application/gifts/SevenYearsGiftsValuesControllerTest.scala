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

package iht.controllers.application.gifts

import iht.config.AppConfig
import iht.controllers.application.{ApplicationControllerTest, KickoutAppController}
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import iht.views.HtmlSpec
import org.jsoup.nodes.Document
import play.api.i18n.MessagesApi
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

/**
 * Created by james on 14/01/16.
 */
class SevenYearsGiftsValuesControllerTest extends ApplicationControllerTest with HtmlSpec {

  override implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val hc = new HeaderCarrier()

  protected abstract class TestController extends FrontendController(mockControllerComponents) with SevenYearsGiftsValuesController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }


  def sevenYearsGiftsValuesController = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def sevenYearsGiftsValuesControllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  val registrationDetails = CommonBuilder.buildRegistrationDetails copy(
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    ihtReference = Some("ABC1234567890"),
    deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath)
  )

  "SevenYearsGiftsValuesController" must {
    "return OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = registrationDetails,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        getAppDetailsFromCache = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = sevenYearsGiftsValuesController.onPageLoad()(createFakeRequest(isAuthorised = true))
      status(result) must be(OK)
    }

    "redirect to ida login page on PageLoad if the user is not logged in" in {
      val result = sevenYearsGiftsValuesControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(loginUrl))
    }

    "page loads when there are no gifts in persist storage" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = registrationDetails,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        getAppDetailsFromCache = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = sevenYearsGiftsValuesController.onPageLoad()(createFakeRequest(isAuthorised = true))
      status(result) must be(OK)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      sevenYearsGiftsValuesController.onPageLoad(createFakeRequest()))
  }

  private def testGiftYearLinkData(doc: Document, yearId: String) = {
    val yearLink = doc.getElementById(s"edit-gift-$yearId")
    assertEqualsValue(doc, s"a#edit-gift-$yearId span",
      messagesApi("iht.change"))
    yearLink.attr("href") mustBe
      routes.GiftsDetailsController.onPageLoad(yearId).url
  }
}
