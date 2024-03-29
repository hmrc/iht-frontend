/*
 * Copyright 2022 HM Revenue & Customs
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

package iht.controllers

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.auth.IhtBaseController
import iht.utils.IhtSection
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.iv.wayfinderpages.login_pass
import iht.views.html.iv.wayfinderpages.verification_pass

import scala.concurrent.Future

class IVWayfinderControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                          val cachingConnector: CachingConnector,
                                          val authConnector: AuthConnector,
                                          val loginPassView: login_pass,
                                          val verificationPassView: verification_pass,
                                          implicit val appConfig: AppConfig,
                                          val cc: MessagesControllerComponents) extends FrontendController(cc) with IVWayfinderController

trait IVWayfinderController extends IhtBaseController {
  override val ihtSection: IhtSection.Value = IhtSection.Registration
  val loginPassView: login_pass
  val verificationPassView: verification_pass
  def loginPass: Action[AnyContent] = authorisedForIht {
    implicit request => {
      Future.successful(Ok(loginPassView()))
    }
  }

  def verificationPass: Action[AnyContent] = authorisedForIht {
    implicit request => {
      Future.successful(Ok(verificationPassView()))
    }
  }
}
