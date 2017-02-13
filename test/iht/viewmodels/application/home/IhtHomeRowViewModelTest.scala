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

package iht.viewmodels.application.home

import iht.connector.IhtConnector
import iht.constants.IhtProperties
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import iht.utils.{ApplicationStatus, CommonHelper}
import org.joda.time.LocalDate
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.play.http.HeaderCarrier

class IhtHomeRowViewModelTest extends ApplicationControllerTest{

  var mockIhtConnector = mock[IhtConnector]
  implicit val hc = new HeaderCarrier

  before {
    mockIhtConnector = mock[IhtConnector]
    createMockToGetApplicationDetails(mockIhtConnector)
  }

  "IhtHomeRowViewModel" must {
    implicit val messages: Messages = app.injector.instanceOf[Messages]
    val ihtApp = CommonBuilder.buildIhtApplication.copy(currentStatus = ApplicationStatus.NotStarted)
    def viewModel = IhtHomeRowViewModel("", ihtApp, mockIhtConnector)

    "should be created from IhtApplication with deceased name" in {
      viewModel.deceasedName shouldBe CommonBuilder.DefaultName
    }

    "should be created from IhtApplication with Iht reference number" in {
      viewModel.ihtRefNo shouldBe "A1912232"
    }

    "should be created from IhtApplication with deceased date of death" in {
      viewModel.dateOfDeath shouldBe new LocalDate(2014, 10, 5).toString(IhtProperties.dateFormatForDisplay)
    }

    "should be created from IhtApplication with current status" in {
      viewModel.currentStatus shouldBe CommonHelper.formatStatus(ApplicationStatus.NotStarted)
    }

    "should be created from IhtApplication with link label" in {
      viewModel.linkLabel shouldBe Messages("iht.start")
    }

    "should be created from IhtApplication with link" in {
      viewModel.link shouldBe
        iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(ihtApp.ihtRefNo)
    }
  }

}
