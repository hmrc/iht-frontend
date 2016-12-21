/*
 * Copyright 2016 HM Revenue & Customs
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

import iht.forms.registration.ApplicantForms.applyingForProbateForm
import iht.views.html.registration.applicant.applying_for_probate
import iht.views.registration.RegistrationPageBehaviour
import play.api.i18n.Lang
import play.api.mvc.Call

class ApplyingForProbateViewTest extends RegistrationPageBehaviour {

  override def pageTitleKey = "iht.registration.applicant.applyingForProbate"
  override def browserTitleKey = "page.iht.registration.applicant.applyingForProbate.browserTitle"

  override def fixture() = new {
    implicit val request = createFakeRequest()
    val view = applying_for_probate(applyingForProbateForm, Call("", "")).toString
    val doc = asDocument(view)
  }

  "Applying for Probate View" must {

    behave like registrationPage()

    "show the correct guidance" in {
      val f = fixture()
      messagesShouldBePresent(f.view,
        "page.iht.registration.applicant.applyingForProbate.p1",
        "page.iht.registration.applicant.applyingForProbate.p2")
    }

    "have a fieldset with the Id 'applying-for-probate'" in {
      val view = applying_for_probate(applyingForProbateForm, Call("", ""))(createFakeRequest(), Lang("", "")).toString

      asDocument(view).getElementsByTag("fieldset").first.id shouldBe "applying-for-probate"
    }
  }
}
