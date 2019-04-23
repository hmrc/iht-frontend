/*
 * Copyright 2019 HM Revenue & Customs
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

package iht.views.registration.applicant

import iht.forms.registration.ApplicantForms.{applicantAddressAbroadForm, applicantAddressUkForm}
import iht.models.UkAddress
import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.html.registration.applicant.applicant_address
import iht.views.registration.RegistrationPageBehaviour
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import iht.config.AppConfig
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable

trait ApplicantAddressViewTest extends ViewTestHelper {
  def guidance: Seq[String] = Seq(messagesApi("page.iht.registration.applicantAddress.hint"))
}

class ApplicantAddressViewInUKModeTest extends RegistrationPageBehaviour[UkAddress] with ApplicantAddressViewTest {
  override def pageTitle = messagesApi("page.iht.registration.applicantAddress.title")

  override def browserTitle = messagesApi("page.iht.registration.applicantAddress.title")

  override def form: Form[UkAddress] = applicantAddressUkForm

  override def formToView: Form[UkAddress] => Appendable = form =>
    applicant_address(form, isInternational=false,
      CommonBuilder.DefaultCall1, CommonBuilder.DefaultCall1)

  def abroadAddressDocument(): Document = {
    val view = applicant_address(applicantAddressAbroadForm, isInternational=true,
      CommonBuilder.DefaultCall1, CommonBuilder.DefaultCall1).toString
    asDocument(view)
  }

  "Applicant Address View in UK Mode" must {

    behave like registrationPage()

    behave like addressPageUK(guidance)
  }
}

class ApplicantAddressViewInAbroadModeTest extends RegistrationPageBehaviour[UkAddress] with ApplicantAddressViewTest {
  override def pageTitle = messagesApi("page.iht.registration.applicantAddress.title")

  override def browserTitle = messagesApi("page.iht.registration.applicantAddress.title")

  override def form: Form[UkAddress] = applicantAddressAbroadForm

  override def formToView: Form[UkAddress] => Appendable = form =>
    applicant_address(form, isInternational=true,
      CommonBuilder.DefaultCall1, CommonBuilder.DefaultCall1)

  "Applicant Address View In Abroad Mode" must {

    behave like registrationPage()

    behave like addressPageAbroad(guidance)
  }
}
