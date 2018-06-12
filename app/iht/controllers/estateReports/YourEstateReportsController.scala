/*
 * Copyright 2018 HM Revenue & Customs
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

package iht.controllers.estateReports

import java.util.UUID

import iht.connector.{CachingConnector, IhtConnector, IhtConnectors}
import iht.constants.Constants
import iht.controllers.application.ApplicationController
import iht.models.application.IhtApplication
import iht.utils.{CommonHelper, DeceasedInfoHelper, SessionHelper, StringHelper, ApplicationStatus => AppStatus}
import iht.viewmodels.estateReports.YourEstateReportsRowViewModel
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys, Upstream4xxResponse}

import scala.concurrent.Future

object YourEstateReportsController extends YourEstateReportsController with IhtConnectors

trait YourEstateReportsController extends ApplicationController {

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def onPageLoad: Action[AnyContent] = authorisedForIht {
    implicit user => {
      implicit request => {
        val nino = StringHelper.getNino(user)

        ihtConnector.getCaseList(nino).flatMap {
          case listOfCases if listOfCases.nonEmpty => {

            listOfCases.foreach { ihtCase =>
              Logger.info("Application status retrieved from DES is ::: " + ihtCase.currentStatus)

              ihtCase.currentStatus match {
                case AppStatus.AwaitingReturn | AppStatus.KickOut =>
                case _ => ihtConnector.deleteApplication(nino, ihtCase.ihtRefNo)
              }
            }

            val futureViewModels = Future.sequence(listOfCases.map {
              ihtCase =>
                getStatus(nino, ihtCase, ihtConnector).map(status =>
                  YourEstateReportsRowViewModel(nino, ihtCase, ihtConnector, status)
                )
            })

            futureViewModels.map(seqOfModels =>
              Ok(iht.views.html.estateReports.your_estate_reports(seqOfModels))
                .withSession(request.session + (SessionKeys.sessionId -> s"session-${UUID.randomUUID}") + (Constants.NINO -> nino)))
          }

          case _ =>
            Future.successful(Ok(iht.views.html.estateReports.your_estate_reports(Nil)).withSession(
              SessionHelper.ensureSessionHasNino(request.session, user) +
                (SessionKeys.sessionId -> s"session-${UUID.randomUUID}")))
        } recover {
          case e: Upstream4xxResponse if e.upstreamResponseCode == 404 =>
            Ok(iht.views.html.estateReports.your_estate_reports(Nil)).withSession(
              SessionHelper.ensureSessionHasNino(request.session, user) +
                (SessionKeys.sessionId -> s"session-${UUID.randomUUID}")
            )
        }
      }
    }
  }

  private[controllers] def getStatus(nino: String,
                        ihtApp: IhtApplication,
                        ihtConnector: IhtConnector)(implicit headerCarrier: HeaderCarrier): Future[String] = {

    ihtApp.currentStatus match {
      // For display purposes only, change 'Under Enquiry' to 'In Review'
      case AppStatus.UnderEnquiry => Future.successful(AppStatus.InReview)
      case AppStatus.Closed => Future.successful(AppStatus.Closed)
      case AppStatus.IneligibleApplication => Future.successful(AppStatus.IneligibleApplication)
      case _ => for{
        appDetails <- ihtConnector.getApplication(nino, ihtApp.ihtRefNo, ihtApp.acknowledgmentReference)
      }yield{
        DeceasedInfoHelper.determineStatusToUse(ihtApp.currentStatus, CommonHelper.getOrExceptionNoApplication(appDetails).status)
      }
    }
  }
}
