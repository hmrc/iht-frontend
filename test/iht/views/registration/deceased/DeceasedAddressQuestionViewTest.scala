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

package iht.views.registration.deceased

import iht.forms.registration.DeceasedForms.deceasedAddressQuestionForm
import iht.models.DeceasedDetails
import iht.testhelpers.CommonBuilder
import iht.views.html.registration.deceased.deceased_address_question
import iht.views.registration.YesNoQuestionViewBehaviour
import play.api.data.Form
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import iht.config.AppConfig
import play.twirl.api.HtmlFormat.Appendable

class DeceasedAddressQuestionViewTest extends YesNoQuestionViewBehaviour[DeceasedDetails] {

  lazy val name = CommonBuilder.firstNameGenerator

  override def guidanceParagraphs = Set(messagesApi("page.iht.registration.deceasedAddressQuestion.p1", name))

  override def pageTitle = messagesApi("page.iht.registration.deceasedAddressQuestion.title", name)

  override def browserTitle = messagesApi("iht.registration.contactAddress")

  override def form: Form[DeceasedDetails] = deceasedAddressQuestionForm

  override def formToView: Form[DeceasedDetails] => Appendable =
    form => deceased_address_question(form, name, CommonBuilder.DefaultCall1)

  "Deceased Address Question View" must {
    "show the correct guidance paragraphs" in {
      for (paragraph <- guidanceParagraphs) messagesShouldBePresent(view, paragraph)
    }

    "show the correct answer text" in {
      doc.getElementById("isAddressInUk-true-label").text mustBe messagesApi("page.iht.registration.deceasedAddressAnswerInUk")
      doc.getElementById("isAddressInUk-false-label").text mustBe messagesApi("page.iht.registration.deceasedAddressAnswerAbroad")
    }

    "show the Continue button with the correct target" in {
      val continueButton = doc.getElementById("continue-button")
      continueButton.`val` mustBe messagesApi("iht.continue")
      doc.getElementsByTag("form").attr("action") mustBe CommonBuilder.DefaultCall1.url
    }
  }
}
