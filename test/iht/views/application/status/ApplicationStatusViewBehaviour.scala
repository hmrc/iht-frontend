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

import iht.models.application.ProbateDetails
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}

trait ApplicationStatusViewBehaviour extends GenericNonSubmittablePageBehaviour {

  def commonGuidanceParagraphs = Set(
    messagesApi("page.iht.application.overview.common.ifYouNeed"),
    messagesApi("page.iht.application.overview.common.p1", "https://www.gov.uk/wills-probate-inheritance/applying-for-a-grant-of-representation"),
    messagesApi("page.iht.application.probate.data.ihtIdentifier"),
    messagesApi("page.iht.application.probate.data.grossEstateFigure"),
    messagesApi("page.iht.application.probate.data.netEstateFigure"),
    messagesApi("page.iht.application.overview.common.ifYouFind"),
    messagesApi("page.iht.application.overview.common.p4"),
    messagesApi("page.iht.application.overview.common.youWillNeedTo")
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

    link("tellHMRC",
      TestHelper.linkEstateReportKickOut,
      messagesApi("page.iht.application.overview.common.tellHMRC")
    )

    link("view-app-copy",
      iht.controllers.application.pdf.routes.PDFController.onPostSubmissionPDF().url,
      messagesApi("page.iht.application.overview.common.viewcopy")
    )
  }
}
