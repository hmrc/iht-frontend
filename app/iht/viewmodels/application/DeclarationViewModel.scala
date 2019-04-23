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

package iht.viewmodels.application

import iht.config.AppConfig
import iht.connector.IhtConnector
import iht.models.application.ApplicationDetails
import iht.models.{CoExecutor, RegistrationDetails}
import iht.utils.DeclarationHelper
import play.api.data.Form
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier

case class DeclarationViewModel(declarationForm: Form[Boolean],
                                declarationType: String,
                                executors: Seq[CoExecutor],
                                isMultipleExecutor: Boolean,
                                registrationDetails: RegistrationDetails,
                                riskMessageFromEdh: Option[String])

object DeclarationViewModel {
  def apply(form: Form[Boolean],
            appDetails: ApplicationDetails,
            regDetails: RegistrationDetails,
            nino: String,
            ihtConnector: IhtConnector,
            riskMsgFromEdh: Option[String])(implicit request: Request[_], hc: HeaderCarrier, appConfig: AppConfig): DeclarationViewModel = {

    new DeclarationViewModel(
      form,
      DeclarationHelper.getDeclarationType(appDetails),
      regDetails.coExecutors,
      isMultipleExecutors(regDetails),
      regDetails,
      riskMsgFromEdh)
  }

  private def isMultipleExecutors(regDetails: RegistrationDetails) = {
    regDetails.coExecutors.nonEmpty
  }
}
