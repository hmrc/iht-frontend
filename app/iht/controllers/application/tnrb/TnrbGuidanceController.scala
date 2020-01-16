/*
 * Copyright 2020 HM Revenue & Customs
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
import iht.utils._
import iht.utils.tnrb.TnrbHelper
import iht.views.html.application.tnrb.tnrb_guidance
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class TnrbGuidanceControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                           val cachingConnector: CachingConnector,
                                           val authConnector: AuthConnector,
                                           val formPartialRetriever: FormPartialRetriever,
                                           implicit val appConfig: AppConfig,
                                           val cc: MessagesControllerComponents) extends FrontendController(cc) with TnrbGuidanceController

trait TnrbGuidanceController extends EstateController with TnrbHelper {

  def onPageLoad: Action[AnyContent] = authorisedForIht {
    implicit request => {
      withRegistrationDetails { rd =>
          val ihtReference = CommonHelper.getOrException(rd.ihtReference)
          val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(rd)
          val title = "iht.estateReport.tnrb.increasingIHTThreshold"
          val browserTitle = "iht.estateReport.tnrb.increasingThreshold"
          Future.successful(Ok(tnrb_guidance(
            ihtReference,
            urlForIncreasingThreshold(CommonHelper.getOrException(rd.deceasedDetails.flatMap(_.maritalStatus))).url,
            deceasedName,
            title,
            browserTitle,
            systemGenerated = false))
          )
      }
    }
  }

  def onSystemPageLoad: Action[AnyContent] = authorisedForIht {

      implicit request => {
        withRegistrationDetails { rd =>
          val ihtReference = CommonHelper.getOrException(rd.ihtReference)
          val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(rd)
          val title = "page.iht.application.tnrb.guidance.system.title"
          Future.successful(Ok(tnrb_guidance(
            ihtReference,
            urlForIncreasingThreshold(CommonHelper.getOrException(rd.deceasedDetails.flatMap(_.maritalStatus))).url,
            deceasedName,
            title, title,
            systemGenerated = true))
          )
        }
      }
  }

}
