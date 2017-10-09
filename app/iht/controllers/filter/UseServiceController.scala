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

package iht.controllers.filter

import iht.config.{IhtFormPartialRetriever, FrontendAuthConnector}
import iht.connector.{CachingConnector, IhtConnector}
import iht.constants.Constants._
import iht.constants.IhtProperties._
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.controller.{UnauthorisedAction, FrontendController}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

object UseServiceController extends UseServiceController {
  val cachingConnector = CachingConnector
  val ihtConnector = IhtConnector
  val authConnector: AuthConnector = FrontendAuthConnector

}

trait UseServiceController extends FrontendController {
  def cachingConnector: CachingConnector
  def ihtConnector: IhtConnector

  implicit val formPartialRetriever: FormPartialRetriever = IhtFormPartialRetriever

  private def onPageLoad(estimatedValue: String, jointAssets: Boolean) = UnauthorisedAction.async {
    implicit request => {
      Future.successful(Ok(iht.views.html.filter.use_service(estimatedValue, jointAssets)))
    }
  }

  def onPageLoadUnder = onPageLoad(under325000, jointAssets = false)
  def onPageLoadUnderWithJointAssets = onPageLoad(under325000, jointAssets = true)

  def onPageLoadOver = onPageLoad(between325000and1million, jointAssets = false)
  def onPageLoadOverWithJointAssets = onPageLoad(between325000and1million, jointAssets = true)
}
