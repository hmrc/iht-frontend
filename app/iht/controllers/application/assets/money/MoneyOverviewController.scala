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

package iht.controllers.application.assets.money

import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.utils.{CommonHelper, StringHelper}
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

/**
  * Created by jennygj on 30/06/16.
  */

object MoneyOverviewController extends MoneyOverviewController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait MoneyOverviewController extends EstateController {

  def onPageLoad = authorisedForIht {
    implicit user =>
      implicit request => {
        withRegistrationDetails { registrationDetails =>
          for {
            applicationDetails: Option[ApplicationDetails] <- ihtConnector.getApplication(
              StringHelper.getNino(user),
              CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
              registrationDetails.acknowledgmentReference
            )
            money: Option[ShareableBasicEstateElement] = applicationDetails.flatMap(_.allAssets.flatMap(_.money))
          } yield {
            Ok(iht.views.html.application.asset.money.money_overview(money, registrationDetails))
          }
        }
      }
  }
}
