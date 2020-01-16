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

package iht.views.registration.executor

import iht.forms.registration.CoExecutorForms
import iht.models.UkAddress
import iht.testhelpers.CommonBuilder
import iht.utils.StringHelper
import iht.views.ViewTestHelper
import iht.views.html.registration.executor.others_applying_for_probate_address
import iht.views.registration.RegistrationPageBehaviour
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable

trait OthersApplyingForProbateAddressViewTest extends ViewTestHelper with StringHelper with CoExecutorForms {
  def guidance: Seq[String] = Seq(messagesApi("page.iht.registration.others-applying-for-probate-address.address.guidance"))
  def executorName: String = CommonBuilder.firstNameGenerator
}

class OthersApplyingForProbateAddressViewInUKModeTest extends RegistrationPageBehaviour[UkAddress] with OthersApplyingForProbateAddressViewTest {
  override def pageTitle = messagesApi("page.iht.registration.others-applying-for-probate-address.sectionTitlePostfix",
    addApostrophe(executorName))
  override def browserTitle = messagesApi("page.iht.registration.others-applying-for-probate-address.browserTitle")

  override def form:Form[UkAddress] = coExecutorAddressUkForm
  override def formToView:Form[UkAddress] => Appendable = form =>
    others_applying_for_probate_address(form, "1", executorName,
      isInternational = false, CommonBuilder.DefaultCall1, CommonBuilder.DefaultCall1)

  "Others Applying for Probate Address View in UK Mode" must {

    behave like addressPageUK(guidance)
  }
}

class OthersApplyingForProbateAddressViewInAbroadModeTest extends RegistrationPageBehaviour[UkAddress] with OthersApplyingForProbateAddressViewTest {
  override def pageTitle = messagesApi("page.iht.registration.others-applying-for-probate-address.sectionTitlePostfix",
    addApostrophe(executorName))
  override def browserTitle = messagesApi("page.iht.registration.others-applying-for-probate-address.browserTitle")

  override def form:Form[UkAddress] = coExecutorAddressAbroadForm
  override def formToView:Form[UkAddress] => Appendable = form =>
    others_applying_for_probate_address(form, "1", executorName,
      isInternational = true, CommonBuilder.DefaultCall1, CommonBuilder.DefaultCall1)

  "Others Applying for Probate Address View in Abroad Mode" must {

    behave like addressPageAbroad(guidance)
  }
}
