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

package iht.views.estateReports

import iht.views.ViewTestHelper
import iht.views.html.estateReports.save_your_estate_report
import org.jsoup.Jsoup
import play.api.test.FakeRequest

class SaveYourEstateReportViewTest extends ViewTestHelper {
  lazy val saveYourEstateReportView: save_your_estate_report = app.injector.instanceOf[save_your_estate_report]

  "the SaveYourEstateReport" must {

      implicit lazy val fakeRequest = FakeRequest()

    "have the correct title" in {
      lazy val view = saveYourEstateReportView()
      lazy val doc = Jsoup.parse(view.body)

      doc.select("h1").text() mustBe messagesApi("page.iht.exit.title")
    }

    "have some introductory text" in {
      lazy val view = saveYourEstateReportView()
      lazy val doc = Jsoup.parse(view.body)

      doc.select("div p").get(2).text() mustBe messagesApi("page.iht.exit.text")
    }

    "have a link for users to save" ignore {
      lazy val view = saveYourEstateReportView()
      lazy val doc = Jsoup.parse(view.body)

      doc.select("div a").get(6).text() mustBe messagesApi("page.iht.exit.link")
    }

    "have a link with href" ignore {
      lazy val view = saveYourEstateReportView()
      lazy val doc = Jsoup.parse(view.body)

      doc.select("div a").get(6).attr("href") mustBe "/inheritance-tax/estate-report"
    }

    "have a button that" should {
      "text on button that..." in {
        lazy val view = saveYourEstateReportView()
        lazy val doc = Jsoup.parse(view.body)

        doc.getElementById("exit-button").text() mustBe messagesApi("page.iht.exit.button")
      }

      "have href of...." in {
        lazy val view = saveYourEstateReportView()
        lazy val doc = Jsoup.parse(view.body)

        doc.getElementById("exit-button").attr("href") mustBe "/inheritance-tax/feedback-survey"
      }

      "have a class of button" in {
        lazy val view = saveYourEstateReportView()
        lazy val doc = Jsoup.parse(view.body)

        doc.getElementById("exit-button").hasClass("button") mustBe true
      }
    }
  }

}
