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

package iht.views.home

import iht.connector.IhtConnector
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import iht.viewmodels.application.home.IhtHomeRowViewModel
import iht.views.ViewTestHelper
import iht.views.html.home.your_estate_reports
import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.test.Helpers._

class IhtHomeViewTest extends ViewTestHelper with ApplicationControllerTest {

  override implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  var mockIhtConnector = mock[IhtConnector]
  lazy val registrationChecklistPageUrl= iht.controllers.registration.routes.RegistrationChecklistController.onPageLoad()

  def ihtHomeView(ihtApplications: Seq[IhtHomeRowViewModel] = Nil) = {
    implicit val request = createFakeRequest()

    val view = your_estate_reports(ihtApplications).toString()
    asDocument(view)
  }

  lazy val ihtApplications = {
    implicit val hc = new HeaderCarrier
    Seq(IhtHomeRowViewModel("", CommonBuilder.buildIhtApplication, mockIhtConnector),
      IhtHomeRowViewModel("", CommonBuilder.buildIhtApplication, mockIhtConnector))
  }

  before {
    mockIhtConnector = mock[IhtConnector]
    createMockToGetApplicationDetails(mockIhtConnector)
  }

  "IhtHome view" must {
    "have no message keys in html" in {
      running(app) {
        val view = ihtHomeView(ihtApplications).toString
        noMessageKeysShouldBePresent(view)
      }
    }

    "have correct title and browser title " in {
      running(app) {
        val view = ihtHomeView(ihtApplications).toString

        titleShouldBeCorrect(view, messagesApi("page.iht.home.title"))
        browserTitleShouldBeCorrect(view, messagesApi("page.iht.home.browserTitle"))
      }

    }

    "have correct guidance paragraphs when case list have records" in {
      running(app) {
        val view = ihtHomeView(ihtApplications).toString
        messagesShouldBePresent(view, messagesApi("page.iht.home.applicationList.table.guidance.label"))
      }
    }

    "show all the table headers when case list have records" in {
      running(app) {
        val view = ihtHomeView(ihtApplications)
        assertEqualsValue(view, "th#deceased-name-header", messagesApi("page.iht.home.deceasedName.label"))
        assertEqualsValue(view, "th#iht-reference-header", messagesApi("page.iht.home.ihtReference.label"))
        assertEqualsValue(view, "th#date-of-death-header", messagesApi("iht.dateOfDeath"))
        assertEqualsValue(view, "th#status-header", messagesApi("page.iht.home.currentStatus"))
      }
    }

    "have correct guidance paragraphs when the case list is empty" in {
      running(app){
        val view = ihtHomeView().toString
        messagesShouldBePresent(view, messagesApi("page.iht.home.applicationList.table.guidance.label.empty"))
        messagesShouldBePresent(view, messagesApi("page.iht.home.applicationList.table.guidance.p2.empty"))
      }
    }

    "have link to start new registration with correct text" in {
      running(app){
        val view = ihtHomeView(ihtApplications)

        val returnLink = view.getElementById("start-new-registration")
        returnLink.attr("href") shouldBe registrationChecklistPageUrl.url
        returnLink.text() shouldBe messagesApi("site.link.startNewRegistration")
      }

    }
  }


}
