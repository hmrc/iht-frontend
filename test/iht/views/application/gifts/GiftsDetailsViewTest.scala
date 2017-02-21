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

package iht.views.application.gifts

import iht.forms.ApplicationForms._
import iht.models.application.gifts.PreviousYearsGifts
import iht.testhelpers.CommonBuilder
import iht.views.application.{ApplicationPageBehaviour, CancelComponent}
import iht.views.html.application.gift.gifts_details
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat.Appendable

class GiftsDetailsViewTest extends ApplicationPageBehaviour[PreviousYearsGifts] {
  lazy val ihtRef = "ABC123"
  lazy val regDetails = CommonBuilder.buildRegistrationDetails1.copy(ihtReference = Some(ihtRef))
  lazy val returnLocation = iht.controllers.application.gifts.routes.SevenYearsGiftsValuesController.onPageLoad()
  lazy val returnLinkLabelMsgKey = "iht.estateReport.gifts.returnToGiftsGivenAwayInThe7YearsBeforeDeath"

  lazy val giftsValue = BigDecimal(200)
  lazy val exemptionsValue = BigDecimal(100)

  def pageTitle: String = Messages("page.iht.application.giftsDetails.subtitle", "13 March 2007", "5 April 2007")

  def browserTitle: String = Messages("page.iht.application.giftsDetails.browserTitle")

  override def guidance = noGuidance

  override def formTarget = Some(iht.controllers.application.gifts.routes.GiftsDetailsController.onSubmit())

  override def cancelComponent = Some(
    CancelComponent(
      returnLocation,
      Messages(returnLinkLabelMsgKey)
    )
  )

  override def form: Form[PreviousYearsGifts] = {
    val previousYearsGifts = CommonBuilder.buildPreviousYearsGifts.copy(yearId = Some("1"),
      value = Some(giftsValue), exemptions = Some(exemptionsValue),
      startDate = Some("13 March 2007"), endDate = Some("5 April 2007"))

    previousYearsGiftsForm.fill(previousYearsGifts)
  }

  override def formToView: Form[PreviousYearsGifts] => Appendable =
    form =>
      gifts_details(form,
        regDetails,
        Some(returnLocation),
        Some(Messages(returnLinkLabelMsgKey)))

  override val cancelId = "cancel-button"

  "GiftsOverview view" must {

    behave like applicationPageWithErrorSummaryBox()

    "have correct gifts given away input text labels and value" in {
      val giftsGivenAwaySection = doc.getElementById("value-container")
      val giftsGivenAwaySectionText = giftsGivenAwaySection.getElementsByTag("span").get(0)
      val giftsGivenAwaySectionValue = giftsGivenAwaySection.getElementsByTag("span").get(1)

      giftsGivenAwaySectionText.text shouldBe Messages("page.iht.application.giftsDetails.value.label")
      giftsGivenAwaySectionValue.text shouldBe "£"
    }

    "have correct exemptions being claimed input text labels and value" in {

      val exemptionsClaimedSection = doc.getElementById("exemptions-container")
      val exemptionsClaimedSectionText = exemptionsClaimedSection.getElementsByTag("span").get(0)
      val exemptionsClaimedSectionValue = exemptionsClaimedSection.getElementsByTag("span").get(1)

      exemptionsClaimedSectionText.text shouldBe Messages("page.iht.application.giftsDetails.exemptions.label")
      exemptionsClaimedSectionValue.text shouldBe "£"
    }

    "show amount added to the estate value label with correct value" in {
      val amountAddedSection = doc.getElementById("value-of-gifts-added")
      amountAddedSection.attr("data-combine-copy", Messages("page.iht.application.giftsDetails.amountAdded"))
    }

  }

}
