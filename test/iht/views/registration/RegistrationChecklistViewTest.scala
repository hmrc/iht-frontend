/*
 * Copyright 2022 HM Revenue & Customs
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

import iht.testhelpers.viewSpecshelper.registration.RegistrationChecklistMessages
import iht.views.ViewTestHelper
import iht.views.html.registration.registration_checklist
import org.jsoup.Jsoup

class RegistrationChecklistViewTest extends ViewTestHelper with RegistrationChecklistMessages {
  lazy val registrationChecklistView: registration_checklist = app.injector.instanceOf[registration_checklist]

  "RegistrationChecklistView" should {

    lazy val view = registrationChecklistView()(createFakeRequest(), messages)
    lazy val doc = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title() mustBe pageIhtRegistrationChecklistTitle
    }

    "have h1 tage with page title" in {
      doc.select("h1").text() mustBe pageIhtRegistrationChecklistTitle
    }

    "have introduction paragraphs" in {
      doc.select("p").get(2).text() mustBe pageIhtRegistrationChecklistLabel1
      doc.select("p").get(3).text() mustBe pageIhtRegistrationChecklistLabel2
    }

    "have bullet points for user details required" in {
      doc.select("li").get(0).text() mustBe ihtRegistrationChecklistYourNino
      doc.select("li").get(1).text() mustBe ihtRegistrationChecklist2FA
      doc.select("li").get(2).text() mustBe ihtRegistrationChecklistPassport
      doc.select("li").get(3).text() mustBe ihtRegistrationChecklistPayslip
      doc.select("li").get(4).text() mustBe ihtRegistrationChecklistTaxCredit
    }

    "have a h2 tag" in {
      doc.select("div#applicant-details-list h2").text() mustBe ihtRegistrationDetailsNeededTitle
    }

    "have a details needed paragraphs" in {
      doc.select("p").get(4).text() mustBe ihtRegistrationDetailsNeededLabel1
      doc.select("p").get(5).text() mustBe ihtRegistrationDetailsNeededLabel2

    }

    "have bullet points for deceased details required" in {
      doc.select("li").get(5).text() mustBe ihtRegistrationDetailsNeededOname
      doc.select("li").get(6).text() mustBe ihtRegistrationChecklistDateOfBirth
      doc.select("li").get(7).text() mustBe pageIhtRegistrationChecklistDeceasedLabel3
      doc.select("li").get(8).text() mustBe ihtNationalInsuranceNo
      doc.select("li").get(9).text() mustBe pageIhtRegistrationChecklistDeceasedLabel5
      doc.select("li").get(10).text() mustBe pageIhtRegistrationChecklistDeceasedLabel7

    }

    "have a progressive disclosure relating to deceased details" should  {
      "have a reveal text in" in {
        doc.getElementById("application-details-reveal").select("summary").text() mustBe pageIhtRegistrationChecklistRevealTextDied
      }
      "have information relating to deceased details required" in {
        doc.getElementById("application-details-reveal").select("p").get(0).text() mustBe ihtRegistrationDetailsNeededLabel3
        doc.getElementById("application-details-reveal").select("p").get(1).text() mustBe ihtRegistrationDetailsNeededLabel4
        doc.getElementById("application-details-reveal").select("p").get(2).text() mustBe ihtRegistrationDetailsNeededLabel5
      }
    }

    "executor details section" should {
      "have a introduction text" in {
        doc.getElementById("co-execs-details-list").select("p").get(0).text() mustBe ihtRegistrationExecutorLabel1
      }
      "have a list of bullet points" in {
        doc.getElementById("co-execs-details-list").select("li").get(0).text() mustBe ihtRegistrationDetailsNeededOname
        doc.getElementById("co-execs-details-list").select("li").get(1).text() mustBe ihtNationalInsuranceNo
        doc.getElementById("co-execs-details-list").select("li").get(2).text() mustBe ihtRegistrationChecklistDateOfBirth
        doc.getElementById("co-execs-details-list").select("li").get(3).text() mustBe ihtRegistrationExecutorAddress
        doc.getElementById("co-execs-details-list").select("li").get(4).text() mustBe ihtRegistrationChecklistPhoneNoLowerCaseInitial
      }

      "have a progressive disclosure relating to executor details" should  {
        "have a reveal text in" in {
          doc.getElementById("co-execs-details-reveal").select("summary").text() mustBe pageIhtRegistrationChecklistRevealTextExecutors
        }
        "have information relating to executor details required" in {
          doc.getElementById("co-execs-details-reveal").select("p").get(0).text() mustBe ihtRegistrationExecutorLabel2
          doc.getElementById("co-execs-details-reveal").select("p").get(1).text() mustBe ihtRegistrationExecutorLabel3
        }
      }
    }

    "have a continue button" in {
      doc.getElementById("start-registration").text() mustBe pageIhtRegistrationChecklistContinueButton
      doc.getElementById("start-registration").attr("href") mustBe iht.controllers.registration.deceased.routes.DeceasedDateOfDeathController.onPageLoad.url
    }

    "have a leave this page text link" in {
      doc.getElementById("leave-page").text() mustBe pageIhtRegistrationChecklistLeaveLink
      doc.getElementById("leave-page").attr("href") mustBe iht.controllers.filter.routes.FilterController.onPageLoad.url
    }

    "have a save link text" in {
      doc.select("p").get(12).text() mustBe pageIhtRegistrationChecklistSaveLink
    }

  }
}
