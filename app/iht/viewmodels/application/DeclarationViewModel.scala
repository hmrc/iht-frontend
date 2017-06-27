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

package iht.viewmodels.application

import iht.connector.IhtConnector
import iht.models.application.ApplicationDetails
import iht.models.{CoExecutor, RegistrationDetails}
import iht.utils.{CommonHelper, DeclarationHelper}
import play.api.Logger
import play.api.data.Form
import play.api.mvc.Request
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.i18n.Messages.Implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

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
            ihtConnector: IhtConnector)(implicit request: Request[_], hc: HeaderCarrier): DeclarationViewModel = {

    new DeclarationViewModel(
      form,
      DeclarationHelper.getDeclarationType(appDetails),
      regDetails.coExecutors,
      isMultipleExecutors(regDetails),
      regDetails,
      realTimeRiskingMessage(appDetails, CommonHelper.getOrException(regDetails.ihtReference), nino, ihtConnector))
  }

  private def isMultipleExecutors(regDetails: RegistrationDetails) = {
    regDetails.coExecutors.nonEmpty
  }

  private def realTimeRiskingMessage(ad: ApplicationDetails,
                                     ihtAppReference: String,
                                     nino: String,
                                     ihtConnector: IhtConnector)(implicit request: Request[_],
                                                                 hc: HeaderCarrier): Option[String] = {
    val moneyValue = for {
      assets <- ad.allAssets
      money <- assets.money
    } yield {
      money.value.getOrElse(BigDecimal(0)) + money.shareValue.getOrElse(BigDecimal(0))
    }

    val riskMessage = moneyValue.fold(getRealTimeRiskMessage(ihtConnector, ihtAppReference, nino)) {
        result =>
          if (result == 0) {
            getRealTimeRiskMessage(ihtConnector, ihtAppReference, nino)
          } else {
            Logger.debug("Money has a value, hence no need to check for real-time risking message")
            Future.successful(None)
          }
      }

    Await.result(riskMessage, Duration.Inf)
  }

  private def getRealTimeRiskMessage(ihtConnector: IhtConnector, ihtAppReference: String, nino: String)
                                    (implicit hc: HeaderCarrier) = {
    Logger.debug("Money has no value, hence need to check for real-time risking message")
    ihtConnector.getRealtimeRiskingMessage(ihtAppReference, nino).recover {
      case e: Exception => {
        Logger.warn(s"Problem getting realtime risking message: ${e.getMessage}")
        None
      }
    }
  }

}
