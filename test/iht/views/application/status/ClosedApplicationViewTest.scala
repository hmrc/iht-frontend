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

import iht.utils.{CommonHelper, formattedProbateReference}
import iht.views.ExitComponent
import iht.views.html.application.status.closed_application
import play.api.i18n.Messages.Implicits._

class ClosedApplicationViewTest extends ApplicationStatusViewBehaviour {

  def guidanceParagraphs = commonGuidanceParagraphs ++ Set(
    messagesApi("page.iht.application.overview.closed.keepingRecords")
  )

  def pageTitle = messagesApi("page.iht.application.overview.closed.title", deceasedName)

  def browserTitle = messagesApi("page.iht.application.overview.closed.browserTitle")

  def view: String = closed_application(ihtRef, deceasedName, probateDetails)(createFakeRequest(),
    applicationMessages,
    formPartialRetriever).toString

  override val exitId: String = "return-link"

  override def exitComponent = Some(
    ExitComponent(
      iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad(),
      messagesApi("page.iht.application.overview.common.return")
    )
  )

  "Closed Cleared Application View" must {
    behave like applicationStatusPage()

    behave like link("view-certificate-button",
      iht.controllers.application.pdf.routes.PDFController.onClearancePDF().url,
      messagesApi("page.iht.application.overview.closed.viewClearanceCertificate")
    )

    "show the IHT identifier" in {
      val expectedContent = formattedProbateReference(probateDetails.probateReference)
      elementShouldHaveText(doc, "probate-details-iht-identifier", expectedContent)
    }

    "show the gross estate figure" in {
      val expectedContent = "£" + CommonHelper.numberWithCommas(probateDetails.grossEstateforProbatePurposes)
      elementShouldHaveText(doc, "probate-details-gross-estate-figure", expectedContent)
    }

    "show the net estate figure" in {
      val expectedContent = "£" + CommonHelper.numberWithCommas(probateDetails.netEstateForProbatePurposes)
      elementShouldHaveText(doc, "probate-details-net-estate-figure", expectedContent)
    }
  }
}
