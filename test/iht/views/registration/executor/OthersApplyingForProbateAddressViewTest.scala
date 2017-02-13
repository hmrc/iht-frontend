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

import iht.forms.registration.ApplicantForms.{applicantAddressAbroadForm, applicantAddressUkForm}
import iht.forms.registration.CoExecutorForms.{coExecutorAddressAbroadForm, coExecutorAddressUkForm}
import iht.models.UkAddress
import iht.testhelpers.CommonBuilder
import iht.utils.CommonHelper
import iht.views.html.registration.executor.others_applying_for_probate_address
import iht.views.registration.RegistrationPageBehaviour
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.HtmlFormat.Appendable

class OthersApplyingForProbateAddressViewTest extends RegistrationPageBehaviour[UkAddress] {

  val executorName: String = CommonBuilder.firstNameGenerator

  override def pageTitle = Messages("page.iht.registration.others-applying-for-probate-address.sectionTitlePostfix",
    CommonHelper.addApostrophe(executorName))
  override def browserTitle = Messages("page.iht.registration.others-applying-for-probate-address.browserTitle")

  override def form:Form[UkAddress] = applicantAddressUkForm
  override def formToView:Form[UkAddress] => Appendable = form =>
    others_applying_for_probate_address(form, "1", executorName,
      isInternational = false, CommonBuilder.DefaultCall1, CommonBuilder.DefaultCall1)

  def formToViewInternational:Form[UkAddress] => Appendable = form =>
    others_applying_for_probate_address(form, "1", executorName,
      isInternational = true, CommonBuilder.DefaultCall1, CommonBuilder.DefaultCall1)

  "Others Applying for Probate Address View" must {

    "have a fieldset with the Id 'details' in UK mode" in {
      asDocument(view).getElementsByTag("fieldset").first.id shouldBe "details"
    }

    "have a fieldset with the Id 'details' in non-UK mode" in {
      val view = formToViewInternational(applicantAddressAbroadForm).toString
      asDocument(view).getElementsByTag("fieldset").first.id shouldBe "details"
    }

    "show the correct guidance" in {
      messagesShouldBePresent(view, Messages("page.iht.registration.others-applying-for-probate-address.address.guidance"))
    }

    behave like addressPage()
  }
}
