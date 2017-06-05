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

package iht.views.application.gifts.guidance

import iht.testhelpers.CommonBuilder
import iht.views.GenericNonSubmittablePageBehaviour
import iht.views.html.application.gift.guidance.gifts_guidance_main_template
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{AnyContent, Call, Request}
import play.twirl.api.Html

import scala.concurrent.Future

class GiftsGuidanceMainTemplateViewTest extends GenericNonSubmittablePageBehaviour {

  val ihtReference = "ihtRef"

  def guidanceParagraphs = Set(
    messagesApi("site.currentPage")
  )

  def pageTitle = messagesApi("page.iht.application.gifts.guidance.title")

  def browserTitle = messagesApi("page.iht.application.gifts.guidance.title")

  def view = {
    implicit val request = createFakeRequest()
    gifts_guidance_main_template(
      title = CommonBuilder.DefaultString,
      backUrl = Some(CommonBuilder.DefaultCall1),
      isFullWidth = false,
      ihtReference = ihtReference,
      backToLastQuestionUrl = Some(CommonBuilder.DefaultCall2.url),
      backToLastQuestionMessageKey = Some("first"),
      backToLastQuestionMessageKeyAccessibility = Some("second")
    )(mainContent = Html("html content")).toString
  }

  def docUsingRequest(request:Request[AnyContent]) = {
    val view = gifts_guidance_main_template(
      title = CommonBuilder.DefaultString,
      backUrl = Some(CommonBuilder.DefaultCall1),
      isFullWidth = false,
      ihtReference = ihtReference,
      backToLastQuestionUrl = Some(CommonBuilder.DefaultCall2.url),
      backToLastQuestionMessageKey = Some("first"),
      backToLastQuestionMessageKeyAccessibility = Some("second")
    )(mainContent = Html("html content"))(request, play.api.i18n.Messages.Implicits.applicationMessages).toString
    asDocument(view)
  }

  def currentLinkRenderedAsSpan(pageNo: => String, mockCall: => Call, expectedText: => String) = {
    s"show the current page as non-link for page $pageNo" in {
      implicit val request = mock[Request[AnyContent]]
      when(request.path).thenReturn(mockCall.toString)

      val result = docUsingRequest(request).getElementById(s"guidance$pageNo-span")
      result.text shouldBe expectedText
    }
  }

  override def exitComponent = None

  "gifts guidance main template view" must {
    behave like nonSubmittablePage()

    behave like currentLinkRenderedAsSpan("1",
      iht.controllers.application.gifts.guidance.routes.WhatIsAGiftController.onPageLoad(),
      messagesApi("page.iht.application.gifts.guidance.whatsAGift.title"))

    behave like currentLinkRenderedAsSpan("2",
      iht.controllers.application.gifts.guidance.routes.KindsOfGiftsController.onPageLoad(),
      messagesApi("page.iht.application.gifts.guidance.kindOfGifts.title"))

    behave like currentLinkRenderedAsSpan("3",
      iht.controllers.application.gifts.guidance.routes.WithReservationController.onPageLoad(),
      messagesApi("iht.estateReport.gifts.withReservation.browserTitle"))

    behave like currentLinkRenderedAsSpan("4",
      iht.controllers.application.gifts.guidance.routes.ClaimingExemptionsController.onPageLoad(),
      messagesApi("page.iht.application.gifts.guidance.claimingExemptions.title"))

    behave like currentLinkRenderedAsSpan("5",
      iht.controllers.application.gifts.guidance.routes.IncreasingAnnualLimitController.onPageLoad(),
      messagesApi("page.iht.application.gifts.guidance.increasingAnnualLimit.title"))

    behave like currentLinkRenderedAsSpan("6",
      iht.controllers.application.gifts.guidance.routes.GiftsGivenAwayController.onPageLoad(),
      messagesApi("page.iht.application.gifts.guidance.giftsGivenAway.title"))
  }
}
