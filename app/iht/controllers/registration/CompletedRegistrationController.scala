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

package iht.controllers.registration

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.views.html.registration.completed_registration
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future


class CompletedRegistrationControllerImpl @Inject()(val cachingConnector: CachingConnector,
                                                    val ihtConnector: IhtConnector,
                                                    val authConnector: AuthConnector,
                                                    val completedRegistrationView: completed_registration,
                                                    implicit val appConfig: AppConfig,
                                                    val cc: MessagesControllerComponents) extends FrontendController(cc) with CompletedRegistrationController

trait CompletedRegistrationController extends RegistrationController {
  def cachingConnector: CachingConnector
  override def guardConditions: Set[Predicate] = Set.empty
  val completedRegistrationView: completed_registration
  def onPageLoad() = authorisedForIht {
    implicit request => { // False positive warning. Workaround: scala/bug#11175 -Ywarn-unused:params false positive
      withRegistrationDetailsOrRedirect(request.uri) { rd =>
         Future.successful(Ok(completedRegistrationView(rd.ihtReference.get)))
      }
    }
  }

  def onSubmit = authorisedForIht {
    implicit request => { // False positive warning. Workaround: scala/bug#11175 -Ywarn-unused:params false positive
      Future(Redirect(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad))
    }
  }
}
