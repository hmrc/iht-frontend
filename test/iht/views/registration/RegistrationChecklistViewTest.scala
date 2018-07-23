/*
 * Copyright 2018 HM Revenue & Customs
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
import play.api.i18n.Messages.Implicits._
import iht.views.html.registration.{registration_checklist => views}
import org.jsoup.Jsoup

class RegistrationChecklistViewTest extends ViewTestHelper with RegistrationChecklistMessages {

  "RegistrationChecklistView" should {

    lazy val view = views()(createFakeRequest(), applicationMessages, formPartialRetriever)
    lazy val doc = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title() shouldBe pageIhtRegistrationChecklistTitle
    }

    "have h1 tage with page title" in {
      doc.select("h1").text() shouldBe pageIhtRegistrationChecklistTitle
    }

    "have introduction paragraphs" in {
      doc.select("p").get(2).text() shouldBe pageIhtRegistrationChecklistLabel1
      doc.select("p").get(3).text() shouldBe pageIhtRegistrationChecklistLabel2
    }

    "have bullet points for user details required" in {
      doc.select("li").get(0).text() shouldBe ihtRegistrationChecklistYourNino
      doc.select("li").get(1).text() shouldBe ihtRegistrationChecklist2FA
      doc.select("li").get(2).text() shouldBe ihtRegistrationChecklistPassport
      doc.select("li").get(3).text() shouldBe ihtRegistrationChecklistPayslip
      doc.select("li").get(4).text() shouldBe ihtRegistrationChecklistTaxCredit
    }

    "have a h2 tag" in {
      doc.select("h2").text() shouldBe ihtRegistrationDetailsNeededTitle
    }

    "have a details needed paragraphs" in {
      doc.select("p").get(4).text() shouldBe ihtRegistrationDetailsNeededLabel1
      doc.select("p").get(5).text() shouldBe ihtRegistrationDetailsNeededLabel2

    }

    "have bullet points for deceased details required" in {
      doc.select("li").get(5).text() shouldBe ihtRegistrationDetailsNeededOname
      doc.select("li").get(6).text() shouldBe ihtRegistrationChecklistDateOfBirth
      doc.select("li").get(7).text() shouldBe pageIhtRegistrationChecklistDeceasedLabel3
      doc.select("li").get(8).text() shouldBe ihtNationalInsuranceNo
      doc.select("li").get(9).text() shouldBe pageIhtRegistrationChecklistDeceasedLabel5
      doc.select("li").get(10).text() shouldBe pageIhtRegistrationChecklistDeceasedLabel7

    }

    "have a progressive disclosure relating to deceased details" should  {
      "have a reveal text in" in {
        doc.getElementById("application-details-reveal").select("summary").text() shouldBe pageIhtRegistrationChecklistRevealText
      }
      "have information relating to deceased details required" in {
        doc.getElementById("application-details-reveal").select("p").get(0).text() shouldBe ihtRegistrationDetailsNeededLabel3
        doc.getElementById("application-details-reveal").select("p").get(1).text() shouldBe ihtRegistrationDetailsNeededLabel4
        doc.getElementById("application-details-reveal").select("p").get(2).text() shouldBe ihtRegistrationDetailsNeededLabel5
      }
    }

    "executor details section" should {
      "have a introduction text" in {
        doc.getElementById("co-execs-details-list").select("p").get(0).text() shouldBe ihtRegistrationExecutorLabel1
      }
      "have a list of bullet points" in {
        doc.getElementById("co-execs-details-list").select("li").get(0).text() shouldBe ihtRegistrationDetailsNeededOname
        doc.getElementById("co-execs-details-list").select("li").get(1).text() shouldBe ihtNationalInsuranceNo
        doc.getElementById("co-execs-details-list").select("li").get(2).text() shouldBe ihtRegistrationChecklistDateOfBirth
        doc.getElementById("co-execs-details-list").select("li").get(3).text() shouldBe ihtRegistrationExecutorAddress
        doc.getElementById("co-execs-details-list").select("li").get(4).text() shouldBe ihtRegistrationChecklistPhoneNoLowerCaseInitial
      }

      "have a progressive disclosure relating to executor details" should  {
        "have a reveal text in" in {
          doc.getElementById("co-execs-details-reveal").select("summary").text() shouldBe pageIhtRegistrationChecklistRevealText
        }
        "have information relating to executor details required" in {
          doc.getElementById("co-execs-details-reveal").select("p").get(0).text() shouldBe ihtRegistrationExecutorLabel2
          doc.getElementById("co-execs-details-reveal").select("p").get(1).text() shouldBe ihtRegistrationExecutorLabel3
        }
      }
    }

    "have a continue button" in {
      doc.getElementById("start-registration").text() shouldBe pageIhtRegistrationChecklistContinueButton
      doc.getElementById("start-registration").attr("href") shouldBe iht.controllers.registration.deceased.routes.DeceasedDateOfDeathController.onPageLoad().url
    }

    "have a leave this page text link" in {
      doc.getElementById("leave-page").text() shouldBe pageIhtRegistrationChecklistLeaveLink
      doc.getElementById("leave-page").attr("href") shouldBe iht.controllers.filter.routes.FilterController.onPageLoad().url
    }

    "have a save link text" in {
      doc.select("p").get(12).text() shouldBe pageIhtRegistrationChecklistSaveLink
    }

  }
}
