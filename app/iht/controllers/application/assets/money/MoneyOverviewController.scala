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

package iht.controllers.application.assets.money

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.metrics.IhtMetrics
import iht.models.application.ApplicationDetails
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.utils.{CommonHelper, StringHelper}
import javax.inject.Inject
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.partials.FormPartialRetriever


/**
  * Created by jennygj on 30/06/16.
  */

class MoneyOverviewControllerImpl @Inject()(val metrics: IhtMetrics,
                                            val ihtConnector: IhtConnector,
                                            val cachingConnector: CachingConnector,
                                            val authConnector: AuthConnector,
                                            val formPartialRetriever: FormPartialRetriever) extends MoneyOverviewController {

}

trait MoneyOverviewController extends EstateController {


  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withRegistrationDetails { registrationDetails =>
        for {
          applicationDetails: Option[ApplicationDetails] <- ihtConnector.getApplication(
            StringHelper.getNino(userNino),
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
