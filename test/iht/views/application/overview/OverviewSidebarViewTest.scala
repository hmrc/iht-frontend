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

package iht.views.application.overview

import iht.views.ViewTestHelper
import iht.views.html.application.overview.overview_sidebar
import play.api.i18n.Messages.Implicits._

class OverviewSidebarViewTest extends ViewTestHelper {

 lazy val submissionDate = "2 October 2016"
  lazy val submissionMonthsLeftOver = 5
  lazy val submissionMonthsYear = 12
  lazy val submissionMonthsLeft = 13

 lazy val viewAsDoc = {
    implicit val request = createFakeRequest()
    asDocument(overview_sidebar(submissionDate, submissionMonthsLeftOver).toString)
  }
  lazy val viewAsDocWithYear = {
    implicit val request = createFakeRequest()
    asDocument(overview_sidebar(submissionDate, submissionMonthsYear).toString)
  }
  lazy val viewAsDocWithMonths = {
    implicit val request = createFakeRequest()
    asDocument(overview_sidebar(submissionDate, submissionMonthsLeft).toString)
  }

  "Overview Sidebar view" must {

    "have no message keys in html" in {
      val view = viewAsDoc.toString
      noMessageKeysShouldBePresent(view)
    }

    "show the deadline date when there are 11 or less months remaining " in {
      assertRenderedById(viewAsDoc, "estate-report-deadline-date")
      assertContainsText(viewAsDoc, submissionDate)
    }

    "show the year copy when there are 12 months remaining " in {
      assertRenderedById(viewAsDocWithYear, "estate-report-deadline-date")
      assertContainsText(viewAsDocWithYear, "1 year")
    }

    "show the month countdown when there are 13 months remaining " in {
      assertRenderedById(viewAsDocWithMonths, "estate-report-deadline-date")
      assertContainsText(viewAsDocWithMonths, "13 months")
    }

    "show the correct style class for the date panel" in {
      val datePanel = viewAsDoc.getElementById("estate-report-deadline-date")
      datePanel.attr("class") mustBe "panel panel-border-wide panel-indent--gutter"
    }

    "show the correct guidance" in {
      assertContainsText(viewAsDoc, messagesApi("page.iht.application.overview.time.limit1"))
      assertContainsText(viewAsDoc, messagesApi("page.iht.application.overview.time.limit2"))
      assertContainsText(viewAsDoc, messagesApi("page.iht.application.overview.timeScale.guidance"))
    }

    "show the return link with correct text" in {
      val link = viewAsDoc.getElementById("return-to-estate-report-link")
      link.text mustBe messagesApi("iht.estateReport.goToEstateReports")
      link.attr("href") mustBe iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url
    }
  }

}
