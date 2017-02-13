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
import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.html.application.gift.gifts_details
import play.api.i18n.{Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class GiftsDetailsViewTest extends ViewTestHelper {
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val ihtRef = "ABC123"
  lazy val regDetails = CommonBuilder.buildRegistrationDetails1.copy(ihtReference = Some(ihtRef))
  lazy val returnLocation = iht.controllers.application.gifts.routes.SevenYearsGiftsValuesController.onPageLoad()
  lazy val returnLinkLabelMsgKey = "iht.estateReport.gifts.returnToGiftsGivenAwayInThe7YearsBeforeDeath"

  lazy val giftsValue = BigDecimal(200)
  lazy val exemptionsValue = BigDecimal(100)

  def giftsDetailsView() = {
    implicit val request = createFakeRequest()

    val previousYearsGifts = CommonBuilder.buildPreviousYearsGifts.copy(yearId = Some("1"),
                               value  = Some(giftsValue), exemptions = Some(exemptionsValue),
                               startDate = Some("13 March 2007"),endDate = Some("5 April 2007"))

    val filledPreviousYearsGiftsForm = previousYearsGiftsForm.fill(previousYearsGifts)

    val view = gifts_details(filledPreviousYearsGiftsForm,
                            regDetails,
                            Some(returnLocation),
                            Some(Messages(returnLinkLabelMsgKey))).toString()
    asDocument(view)
  }

  "GiftsOverview view" must {

    "have correct title and browser title " in {
      val view = giftsDetailsView().toString

      titleShouldBeCorrect(view, Messages("page.iht.application.giftsDetails.subtitle", "13 March 2007", "5 April 2007"))
      browserTitleShouldBeCorrect(view, Messages("page.iht.application.giftsDetails.browserTitle"))
    }

    "have 'Save and continue' button" in {
      val view = giftsDetailsView()

      val saveAndContinueButton = view.getElementById("save-continue")
      saveAndContinueButton.getElementsByAttributeValueContaining("value", Messages("iht.saveAndContinue"))
    }

    "have the return link with correct text" in {
      val view = giftsDetailsView()

      val returnLink = view.getElementById("cancel-button")
      returnLink.attr("href") shouldBe returnLocation.url
      returnLink.text() shouldBe Messages(returnLinkLabelMsgKey)
    }

    "have correct gifts given away input text labels and value" in {
      val view = giftsDetailsView()

      val giftsGivenAwaySection = view.getElementById("value-container")
      val giftsGivenAwaySectionText = giftsGivenAwaySection.getElementsByTag("span").get(0)
      val giftsGivenAwaySectionValue = giftsGivenAwaySection.getElementsByTag("span").get(1)

      giftsGivenAwaySectionText.text shouldBe Messages("page.iht.application.giftsDetails.value.label")
      giftsGivenAwaySectionValue.text shouldBe "£"
    }

    "have correct exemptions being claimed input text labels and value" in {
      val view = giftsDetailsView()

      val exemptionsClaimedSection = view.getElementById("exemptions-container")
      val exemptionsClaimedSectionText = exemptionsClaimedSection.getElementsByTag("span").get(0)
      val exemptionsClaimedSectionValue = exemptionsClaimedSection.getElementsByTag("span").get(1)

      exemptionsClaimedSectionText.text shouldBe Messages("page.iht.application.giftsDetails.exemptions.label")
      exemptionsClaimedSectionValue.text shouldBe "£"
    }

    "show amount added to the estate value label with correct value" in {
      val view = giftsDetailsView()

      val amountAddedSection = view.getElementById("value-of-gifts-added")
      amountAddedSection.attr("data-combine-copy", Messages("page.iht.application.giftsDetails.amountAdded"))
    }

  }

}
