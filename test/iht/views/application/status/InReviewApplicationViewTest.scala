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

package iht.views.application.status

import iht.testhelpers.TestHelper
import iht.utils.{CommonHelper, formattedProbateReference}
import iht.views.html.application.status.in_review_application

class InReviewApplicationViewTest extends ApplicationStatusViewBehaviour {

  def guidanceParagraphs = commonGuidanceParagraphs

  def pageTitle = messagesApi("page.iht.application.overview.inreview.title", deceasedName)

  def browserTitle = messagesApi("page.iht.application.overview.inreview.browserTitle")
  lazy val inReviewApplicationView: in_review_application = app.injector.instanceOf[in_review_application]

  def view: String = inReviewApplicationView(ihtRef, deceasedName, probateDetails)(createFakeRequest(), messages).toString

  override val exitId: String = "return-link"

  override def exitComponent = None

  "In Review Application View" must {


    link("tellHMRC",
      TestHelper.linkEstateReportKickOut,
      messagesApi("page.iht.application.overview.common.tellHMRC")
    )

    link("view-app-copy",
      iht.controllers.application.pdf.routes.PDFController.onPostSubmissionPDF.url,
      messagesApi("page.iht.application.overview.common.viewcopy")
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

    "show submit button with correct target and text" in {
      doc.getElementsByTag("form").attr("action") mustBe iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad.url
      val submitButton = doc.getElementById("return-input")
      submitButton.`val` mustBe messagesApi("page.iht.application.overview.common.return")
    }
  }
}
