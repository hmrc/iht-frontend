/*
 * Copyright 2021 HM Revenue & Customs
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

package iht.controllers.application.exemptions.qualifyingBody

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.models.application.ApplicationDetails
import iht.models.application.exemptions.QualifyingBody
import iht.testhelpers.CommonBuilder._
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class QualifyingBodiesOverviewControllerTest extends ApplicationControllerTest {

  implicit val hc = new HeaderCarrier()

  val appDetailsWithNoQualifyingBodies = CommonBuilder.buildApplicationDetails.copy(
    allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
      qualifyingBody = Some(CommonBuilder.buildBasicExemptionElement.copy(isSelected = Some(true))))))

  val appDetailsWithQualifyingBodyUnanswered = CommonBuilder.buildApplicationDetails

  val qualifyingBody1 = QualifyingBody(Some("1"), Some("QB 1"), Some(123.45))
  val qualifyingBody2 = QualifyingBody(Some("2"), Some("QB 2"), Some(678.9))

  val appDetailsWithQualifyingBodies = appDetailsWithNoQualifyingBodies.copy(
    qualifyingBodies = Seq(qualifyingBody1, qualifyingBody2)
  )

  def setupMocks(appDetails: ApplicationDetails) = createMocksForApplication(mockCachingConnector,
    mockIhtConnector,
    appDetails = Some(appDetails),
    getAppDetails = true,
    saveAppDetails = true,
    storeAppDetailsInCache = true)

  def request = createFakeRequest()

  def requestUnauthorised = createFakeRequest(isAuthorised = false)

  def controller = getController()

  def controllerNotAuthorised = getController(authorised = false)

  protected abstract class TestController extends FrontendController(mockControllerComponents) with QualifyingBodiesOverviewController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  private def getController(authorised: Boolean = true) = new TestController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
//    override val authConnector = createFakeAuthConnector(isAuthorised = authorised)
    override val authConnector = mockAuthConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  "Qualifying Bodies Overview Controller" must {
    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = controllerNotAuthorised.onPageLoad()(requestUnauthorised)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(loginUrl)
    }

    "throw an illegal page navigation error when accessed before the Yes/No question is answered" in {
      setupMocks(appDetailsWithQualifyingBodyUnanswered)

      val exception = intercept[Exception] {
        await(controller.onPageLoad()(request))
      }

      exception.getMessage must include("Illegal page navigation")
    }

    "load the page" in {

      setupMocks(appDetailsWithNoQualifyingBodies)

      val result = controller.onPageLoad()(request)
      status(result) mustBe OK

      val content = contentAsString(result)
      content must include(messagesApi("iht.estateReport.exemptions.qualifyingBodies.assetsLeftToQualifyingBodies.title"))
    }
  }
}
