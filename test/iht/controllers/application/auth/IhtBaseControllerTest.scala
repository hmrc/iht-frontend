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

package iht.controllers.auth

import akka.stream.Materializer
import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.utils.IhtSection
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.test.Helpers.{redirectLocation, status => playStatus}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

class IhtBaseControllerTest extends ApplicationControllerTest {
  implicit val materializer: Materializer = app.injector.instanceOf[Materializer]

  protected abstract class TestController extends FrontendController(mockControllerComponents) with IhtBaseController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  class Setup(section: IhtSection.Value, responseContent: String = "default response") {
    object testController extends TestController {
      override protected val ihtSection: IhtSection.Value = section
      override val authConnector: AuthConnector = mockAuthConnector

      val testAction: Action[AnyContent] = authorisedForIht {
        implicit request => { // False positive warning. Workaround: scala/bug#11175 -Ywarn-unused:params false positive
          Future.successful(Ok(s"$responseContent"))
        }
      }

      val testActionWithNino: Action[AnyContent] = authorisedForIhtWithRetrievals(Retrievals.nino) { userNino =>
        implicit request => { // False positive warning. Workaround: scala/bug#11175 -Ywarn-unused:params false positive
          Future.successful(Ok(s"$responseContent with $userNino"))
        }
      }
    }
  }

  "authorisedForIht" should {
    "redirect to auth with correct continue URL when the user is not logged in" when {
      "trying to access an Application page" in new Setup(IhtSection.Application) {
        private val result = testController.testAction(createFakeRequest(isAuthorised = false, authRetrieveNino = false))
        playStatus(result) mustBe 303
        private val locationHeader = redirectLocation(result).getOrElse("NO LOCATION HEADER!")
        locationHeader must include("sign-in")
        locationHeader must include("estate-report")
      }
      "trying to access an Registration page" in new Setup(IhtSection.Registration) {
        private val result = testController.testAction(createFakeRequest(isAuthorised = false, authRetrieveNino = false))
        playStatus(result) mustBe 303
        private val locationHeader = redirectLocation(result).getOrElse("NO LOCATION HEADER!")
        locationHeader must include("sign-in")
        locationHeader must include("login-pass")
      }
    }
  }

  "authorisedForIhtWithRetrievals" should {
    "redirect to auth with correct continue URL when the user is not logged in" when {
      "trying to access an Application page" in new Setup(IhtSection.Application) {
        private val result = testController.testActionWithNino(createFakeRequest(isAuthorised = false))
        playStatus(result) mustBe 303
        private val locationHeader = redirectLocation(result).getOrElse("NO LOCATION HEADER!")
        locationHeader must include("sign-in")
        locationHeader must include("estate-report")
      }
      "trying to access an Registration page" in new Setup(IhtSection.Registration) {
        private val result = testController.testActionWithNino(createFakeRequest(isAuthorised = false))
        playStatus(result) mustBe 303
        private val locationHeader = redirectLocation(result).getOrElse("NO LOCATION HEADER!")
        locationHeader must include("sign-in")
        locationHeader must include("login-pass")
      }
    }
  }
}