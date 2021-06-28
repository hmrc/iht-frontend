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

package iht.controllers.application.status

import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.CommonBuilder
import iht.views.HtmlSpec
import iht.views.html.application.status.closed_application

class ApplicationClosedControllerTest extends ApplicationControllerTest with HtmlSpec {

  val closedApplicationView: closed_application = app.injector.instanceOf[closed_application]

  val applicationClosedController = new ApplicationClosedControllerImpl(
    mockIhtConnector,
    mockCachingConnector,
    mockAuthConnector,
    closedApplicationView,
    mockAppConfig,
    mockControllerComponents
  )

  "ApplicationClosedController" must {
    "implement a view" in {
      val deceasedName = "Xyz"
      val request = createFakeRequest()
      val pageContent = applicationClosedController.getView("",deceasedName,CommonBuilder.buildProbateDetails)(request).toString
      titleShouldBeCorrect(pageContent, messagesApi("page.iht.application.overview.closed.title", deceasedName))
    }
  }
}
