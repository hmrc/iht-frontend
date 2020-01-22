/*
 * Copyright 2020 HM Revenue & Customs
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

package iht.views.application.tnrb

import iht.forms.TnrbForms._
import iht.models.application.tnrb.TnrbEligibiltyModel
import iht.testhelpers.CommonBuilder
import iht.utils.CommonHelper
import iht.testhelpers.TestHelper
import iht.views.application.{CancelComponent, SubmittableApplicationPageBehaviour}
import iht.views.html.application.tnrb.partner_name
import org.joda.time.LocalDate
import play.api.data.Form
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import iht.config.AppConfig
import play.twirl.api.HtmlFormat.Appendable
import iht.testhelpers.TestHelper

class PartnerNameViewTest extends SubmittableApplicationPageBehaviour[TnrbEligibiltyModel] {

  def tnrbModel = CommonBuilder.buildTnrbEligibility

  def widowCheck = CommonBuilder.buildWidowedCheck

  val deceasedDetailsName = CommonBuilder.buildDeceasedDetails.name

  override def pageTitle = messagesApi("page.iht.application.TnrbEligibilty.partnerName.label", messagesApi(TestHelper.spouseMessageKey))

  override def browserTitle = messagesApi("page.iht.application.TnrbEligibilty.partnerName.label", messagesApi(TestHelper.spouseMessageKey))

  override def guidance = guidance(
    Set(
      messagesApi( "page.iht.application.TnrbEligibilty.partnerName.hint", "2000")
    )
  )

  override def formTarget = Some(iht.controllers.application.tnrb.routes.PartnerNameController.onSubmit())

  override def form: Form[TnrbEligibiltyModel] = partnerNameForm

  override def formToView: Form[TnrbEligibiltyModel] => Appendable =
    form =>
      partner_name(form, Some(new LocalDate(2000,10,1)),
        CommonHelper.addFragmentIdentifier(iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad(), Some(TestHelper.TnrbSpouseNameID))
        )

  override val cancelId: String = "cancel-button"

  override def cancelComponent = Some(
    CancelComponent(iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad(),
      messagesApi("page.iht.application.tnrb.returnToIncreasingThreshold"),
      TestHelper.TnrbSpouseNameID
    )
  )

  "Partner Name View" must {
    behave like applicationPageWithErrorSummaryBox()

    "have a first name label with hint text" in {
      labelShouldBe(doc, "firstName-container", messagesApi("iht.firstName"))
      labelHelpTextShouldBe(doc, "firstName-container", "iht.firstName.hint")
    }

    "have a first name field" in {
      Option(doc.getElementById("firstName")).isDefined mustBe true
    }

    "have a last name label" in {
      elementShouldHaveText(doc, "lastName-container", messagesApi("iht.lastName"))
    }

    "have a last name field" in {
      Option(doc.getElementById("lastName")).isDefined mustBe true
    }
  }
}
