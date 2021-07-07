/*
 * Copyright 2021 HM Revenue & Customs
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

package iht.controllers.application.assets.trusts

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.models.application.ApplicationDetails
import iht.models.application.assets.HeldInTrust
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.application.asset.trusts.trusts_overview

class TrustsOverviewControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                             val cachingConnector: CachingConnector,
                                             val authConnector: AuthConnector,
                                             val trustsOverviewView: trusts_overview,
                                             implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with TrustsOverviewController {

}

trait TrustsOverviewController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsInTrust)

  val trustsOverviewView: trusts_overview
  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>

    implicit request => {
      withRegistrationDetails { registrationDetails =>
        for {
          applicationDetails: Option[ApplicationDetails] <- ihtConnector.getApplication(
            getNino(userNino),
            CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
            registrationDetails.acknowledgmentReference
          )
          trusts: Option[HeldInTrust] = applicationDetails.flatMap(_.allAssets.flatMap(_.heldInTrust))
        } yield {
          Ok(trustsOverviewView(trusts, registrationDetails))
        }
      }
    }
  }
}
