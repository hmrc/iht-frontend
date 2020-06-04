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

package iht.controllers.application

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.auth.IhtBaseController
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.utils.{CommonHelper, IhtSection, StringHelper}
import play.api.Logger
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait ApplicationController extends IhtBaseController with StringHelper {
  override lazy val ihtSection: IhtSection.Value = IhtSection.Application

  def cachingConnector: CachingConnector
  def ihtConnector: IhtConnector

  def withApplicationDetails(userNino: Option[String])
                            (body: RegistrationDetails => ApplicationDetails => Future[Result])
                            (implicit request: Request[_], hc: HeaderCarrier): Future[Result] = {
    withRegistrationDetails { registrationDetails =>
      val optionApplicationDetailsFuture = ihtConnector.getApplication(
        getNino(userNino),
        CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
        registrationDetails.acknowledgmentReference)

      optionApplicationDetailsFuture.flatMap { oad =>
        val applicationDetails = CommonHelper.getOrException(oad)
        body(registrationDetails)(applicationDetails)
      }
    }
  }

  def getApplicationDetails(ihtReference: String, acknowledgementReference: String, userNino: Option[String])
                           (implicit request: Request[_]): Future[ApplicationDetails] = {
    for {
      Some(applicationDetails) <- ihtConnector.getApplication(
        getNino(userNino),
        CommonHelper.getOrExceptionNoIHTRef(Some(ihtReference)),
        acknowledgementReference)
    } yield applicationDetails
  }

  def withRegistrationDetails(body: RegistrationDetails => Future[Result])
                             (implicit request: Request[_]): Future[Result] = {
    cachingConnector.getRegistrationDetails flatMap {
      case None =>
        Logger.info("Registration details not found so re-directing to application overview page")
        Future.successful(Redirect(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad()))
      case Some(rd) => body(rd)
    }
  }
}
