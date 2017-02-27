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

package iht.views.application.status

import iht.testhelpers.TestHelper
import iht.views.ExitComponent
import iht.views.html.application.status.closed_cleared_application
import play.api.i18n.Messages.Implicits._

class ClosedClearedApplicationViewTest extends ApplicationStatusViewBehaviour {

  override def sidebarTitle: String = messagesApi("page.iht.application.overview.cleared.sidebartitle")

  def guidanceParagraphs = commonGuidanceParagraphs ++ Set(
    messagesApi("page.iht.application.overview.cleared.helptext1"),
    messagesApi("page.iht.application.overview.cleared.helptext2")
  )

  def pageTitle = messagesApi("page.iht.application.overview.common.title")

  def browserTitle = messagesApi("page.iht.application.overview.common.title")

  def view: String = closed_cleared_application(ihtRef, deceasedName, probateDetails)(createFakeRequest(), applicationMessages).toString

  override val exitId: String = "return-link"

  override def exitComponent = Some(
    ExitComponent(
      iht.controllers.home.routes.IhtHomeController.onPageLoad(),
      messagesApi("page.iht.application.overview.common.return")
    )
  )

  "Closed Cleared Application View" must {
    behave like applicationStatusPage()

    behave like link("view-certificate-button",
      iht.controllers.application.pdf.routes.PDFController.onClearancePDF().url,
      messagesApi("page.iht.application.overview.common.viewcertificate")
    )
  }
}
