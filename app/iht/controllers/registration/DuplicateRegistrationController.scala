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
import iht.views.html.registration.duplicate_registration
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

class DuplicateRegistrationControllerImpl @Inject()(val cachingConnector: CachingConnector,
                                                    val ihtConnector: IhtConnector,
                                                    val authConnector: AuthConnector,
                                                    val duplicateRegistrationView: duplicate_registration,
                                                    implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with DuplicateRegistrationController

trait DuplicateRegistrationController extends RegistrationController {
  override def guardConditions: Set[Predicate] = Set.empty
  val duplicateRegistrationView: duplicate_registration
  def onPageLoad(ihtReference: String) = authorisedForIht {
    implicit request => { // False positive warning. Workaround: scala/bug#11175 -Ywarn-unused:params false positive
      Future.successful(Ok(duplicateRegistrationView(ihtReference)))
    }
  }

  def onSubmit = authorisedForIht {
    implicit request => { // False positive warning. Workaround: scala/bug#11175 -Ywarn-unused:params false positive
        Future(Redirect(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad))
    }
  }
}
