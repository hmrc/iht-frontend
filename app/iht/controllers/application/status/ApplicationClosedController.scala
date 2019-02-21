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

package iht.controllers.application.status

import iht.connector.{CachingConnector, IhtConnector}
import javax.inject.Inject
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Request
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.partials.FormPartialRetriever

class ApplicationClosedControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                val cachingConnector: CachingConnector,
                                                val authConnector: AuthConnector,
                                                val formPartialRetriever: FormPartialRetriever) extends ApplicationClosedController

trait ApplicationClosedController extends ApplicationStatusController {
  def getView = (ihtReference, deceasedName, probateDetails) => (request: Request[_], formPartialRetriever: FormPartialRetriever) => {

    implicit val req = request
    implicit val fpr = formPartialRetriever

    iht.views.html.application.status.closed_application(ihtReference, deceasedName, probateDetails)
  }
}
