/*
 * Copyright 2019 HM Revenue & Customs
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

import iht.config.IhtFormPartialRetriever
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.auth.IhtBaseController
import iht.utils.IhtSection
import javax.inject.Inject
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.Future

class IVWayfinderControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                          val cachingConnector: CachingConnector,
                                          val authConnector: AuthConnector,
                                          implicit val formPartialRetriever: IhtFormPartialRetriever) extends IVWayfinderController

trait IVWayfinderController extends IhtBaseController {
  override val ihtSection: IhtSection.Value = IhtSection.Registration

  def loginPass: Action[AnyContent] = authorisedForIht {
    implicit request => {
      Future.successful(Ok(iht.views.html.iv.wayfinderpages.login_pass()))
    }
  }

  def verificationPass: Action[AnyContent] = authorisedForIht {
    implicit request => {
      Future.successful(Ok(iht.views.html.iv.wayfinderpages.verification_pass()))
    }
  }
}
