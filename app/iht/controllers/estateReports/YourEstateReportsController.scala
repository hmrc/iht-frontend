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

package iht.controllers.estateReports

import java.util.UUID

import iht.connector.{CachingConnector, IhtConnector, IhtConnectors}
import iht.constants.Constants
import iht.controllers.application.ApplicationController
import iht.utils.{SessionHelper, StringHelper, ApplicationStatus => AppStatus}
import iht.viewmodels.estateReports.YourEstateReportsRowViewModel
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.http.{SessionKeys, Upstream4xxResponse}

/**
  *
  * Created by Vineet Tyagi on 18/06/15.
  *
  */
object YourEstateReportsController extends YourEstateReportsController with IhtConnectors

trait YourEstateReportsController extends ApplicationController {

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def onPageLoad = authorisedForIht {
    implicit user =>
      implicit request => {
        val nino = StringHelper.getNino(user)

        ihtConnector.getCaseList(nino).map {
          case listOfCases if listOfCases.nonEmpty => {

            listOfCases.foreach { ihtCase =>
              Logger.info("Application status retrieved from DES is ::: " + ihtCase.currentStatus)

              ihtCase.currentStatus match {
                case AppStatus.AwaitingReturn | AppStatus.KickOut => {}
                case _ => {
                  ihtConnector.deleteApplication(nino, ihtCase.ihtRefNo)
                }
              }
            }

            val viewModels = listOfCases.map {
              ihtCase => YourEstateReportsRowViewModel(nino, ihtCase, ihtConnector)
            }

            Ok(iht.views.html.estateReports.your_estate_reports(viewModels))
              .withSession(request.session + (SessionKeys.sessionId -> s"session-${UUID.randomUUID}") + (Constants.NINO -> nino))
          }
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
