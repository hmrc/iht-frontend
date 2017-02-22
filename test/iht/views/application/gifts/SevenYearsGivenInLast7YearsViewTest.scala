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
import iht.models.application.gifts.AllGifts
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils.CommonHelper
import iht.views.application.{SubmittableApplicationPageBehaviour, CancelComponent}
import iht.views.html.application.gift.seven_years_given_in_last_7_years
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat.Appendable
import play.api.i18n.Messages.Implicits._

/**
  * Created by vineet on 15/11/16.
  */
class SevenYearsGivenInLast7YearsViewTest extends SubmittableApplicationPageBehaviour[AllGifts] {

  val regDetails = CommonBuilder.buildRegistrationDetails.copy(ihtReference = Some("ABC1A1A1A"),
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(
      maritalStatus = Some(TestHelper.MaritalStatusMarried))),
    deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath))

  val fakeRequest = createFakeRequest(isAuthorised = false)

  override def pageTitle = messagesApi("iht.estateReport.gifts.givenAwayIn7YearsBeforeDeath")

  override def browserTitle = messagesApi("iht.estateReport.gifts.givenAwayIn7YearsBeforeDeath")

  override def guidance = guidance(
    Set(
      messagesApi("page.iht.application.gifts.lastYears.question", CommonHelper.getDeceasedNameOrDefaultString(regDetails)),
      messagesApi("page.iht.application.gifts.lastYears.description.p1"),
      messagesApi("iht.estateReport.assets.money.lowerCaseInitial"),
      messagesApi("iht.estateReport.gifts.stocksAndSharesListed"),
      messagesApi("page.iht.application.gifts.lastYears.description.e3"),
      messagesApi("page.iht.application.gifts.lastYears.description.e4"),
      messagesApi("page.iht.application.gifts.lastYears.description.p3", CommonHelper.getDeceasedNameOrDefaultString(regDetails))
    )
  )

  override def formTarget = Some(iht.controllers.application.gifts.routes.SevenYearsGivenInLast7YearsController.onSubmit())

  override def cancelComponent = Some(
    CancelComponent(
      iht.controllers.application.gifts.routes.GiftsOverviewController.onPageLoad(),
      messagesApi("page.iht.application.gifts.return.to.givenAwayBy",
        CommonHelper.getOrException(regDetails.deceasedDetails).name)
    )
  )

  override def form: Form[AllGifts] = giftSevenYearsGivenInLast7YearsForm

  override def formToView: Form[AllGifts] => Appendable =
    form =>
      seven_years_given_in_last_7_years(form, regDetails)

  "SevenYearsGivenInLast7Years Page" must {
    behave like applicationPageWithErrorSummaryBox()
  }
}
