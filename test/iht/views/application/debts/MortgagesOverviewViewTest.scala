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

package iht.views.application.debts

import iht.constants.FieldMappings
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils.CommonHelper
import iht.views.application.{ApplicationPageBehaviour, CancelComponent}
import iht.views.html.application.debts.mortgages_overview
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.config.AppConfig
import iht.constants.Constants._



class MortgagesOverviewViewTest extends ApplicationPageBehaviour {
  val ihtReference = Some("ABC1A1A1A")
  val regDetails = CommonBuilder.buildRegistrationDetails.copy(ihtReference = ihtReference,
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(
      maritalStatus = Some(TestHelper.MaritalStatusMarried))),
    deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath))
  val deceasedName = CommonHelper.getOrException(regDetails.deceasedDetails).name

  val fakeRequest = createFakeRequest(isAuthorised = false)
  val debtsOverviewPageUrl = iht.controllers.application.debts.routes.DebtsOverviewController.onPageLoad()

  override def guidance = guidance(
    Set(
      messagesApi("page.iht.application.debts.mortgages.description.p1"),
      messagesApi("page.iht.application.debts.mortgages.description", deceasedName),
      messagesApi("page.iht.application.debts.mortgages.description.p3", deceasedName)
    )
  )

  override def pageTitle = messagesApi("iht.estateReport.debts.mortgages")

  override def browserTitle = messagesApi("iht.estateReport.debts.mortgages")

  override def formTarget = None
  override def linkHash = appConfig.DebtsMortgagesID

  override def cancelComponent = Some(
    CancelComponent(
      debtsOverviewPageUrl,
      returnLinkText,
      TestHelper.DebtsMortgagesID
    )
  )

  val returnLinkText = messagesApi("site.link.return.debts")

  override def view = mortgages_overview(List(CommonBuilder.property, CommonBuilder.property2),
    Nil,
    FieldMappings.typesOfOwnership(deceasedName),
    regDetails, debtsOverviewPageUrl, returnLinkText)(fakeRequest, messages, formPartialRetriever, appConfig).toString

  val addressTableId = "properties"

  def addressWithDeleteAndModify(rowNo: Int, expectedValue: String) = {
    s"show address number ${rowNo + 1}" in {
      tableCell(doc, addressTableId, 0, rowNo).ownText mustBe expectedValue
    }

    s"show address number ${rowNo + 1} Give details link" in {
      val deleteDiv = tableCell(doc, addressTableId, 2, rowNo)
      val anchor = deleteDiv.getElementsByTag("a").first
      getVisibleText(anchor) mustBe messagesApi("site.link.giveDetails")
    }
  }

  "MortgagesOverview Page" must {
    behave like applicationPage()

    behave like addressWithDeleteAndModify(0, formatAddressForDisplay(CommonBuilder.DefaultUkAddress))

    behave like addressWithDeleteAndModify(1, formatAddressForDisplay(CommonBuilder.DefaultUkAddress2))
  }
}
