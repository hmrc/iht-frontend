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

package iht.views.registration

import iht.models.RegistrationDetails
import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.html.registration.registration_summary
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

class RegistrationSummaryViewTest extends ViewTestHelper {
  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()
  def registrationDetails = CommonBuilder.buildRegistrationDetails1
  def viewAsString: String = registration_summary(registrationDetails, "").toString
  def doc = asDocument(viewAsString)

  "Registration summary view" must {
    "have the correct title" in {
      titleShouldBeCorrect(viewAsString, Messages("iht.registration.checkYourAnswers"))
    }

    "have the correct browser title" in {
      browserTitleShouldBeCorrect(viewAsString, Messages("iht.registration.checkYourAnswers"))
    }

    "have a Confirm details button" in {
      doc.getElementsByClass("button").first.attr("value") shouldBe Messages("page.iht.registration.registrationSummary.button")
    }
  }
}
