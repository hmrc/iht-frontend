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

package iht.views.registration.executor

import iht.controllers.ControllerHelper.Mode
import iht.forms.registration.CoExecutorForms._
import iht.models.CoExecutor
import iht.testhelpers.CommonBuilder
import iht.views.html.registration.executor.coexecutor_personal_details
import iht.views.registration.{RegistrationPageBehaviour, YesNoQuestionViewBehaviour}
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat.Appendable

class CoexecutorPersonalDetailsViewTest extends YesNoQuestionViewBehaviour[CoExecutor] {

  override def guidanceParagraphs = Set.empty

  override def pageTitle = Messages("page.iht.registration.co-executor-personal-details.title")

  override def browserTitle = Messages("page.iht.registration.co-executor-personal-details.browserTitle")

  override def form: Form[CoExecutor] = coExecutorPersonalDetailsForm

  override def formToView: Form[CoExecutor] => Appendable =
    form => coexecutor_personal_details(form, Mode.Standard, CommonBuilder.DefaultCall1)(createFakeRequest())


  def editModeViewAsDocument = {
    implicit val request = createFakeRequest()
    val view = coexecutor_personal_details(form, Mode.Edit, CommonBuilder.DefaultCall1, Some(CommonBuilder.DefaultCall2))(createFakeRequest())
    asDocument(view)
  }

  "Co Exec Personal Details View" must {

    "have the correct label for first name" in {
      labelShouldBe(doc, "firstName-container", "iht.firstName")
    }

    "have hint text for first name" in {
      labelHelpTextShouldBe(doc, "firstName-container", "iht.firstName.hint")
    }

    "have a first name field" in {
      assertRenderedById(doc, "firstName")
    }

    "have the correct label for last name" in {
      labelShouldBe(doc, "lastName-container", "iht.lastName")
    }

    "have a last name field" in {
      assertRenderedById(doc, "lastName")
    }

    "have a fieldset with the Id 'date-of-birth'" in {
      assertRenderedById(doc, "date-of-birth")
    }

    "have a 'day' input box" in {
      assertRenderedById(doc, "dateOfBirth.day")
    }

    "have a 'month' input box" in {
      assertRenderedById(doc, "dateOfBirth.month")
    }

    "have a 'year' input box" in {
      assertRenderedById(doc, "dateOfBirth.year")
    }

    "have a form hint for date of birth" in {
      messagesShouldBePresent(view, Messages("iht.dateExample"))
    }

    "have the correct label for nino" in {
      labelShouldBe(doc, "nino-container", "iht.nationalInsuranceNo")
    }

    "have a nino field" in {
      assertRenderedById(doc, "nino")
    }

    "have a phone number field" in {
      assertRenderedById(doc, "phoneNo")
    }

    behave like yesNoQuestion

    "have a continue and cancel link in edit mode" in {
      val doc = editModeViewAsDocument

      val continueLink = doc.getElementById("continue-button")
      continueLink.attr("value") shouldBe Messages("iht.continue")

      val cancelLink = doc.getElementById("cancel-button")
      cancelLink.attr("href") shouldBe CommonBuilder.DefaultCall2.url
      cancelLink.text() shouldBe Messages("site.link.cancel")
    }
  }
}
