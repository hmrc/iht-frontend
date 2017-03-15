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

import iht.views.ViewTestHelper
import iht.views.html.registration.registration_checklist
import play.api.i18n.Messages.Implicits._

class RegistrationChecklistViewTest extends ViewTestHelper {

  "RegistrationChecklistView" must {

    "have no message keys in html" in {
      val view = registration_checklist()(createFakeRequest(), applicationMessages).toString
      noMessageKeysShouldBePresent(view)
    }

    "contain the correct page heading and contents for first paragraph of guidance" in {

      val view = registration_checklist()(createFakeRequest(), applicationMessages).toString

      view should include (messagesApi("page.iht.registration.checklist.title"))
      view should include (messagesApi("page.iht.registration.checklist.label1"))
      view should include (messagesApi("page.iht.registration.checklist.label2"))
      view should include (messagesApi("page.iht.registration.checklist.label3"))
    }

    "contain the required contents for the deceased guidance section" in {
      val view = registration_checklist()(createFakeRequest(), applicationMessages).toString

      view should include (messagesApi("iht.name.lowerCaseInitial"))
      view should include (messagesApi("iht.registration.checklist.dateOfBirth"))
      view should include (messagesApi("page.iht.registration.checklist.deceased.label3"))
      view should include (messagesApi("iht.nationalInsuranceNo"))
      view should include (messagesApi("page.iht.registration.checklist.deceased.label5"))
      view should include (messagesApi("page.iht.registration.checklist.deceased.label6"))
      view should include (messagesApi("page.iht.registration.checklist.deceased.label7"))


      view should include (messagesApi("page.iht.registration.checklist.deceased.reveal.label1"))
      view should include (messagesApi("page.iht.registration.checklist.deceased.reveal.label2"))
      view should include (messagesApi("iht.registration.ninoNotOnPayslip"))
      view should include (messagesApi("page.iht.registration.checklist.deceased.reveal.label4"))
    }

    "contain the required contents for the Applicant guidance section" in {

      val view = registration_checklist()(createFakeRequest(), applicationMessages).toString

      view should include (messagesApi("iht.address.lowerCaseInitial"))
      view should include (messagesApi("iht.nationalInsuranceNo"))
      view should include (messagesApi("iht.registration.checklist.phoneNo.lowerCaseInitial"))
      view should include (messagesApi("page.iht.registration.checklist.applicant.label4"))
      view should include (messagesApi("page.iht.registration.checklist.applicant.label5"))

      view should include (messagesApi("page.iht.registration.checklist.applicant.reveal.label1"))
      view should include (messagesApi("page.iht.registration.checklist.applicant.reveal.label2"))
      view should include (messagesApi("page.iht.registration.checklist.applicant.reveal.label3"))
      view should include (messagesApi("page.iht.registration.checklist.applicant.reveal.label4"))

    }

    "contain the required contents for the grant of representation guidance section" in {

      val view = registration_checklist()(createFakeRequest(), applicationMessages).toString

      view should include (messagesApi("iht.name.lowerCaseInitial"))
      view should include (messagesApi("iht.registration.checklist.dateOfBirth"))
      view should include (messagesApi("iht.nationalInsuranceNo"))
      view should include (messagesApi("iht.address.lowerCaseInitial"))
      view should include (messagesApi("iht.registration.checklist.phoneNo.lowerCaseInitial"))

      view should include (messagesApi("page.iht.registration.checklist.exec.reveal.label1"))
      view should include (messagesApi("page.iht.registration.checklist.exec.reveal.label2"))
      view should include (messagesApi("page.iht.registration.checklist.exec.reveal.label3"))

    }

    "contain Start registration button with target as deceased date of death page" in {

      val view = registration_checklist()(createFakeRequest(), applicationMessages).toString
      val doc = asDocument(view)

      view should include (messagesApi("page.iht.registration.checklist.startRegistrationButton"))

      val link = doc.getElementById("start-registration")
      link.text shouldBe messagesApi("page.iht.registration.checklist.startRegistrationButton")
      link.attr("href") shouldBe iht.controllers.registration.deceased.routes.DeceasedDateOfDeathController.onPageLoad.url

    }

    "contain Leave this page to get all the details you need link and has a target as what you want to do page" in {

      val view = registration_checklist()(createFakeRequest(), applicationMessages).toString
      val doc = asDocument(view)

      val link = doc.getElementById("leave-page")
      link.text shouldBe messagesApi("page.iht.registration.checklist.leaveLink")
      link.attr("href") shouldBe iht.controllers.filter.routes.FilterController.onPageLoad.url

    }
  }
}
