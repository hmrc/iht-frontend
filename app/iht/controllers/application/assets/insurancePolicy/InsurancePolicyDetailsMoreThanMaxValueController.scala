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

package iht.controllers.application.assets.insurancePolicy

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.IhtMetrics
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.utils.ApplicationKickOutHelper
import iht.views.html.application.asset.insurancePolicy.insurance_policy_details_more_than_max_value
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class InsurancePolicyDetailsMoreThanMaxValueControllerImpl @Inject()(val metrics: IhtMetrics,
                                                                     val ihtConnector: IhtConnector,
                                                                     val cachingConnector: CachingConnector,
                                                                     val authConnector: AuthConnector,
                                                                     val formPartialRetriever: FormPartialRetriever,
                                                                     implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with InsurancePolicyDetailsMoreThanMaxValueController {

}

trait InsurancePolicyDetailsMoreThanMaxValueController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsInsurancePoliciesMoreThanMax)


  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      estateElementOnPageLoad[InsurancePolicy](insurancePolicyMoreThanMaxForm, insurance_policy_details_more_than_max_value.apply,
        _.allAssets.flatMap(_.insurancePolicy), userNino)
    }
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], InsurancePolicy) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, insurancePolicy) => {
          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
          (new AllAssets(action = None, insurancePolicy = Some(insurancePolicy)))(allAssets =>
            updateAllAssetsWithInsurancePolicy(allAssets, insurancePolicy, identity))
          ))
          (updatedAD, None)
        }

      estateElementOnSubmitConditionalRedirect[InsurancePolicy](insurancePolicyMoreThanMaxForm,
        insurance_policy_details_more_than_max_value.apply, updateApplicationDetails,
        (ad, _) => ad.allAssets.flatMap(allAssets => allAssets.insurancePolicy).flatMap(_.moreThanMaxValue)
          .fold(insurancePoliciesRedirectLocation)(_ =>
            iht.controllers.application.assets.insurancePolicy.routes.InsurancePolicyDetailsAnnuityController.onPageLoad()),
        userNino)
    }
  }
}
