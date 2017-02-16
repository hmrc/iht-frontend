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

package iht.views.registration

import iht.controllers.registration.executor.routes._
import iht.views.html.registration.registration_summary_coexecutor_panel
import iht.{FakeIhtApp, TestUtils}
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi

class RegistrationSummaryCoExecutorPanelTest extends UnitSpec with FakeIhtApp with MockitoSugar with TestUtils {

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  "RegistrationSummaryCoExecutorPanelTest" must {

    "link to the others applying for probate change" in {
      implicit val request = createFakeRequest()
      implicit val messages: MessagesApi = app.injector.instanceOf[MessagesApi]
      registration_summary_coexecutor_panel(Seq())(request, applicationMessages).toString should include (OthersApplyingForProbateController.onPageLoadFromOverview().url)
    }
  }
}
