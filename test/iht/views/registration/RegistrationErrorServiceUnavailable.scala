/*
 * Copyright 2021 HM Revenue & Customs
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
import iht.views.helpers.MessagesHelper
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.config.AppConfig

class RegistrationErrorServiceUnavailable extends ViewTestHelper with MessagesHelper {

  implicit lazy val fakeRequest = FakeRequest()
  lazy val view = iht.views.html.registration.registration_error_serviceUnavailable()
  lazy val doc = Jsoup.parse(view.body)

  "EstateReportsErrorServiceUnavailable" must {

    "have the correct title" in {
      doc.title() mustBe pageIhtIVFailureTechnicalIssueHeading

    }
    "have the correct first paragraph" in {
      doc.select("p").eq(2).text mustBe errorRegistrationServiceUnavailablep1
    }

    "have the correct second paragraph" in {
      doc.select("p").eq(3).text mustBe errorRegistrationServiceUnavailablep2
    }

    "have the correct third paragraph" in {
      doc.select("p").eq(4).text mustBe errorRegistrationServiceUnavailablep3
    }

    "have a try again button" should {
      "that has a text" in {
        doc.getElementsByClass("button").text() mustBe ihtIVTryAgain
      }
      "that has a href" in {
        doc.getElementsByClass("button").attr("href") mustBe ihtIVTryAgainLink
      }
    }

    "have a sign out link" should {
      "that has a text" in {
        doc.select("div > a").eq(3).text() mustBe pageIHTSignOut
      }
      "that has a href" in {
        doc.select("div > a").eq(3).attr("href") mustBe pageIHTSignOutLink
      }
    }
  }

}