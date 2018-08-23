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

package iht.controllers

import iht.config.IhtFormPartialRetriever
import iht.connector.IhtConnectors
import iht.controllers.auth.IhtActions
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import iht.utils.IhtSection
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

object IVWayfinderController extends IVWayfinderController with IhtConnectors

trait IVWayfinderController extends IhtActions {
  override val ihtSection: IhtSection.Value = IhtSection.Registration

  implicit val formPartialRetriever: FormPartialRetriever = IhtFormPartialRetriever

  def loginPass: Action[AnyContent] = authorisedForIht {
    implicit user =>
      implicit request => {
        Future.successful(Ok(iht.views.html.iv.wayfinderpages.login_pass()))
      }
  }

  def verificationPass: Action[AnyContent] = authorisedForIht {
    implicit user =>
      implicit request => {
        Future.successful(Ok(iht.views.html.iv.wayfinderpages.verifcation_pass()))
      }
  }
}
