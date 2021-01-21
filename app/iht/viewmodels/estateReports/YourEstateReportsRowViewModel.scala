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

package iht.viewmodels.estateReports

import iht.connector.IhtConnector
import iht.models.application.IhtApplication
import iht.utils.CustomLanguageUtils.Dates
import iht.utils.{ApplicationStatus => AppStatus}
import play.api.Logger
import play.api.i18n.Messages
import play.api.mvc.Call

case class YourEstateReportsRowViewModel(deceasedName: String,
                               ihtRefNo: String,
                               dateOfDeath: String,
                               currentStatus: String,
                               linkLabel: String,
                               link: Call,
                               linkScreenreader: String)

object YourEstateReportsRowViewModel {
  def apply(nino: String, ihtApp: IhtApplication, ihtConnector: IhtConnector, currentStatus: String)
           (implicit messages: Messages): YourEstateReportsRowViewModel = {

    val ihtRef = ihtApp.ihtRefNo

    new YourEstateReportsRowViewModel(deceasedName = s"${ihtApp.firstName} ${ihtApp.lastName}",
      ihtRefNo = ihtApp.ihtRefNo,
      dateOfDeath = Dates.formatDate(ihtApp.dateOfDeath)(messages).toString,
      currentStatus = getApplicationStatusMessage(currentStatus)(messages),
      linkLabel = getLinkLabel(currentStatus)(messages),
      link = getLink(currentStatus, ihtRef),
      linkScreenreader = getLinkScreenreader(currentStatus, s"${ihtApp.firstName} ${ihtApp.lastName}")(messages)
    )
  }

  private def getApplicationStatusMessage(currentStatus: String)(implicit messages: Messages) = {
    currentStatus match {
      case AppStatus.NotStarted => messages("iht.notStarted")
      case AppStatus.InProgress => messages("iht.inProgress")
      case AppStatus.KickOut => messages("iht.inProgress")
      case AppStatus.InReview => messages("iht.inReview")
      case AppStatus.UnderEnquiry => messages("iht.inReview")
      case AppStatus.Closed => messages("iht.closed")
      case AppStatus.ClearanceGranted => messages("iht.closed")
      case AppStatus.IneligibleApplication => messages("iht.ineligibleApplication")
    }
  }

  private def getLinkLabel(currentStatus: String)(implicit messages: Messages) = {
    currentStatus match {
      case AppStatus.NotStarted => messages("iht.start")
      case AppStatus.InProgress => messages("iht.continue")
      case AppStatus.KickOut => messages("iht.continue")
      case _ => messages("page.iht.home.button.viewApplication.label")

    }
  }

  private def getLinkScreenreader(currentStatus: String, deceasedName: String)(implicit messages: Messages) = {
    currentStatus match {
      case AppStatus.NotStarted => messages("page.iht.home.button.startApplication.screenReader", deceasedName)
      case AppStatus.InProgress => messages("page.iht.home.button.continueApplication.screenReader", deceasedName)
      case AppStatus.KickOut => messages("page.iht.home.button.continueApplication.screenReader", deceasedName)
      case _ => messages("page.iht.home.button.viewApplication.screenReader", deceasedName)

    }
  }

  private def getLink(currentStatus: String, ihtRef: String) = {

    currentStatus match {

       case AppStatus.NotStarted | AppStatus.InProgress | AppStatus.KickOut =>
         iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(ihtRef)

       case AppStatus.InReview | AppStatus.UnderEnquiry =>
         iht.controllers.application.status.routes.ApplicationInReviewController.onPageLoad(ihtRef)

       case AppStatus.Closed =>
         iht.controllers.application.status.routes.ApplicationClosedController.onPageLoad(ihtRef)

       case AppStatus.ClearanceGranted =>
         iht.controllers.application.status.routes.ApplicationClosedController.onPageLoad(ihtRef)

       case AppStatus.IneligibleApplication =>
         Logger.warn("Ineligible Application status found")
         throw new RuntimeException("Ineligible Application status found")

       case _ =>
         Logger.error("Unknown Application status found")
         throw new RuntimeException("Unknown Application status found")

    }
  }
}
