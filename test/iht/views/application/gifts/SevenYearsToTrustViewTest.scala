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

package iht.views.application.gifts

import iht.forms.ApplicationForms._
import iht.models.application.gifts.AllGifts
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils.{CommonHelper, DeceasedInfoHelper}
import iht.views.application.{SubmittableApplicationPageBehaviour, CancelComponent}
import iht.views.html.application.gift.seven_years_to_trust
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat.Appendable
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.config.AppConfig
import iht.constants.Constants._



class SevenYearsToTrustViewTest extends SubmittableApplicationPageBehaviour[AllGifts] {
  val ihtReference = Some("ABC1A1A1A")
  val regDetails = CommonBuilder.buildRegistrationDetails.copy(ihtReference = ihtReference,
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(
      maritalStatus = Some(TestHelper.MaritalStatusMarried))),
    deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath))

  val allGifts = CommonBuilder.buildAllGifts.copy(isReservation = Some(false))
  val fakeRequest = createFakeRequest(isAuthorised = false)


  override def pageTitle = messagesApi("iht.estateReport.gifts.givenAwayIn7YearsBeforeDeath")

  override def browserTitle = messagesApi("iht.estateReport.gifts.givenAwayIn7YearsBeforeDeath")

  override def guidance = guidance(
    Set(
      messagesApi("page.iht.application.gifts.trust.question", DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails))
    )
  )

  override def formTarget = Some(iht.controllers.application.gifts.routes.SevenYearsToTrustController.onSubmit())

  override def cancelComponent = Some(
    CancelComponent(
      iht.controllers.application.gifts.routes.GiftsOverviewController.onPageLoad(),
      messagesApi("page.iht.application.gifts.return.to.givenAwayBy",
        CommonHelper.getOrException(regDetails.deceasedDetails).name),
      TestHelper.GiftsSevenYearsQuestionID2
    )
  )

  override def linkHash = appConfig.GiftsSevenYearsQuestionID2

  override def form: Form[AllGifts] = giftSevenYearsGivenInLast7YearsForm

  override def formToView: Form[AllGifts] => Appendable =
    form =>
      seven_years_to_trust(form, regDetails)


  "SevenYearsToTrust Page" must {
    behave like applicationPageWithErrorSummaryBox()
  }
}
