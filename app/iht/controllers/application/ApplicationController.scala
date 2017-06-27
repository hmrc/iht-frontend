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

package iht.controllers.application

import iht.connector.{CachingConnector, IhtConnector, IhtConnectors}
import iht.controllers.auth.IhtActions
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.utils.{CommonHelper, IhtSection}
import play.api.Logger
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.i18n.I18nSupport

import scala.concurrent.Future

trait ApplicationController extends FrontendController with IhtActions with I18nSupport with IhtConnectors  {
  override lazy val ihtSection = IhtSection.Application

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def withApplicationDetails(body: RegistrationDetails => ApplicationDetails => Future[Result])
                            (implicit request: Request[_], user: AuthContext, hc: HeaderCarrier): Future[Result] = {
    withRegistrationDetails { registrationDetails =>
      val optionApplicationDetailsFuture = ihtConnector.getApplication(
        CommonHelper.getNino(user),
        CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
        registrationDetails.acknowledgmentReference)

      optionApplicationDetailsFuture.flatMap { oad =>
        val applicationDetails = CommonHelper.getOrException(oad)
        body(registrationDetails)(applicationDetails)
      }
    }
  }

  def getApplicationDetails(ihtReference: String, acknowledgementReference: String)
                           (implicit request: Request[_], user: AuthContext, hc: HeaderCarrier) = {
    for {
      Some(applicationDetails) <- ihtConnector.getApplication(
        CommonHelper.getNino(user),
        CommonHelper.getOrExceptionNoIHTRef(Some(ihtReference)),
        acknowledgementReference)
    } yield applicationDetails
  }

  def withRegistrationDetails(body: RegistrationDetails => Future[Result])
                             (implicit request: Request[_], user: AuthContext, hc: HeaderCarrier): Future[Result] = {
    cachingConnector.getRegistrationDetails flatMap {
      case None =>
        Logger.info("Registration details not found so re-directing to application overview page")
        Future.successful(Redirect(iht.controllers.home.routes.IhtHomeController.onPageLoad()))
      case Some(rd) => body(rd)
    }
  }
}
