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

package iht.controllers.application.debts

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest

import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import play.api.http.Status._
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers.{await, status => playStatus}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class DebtsOverviewControllerTest extends ApplicationControllerTest {

  implicit val hc = new HeaderCarrier()

  protected abstract class TestController extends FrontendController(mockControllerComponents) with DebtsOverviewController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def debtsOverviewController = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  val registrationDetails = CommonBuilder.buildRegistrationDetails copy (
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    ihtReference = Some("ABC1234567890")
    )

  "Debts Overview" must {
    "return OK on Page Load" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(CommonBuilder.buildApplicationDetails),
        getAppDetails = true)

      val result = debtsOverviewController.onPageLoad()(createFakeRequest())
      playStatus(result) must be (OK)
    }

    "return Bad Request on internal server error" in {
      val registrationDetails = CommonBuilder.buildRegistrationDetails copy (
          deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
          ihtReference = Some(""))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        registrationDetails,
        appDetails = None,
        getAppDetails = true)

      a[RuntimeException] mustBe thrownBy {
        await(debtsOverviewController.onPageLoad()(createFakeRequest()))
      }
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      debtsOverviewController.onPageLoad(createFakeRequest()))
  }
}
