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

package iht.views.application.gifts

import iht.forms.ApplicationForms._
import iht.models.application.gifts.AllGifts
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils.{CommonHelper, DeceasedInfoHelper}
import iht.views.application.{CancelComponent, SubmittableApplicationPageBehaviour}
import iht.views.html.application.gift.with_reservation_of_benefit
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable



class WithReservationOfBenefitViewTest extends SubmittableApplicationPageBehaviour[AllGifts] {

  val regDetails = CommonBuilder.buildRegistrationDetails.copy(ihtReference = Some("ABC1234567890"),
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(
      maritalStatus = Some(TestHelper.MaritalStatusMarried))),
    deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath))

  def deceasedName = regDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def pageTitle = messagesApi("iht.estateReport.gifts.withReservation.title", deceasedName)

  override def browserTitle = messagesApi("iht.estateReport.gifts.withReservation.titleWithoutName")

  override def guidance = guidance(
    Set(
      messagesApi("iht.estateReport.gifts.reservation.question",
        DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails))
    )
  )

  override def formTarget = Some(iht.controllers.application.gifts.routes.WithReservationOfBenefitController.onSubmit)

  override def cancelComponent = Some(
    CancelComponent(
      iht.controllers.application.gifts.routes.GiftsOverviewController.onPageLoad,
      messagesApi("page.iht.application.gifts.return.to.givenAwayBy",
        CommonHelper.getOrException(regDetails.deceasedDetails).name),
      TestHelper.GiftsReservationBenefitQuestionID
    )
  )

  override def form: Form[AllGifts] = giftWithReservationFromBenefitForm
  override def linkHash = TestHelper.GiftsReservationBenefitQuestionID
  lazy val withReservationOfBenefitView: with_reservation_of_benefit = app.injector.instanceOf[with_reservation_of_benefit]

  override def formToView: Form[AllGifts] => Appendable =
    form =>
      withReservationOfBenefitView(form, regDetails)

  "WithReservationOfBenefit Page" must {
    behave like applicationPageWithErrorSummaryBox()
  }
}
