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

package iht.controllers.application.tnrb

import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.metrics.Metrics
import iht.utils._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current


object TnrbSuccessController extends TnrbSuccessController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait TnrbSuccessController extends EstateController {

  def onPageLoad = authorisedForIht {
    implicit user =>
      implicit request => {
        withRegistrationDetails { registrationDetails =>
          for {
            applicationDetails <- ihtConnector.getApplication(StringHelper.getNino(user),
              CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
              registrationDetails.acknowledgmentReference)
          } yield {
            applicationDetails match {
              case Some(appDetails) => {
                Ok(iht.views.html.application.tnrb.tnrb_success(
                  CommonHelper.getOrException(registrationDetails.deceasedDetails).name,
                  CommonHelper.getOrException(appDetails.increaseIhtThreshold).Name.toString,
                  CommonHelper.getOrException(registrationDetails.ihtReference)
                ))
              }
              case _ => InternalServerError("Application details not found")
            }
          }
        }
      }
  }
}
