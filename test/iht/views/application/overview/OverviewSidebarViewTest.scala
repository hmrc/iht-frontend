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

package iht.views.application.overview

import iht.views.ViewTestHelper
import iht.views.html.application.overview.overview_sidebar
import play.api.i18n.Messages.Implicits._

class OverviewSidebarViewTest extends ViewTestHelper {

 lazy val submissionDate = "2 October 2016"
 lazy val viewAsDoc = {
    implicit val request = createFakeRequest()
    asDocument(overview_sidebar(submissionDate).toString)
  }

  "Overview Sidebar view" must {

    "show the correct date that has been input to the view " in {
      assertRenderedById(viewAsDoc, "estate-report-deadline-date")
      assertContainsText(viewAsDoc, submissionDate)
    }

    "show the correct style class for the date panel" in {
      val datePanel = viewAsDoc.getElementById("estate-report-deadline-date")
      datePanel.attr("class") shouldBe "panel-indent panel-indent--gutter"
    }

    "show the correct guidance" in {
      assertContainsText(viewAsDoc, messagesApi("page.iht.application.overview.time.limit1"))
      assertContainsText(viewAsDoc, messagesApi("page.iht.application.overview.time.limit2"))
      assertContainsText(viewAsDoc, messagesApi("page.iht.application.overview.timeScale.guidance"))
    }

    "show the return link with correct text" in {
      val link = viewAsDoc.getElementById("return-to-estate-report-link")
      link.text shouldBe messagesApi("iht.estateReport.goToEstateReports")
      link.attr("href") shouldBe iht.controllers.home.routes.IhtHomeController.onPageLoad().url
    }
  }

}
