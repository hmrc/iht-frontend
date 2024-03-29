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

package iht.controllers

import iht.config.AppConfig
import iht.views.ViewTestHelper
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, status => playStatus}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class feedbackSurveyControllerTest extends ViewTestHelper {

  implicit val request = FakeRequest()
  val appConfigBinding: AppConfig = appConfig

  protected abstract class TestController extends FrontendController(mockControllerComponents) with FeedbackSurveyController

  class Setup {
    val controller: FeedbackSurveyController = new TestController {
      override implicit val appConfig: AppConfig = appConfigBinding
    }
  }

  "Feedback Survey controller" must {
    "return 303 for a GET" in new Setup {
      val result =  controller.redirectExitSurvey(request)
      playStatus(result) mustBe Status.SEE_OTHER
    }
  }

}
