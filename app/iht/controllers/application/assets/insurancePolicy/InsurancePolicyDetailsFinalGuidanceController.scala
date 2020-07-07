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

package iht.controllers.application.assets.insurancePolicy

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.metrics.IhtMetrics
import iht.utils._
import iht.views.html.application.asset.insurancePolicy.insurance_policy_details_final_guidance
import javax.inject.Inject
import play.api.mvc.{Call, MessagesControllerComponents, Result}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class InsurancePolicyDetailsFinalGuidanceControllerImpl @Inject()(val metrics: IhtMetrics,
                                                                  val ihtConnector: IhtConnector,
                                                                  val cachingConnector: CachingConnector,
                                                                  val authConnector: AuthConnector,
                                                                  val formPartialRetriever: FormPartialRetriever,
                                                                  implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with InsurancePolicyDetailsFinalGuidanceController {

}

trait InsurancePolicyDetailsFinalGuidanceController extends EstateController {


  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withRegistrationDetails { registrationDetails =>
        val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(registrationDetails)

        for {
          applicationDetails <- ihtConnector.getApplication(getNino(userNino),
            CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
            registrationDetails.acknowledgmentReference)
        } yield {
          applicationDetails.fold[Result](InternalServerError)(ad =>
            Ok(
              insurance_policy_details_final_guidance(
                giftsPageRedirect(ad.allGifts.flatMap(_.isGivenAway)),
                deceasedName
              )
            )
          )
        }
      }
    }
  }

  def giftsPageRedirect(initialGiftsQuestionAnswerOption: Option[Boolean]): Call = {
    if (initialGiftsQuestionAnswerOption.fold(false)(identity)) {
      iht.controllers.application.gifts.routes.GiftsOverviewController.onPageLoad()
    } else {
      iht.controllers.application.gifts.routes.GivenAwayController.onPageLoad()
    }
  }
}
