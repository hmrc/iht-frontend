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

import iht.models.application.ProbateDetails
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils._
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}

trait ApplicationStatusViewBehaviour extends GenericNonSubmittablePageBehaviour {
  def sidebarTitle: String

  def commonGuidanceParagraphs = Set(
    messagesApi("page.iht.application.overview.common.helptext.part1", deceasedName),
    messagesApi("page.iht.application.overview.common.helptext.part2"),
    messagesApi("page.iht.application.probateDetails.yourProbateText")
  )

  val ihtRef: String = "test1"
  
  val deceasedName: String = "test2"
  
  val probateDetails: ProbateDetails = CommonBuilder.buildProbateDetails

  override val exitId: String = "return-link"

  override def exitComponent: Option[ExitComponent] = Some(
    ExitComponent(
      iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad(),
      messagesApi("page.iht.application.overview.common.return")
    )
  )

  def applicationStatusPage() = {
    nonSubmittablePage()

    link("corrective-account-form",
      TestHelper.ihtPropertiesCorrectiveAccountsLink,
      messagesApi("iht.estateReport.correctiveAccountForm")
    )

    "show sidebar title" in {
      elementShouldHaveText(doc, "in-review-side-text", sidebarTitle)
    }

    link("view-app-copy",
      iht.controllers.application.pdf.routes.PDFController.onPostSubmissionPDF().url,
      messagesApi("page.iht.application.overview.common.viewcopy")
    )

    "show the IHT identifier" in {
      val expectedContent = messagesApi("page.iht.application.probateDetails.content2.bullet1") + " " +
        formattedProbateReference(probateDetails.probateReference)
      elementShouldHaveText(doc, "probate-details-iht-identifier", expectedContent)
    }

    "show the gross estate figure" in {
      val expectedContent = messagesApi("page.iht.application.probateDetails.content2.bullet2",
        probateDetails.grossEstateforProbatePurposes)
      elementShouldHaveText(doc, "probate-details-gross-estate-figure", expectedContent)
    }

    "show the net estate figure" in {
      val expectedContent = messagesApi("page.iht.application.probateDetails.content2.bullet3",
        probateDetails.netEstateForProbatePurposes)
      elementShouldHaveText(doc, "probate-details-net-estate-figure", expectedContent)
    }

    "show the in review sidebar title text" in {
      elementShouldHaveText(doc, "in-review-side-text", sidebarTitle)
    }
  }
}
