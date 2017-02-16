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

package iht.views.application.assets.properties

import iht.forms.ApplicationForms._
import iht.models.application.assets.Property
import iht.testhelpers.CommonBuilder
import iht.views.application.{ApplicationPageBehaviour, CancelComponent}
import iht.views.html.application.asset.properties.property_address
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat.Appendable

trait ApplicantAddressViewTest {
  def guidance: Seq[String] = Seq(Messages("page.iht.registration.applicantAddress.hint"))
}

class PropertyAddressViewTest extends ApplicationPageBehaviour[Property] with ApplicantAddressViewTest {
  override def pageTitle = Messages("iht.estateReport.assets.property.whatIsAddress.question")

  override def browserTitle = Messages("page.iht.application.assets.property.address.browserTitle")

  override def guidanceParagraphs = Set.empty

  override def form: Form[Property] = propertyAddressForm

  override def formToView: Form[Property] => Appendable = form =>
    property_address(form, CommonBuilder.DefaultCall2, CommonBuilder.DefaultCall1)

  override def formTarget = Some(CommonBuilder.DefaultCall1)

  override def cancelComponent = Some(
    CancelComponent(
      CommonBuilder.DefaultCall2,
      Messages("iht.estateReport.assets.properties.returnToAddAProperty")
    )
  )

  override val cancelId: String = "cancel-button"

//  def abroadAddressDocument(): Document = {
//    val view = applicant_address(applicantAddressAbroadForm, isInternational=true,
//      CommonBuilder.DefaultCall1, CommonBuilder.DefaultCall1).toString
//    asDocument(view)
//  }

  "Property Address View in UK Mode" must {

    behave like applicationPageWithErrorSummaryBox()

    behave like addressPageUK()
  }
}

//class ApplicantAddressViewInAbroadModeTest extends RegistrationPageBehaviour[Property] with ApplicantAddressViewTest {
//  override def pageTitle = Messages("page.iht.registration.applicantAddress.title")
//
//  override def browserTitle = Messages("page.iht.registration.applicantAddress.title")
//
//  override def form: Form[Property] = applicantAddressAbroadForm
//
//  override def formToView: Form[Property] => Appendable = form =>
//    applicant_address(form, isInternational=true,
//      CommonBuilder.DefaultCall1, CommonBuilder.DefaultCall1)
//
//  "Applicant Address View In Abroad Mode" must {
//
//    behave like registrationPage()
//
//    behave like addressPageAbroad(guidance)
//  }
//}
