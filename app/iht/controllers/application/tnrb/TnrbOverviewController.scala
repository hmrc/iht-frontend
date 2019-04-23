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

package iht.controllers.application.tnrb

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.models.application.ApplicationDetails
import iht.models.application.tnrb.{TnrbEligibiltyModel, WidowCheck}
import iht.utils.CommonHelper._
import iht.utils.{CommonHelper, StringHelper}
import javax.inject.Inject
import play.api.i18n.Lang
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class TnrbOverviewControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                           val cachingConnector: CachingConnector,
                                           val authConnector: AuthConnector,
                                           val formPartialRetriever: FormPartialRetriever,
                                           implicit val appConfig: AppConfig,
                                           val cc: MessagesControllerComponents) extends FrontendController(cc) with TnrbOverviewController

trait TnrbOverviewController extends EstateController with StringHelper {

  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
      implicit request => {
        implicit val lang: Lang = messagesApi.preferred(request).lang

        withRegistrationDetails { regDetails =>
          val applicationDetailsFuture: Future[Option[ApplicationDetails]] = ihtConnector
            .getApplication(getNino(userNino), getOrExceptionNoIHTRef(regDetails.ihtReference),
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
