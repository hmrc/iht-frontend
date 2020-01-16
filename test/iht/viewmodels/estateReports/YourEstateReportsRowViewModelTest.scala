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

package iht.viewmodels.estateReports

import iht.connector.IhtConnector
import iht.constants.IhtProperties
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.CommonBuilder

import iht.utils.ApplicationStatus
import org.joda.time.LocalDate
import uk.gov.hmrc.http.HeaderCarrier

class YourEstateReportsRowViewModelTest extends ApplicationControllerTest {

  implicit val hc = new HeaderCarrier

  before {
    createMockToGetApplicationDetails(mockIhtConnector)
  }

  "YourEstateReportsRowViewModel" must {

    val ihtApp = CommonBuilder.buildIhtApplication.copy(currentStatus = ApplicationStatus.NotStarted)
    def viewModel = YourEstateReportsRowViewModel("", ihtApp, mockIhtConnector, "Not Started")

    "should be created from IhtApplication with deceased name" in {
      viewModel.deceasedName mustBe CommonBuilder.DefaultName
    }

    "should be created from IhtApplication with Iht reference number" in {
      viewModel.ihtRefNo mustBe "A1912232"
    }

    "should be created from IhtApplication with deceased date of death" in {
      viewModel.dateOfDeath mustBe new LocalDate(2014, 10, 5).toString(appConfig.dateFormatForDisplay)
    }

    "should be created from IhtApplication with status of not started" in {
      viewModel.currentStatus mustBe messagesApi("iht.notStarted")
    }

    "should be created from IhtApplication with status of in progress" in {
      val ihtApp = CommonBuilder.buildIhtApplication.copy(currentStatus = ApplicationStatus.InProgress)
      def viewModel = YourEstateReportsRowViewModel("", ihtApp, mockIhtConnector, "In Progress")
      viewModel.currentStatus mustBe messagesApi("iht.inProgress")
    }

    "should be created from IhtApplication with link label" in {
      viewModel.linkLabel mustBe messagesApi("iht.start")
    }

    "should be created from IhtApplication with link" in {
      viewModel.link mustBe
        iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(ihtApp.ihtRefNo)
    }
  }

}
