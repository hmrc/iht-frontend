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
import iht.models.application.ApplicationDetails
import iht.models.application.tnrb.{WidowCheck, TnrbEligibiltyModel}
import iht.utils.CommonHelper
import iht.utils.CommonHelper._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import scala.concurrent.Future

object TnrbOverviewController extends TnrbOverviewController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait TnrbOverviewController extends EstateController {

  def onPageLoad = authorisedForIht {
    implicit user =>
      implicit request => {

        withRegistrationDetails { regDetails =>
          val applicationDetailsFuture: Future[Option[ApplicationDetails]] = ihtConnector
            .getApplication(getNino(user), getOrExceptionNoIHTRef(regDetails.ihtReference),
              regDetails.acknowledgmentReference)

          applicationDetailsFuture.map { optionApplicationDetails =>
            val ad = getOrExceptionNoApplication(optionApplicationDetails)
            lazy val ihtRef = CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference)

            Ok(iht.views.html.application.tnrb.tnrb_overview(regDetails,
              ad.widowCheck.fold(WidowCheck(None, None))(identity),
              ad.increaseIhtThreshold.fold(TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None))(identity),
              ihtRef))
          }
        }
      }
  }
}
