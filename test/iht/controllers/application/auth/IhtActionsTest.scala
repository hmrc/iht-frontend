/*
 * Copyright 2018 HM Revenue & Customs
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
import iht.FakeIhtApp
import iht.utils.IhtSection
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.test.UnitSpec
import play.api.test.Helpers._

import scala.concurrent.Future

class IhtActionsTest extends UnitSpec with FakeIhtApp with MockitoSugar with BeforeAndAfterEach {

  private val testAuthConnector = mock[AuthConnector]

  implicit val materializer: Materializer = app.injector.instanceOf[Materializer]

  override def beforeEach(): Unit = {
    reset(testAuthConnector)

    super.beforeEach()
  }

  class Setup(section: IhtSection.Value, responseContent: String = "default response") extends IhtActions {

    override protected val ihtSection: IhtSection.Value = section
    override val authConnector: AuthConnector = testAuthConnector

    val testAction: Action[AnyContent] = authorisedForIht {
      implicit user =>
        implicit request => {
          Future.successful(Ok(s"$responseContent"))
        }
    }
  }

  "AuthorisedForIHT" should {
    "redirect to auth with correct continue URL when the user is not logged in" when {
      "trying to access an Application page" in new Setup(IhtSection.Application) {
        private val result = await(testAction(createFakeRequest(isAuthorised = false)))
        status(result) shouldBe 303
        private val locationHeader = redirectLocation(result).getOrElse("NO LOCATION HEADER!")
        locationHeader should include("sign-in")
        locationHeader should include("estate-report")
      }
      "trying to access an Registration page" in new Setup(IhtSection.Registration) {
        private val result = await(testAction(createFakeRequest(isAuthorised = false)))
        status(result) shouldBe 303
        private val locationHeader = redirectLocation(result).getOrElse("NO LOCATION HEADER!")
        locationHeader should include("sign-in")
        locationHeader should include("login-pass")
      }

    }

  }
}