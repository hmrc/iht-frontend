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

package iht.controllers.application.status

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import javax.inject.Inject
import play.api.mvc.{MessagesControllerComponents, Request}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.application.status.in_review_application

class ApplicationInReviewControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                  val cachingConnector: CachingConnector,
                                                  val authConnector: AuthConnector,
                                                  val inReviewApplicationView: in_review_application,
implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with ApplicationInReviewController

trait ApplicationInReviewController extends ApplicationStatusController {
  val inReviewApplicationView: in_review_application
  def getView = (ihtReference, deceasedName, probateDetails) => (request: Request[_]) => {

        implicit val req = request

    inReviewApplicationView(ihtReference, deceasedName, probateDetails)
      }
}
