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

import javax.inject.Inject

import iht.connector.IhtConnector
import iht.constants.IhtProperties
import iht.models.application.IhtApplication
import iht.utils.{CommonHelper, ApplicationStatus => AppStatus}
import play.api.{Application, Logger}
import play.api.i18n.{Lang, Messages}
import play.api.mvc.Call
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by vineet on 26/09/16.
  */
case class IhtHomeRowViewModel(deceasedName: String,
                               ihtRefNo: String,
                               dateOfDeath: String,
                               currentStatus: String,
                               linkLabel: String,
                               link: Call,
                               linkScreenreader: String)

object IhtHomeRowViewModel {
  def apply(nino: String, ihtApp: IhtApplication, ihtConnector: IhtConnector)(implicit headerCarrier: HeaderCarrier,
                                                    lang: Lang, application: Application) = {

    implicit val messages: Messages = applicationMessages(lang, application)
    val currentStatus = getStatus(nino, ihtApp, ihtConnector)
    val ihtRef = ihtApp.ihtRefNo

    new IhtHomeRowViewModel(deceasedName = s"${ihtApp.firstName} ${ihtApp.lastName}",
      ihtRefNo = ihtApp.ihtRefNo,
      dateOfDeath = ihtApp.dateOfDeath.toString(IhtProperties.dateFormatForDisplay),
      currentStatus = getApplicationStatusMessage(currentStatus),
      linkLabel = getLinkLabel(currentStatus),
      link = getLink(currentStatus, ihtRef),
      linkScreenreader = getLinkScreenreader(currentStatus, s"${ihtApp.firstName} ${ihtApp.lastName}")
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

        case AppStatus.NotStarted | AppStatus.InProgress | AppStatus.KickOut => {

          iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(ihtRef)
        }

        case AppStatus.InReview | AppStatus.UnderEnquiry =>
          iht.controllers.application.status.routes.ApplicationInReviewController.onPageLoad(ihtRef)

        case AppStatus.Closed =>
          iht.controllers.application.status.routes.ApplicationClosedController.onPageLoad(ihtRef)

        case AppStatus.ClearanceGranted =>
          iht.controllers.application.status.routes.ApplicationClosedAndClearedController.onPageLoad(ihtRef)

        case AppStatus.IneligibleApplication => {
          Logger.warn("Ineligible Application status found")
          throw new RuntimeException("Ineligible Application status found")
        }
        case _ => {
          Logger.error("Unknown Application status found")
          throw new RuntimeException("Unknown Application status found")
        }

      }
    }

  private def getStatus(nino: String,
                        ihtApp: IhtApplication,
                        ihtConnector: IhtConnector)(implicit headerCarrier: HeaderCarrier) = {

    ihtApp.currentStatus match {
      // For display purposes only, change 'Under Enquiry' to 'In Review'
      case AppStatus.UnderEnquiry => AppStatus.InReview
      case AppStatus.Closed => AppStatus.Closed
      case AppStatus.IneligibleApplication => AppStatus.IneligibleApplication
      case _ => {
        val applicationDetails = CommonHelper.getOrExceptionNoApplication(Await.result(ihtConnector
          .getApplication(nino, ihtApp.ihtRefNo, ihtApp.acknowledgmentReference), Duration.Inf))
         CommonHelper.determineStatusToUse(ihtApp.currentStatus, applicationDetails.status)
      }
    }
  }
}
