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

package iht.controllers.application.debts

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder}
import iht.testhelpers.MockObjectBuilder._
import play.api.http.Status._
import uk.gov.hmrc.play.partials.FormPartialRetriever
import uk.gov.hmrc.http.HeaderCarrier

class DebtsOverviewControllerTest extends ApplicationControllerTest {

  implicit val hc = new HeaderCarrier()
  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def debtsOverviewController = new DebtsOverviewController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
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
      status(result) should be (OK)
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

      a[RuntimeException] shouldBe thrownBy {
        await(debtsOverviewController.onPageLoad()(createFakeRequest()))
      }
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      debtsOverviewController.onPageLoad(createFakeRequest()))
  }
}
