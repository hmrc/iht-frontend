/*
 * Copyright 2020 HM Revenue & Customs
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

import iht.connector.IhtConnector
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.CommonBuilder

import iht.viewmodels.estateReports.YourEstateReportsRowViewModel
import iht.views.ViewTestHelper
import iht.views.html.estateReports.your_estate_reports
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import iht.config.AppConfig
import play.api.i18n.MessagesApi
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import iht.testhelpers.viewSpecshelper.estateReport.EstateReportMessage
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import iht.config.AppConfig

class YourEstateReportsViewTest extends ViewTestHelper with ApplicationControllerTest with EstateReportMessage {

  override implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val registrationChecklistPageUrl = iht.controllers.registration.routes.RegistrationChecklistController.onPageLoad()

  def ihtHomeView(ihtApplications: Seq[YourEstateReportsRowViewModel] = Nil) = {
    implicit val request = createFakeRequest()

    val view = your_estate_reports(ihtApplications, false).toString()
    asDocument(view)
  }

  lazy val ihtApplications = {
    implicit val hc = new HeaderCarrier
    Seq(YourEstateReportsRowViewModel("", CommonBuilder.buildIhtApplication, mockIhtConnector, "Not Started"),
      YourEstateReportsRowViewModel("", CommonBuilder.buildIhtApplication, mockIhtConnector, "Not Started"))
  }

  before {
    createMockToGetApplicationDetails(mockIhtConnector)
  }

  "IhtHome view" must {
    "have no message keys in html" in {
      val view = ihtHomeView(ihtApplications).toString
      noMessageKeysShouldBePresent(view)
    }

    "have correct title and browser title " in {
      val view = ihtHomeView(ihtApplications).toString
      titleShouldBeCorrect(view, messagesApi("page.iht.home.title"))
      browserTitleShouldBeCorrect(view, messagesApi("page.iht.home.browserTitle"))
    }

    "have correct guidance paragraphs when case list have records" in {
      val view = ihtHomeView(ihtApplications).toString
      messagesShouldBePresent(view, messagesApi("page.iht.home.applicationList.table.guidance.label"))
    }

    "show all the table headers when case list have records" in {
      val view = ihtHomeView(ihtApplications)
      assertEqualsValue(view, "th#deceased-name-header", messagesApi("page.iht.home.deceasedName.label"))
      assertEqualsValue(view, "th#iht-reference-header", messagesApi("page.iht.home.ihtReference.label"))
      assertEqualsValue(view, "th#date-of-death-header", messagesApi("iht.dateOfDeath"))
      assertEqualsValue(view, "th#status-header", messagesApi("page.iht.home.currentStatus"))
    }

    "have correct guidance paragraphs when the case list is empty" in {
      val view = ihtHomeView().toString
      messagesShouldBePresent(view, messagesApi("page.iht.home.applicationList.table.guidance.label.empty"))
      messagesShouldBePresent(view, messagesApi("page.iht.home.applicationList.table.guidance.p2.empty"))
    }

    "have link to start new registration with correct text" in {
      val view = ihtHomeView(ihtApplications)

      val returnLink = view.getElementById("start-new-registration")
      returnLink.attr("href") mustBe registrationChecklistPageUrl.url
      returnLink.text() mustBe messagesApi("site.link.startNewRegistration")

    }

  }

  "Progressive disclosure default" should {

    "display personal details submitted reveal" in {
      val view = ihtHomeView(ihtApplications)

      val detailsReveal = view.getElementById("personal-details-submitted-reveal")

      detailsReveal.select("p").get(0).text() mustBe estateReportPersonalDetailsSubmittedErrorP1
      detailsReveal.select("p").get(1).text() mustBe estateReportPersonalDetailsSubmittedErrorP2
      detailsReveal.select("p").get(2).text() mustBe estateReportPersonalDetailsSubmittedErrorP3
    }
  }
}
